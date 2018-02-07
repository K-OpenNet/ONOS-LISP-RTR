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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onosproject.lisp.ctl.LispController;
import org.onosproject.lisp.ctl.LispMessageListener;
import org.onosproject.lisp.ctl.LispRouterId;
import org.onosproject.lisp.msg.protocols.LispMessage;

import java.net.InetSocketAddress;
import java.net.InetAddress;

@Component(immediate = true)
public class RTRManager {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected LispController controller;

	private Mapcache map = new Mapcache();
	private LispChannelManage channel = new LispChannelManage();

	@Activate
	protected void activate() {
		
		map.initialize();
		channel.initialize(this);
//		controller.addMessageListener(new LispCtlMsgListener());

	        log.info("Started");
	}

	@Deactivate
	protected void deactivate() {
	        log.info("Stopped");
	}

	public void addMapcacheMapping(InetSocketAddress grloc, byte len, InetAddress prefix, InetAddress rloc, long id1, long id2) {
		map.addMapping(grloc, len, prefix, rloc, id1, id2);
		log.info(Integer.toString(map.getSize()));
	}
	private class LispCtlMsgListener implements LispMessageListener {

	        @Override
        	public void handleIncomingMessage(LispRouterId routerId, LispMessage msg) {
			log.info("WTF");
	        }
	
	        @Override
        	public void handleOutgoingMessage(LispRouterId routerId, LispMessage msg) {
	        }	
	}
}
