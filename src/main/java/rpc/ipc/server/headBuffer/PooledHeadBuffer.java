package rpc.ipc.server.headBuffer;

import java.nio.ByteBuffer;

import rpc.ipc.server.headBuffer.manager.HeadBufferPool;

public class PooledHeadBuffer extends HeadBuffer {
	private HeadBufferPool pool;

	public PooledHeadBuffer(ByteBuffer buffer, HeadBufferPool pool) {
		this.buffer = buffer;
		this.pool = pool;
	}

	@Override
	public void deallocate() {
		pool.returnBackHeadBuffer(this);
	}
}
