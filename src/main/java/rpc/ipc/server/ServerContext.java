package rpc.ipc.server;

import java.util.concurrent.BlockingQueue;

public class ServerContext {

	private String host;
	private int port;
	private Object instance;
	private Responder responder;
	private BlockingQueue<Call> callQueue;
	private Reader[] readers;
	private int currentReader = 0;

	Reader getReader() {
		currentReader = (currentReader + 1) % readers.length;
		return readers[currentReader];
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

	public void setResponder(Responder responder) {
		this.responder = responder;
	}

	public void setCallQueue(BlockingQueue<Call> callQueue) {
		this.callQueue = callQueue;
	}

	public BlockingQueue<Call> getCallQueue() {
		return this.callQueue;
	}

	public Responder getResponder() {
		return this.responder;
	}

	public void setReaders(Reader[] readers) {
		this.readers = readers;
	}

	public Reader[] getReaders() {
		return readers;
	}
}
