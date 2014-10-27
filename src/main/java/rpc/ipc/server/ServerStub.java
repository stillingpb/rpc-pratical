package rpc.ipc.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import rpc.ipc.util.RPCServerException;

public class ServerStub {
	private ServerContext context;

	/**
	 * 调用队列
	 */
	private BlockingQueue<Call> callQueue;

	private ExecutorService readPool;
	private ExecutorService handlerPool;
	private ExecutorService responsePool;

	private Listener listener = null;
	private Responder[] responders = null;
	private Reader[] readers;
	private Handler[] handlers = null;

	public ServerStub(Object instance, String host, int port) throws RPCServerException {
		this(instance, host, port, ServerContext.DEFAULT_READER_NUM,
				ServerContext.DEFAULT_HANDLER_NUM, ServerContext.DEFAULT_RESPONDER_NUM);
	}

	public ServerStub(Object instance, String host, int port, int readerNum, int handlerNum,
			int responseNum) throws RPCServerException {
		context = new ServerContext();
		context.setInstance(instance);
		context.setHost(host);
		context.setPort(port);

		callQueue = new LinkedBlockingQueue<Call>();
		context.setCallQueue(callQueue);

		responders = new Responder[responseNum];
		for (int i = 0; i < responseNum; i++)
			responders[i] = new Responder(context);
		context.setResponders(responders);

		readers = new Reader[readerNum];
		for (int i = 0; i < readerNum; i++) {
			readers[i] = new Reader(context);
		}
		context.setReaders(readers);

		listener = new Listener(context);

		handlers = new Handler[handlerNum];
		for (int i = 0; i < handlerNum; i++)
			handlers[i] = new Handler(context);
	}

	public void start() {
		handlerPool = Executors.newFixedThreadPool(handlers.length);
		for (Handler handler : handlers)
			handlerPool.execute(handler);

		responsePool = Executors.newFixedThreadPool(responders.length);
		for (Responder responder : responders)
			responsePool.execute(responder);

		readPool = Executors.newFixedThreadPool(readers.length);
		for (Reader reader : readers)
			readPool.execute(reader);

		listener.start();
	}

	public void close() {
		context.running = false;
	}
}
