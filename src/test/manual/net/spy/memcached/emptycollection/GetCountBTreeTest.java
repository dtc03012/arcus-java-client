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
package net.spy.memcached.emptycollection;

import junit.framework.Assert;
import net.spy.memcached.collection.BaseIntegrationTest;
import net.spy.memcached.collection.CollectionAttributes;
import net.spy.memcached.collection.CollectionResponse;
import net.spy.memcached.collection.ElementFlagFilter;
import net.spy.memcached.collection.ElementValueType;
import net.spy.memcached.internal.CollectionFuture;

public class GetCountBTreeTest extends BaseIntegrationTest {

	private final String KEY = this.getClass().getSimpleName();
	private final long BKEY = 10L;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mc.delete(KEY).get();
		Assert.assertNull(mc.asyncGetAttr(KEY).get());
	}

	@Override
	protected void tearDown() throws Exception {
		mc.delete(KEY).get();
		super.tearDown();
	}

	public void testGetBKeyCountFromInvalidKey() {
		try {
			CollectionFuture<Integer> future = mc.asyncBopGetItemCount(
					"INVALIDKEY", BKEY, BKEY, ElementFlagFilter.DO_NOT_FILTER);
			Integer count = future.get();
			Assert.assertNull(count);
			Assert.assertFalse(future.getOperationStatus().isSuccess());
			Assert.assertEquals(CollectionResponse.NOT_FOUND, future
					.getOperationStatus().getResponse());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public void testGetBKeyCountFromInvalidType() {
		try {
			// insert value into set
			Boolean insertResult = mc.asyncSopInsert(KEY, "value",
					new CollectionAttributes()).get();
			Assert.assertTrue(insertResult);

			// get count from key
			CollectionFuture<Integer> future = mc.asyncBopGetItemCount(KEY,
					BKEY, BKEY, ElementFlagFilter.DO_NOT_FILTER);
			Integer count = future.get();
			Assert.assertNull(count);
			Assert.assertFalse(future.getOperationStatus().isSuccess());
			Assert.assertEquals(CollectionResponse.TYPE_MISMATCH, future
					.getOperationStatus().getResponse());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public void testGetBKeyCountUnreadable() {
		try {
			CollectionAttributes attributes = new CollectionAttributes();
			attributes.setReadable(false);

			Boolean createResult = mc.asyncBopCreate(KEY,
					ElementValueType.STRING, attributes).get();
			Assert.assertTrue(createResult);

			// get count from key
			CollectionFuture<Integer> future = mc.asyncBopGetItemCount(KEY,
					BKEY, BKEY, ElementFlagFilter.DO_NOT_FILTER);
			Integer count = future.get();
			Assert.assertNull(count);
			Assert.assertFalse(future.getOperationStatus().isSuccess());
			Assert.assertEquals(CollectionResponse.UNREADABLE, future
					.getOperationStatus().getResponse());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public void testGetBKeyCountFromInvalidBKeyType() {
		try {
			// insert an item
			Boolean insertResult = mc.asyncBopInsert(KEY, new byte[] { 0 },
					null, "value", new CollectionAttributes()).get();
			Assert.assertTrue(insertResult);

			// get count from key
			CollectionFuture<Integer> future = mc.asyncBopGetItemCount(KEY,
					BKEY, BKEY, ElementFlagFilter.DO_NOT_FILTER);
			Integer count = future.get();
			Assert.assertNull(count);
			Assert.assertFalse(future.getOperationStatus().isSuccess());
			Assert.assertEquals(CollectionResponse.BKEY_MISMATCH, future
					.getOperationStatus().getResponse());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public void testGetBKeyCountFromNotEmpty() {
		try {
			// check not exists
			Assert.assertNull(mc.asyncGetAttr(KEY).get());

			// insert two items
			Boolean insertResult = mc.asyncBopInsert(KEY, BKEY, null, "value",
					new CollectionAttributes()).get();
			Assert.assertTrue(insertResult);

			Boolean insertResult2 = mc.asyncBopInsert(KEY, BKEY + 1, null,
					"value", new CollectionAttributes()).get();
			Assert.assertTrue(insertResult2);

			// check count in attributes
			Assert.assertEquals(new Long(2), mc.asyncGetAttr(KEY).get()
					.getCount());

			// get btree item count
			CollectionFuture<Integer> future = mc.asyncBopGetItemCount(KEY,
					BKEY, BKEY, ElementFlagFilter.DO_NOT_FILTER);
			Integer count = future.get();
			Assert.assertNotNull(count);
			Assert.assertEquals(new Integer(1), count);
			Assert.assertEquals(CollectionResponse.END, future
					.getOperationStatus().getResponse());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public void testGetBKeyCountFromNotEmpty2() {
		try {
			// check not exists
			Assert.assertNull(mc.asyncGetAttr(KEY).get());

			// insert two items
			Boolean insertResult = mc.asyncBopInsert(KEY, BKEY, null, "value",
					new CollectionAttributes()).get();
			Assert.assertTrue(insertResult);

			Boolean insertResult2 = mc.asyncBopInsert(KEY, BKEY + 1, null,
					"value", new CollectionAttributes()).get();
			Assert.assertTrue(insertResult2);

			// check count in attributes
			Assert.assertEquals(new Long(2), mc.asyncGetAttr(KEY).get()
					.getCount());

			// get btree item count
			CollectionFuture<Integer> future = mc.asyncBopGetItemCount(KEY,
					BKEY, BKEY + 1, ElementFlagFilter.DO_NOT_FILTER);
			Integer count = future.get();
			Assert.assertNotNull(count);
			Assert.assertEquals(new Integer(2), count);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public void testGetBKeyCountByNotExistsBKey() {
		try {
			// check not exists
			Assert.assertNull(mc.asyncGetAttr(KEY).get());

			// insert two items
			Boolean insertResult = mc.asyncBopInsert(KEY, BKEY, null, "value",
					new CollectionAttributes()).get();
			Assert.assertTrue(insertResult);

			Boolean insertResult2 = mc.asyncBopInsert(KEY, BKEY + 1, null,
					"value", new CollectionAttributes()).get();
			Assert.assertTrue(insertResult2);

			// check count in attributes
			Assert.assertEquals(new Long(2), mc.asyncGetAttr(KEY).get()
					.getCount());

			// get btree item count
			CollectionFuture<Integer> future = mc.asyncBopGetItemCount(KEY,
					BKEY + 3, BKEY + 3, ElementFlagFilter.DO_NOT_FILTER);
			Integer count = future.get();
			Assert.assertNotNull(count);
			Assert.assertEquals(new Integer(0), count);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public void testGetBKeyCountByNotExistsRange() {
		try {
			// check not exists
			Assert.assertNull(mc.asyncGetAttr(KEY).get());

			// insert two items
			Boolean insertResult = mc.asyncBopInsert(KEY, BKEY, null, "value",
					new CollectionAttributes()).get();
			Assert.assertTrue(insertResult);

			Boolean insertResult2 = mc.asyncBopInsert(KEY, BKEY + 1, null,
					"value", new CollectionAttributes()).get();
			Assert.assertTrue(insertResult2);

			// check count in attributes
			Assert.assertEquals(new Long(2), mc.asyncGetAttr(KEY).get()
					.getCount());

			// get btree item count
			CollectionFuture<Integer> future = mc.asyncBopGetItemCount(KEY,
					BKEY + 2, BKEY + 3, ElementFlagFilter.DO_NOT_FILTER);
			Integer count = future.get();
			Assert.assertNotNull(count);
			Assert.assertEquals(new Integer(0), count);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
