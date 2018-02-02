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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class LispChannelManage {

	private final int LISP_DATA_PORT = 4341;

	private ChannelFuture dataChannel;
	private EventLoopGroup group;
	private Bootstrap boot;

	public void initialize() {
		
		boot = new Bootstrap();
		group = new NioEventLoopGroup();
		boot.group(group)
		.channel(NioDatagramChannel.class)
		.handler(new ChannelInitializer<NioDatagramChannel>() {
			@Override
			protected void initChannel(NioDatagramChannel socket) throws Exception {
				ChannelPipeline pipe = socket.pipeline();
		//		pipe.addList("lispdatahandler", new 
			}	
		});
	}

	public void close() {

	}

	private void createBootstrap() {

	}

	private class LispDataPacketHandler extends ChannelInboundHandlerAdapter {

		private final Logger log = LoggerFactory.getLogger(getClass());
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	
		}

	}
	
}

