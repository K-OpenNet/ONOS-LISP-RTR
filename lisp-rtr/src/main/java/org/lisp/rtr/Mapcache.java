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

import java.util.ArrayList;
import java.util.List;

import org.onlab.packet.IP;
import org.onlab.packet.UDP;

import java.net.InetSocketAddress;
import java.net.InetAddress;

public class Mapcache {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ArrayList<MapcacheEntry> mapDb; 

	public void initialize() {
		mapDb = new ArrayList<MapcacheEntry>();
	}

	public void addMapping(MapcacheEntry entry) {
		MapcacheEntry t;
		log.info("Mapcache add : prefix : " + entry.eidPrefix.toString() + " : GRLOC : " + entry.sxTR_public_RLOC.toString() + " : PRLOC : " + entry.sxTR_private_RLOC.toString());

		if ( (t = isContain(entry)) != null )
			updateMapping(entry, t);
		else
			mapDb.add(entry);
	}

	public void addMapping(InetSocketAddress grloc, byte len, InetAddress prefix, InetAddress rloc, long id1, long id2, long nonce, IP iph, UDP udh) {
		MapcacheEntry t;
		MapcacheEntry entry = new MapcacheEntry(len, prefix, grloc, rloc, id1, id2, nonce, iph, udh);
		log.info("Mapcache add : prefix : " + prefix.toString() + " : RLOC : " + rloc.toString() + " : IP : " + iph.toString());
	
		if ( (t = isContain(entry)) != null )
			updateMapping(entry, t);
		else
			mapDb.add(entry);
	}

	public void deleteMapping(InetSocketAddress key) {
	}	

	public MapcacheEntry isContain(MapcacheEntry entry) {
		for ( MapcacheEntry t : mapDb ) {
			if ( t.eidPrefix.equals(entry.eidPrefix) )
				return t;
		}
		return null;
	}

	public void updateMapping(MapcacheEntry src, MapcacheEntry dst) {
		log.info("UpdateMapping : EID : " + src.eidPrefix.toString() + " : From : " + dst.sxTR_public_RLOC.toString() + " : to : " + src.sxTR_public_RLOC.toString());
		dst.eidLen = src.eidLen;
		dst.eidPrefix = src.eidPrefix;
		dst.sxTR_public_RLOC = src.sxTR_public_RLOC;
		dst.sxTR_private_RLOC = src.sxTR_private_RLOC;
		dst.sxTR_Id = src.sxTR_Id;
		dst.nonce = src.nonce;
		dst.iph = src.iph;
		dst.udh = src.udh;		
	}

	public MapcacheEntry getMapping(InetAddress rloc, byte eidLen, long id1, long id2) {
		// RTR <-> MS
		for ( MapcacheEntry entry : mapDb ) {
			if ( entry.eidPrefix.equals(rloc) && entry.eidLen == eidLen && entry.sxTR_Id[0] == id1 && entry.sxTR_Id[1] == id2 ) {
				log.info(entry.toString());
				return entry;
			}
		}

		return null;
	}

	// xTR <-> RTR for data packets forwarding
	public MapcacheEntry getMapping(InetAddress eid) {
		byte[] eidaddr = eid.getAddress();

		for ( MapcacheEntry entry : mapDb ) {
			// Address comparing using subnet mask
			byte t1 = (byte)(entry.eidLen / 8);
			byte t2 = (byte)(entry.eidLen % 8);
			byte[] addr = entry.eidPrefix.getAddress();

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
				return entry;
		}

		return null;
	}

	// Nonce
	public MapcacheEntry getMapping(long nonce) {
		for ( MapcacheEntry entry : mapDb ) {
			if ( entry.nonce == nonce )
				return entry;
		}
		
		return null;
	}

	public int getSize() {
		return this.mapDb.size();
	}
}
