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

import org.onosproject.lisp.msg.protocols.LispMessage;
import org.onosproject.lisp.msg.protocols.LispType;
import org.onosproject.lisp.msg.exceptions.LispWriterException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;

import java.util.Arrays;
import java.util.List;

import org.onlab.packet.IP;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IPv4;

public final class LispDataPacket implements LispMessage {

	private IP iph;
	private ByteBuf content;

	public LispDataPacket() {

	}

	private LispDataPacket(IP iph, ByteBuf content) {
		this.iph = iph;
		this.content = content;
	}

	@Override
	public LispType getType() {
		return null;
	}

	@Override
	public void configSender(InetSocketAddress sender) {
	
	}

	@Override
	public InetSocketAddress getSender() {
		return null;
	}

	@Override
	public void writeTo(ByteBuf byteBuf) throws LispWriterException {
		
	}

	@Override
	public Builder createBuilder() {
		return null;
	}

	public ByteBuf getContent() {
		return this.content;
	}

	public static final class DataPacketBuilder implements Builder {

        	@Override
		public LispType getType() {
			return null;
		}
	}    	

	public static final class DataPacketReader {
		public LispDataPacket readFrom(ByteBuf byteBuf, ByteBuf content) {
			byteBuf.skipBytes(8);

			short totalLength = byteBuf.getShort(byteBuf.readerIndex() + 2);
			byte[] ipHeaderByte = new byte[totalLength];
			byteBuf.getBytes(byteBuf.readerIndex(), ipHeaderByte, 0, totalLength);
			IP innerIpHeader = null;
			try {
				innerIpHeader = IP.deserializer().deserialize(ipHeaderByte, 0,
                                                             totalLength);
				IpAddress test = IpAddress.valueOf(((IPv4)innerIpHeader).getDestinationAddress());
			}
			catch ( Exception e ) {
			}
			return new LispDataPacket(innerIpHeader, content);
		}
	}
}
