package rpc.ipc.server.headBuffer.manager;

import rpc.ipc.server.headBuffer.HeadBuffer;

public interface HeadBufferManager {	
	/**
	 * 获取一个HeadBuffer
	 * 
	 * @return
	 */
	public abstract HeadBuffer achiveHeadBuffer();

	/**
	 * 还回HeadBuffer到池中
	 * 
	 * @param hBuffer
	 */
	public abstract void returnBackHeadBuffer(HeadBuffer hBuffer);

}
