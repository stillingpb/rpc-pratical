package rpc.ipc.server.headBuffer.manager;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import rpc.ipc.server.headBuffer.HeadBuffer;
import rpc.ipc.server.headBuffer.factory.HeadBufferFactory;
import rpc.ipc.server.headBuffer.factory.PooledHeadBufferFactory;

/**
 * 可以动态调整缓存大小的pool，当使用率低于50%，就会回收掉还回的buffer,当不存在空闲的buffer,就申请新的buffer
 * 
 * @author pb
 * 
 */
public class CachedHeadBufferPool extends HeadBufferPool {
	private boolean isDirectBuffer;
	private Queue<HeadBuffer> buffers;
	private HeadBufferFactory bufferFactory;

	private AtomicInteger freeCount;
	private AtomicInteger totalCount;
	private ReentrantLock offerLock = new ReentrantLock();
	private ReentrantLock pollLock = new ReentrantLock();

	private CachedHeadBufferPool(HeadBufferFactory bufferFactory, boolean isDirect) {
		this.bufferFactory = bufferFactory;
		this.isDirectBuffer = isDirect;
	}

	private void setHeadBufferQueue(Queue<HeadBuffer> buffers) {
		this.buffers = buffers;
		this.freeCount = new AtomicInteger(buffers.size());
		this.totalCount = new AtomicInteger(buffers.size());
	}

	@Override
	public HeadBuffer achiveHeadBuffer() {
		if (freeCount.get() <= 0) {
			HeadBuffer buff = bufferFactory.allocHeadBuffer(isDirectBuffer);
			totalCount.incrementAndGet();
			return buff;
		}
		pollLock.lock();
		try {
			if (freeCount.get() <= 0) {
				HeadBuffer buff = bufferFactory.allocHeadBuffer(isDirectBuffer);
				totalCount.incrementAndGet();
				return buff;
			} else {
				freeCount.decrementAndGet();
				return buffers.poll();
			}
		} finally {
			pollLock.unlock();
		}
	}

	@Override
	public void returnBackHeadBuffer(HeadBuffer hBuffer) {
		resetBuffer(hBuffer);
		int freeCnt = freeCount.get();
		int totalCnt = totalCount.get();
		if (freeCnt << 1 < totalCnt) {
			totalCount.decrementAndGet();
			hBuffer = null;
			return;
		}
		offerLock.lock();
		try {
			buffers.offer(hBuffer);
		} finally {
			freeCount.incrementAndGet();
			offerLock.unlock();
		}
	}

	private void resetBuffer(HeadBuffer hBuffer) {
		hBuffer.reset();
	}

	public static Builder newBuilder() {
		return new CachedPoolBuilder();
	}

	public static class CachedPoolBuilder extends Builder {
		private CachedPoolBuilder() {
		}

		@Override
		public HeadBufferPool build() {
			CachedHeadBufferPool pool = new CachedHeadBufferPool(bufferFactory, isDirect);
			((PooledHeadBufferFactory) bufferFactory).setHeadBufferPool(pool);
			Queue<HeadBuffer> buffers = new LinkedList<HeadBuffer>();
			for (int i = 0; i < initSize; i++)
				buffers.offer(bufferFactory.allocHeadBuffer(isDirect));
			pool.setHeadBufferQueue(buffers);
			return pool;
		}
	}
}
