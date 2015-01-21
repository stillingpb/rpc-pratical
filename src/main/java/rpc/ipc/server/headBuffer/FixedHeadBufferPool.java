package rpc.ipc.server.headBuffer;

import java.util.concurrent.LinkedBlockingQueue;

public class FixedHeadBufferPool extends HeadBufferPool {
	private LinkedBlockingQueue<PooledHeadBuffer> buffers;

	private FixedHeadBufferPool(LinkedBlockingQueue<PooledHeadBuffer> buffers) {
		this.buffers = buffers;
	}

	@Override
	public PooledHeadBuffer achiveHeadBuffer() {
		try {
			return buffers.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void returnBackHeadBuffer(PooledHeadBuffer hBuffer) {
		try {
			buffers.put(hBuffer);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Builder newBuilder() {
		return builder;
	}

	private FixedPoolBuilder builder = new FixedPoolBuilder();

	public class FixedPoolBuilder extends Builder {
		private FixedPoolBuilder() {
		}

		@Override
		public HeadBufferPool build() {
			buffers = new LinkedBlockingQueue<PooledHeadBuffer>(capacity);
			for (int i = 0; i < capacity; i++)
				buffers.offer(bufferFactory.allocHeadBuffer());
			return new FixedHeadBufferPool(buffers);
		}
	}
}
