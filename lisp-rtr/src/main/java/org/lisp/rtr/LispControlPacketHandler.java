/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lisp.rtr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.onosproject.lisp.msg.protocols.LispMessage;
import org.onosproject.lisp.msg.protocols.LispEncapsulatedControl;
import org.onosproject.lisp.msg.protocols.LispMapRegister;
import org.onosproject.lisp.msg.protocols.LispMapRecord;
import org.onosproject.lisp.msg.protocols.LispLocator;

import org.onlab.packet.IP;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IPv4;
import org.onosproject.lisp.msg.types.LispAfiAddress;
import org.onosproject.lisp.msg.types.LispIpv4Address;
import static org.onosproject.lisp.msg.types.AddressFamilyIdentifierEnum.IP4;

import java.net.InetSocketAddress;
import java.net.InetAddress;

public class LispControlPacketHandler extends ChannelInboundHandlerAdapter {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RTRManager rtr;

	public LispControlPacketHandler(RTRManager rtr) {
		log.info("LISP control packet handler create");
		this.rtr = rtr;	
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.info("LISP incoming msg");		

		// Only process ECM-mapregister
		if ( msg instanceof LispEncapsulatedControl ) {
			// Extract inner msg
			LispEncapsulatedControl ecm = (LispEncapsulatedControl)msg;
			LispMessage innermsg = ecm.getControlMessage();
			IP innerheader = ecm.innerIpHeader();		
		        innermsg.configSender(ecm.getSender());
			log.info(ecm.getSender().toString());

			if ( innermsg instanceof LispMapRegister ) {
				log.info("ECM ms-register 1");
				LispMapRegister reg = (LispMapRegister) innermsg;

				// Create mapcache
				log.info(Integer.toString(reg.getMapRecords().size()));
				for ( LispMapRecord record : reg.getMapRecords() ) {
					log.info("ECM ms-register 2");
					LispAfiAddress addr = record.getEidPrefixAfi();
						// Only support IPv4
					if ( innerheader.getVersion() == 4 && addr.getAfi() == IP4 ) {
						log.info("ECM ms-register 3");
						IPv4 innerv4 = (IPv4) innerheader;
						IpAddress ip = IpAddress.valueOf(innerv4.getSourceAddress());
						IpAddress eid = ((LispIpv4Address)addr).getAddress();
						log.info(eid.toString());
						log.info(ip.toString());
	
						rtr.addMapcacheMapping(ecm.getSender(), record.getMaskLength(), eid.toInetAddress(),
								ip.toInetAddress(), 0, 0);
						/*
						// Only support one locator
						for ( LispLocator loc : record.getLocators() ) {
							//Global RLOC, EID lendth, EID-prefix, xTR-private-RLOC, xTR-ID
								map.addMapping(msg.getSender(), record.getMaskLength(),
										ip.toInetAddress(), 0, 0);
						}
						*/

						//Re-originate map-register
						ByteBuf byteBuf = Unpooled.buffer();
					        ((LispMessage) ecm).writeTo(byteBuf);
						InetAddress msaddr = (IpAddress.valueOf(innerv4.getDestinationAddress())).toInetAddress();
						ctx.writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(msaddr, 4342)));
					}
				}
		}
			else {
				log.info("Not supported");
			}
		}
		else {
			log.info("Not supported");

		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	
	}
}	
