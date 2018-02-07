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

public class MapcacheEntry {

	byte eidLen;
	InetAddress eidPrefix;
	InetSocketAddress sxTR_public_RLOC;
	InetAddress sxTR_private_RLOC;
	long[] sxTR_Id;
	long nonce;
	IP iph;
	UDP udh;

	MapcacheEntry(byte len, InetAddress prefix, InetSocketAddress grloc, InetAddress rloc, long id1, long id2, long nonce, IP iph, UDP udh) {
		this.eidLen = len;
		this.eidPrefix = prefix;
		this.sxTR_public_RLOC = grloc;
		this.sxTR_private_RLOC = rloc;
		this.sxTR_Id = new long[2];
		this.sxTR_Id[0] = id1;
		this.sxTR_Id[1] = id2;
		this.nonce = nonce;
		this.iph = iph;
		this.udh = udh;
	}		
}
