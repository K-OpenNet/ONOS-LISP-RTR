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
import java.net.NetworkInterface;
import java.net.InterfaceAddress;

import java.util.ArrayList;
import java.util.Properties;
import java.io.FileInputStream;

@Component(immediate = true)
public class RTRManager {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Mapcache map = new Mapcache();
	private LispChannelManager channel = new LispChannelManager();
	private ArrayList<LispDataPacket> pktBuf = new ArrayList<LispDataPacket>();

	private ArrayList<String> whiteList;

	private String rtrAddr;
	private String ms;

	@Activate
	protected void activate() {
		map.initialize();
		channel.initialize(this);
		/*
		whiteList = new ArrayList<String>();
		whiteList.add("141.223.0.0");
		whiteList.add("115.69.0.0");
		whiteList.add("192.168.0.0");
		whiteList.add("172.20.0.0");
		whiteList.add("172.31.0.0");
		whiteList.add("223.33.0.0");
		whiteList.add("20.0.0.0");
		whiteList.add("40.0.0.0");
		*/
		initialize();
	        log.info("Started");
	}

	@Deactivate
	protected void deactivate() {
	        log.info("Stopped");
	}

	private void initialize() {
		Properties properties = new Properties();
		try {
			// Read XML file
			properties.loadFromXML(getClass().getResourceAsStream("config.xml"));

			// MS RLOC
			ms = (String)properties.get("map-server");	
			
			// Control Interface RLOC
			String inter = (String)properties.get("control-interface");
			NetworkInterface interf = NetworkInterface.getByName(inter);
			for ( InterfaceAddress addr : interf.getInterfaceAddresses() ) {
				rtrAddr = addr.getAddress().toString();
			}
		
			log.info("Initialized : ms : " + ms + " interface : " + inter + " : ControlRLOC : " + rtrAddr);
			// White list
			String wl = (String)properties.get("white-list");
			whiteList = new ArrayList<String>();
			for ( String t : wl.split(",") ) {
				whiteList.add(t);
				log.info(t);
			}
		}
		catch ( Exception e ) {
			log.info(e.toString());
		}
	}

	public void addMapcacheMapping(MapcacheEntry entry) {
		map.addMapping(entry);
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

	public boolean isValidPacket(InetAddress address) {
		byte[] eidaddr = address.getAddress();

		for ( String entry : whiteList ) {
			// Address comparing using subnet mask
			byte t1 = (byte)(2);
			byte t2 = (byte)(0);
			byte[] addr;
			
			try { 
				addr = InetAddress.getByName(entry).getAddress();
			}
			catch ( Exception e ) {
				addr = null;
				return false;
			}

			boolean pass = true;
			for ( byte t = 0 ; t < t1 ; t++ ) {
				if ( addr[t] != eidaddr[t] ) {
					pass = false;
					break;
				}
			}

			if ( pass && t2 != 0 ) {
				byte t3 = (byte)(255 << ( 8 - t2));
				if ( (addr[t1] & t3) != (eidaddr[t1] & t3) ) {
					pass = false;
					continue;
				}
			}

			if ( pass )
				return true;
		}
		return false;
	}

	public String getRTRAddr() {
		return this.rtrAddr;
	}
		
	public String getMSAddr() {
		return this.ms;	
	}
}
