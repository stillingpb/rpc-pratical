package rpc.ipc.server.headBuffer.manager;

import java.util.concurrent.LinkedBlockingQueue;

import rpc.ipc.server.headBuffer.HeadBuffer;
import rpc.ipc.server.headBuffer.factory.PooledHeadBufferFactory;

public class FixedHeadBufferPool extends HeadBufferPool {
	private LinkedBlockingQueue<HeadBuffer> buffers;

	private void setHeadBufferQueue(LinkedBlockingQueue<HeadBuffer> buffers) {
		this.buffers = buffers;
	}

	@Override
	public HeadBuffer achiveHeadBuffer() {
		try {
			return buffers.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void returnBackHeadBuffer(HeadBuffer hBuffer) {
		try {
			buffers.put(hBuffer);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Builder newBuilder() {
		return new FixedPoolBuilder();
	}

	public static class FixedPoolBuilder extends Builder {
		private FixedPoolBuilder() {
		}

		@Override
		public HeadBufferPool build() {
			FixedHeadBufferPool pool = new FixedHeadBufferPool();
			((PooledHeadBufferFactory) bufferFactory).setHeadBufferPool(pool);
			LinkedBlockingQueue<HeadBuffer> buffers = new LinkedBlockingQueue<HeadBuffer>(capacity);
			for (int i = 0; i < capacity; i++)
				buffers.offer(bufferFactory.allocHeadBuffer(isDirect));
			pool.setHeadBufferQueue(buffers);
			return pool;
		}
	}
}
