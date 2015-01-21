package rpc.ipc.server.headBuffer.factory;

import rpc.ipc.server.headBuffer.HeadBuffer;

public interface HeadBufferFactory {

	/**
	 * 创建headBuffer
	 * 
	 * @param isDirect
	 *            是否使用直接内存,还是使用堆内存
	 * @return
	 */
	public HeadBuffer allocHeadBuffer(boolean isDirect);
}
