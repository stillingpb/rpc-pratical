package rpc.ipc.server.headBuffer.manager;

import rpc.ipc.server.headBuffer.factory.HeadBufferFactory;

public abstract class HeadBufferPool implements HeadBufferManager {
	public static Builder newBuilder() {
		return null;
	}

	public static abstract class Builder {
		protected int initSize = 5; // 默认大小
		protected int capacity = 5;
		/**
		 * 使用directBuffer ,或者使用 headBuffer
		 */
		protected boolean isDirect = false;
		protected HeadBufferFactory bufferFactory;

		public Builder setInitialSize(int initSize) {
			this.initSize = initSize;
			return this;
		}

		public Builder setCapacity(int capacity) {
			this.capacity = capacity;
			return this;
		}

		public Builder setIsDirect(boolean isDirect) {
			this.isDirect = isDirect;
			return this;
		}

		public Builder setHeadBufferFactory(HeadBufferFactory bufferFactory) {
			this.bufferFactory = bufferFactory;
			return this;
		}

		public abstract HeadBufferPool build();
	}
}
