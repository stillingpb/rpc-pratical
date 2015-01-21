package rpc.ipc.server.headBuffer.factory;

import java.nio.ByteBuffer;

import rpc.ipc.server.headBuffer.HeadBuffer;
import rpc.ipc.server.headBuffer.UnPooledHeadBuffer;

public class UnPooledHeadBufferFactory implements HeadBufferFactory {

	@Override
	public HeadBuffer allocHeadBuffer(boolean isDirect) {
		if (isDirect)
			return new UnPooledHeadBuffer(ByteBuffer.allocateDirect(4));
		return new UnPooledHeadBuffer(ByteBuffer.allocate(4));
	}
}
