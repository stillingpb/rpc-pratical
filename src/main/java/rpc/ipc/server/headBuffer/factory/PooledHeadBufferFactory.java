package rpc.ipc.server.headBuffer.factory;

import java.nio.ByteBuffer;

import rpc.ipc.server.headBuffer.HeadBuffer;
import rpc.ipc.server.headBuffer.PooledHeadBuffer;
import rpc.ipc.server.headBuffer.manager.HeadBufferPool;

public class PooledHeadBufferFactory implements HeadBufferFactory {
	private HeadBufferPool pool;

	public void setHeadBufferPool(HeadBufferPool pool) {
		this.pool = pool;
	}

	@Override
	public HeadBuffer allocHeadBuffer(boolean isDirect) {
		if (isDirect)
			return new PooledHeadBuffer(ByteBuffer.allocateDirect(4), pool);
		return new PooledHeadBuffer(ByteBuffer.allocate(4), pool);
	}
}
