package rpc.ipc.server;

import java.util.concurrent.BlockingQueue;

import rpc.ipc.server.headBuffer.factory.PooledHeadBufferFactory;
import rpc.ipc.server.headBuffer.manager.CachedHeadBufferPool;
import rpc.ipc.server.headBuffer.manager.HeadBufferManager;

public class ServerContext {
	/**
	 * 控制服务器启停的变量
	 */
	volatile boolean running = true;

	static int DEFAULT_READER_NUM = 1;
	static int DEFAULT_HANDLER_NUM = 1;
	static int DEFAULT_RESPONDER_NUM = 1;

	static HeadBufferManager DEFAULT_HEAD_BUFFER_MANAGER;

	static {
		DEFAULT_HEAD_BUFFER_MANAGER = CachedHeadBufferPool.newBuilder().setInitialSize(100)
				.setIsDirect(false).setHeadBufferFactory(new PooledHeadBufferFactory()).build();
	}

	private String host;
	private int port;
	private Object instance;
	private BlockingQueue<Call> callQueue;
	private Reader[] readers;
	private Responder[] responders;
	private int currentReader = 0;
	private int currentResponder = 0;

	Reader getReader() {
		currentReader = (currentReader + 1) % readers.length;
		return readers[currentReader];
	}

	Responder getResponder() {
		currentResponder = (currentResponder + 1) % responders.length;
		return responders[currentResponder];
	}

	public Responder[] getResponders() {
		return responders;
	}

	public void setResponders(Responder[] responders) {
		this.responders = responders;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public void setCallQueue(BlockingQueue<Call> callQueue) {
		this.callQueue = callQueue;
	}

	public BlockingQueue<Call> getCallQueue() {
		return this.callQueue;
	}

	public void setReaders(Reader[] readers) {
		this.readers = readers;
	}

	public Reader[] getReaders() {
		return readers;
	}
}
