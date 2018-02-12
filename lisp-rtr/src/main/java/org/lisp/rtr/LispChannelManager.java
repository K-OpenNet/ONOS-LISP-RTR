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

import org.onosproject.lisp.ctl.LispController;
import org.onosproject.lisp.ctl.LispMessageListener;
import org.onosproject.lisp.ctl.LispRouterId;
import org.onosproject.lisp.msg.protocols.LispMessage;

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

import java.net.InetSocketAddress;

public class LispChannelManager {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final int LISP_DATA_PORT = 4341;
	private final int LISP_CONTROL_PORT = 4342;

	private EventLoopGroup data_group;
	private EventLoopGroup control_group;
	private Bootstrap data_boot;
	private Bootstrap control_boot;

	public void initialize(RTRManager rtr) {
		createBootstrap(rtr);
	}

	public void close() {

	}

	private void createBootstrap(RTRManager rtr) {
		
		try {
			control_boot = new Bootstrap();
			control_group = new NioEventLoopGroup();
			control_boot.group(control_group)
			.channel(NioDatagramChannel.class)
			.handler(new ChannelInitializer<NioDatagramChannel>() {
				@Override
				protected void initChannel(NioDatagramChannel socket) throws Exception {
					log.info("channel initialize");
					ChannelPipeline pipe = socket.pipeline();
					pipe.addLast("lisp_packet_decoder", new LispPacketDecoder());
					pipe.addLast("lisp_channel_handler", new LispChannelHandler(rtr));
				}	
			});
			control_boot.bind(new InetSocketAddress(LISP_CONTROL_PORT));
			control_boot.bind(new InetSocketAddress(LISP_DATA_PORT));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
