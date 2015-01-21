package rpc.ipc.server.headBuffer;

import rpc.ipc.util.HeadBufferException;

/**
 * 长度为4 byte的buffer,用来获取序列化后数据的长度
 * 
 * @author pb
 * 
 */
public abstract class HeadBuffer {
	/**
	 * refCount变量取值合法访问 [0, intMax]
	 * refCount的值表示当前持有该对象的引用数量
	 */
	private int refCount = 0;

	public int getReferenceCount() {
		return refCount;
	}

	public void retain() throws HeadBufferException {
		if (refCount == Integer.MAX_VALUE)
			throw new HeadBufferException("HeadBuffer 的引用计数达到最大值" + Integer.MAX_VALUE);
		refCount++;
	}

	public void retain(int num) throws HeadBufferException {
		if (Integer.MAX_VALUE - num < refCount)
			throw new HeadBufferException("HeadBuffer 的引用计数达到最大值" + Integer.MAX_VALUE);
		refCount += num;
	}

	public void release() throws HeadBufferException {
		if (refCount - 1 < 0)
			throw new HeadBufferException("HeadBuffer 的引用计数过小: " + (refCount - 1));
		refCount--;
		if (refCount == 0)
			deallocate();
	}

	public void release(int num) throws HeadBufferException {
		if (refCount - num < 0)
			throw new HeadBufferException("HeadBuffer 的引用计数过小: " + (refCount - num));
		refCount -= num;
		if (refCount == 0)
			deallocate();
	}

	/**
	 * 释放intBuffer资源，防止内存泄漏，在调用release()时refCount为0是触发
	 */
	protected abstract void deallocate();
}
