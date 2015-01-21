package rpc.ipc.server.headBuffer;

public abstract class HeadBufferPool {
	/**
	 * 获取一个HeadBuffer
	 * 
	 * @return
	 */
	public abstract PooledHeadBuffer achiveHeadBuffer();

	/**
	 * 还回HeadBuffer到池中
	 * 
	 * @param hBuffer
	 */
	public abstract void returnBackHeadBuffer(PooledHeadBuffer hBuffer);

	public abstract Builder newBuilder();

	public abstract class Builder {
		protected int initSize = 5; // 默认大小
		protected int capacity = 5;
		protected HeadBufferFactory bufferFactory;

		public Builder setInitialSize(int initSize) {
			this.initSize = initSize;
			return this;
		}

		public Builder setCapacity(int capacity) {
			this.capacity = capacity;
			return this;
		}

		public Builder setHeadBufferFactory(HeadBufferFactory bufferFactory) {
			this.bufferFactory = bufferFactory;
			return this;
		}

		public abstract HeadBufferPool build();
	}
}
