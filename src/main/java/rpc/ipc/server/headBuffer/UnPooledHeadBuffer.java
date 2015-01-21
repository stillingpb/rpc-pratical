package rpc.ipc.server.headBuffer;

import java.nio.IntBuffer;

public abstract class UnPooledHeadBuffer extends HeadBuffer {
	private IntBuffer buffer;

	/**
	 * 只能够保证资源被标识为无引用，无法避免java本身对directBuffer回收不即时的问题
	 */
	@Override
	public void deallocate() {
		buffer = null;
	}
}
