package rpc.ipc.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import rpc.ipc.util.RPCServerException;

class Reader extends Thread {
	private ServerContext context;

	Selector readSelector;
	private volatile boolean adding;
	private BlockingQueue<Call> callQueue;

	public Reader(ServerContext context) throws RPCServerException {
		this.context = context;
		this.callQueue = context.getCallQueue();
		try {
			readSelector = Selector.open();
		} catch (IOException e) {
			throw new RPCServerException("reader 创建异常", e);
		}
	}

	public void startAdd() {
		adding = true;
		readSelector.wakeup();
	}

	public void finishAdd() {
		adding = false;
		synchronized (this) {
			this.notify();
		}
	}

	public synchronized SelectionKey registerChannel(SocketChannel channel)
			throws RPCServerException {
		try {
			return channel.register(readSelector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			throw new RPCServerException("read事件注册异常", e);
		}
	}

	public void run() {
		try {
			while (context.running) {
				readSelector.select();
				while (adding) {
					synchronized (this) {
						this.wait();
					}
				}
				Iterator<SelectionKey> iter = readSelector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();
					if (key.isValid() && key.isReadable())
						doRead(key);
				}
			}
		} catch (Exception e) {
			RPCServerException serverException = new RPCServerException("reader 抛出异常", e);
			serverException.printStackTrace();
		} finally {
			// 关闭 reader
			try {
				readSelector.close();
			} catch (IOException e) {
				RPCServerException serverException = new RPCServerException("reader 抛出异常", e);
				serverException.printStackTrace();
			}
		}
	}

	private void doRead(SelectionKey key) {
		Connection conn = (Connection) key.attachment();
		try {
			Call call = conn.readCall();
			if (call != null) {
				call.setAttach(conn); // 将connection对象附属到call对象中，供后续返回时使用
				callQueue.add(call);
			}
		} catch (IOException e) {
			RPCServerException serverException = new RPCServerException("reader 读取client调用请求失败", e);
			serverException.printStackTrace();
			conn.close();
		}
	}
}
