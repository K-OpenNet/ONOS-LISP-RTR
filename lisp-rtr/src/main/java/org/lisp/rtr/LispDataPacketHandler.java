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

import org.onlab.packet.IP;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.UDP;
import org.onosproject.lisp.msg.types.LispAfiAddress;
import org.onosproject.lisp.msg.types.LispIpv4Address;
import static org.onosproject.lisp.msg.types.AddressFamilyIdentifierEnum.IP4;

import org.onosproject.lisp.msg.protocols.LispMessage;
import org.onosproject.lisp.msg.protocols.DefaultLispMapRequest.DefaultRequestBuilder;
import org.onosproject.lisp.msg.protocols.LispEncapsulatedControl;
import org.onosproject.lisp.msg.protocols.DefaultLispEncapsulatedControl.DefaultEcmBuilder;
import org.onosproject.lisp.msg.protocols.LispMapRequest;
import org.onosproject.lisp.msg.protocols.LispEidRecord;

import java.util.ArrayList;
import java.util.Random;
import java.net.InetSocketAddress;
import java.net.InetAddress;

import io.netty.channel.socket.DatagramPacket;

public class LispDataPacketHandler {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RTRManager rtr;

	public LispDataPacketHandler(RTRManager rtr) {
		log.info("LISP data packet handler create");
		this.rtr = rtr;	
	}

	public ArrayList<DatagramPacket> processPkt(LispMessage msg) {
		IP iph = ((LispDataPacket)msg).getIP();
		ArrayList<DatagramPacket> list = new ArrayList<DatagramPacket>();

		if ( iph.getVersion() == 4 ) {
			IpAddress sip = IpAddress.valueOf(((IPv4)iph).getSourceAddress());
			InetAddress snetip = sip.toInetAddress();
			IpAddress dip = IpAddress.valueOf(((IPv4)iph).getDestinationAddress());
			InetAddress dnetip = dip.toInetAddress();
			MapcacheEntry map = rtr.getMapcacheMapping(dnetip);
			
			if ( map != null ) 
				log.info("WTF?");
			log.info(snetip.toString());
			log.info(dnetip.toString());
		
			{
				// Need to send map-request 
				log.info("1");
				ArrayList<LispAfiAddress> itr = new ArrayList<LispAfiAddress>();
				itr.add(new LispIpv4Address(IpAddress.valueOf("192.168.36.137")));
				ArrayList<LispEidRecord> eidrec = new ArrayList<LispEidRecord>();
				eidrec.add(new LispEidRecord((byte) 32, new LispIpv4Address(dip)));
				log.info("2");
        			LispMapRequest req = new DefaultRequestBuilder()
							.withIsAuthoritative(true)
							.withIsMapDataPresent(true)
							.withIsPitr(false)
							.withIsProbe(false)
							.withIsSmr(false)
							.withIsSmrInvoked(false)
							.withSourceEid(new LispIpv4Address(sip))
							.withItrRlocs(itr)
							.withEidRecords(eidrec)
							.withNonce(new Random().nextLong())
							.withReplyRecord(1)
							.build();
				log.info("3");
				// Encapsulated
				IPv4 eiph = new IPv4();
				log.info("3.1");
				eiph.setSourceAddress(((Ip4Address)IpAddress.valueOf("192.168.36.137")).toInt());
				eiph.setDestinationAddress(((Ip4Address)IpAddress.valueOf("192.168.36.133")).toInt());
				log.info("3.2");
				UDP eudh = new UDP();
				eudh.setSourcePort(4342);
				eudh.setDestinationPort(4342);
				log.info("4");
				LispEncapsulatedControl ecm = new DefaultEcmBuilder()
							.isSecurity(false)
							.innerIpHeader(eiph)
							.innerUdpHeader(eudh)
							.innerLispMessage(req)
							.build();	
				ByteBuf byteBuf = Unpooled.buffer();
				log.info("hi?");
				try {
					ecm.writeTo(byteBuf);
					list.add(new DatagramPacket(byteBuf, new InetSocketAddress("192.168.36.133", 4342)));
					log.info("ECM write");
				}
				catch ( Exception e ) {
					log.info("fucked");
				}
			}	
		}	
		return list;
	}
}	
