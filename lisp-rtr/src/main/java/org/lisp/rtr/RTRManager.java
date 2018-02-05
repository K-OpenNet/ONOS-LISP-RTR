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
import org.onosproject.lisp.msg.protocols.LispEncapsulatedControl;
import org.onosproject.lisp.msg.protocols.LispMapRegister;
import org.onosproject.lisp.msg.protocols.LispMapRecord;

import org.onosproject.lisp.msg.types.LispAfiAddress;
import static org.onosproject.lisp.msg.types.AddressFamilyIdentifierEnum.IP4;

@Component(immediate = true)
public class RTRManager {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected LispController controller;

	private final LispMsgListener messageListener =
				new LispMsgListener();
	private final LispChannelManage dataListener =
				new LispChannelManage();

	private Mapcache map = new Mapcache();

	@Activate
	protected void activate() {
		
		controller.addMessageListener(messageListener);
		dataListener.initialize();
		map.initialize();

	        log.info("Started");
	}

	@Deactivate
	protected void deactivate() {
	        log.info("Stopped");
	}

	private class LispMsgListener implements LispMessageListener {
	
		@Override
		public void handleIncomingMessage(LispRouterId routerId, LispMessage msg) {
			log.info("LISP incoming msg");		

			// Only process ECM-mapregister
			if ( msg instanceof LispEncapsulatedControl ) {
				// Extract inner msg
				LispMessage innermsg = ((LispEncapsulatedControl)(msg)).getControlMessage();
			        innermsg.configSender(msg.getSender());

				if ( innermsg instanceof LispMapRegister ) {
					LispMapRegister reg = (LispMapRegister) innermsg;

					// Create mapcache
					for ( LispMapRecord record : reg.getMapRecords() ) {
						LispAfiAddress addr = record.getEidPrefixAfi();

						// Only support IPv4
						if ( addr.getAfi() == IP4 ) {

						}
					}
				}
				else {
					log.info("Not supported");
				}
			}
			else {
				log.info("Not supported");
			}
		}

		@Override
		public void handleOutgoingMessage(LispRouterId routerId, LispMessage msg) {
			log.info("LISP outgoing msg");
		}

		private void isCtrlMsg() {
			
		}

		public void forwardMapServer() {

		}

		public void rewriteAddress() {

		}

		public void addHostMapping() {

		}

		public void forwardHost() {

		}
	
	}

}
