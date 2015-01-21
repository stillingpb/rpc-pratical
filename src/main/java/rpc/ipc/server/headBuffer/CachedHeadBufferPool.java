package rpc.ipc.server.headBuffer;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 可以动态调整缓存大小的pool，当使用率低于50%，就回收掉还回的buffer,当不存在空闲的buffer,就申请新的buffer
 * 
 * @author pb
 * 
 */
public class CachedHeadBufferPool extends HeadBufferPool {
	private int usedCount = 0;
	private int freeCount = 0;
	private LinkedBlockingQueue<PooledHeadBuffer> buffers;

	private CachedHeadBufferPool(LinkedBlockingQueue<PooledHeadBuffer> buffers) {
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

	private CachedPoolBuilder builder = new CachedPoolBuilder();

	public class CachedPoolBuilder extends Builder {
		private CachedPoolBuilder() {
		}

		@Override
		public HeadBufferPool build() {
			buffers = new LinkedBlockingQueue<PooledHeadBuffer>();
			for (int i = 0; i < initSize; i++)
				buffers.offer(bufferFactory.allocHeadBuffer());
			return new CachedHeadBufferPool(buffers);
		}
	}

}
