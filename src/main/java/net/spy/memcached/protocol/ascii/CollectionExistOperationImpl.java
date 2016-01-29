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
package net.spy.memcached.protocol.ascii;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;

import net.spy.memcached.KeyUtil;
import net.spy.memcached.collection.CollectionExist;
import net.spy.memcached.collection.CollectionResponse;
import net.spy.memcached.collection.SetExist;
import net.spy.memcached.ops.APIType;
import net.spy.memcached.ops.CollectionExistOperation;
import net.spy.memcached.ops.CollectionOperationStatus;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.OperationType;

/**
 * Operation to check membership of an item in collection in a memcached server.
 */
public class CollectionExistOperationImpl extends OperationImpl
	implements CollectionExistOperation {

	private static final int OVERHEAD = 32;

	private static final OperationStatus EXIST_CANCELED = new CollectionOperationStatus(
			false, "collection canceled", CollectionResponse.CANCELED);

	private static final OperationStatus EXIST = new CollectionOperationStatus(
			true, "EXIST", CollectionResponse.EXIST);
	private static final OperationStatus NOT_EXIST = new CollectionOperationStatus(
			true, "NOT_EXIST", CollectionResponse.NOT_EXIST);
	private static final OperationStatus NOT_FOUND = new CollectionOperationStatus(
			false, "NOT_FOUND", CollectionResponse.NOT_FOUND);
	private static final OperationStatus TYPE_MISMATCH = new CollectionOperationStatus(
			false, "TYPE_MISMATCH", CollectionResponse.TYPE_MISMATCH);
	private static final OperationStatus UNREADABLE = new CollectionOperationStatus(
			false, "UNREADABLE", CollectionResponse.UNREADABLE);

	protected final String key;
	protected final String subkey;
	protected final CollectionExist<?> collectionExist;
	protected final byte[] data;

	public CollectionExistOperationImpl(String key, String subkey,
			CollectionExist<?> collectionExist, OperationCallback cb) {
		super(cb);
		this.key = key;
		this.subkey = subkey;
		this.collectionExist = collectionExist;
		this.data = collectionExist.getData();
		if (this.collectionExist instanceof SetExist)
			setAPIType(APIType.SOP_EXIST);
		setOperationType(OperationType.READ);
	}

	@Override
	public void handleLine(String line) {
		assert getState() == OperationState.READING
			: "Read ``" + line + "'' when in " + getState() + " state";
		getCallback().receivedStatus(
				matchStatus(line, EXIST, NOT_EXIST, NOT_FOUND, NOT_FOUND,
						TYPE_MISMATCH, UNREADABLE));
		transitionState(OperationState.COMPLETE);
	}

	@Override
	public void initialize() {
		String args = collectionExist.stringify();
		ByteBuffer bb = ByteBuffer.allocate(data.length
				+ KeyUtil.getKeyBytes(key).length
				+ KeyUtil.getKeyBytes(subkey).length
				+ args.length()
				+ OVERHEAD);
		setArguments(bb, collectionExist.getCommand(), key, subkey, data.length, args);
		bb.put(data);
		bb.put(CRLF);
		bb.flip();
		setBuffer(bb);

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("Request in ascii protocol: "
					+ (new String(bb.array())).replaceAll("\\r\\n", ""));
		}
	}

	@Override
	protected void wasCancelled() {
		getCallback().receivedStatus(EXIST_CANCELED);
	}

	public Collection<String> getKeys() {
		return Collections.singleton(key);
	}

	public String getSubKey() {
		return subkey;
	}

	public CollectionExist<?> getExist() {
		return collectionExist;
	}

	public byte[] getData() {
		return data;
	}

}
