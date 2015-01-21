package rpc.ipc.server.headBuffer.manager;

import rpc.ipc.server.headBuffer.HeadBuffer;
import rpc.ipc.server.headBuffer.factory.HeadBufferFactory;

public class HeadBufferUnPool implements HeadBufferManager {
	public HeadBufferFactory bufferFactory;
	public boolean isDirect;

	public HeadBufferUnPool(HeadBufferFactory bufferFactory, boolean isDirect) {
		this.bufferFactory = bufferFactory;
		this.isDirect = isDirect;
	}

	@Override
	public HeadBuffer achiveHeadBuffer() {
		return bufferFactory.allocHeadBuffer(isDirect);
	}

	@Override
	public void returnBackHeadBuffer(HeadBuffer hBuffer) {
		// do nothing
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {
		private boolean isDirect = false;
		private HeadBufferFactory bufferFactory;

		private Builder() {
		}

		public Builder setIsDirect(boolean isDirect) {
			this.isDirect = isDirect;
			return this;
		}

		public Builder setHeadBufferFactory(HeadBufferFactory bufferFactory) {
			this.bufferFactory = bufferFactory;
			return this;
		}

		public HeadBufferUnPool build() {
			return new HeadBufferUnPool(bufferFactory, isDirect);
		}
	}
}
