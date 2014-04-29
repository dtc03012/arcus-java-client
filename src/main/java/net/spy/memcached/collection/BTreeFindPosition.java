/*
 * arcus-java-client : Arcus Java client
 * Copyright 2010-2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.spy.memcached.collection;

/**
 * Ascii protocol implementation for "bop position" (B+Tree find position)
 * 
 * bop position <key> <bkey> <order>\r\n <position> = 0 or positive integer
 * END\r\n (CLIENT_ERROR, NOT_FOUND, UNREADABLE, BKEY_MISMATCH, TYPE_MISMATCH,
 * NOT_FOUND_ELEMENT)
 */
public class BTreeFindPosition {

	private static final String command = "bop position";
	
	private final BKeyObject bkeyObject;
	private final BTreeOrder order;
	private String str;

	public BTreeFindPosition(long longBKey, BTreeOrder order) {
		this.bkeyObject = new BKeyObject(longBKey);
		this.order = order;
	}

	public BTreeFindPosition(byte[] byteArrayBKey, BTreeOrder order) {
		this.bkeyObject = new BKeyObject(byteArrayBKey);
		this.order = order;
	}

	public String stringify() {
		if (str != null) return str;
		StringBuilder b = new StringBuilder();
		b.append(bkeyObject.getBKeyAsString());
		b.append(" ");
		b.append(order.getAscii());
		
		str = b.toString();
		return str;
	}
	
	public String getCommand() {
		return command;
	}

	public BKeyObject getBkeyObject() {
		return bkeyObject;
	}

	public BTreeOrder getOrder() {
		return order;
	}
	
}
