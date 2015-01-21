package rpc.ipc.server.headBufer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import rpc.ipc.server.headBuffer.HeadBuffer;
import rpc.ipc.server.headBuffer.factory.PooledHeadBufferFactory;
import rpc.ipc.server.headBuffer.factory.UnPooledHeadBufferFactory;
import rpc.ipc.server.headBuffer.manager.CachedHeadBufferPool;
import rpc.ipc.server.headBuffer.manager.FixedHeadBufferPool;
import rpc.ipc.server.headBuffer.manager.HeadBufferPool;
import rpc.ipc.server.headBuffer.manager.HeadBufferUnPool;
import rpc.ipc.util.HeadBufferException;

public class testHeadBufferPool {
	private PooledHeadBufferFactory pooledFactory;

	@Before
	public void init() {
		pooledFactory = new PooledHeadBufferFactory();
	}

	@Test
	public void testFixedPool() throws HeadBufferException {
		HeadBufferPool pool = FixedHeadBufferPool.newBuilder().setCapacity(5).setIsDirect(false)
				.setHeadBufferFactory(pooledFactory).build();
		assertNotNull(pool);
		assertTrue(pool instanceof FixedHeadBufferPool);
		HeadBuffer buffer = pool.achiveHeadBuffer();
		assertNotNull(buffer);

		buffer.retain();
		buffer.retain();
		assertEquals(buffer.getReferenceCount(), 2);

		buffer.release();
		buffer.release();
		assertEquals(buffer.getReferenceCount(), 0);
	}

	@Test
	public void testCachedPool() throws HeadBufferException {
		HeadBufferPool pool = CachedHeadBufferPool.newBuilder().setCapacity(5).setIsDirect(true)
				.setHeadBufferFactory(pooledFactory).build();
		assertNotNull(pool);
		assertTrue(pool instanceof CachedHeadBufferPool);
		HeadBuffer buffer = pool.achiveHeadBuffer();
		assertNotNull(buffer);

		buffer.retain();
		buffer.retain();
		assertEquals(buffer.getReferenceCount(), 2);

		buffer.release();
		buffer.release();
		assertEquals(buffer.getReferenceCount(), 0);
	}

	@Test
	public void testUnPool() throws HeadBufferException {
		HeadBufferUnPool unPool = HeadBufferUnPool.newBuilder().setIsDirect(false)
				.setHeadBufferFactory(new UnPooledHeadBufferFactory()).build();
		assertNotNull(unPool);
		assertTrue(unPool instanceof HeadBufferUnPool);
		HeadBuffer buffer = unPool.achiveHeadBuffer();
		assertNotNull(buffer);

		buffer.retain();
		buffer.retain();
		assertEquals(buffer.getReferenceCount(), 2);

		buffer.release();
		buffer.release();
		assertEquals(buffer.getReferenceCount(), 0);
	}
}
