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
import org.onosproject.lisp.msg.types.LispAfiAddress;
import org.onosproject.lisp.msg.types.LispIpv4Address;
import static org.onosproject.lisp.msg.types.AddressFamilyIdentifierEnum.IP4;

import java.net.InetSocketAddress;
import java.net.InetAddress;

import io.netty.channel.socket.DatagramPacket;

public class LispDataPacketHandler extends ChannelInboundHandlerAdapter {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RTRManager rtr;

	public LispDataPacketHandler(RTRManager rtr) {
		log.info("LISP data packet handler create");
		this.rtr = rtr;	
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket packet = (DatagramPacket)msg;
		System.out.println("data decode 1");
		System.out.println(Integer.toString(packet.recipient().getPort()));	
		if ( packet.recipient().getPort() == 4341 ) {
			log.info("LISP data incoming msg");		
			ByteBuf byteBuf = (ByteBuf)msg;
			byteBuf.skipBytes(8);

			short totalLength = byteBuf.getShort(byteBuf.readerIndex() + 2);
			byte[] ipHeaderByte = new byte[totalLength];
			byteBuf.getBytes(byteBuf.readerIndex(), ipHeaderByte, 0, totalLength);

			IP innerIpHeader = IP.deserializer().deserialize(ipHeaderByte, 0,
                                                             totalLength);
			IpAddress test = IpAddress.valueOf(((IPv4)innerIpHeader).getDestinationAddress());
			log.info(test.toString());
		}	
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	
	}
}	

