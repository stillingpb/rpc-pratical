package rpc.ipc.server.headBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import rpc.ipc.util.HeadBufferException;

/**
 * 长度为4 byte的buffer,用来获取序列化后数据的长度
 * 
 * @author pb
 * 
 */
public abstract class HeadBuffer {
	/**
	 * refCount变量取值合法访问 [0, intMax] refCount的值表示当前持有该对象的引用数量
	 */
	private int refCount = 0;

	protected ByteBuffer buffer;

	private Integer length;

	/**
	 * 从channel 中读取4字节的长度
	 * 
	 * @param channel
	 * @return 如果读完了长度信息，返回 true，否则返回false
	 * @throws IOException
	 */
	public boolean readLength(SocketChannel channel) throws IOException {
		if (length != null)
			return true;
		channel.read(buffer);
		return buffer.position() == 4;
	}

	public Integer getLength() {
		if (length != null)
			return length;
		if (buffer.position() == 4) {
			buffer.flip();
			length = buffer.asIntBuffer().get();
			return length;
		} else
			return null;
	}

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

	public void reset() {
		refCount = 0;
		buffer.clear();
	}

	/**
	 * 释放intBuffer资源，防止内存泄漏，在调用release()时refCount为0是触发
	 */
	protected abstract void deallocate();
}
