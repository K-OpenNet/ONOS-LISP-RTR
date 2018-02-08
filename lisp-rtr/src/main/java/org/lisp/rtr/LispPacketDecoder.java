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


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.onosproject.lisp.msg.protocols.LispMessage;
import org.onosproject.lisp.msg.protocols.LispMessageReader;
import org.onosproject.lisp.msg.protocols.LispMessageReaderFactory;

import java.util.List;

public class LispPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> list) throws Exception {


	log.info("decode 1");
	log.info(Integer.toString(msg.recipient().getPort()));
	if ( msg.recipient().getPort() == 4342 ) {
		// Control packet
		log.info("control 1");
	        ByteBuf byteBuf = msg.content();
	        LispMessageReader reader = LispMessageReaderFactory.getReader(byteBuf);
        	LispMessage message = (LispMessage) reader.readFrom(byteBuf);
	        message.configSender(msg.sender());
        	list.add(message);
    	}
	else {
		// Data packetA
		log.info("data 1");
		ByteBuf content = msg.content().copy();
		LispDataPacket.DataPacketReader reader = new LispDataPacket.DataPacketReader();
		log.info("data 2");
		LispMessage message = reader.readFrom(msg.content(), content);
		log.info("data 3");
		list.add(message);
	}
   }
}

