package rpc.ipc.server.headBuffer;

public abstract class PooledHeadBuffer extends HeadBuffer {
	private HeadBufferPool pool;
	@Override
	public void deallocate() {
		pool.returnBackHeadBuffer(this);
	}
}
