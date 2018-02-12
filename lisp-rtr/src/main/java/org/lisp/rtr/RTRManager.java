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

import java.util.ArrayList;

@Component(immediate = true)
public class RTRManager {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Mapcache map = new Mapcache();
	private LispChannelManager channel = new LispChannelManager();
	private ArrayList<LispDataPacket> pktBuf = new ArrayList<LispDataPacket>();

	private final String rtrAddr = "13.58.128.5";
	private final String ms = "18.218.233.30";

	@Activate
	protected void activate() {
		map.initialize();
		channel.initialize(this);
	        log.info("Started");
	}

	@Deactivate
	protected void deactivate() {
	        log.info("Stopped");
	}

	public void addMapcacheMapping(MapcacheEntry entry) {
		map.addMapping(entry);
		log.info(Integer.toString(map.getSize()));
	}

	public MapcacheEntry getMapcacheMapping(long nonce) {
		return map.getMapping(nonce);
	}

	public MapcacheEntry getMapcacheMapping(InetAddress addr) {
		return map.getMapping(addr);
	}

	public void addPacket(LispDataPacket msg) {
		log.info("addPacket " + Integer.toString(pktBuf.size()));
		pktBuf.add(msg);
	}

	public ArrayList<LispDataPacket> getPacket() {
		return pktBuf;
	}

	public String getRTRAddr() {
		return this.rtrAddr;
	}
		
	public String getMSAddr() {
		return this.ms;	
	}
}
