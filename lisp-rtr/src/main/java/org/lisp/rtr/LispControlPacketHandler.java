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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.onosproject.lisp.msg.protocols.LispMessage;
import org.onosproject.lisp.msg.protocols.LispEncapsulatedControl;
import org.onosproject.lisp.msg.protocols.LispMapRegister;
import org.onosproject.lisp.msg.protocols.LispMapNotify;
import org.onosproject.lisp.msg.protocols.LispMapRecord;
import org.onosproject.lisp.msg.protocols.LispLocator;
import org.onosproject.lisp.msg.protocols.LispEncapsulatedControl.EcmBuilder;
import org.onosproject.lisp.msg.protocols.DefaultLispMapRegister;
import org.onosproject.lisp.msg.protocols.DefaultLispMapRegister.DefaultRegisterBuilder;
import org.onosproject.lisp.msg.protocols.DefaultLispEncapsulatedControl;
import org.onosproject.lisp.msg.protocols.DefaultLispEncapsulatedControl.DefaultEcmBuilder;

import org.onosproject.lisp.msg.authentication.LispAuthenticationConfig;

import org.onlab.packet.IP;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IPv4;
import org.onosproject.lisp.msg.types.LispAfiAddress;
import org.onosproject.lisp.msg.types.LispIpv4Address;
import static org.onosproject.lisp.msg.types.AddressFamilyIdentifierEnum.IP4;

import java.util.ArrayList;
import java.net.InetSocketAddress;
import java.net.InetAddress;

import org.onosproject.lisp.msg.protocols.LispMessageReader;
import org.onosproject.lisp.msg.protocols.LispMessageReaderFactory;


public class LispControlPacketHandler {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RTRManager rtr;

	private LispAuthenticationConfig authConfig = LispAuthenticationConfig.getInstance();

	public LispControlPacketHandler(RTRManager rtr) {
		log.info("LISP control packet handler create");
		this.rtr = rtr;	
	}

	public ArrayList<DatagramPacket> processPkt(LispMessage msg) {
		log.info("LISP incoming msg");		
		ArrayList<DatagramPacket> list = new ArrayList<DatagramPacket>();

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
						log.info(ecm.innerIpHeader().toString());
	
						// MaskLeng, EID, GRLOC, PRLOC, xTR-ID, nonce, IP, UDP
						rtr.addMapcacheMapping(new MapcacheEntry(record.getMaskLength(), eid.toInetAddress(), ecm.getSender(),
									ip.toInetAddress(), 0, 0, reg.getNonce(),
									ecm.innerIpHeader(), ecm.innerUdp()));
						/*
						// Only support one locator
						for ( LispLocator loc : record.getLocators() ) {
							//Global RLOC, EID lendth, EID-prefix, xTR-private-RLOC, xTR-ID
								map.addMapping(msg.getSender(), record.getMaskLength(),
										ip.toInetAddress(), 0, 0);
						}
						*/

						//Re-originate map-register
						DefaultRegisterBuilder registerBuilder = new DefaultRegisterBuilder();
					        registerBuilder.withKeyId(reg.getKeyId());
				        	registerBuilder.withAuthKey(authConfig.lispAuthKey());
					        registerBuilder.withNonce(reg.getNonce());
				        	registerBuilder.withIsProxyMapReply(reg.isProxyMapReply());
					        registerBuilder.withIsWantMapNotify(reg.isWantMapNotify());
					        registerBuilder.withMapRecords(reg.getMapRecords());

					        LispMapRegister authRegister = registerBuilder.build();
						InetAddress msaddr = (IpAddress.valueOf(innerv4.getDestinationAddress())).toInetAddress();

						try {
							ByteBuf byteBuf = Unpooled.buffer();
							authRegister.writeTo(byteBuf);
							list.add(new DatagramPacket(byteBuf, new InetSocketAddress(msaddr, 4342)));
	//						ctx.writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(msaddr, 4342)));
						}
						catch ( Exception e ) {
						}
					}
				}
			}
			else {
				log.info("Not supported");
			}
		}
		else if ( msg instanceof LispMapNotify ) {
			log.info("Map-notify");
			LispMapNotify noti = (LispMapNotify)msg;
			MapcacheEntry map = rtr.getMapcacheMapping(noti.getNonce());

			if ( map == null ) {
				// This map-request for own cache
				log.info("Map-notify for this RTR");
				for ( LispMapRecord record : noti.getMapRecords() ) {
					LispAfiAddress addr = record.getEidPrefixAfi();
					IpAddress eid = ((LispIpv4Address)addr).getAddress();
					for ( LispLocator loc : record.getLocators() ) {
						LispAfiAddress locator = loc.getLocatorAfi();
						IpAddress iplocator = ((LispIpv4Address)locator).getAddress();
						log.info(eid.toInetAddress().toString());
						log.info(iplocator.toInetAddress().toString());
						MapcacheEntry lmap = new MapcacheEntry(record.getMaskLength(), eid.toInetAddress(), new InetSocketAddress(iplocator.toInetAddress(), 4341), null,
							0, 0, noti.getNonce(), null, null);
					}
				}			
			}
			else {
				IPv4 iph = (IPv4)map.iph;
				int t = iph.getSourceAddress();
				iph.setSourceAddress(iph.getDestinationAddress());
				iph.setDestinationAddress(t);
				DefaultEcmBuilder builder = new DefaultEcmBuilder();
				log.info(iph.toString());
				log.info(noti.toString());
					iph.resetChecksum();
				map.udh.resetChecksum();
				DefaultLispEncapsulatedControl enoti = (DefaultLispEncapsulatedControl)(builder.isSecurity(false)
									.innerIpHeader(iph)
									.innerUdpHeader(map.udh)
									.innerLispMessage(noti)
									.build());

				log.info(enoti.toString());
				try {
					ByteBuf byteBuf = Unpooled.buffer();
					enoti.writeTo(byteBuf);
					list.add(new DatagramPacket(byteBuf, new InetSocketAddress(map.sxTR_public_RLOC.getAddress(), 4342), new InetSocketAddress("192.168.36.137", 4341)));
				}	
				catch ( Exception e ) {
	
				}
			
//				ctx.writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(map.sxTR_public_RLOC.getAddress(), 4342), new InetSocketAddress("192.168.36.137", 4341)));
			}
		}
		else {
			log.info("Not supported");
		}	

		return list;
	}
}	
