package rpc.ipc.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import rpc.ipc.util.RPCServerException;

class Reader extends Thread {
	private ServerContext context;
	Selector readSelector;
	private BlockingQueue<Call> callQueue;

	private volatile boolean adding;
	private ReentrantLock addingLock = new ReentrantLock();
	private Condition addingCondition = addingLock.newCondition();

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
		try {
			addingLock.lock();
			adding = false;
			addingCondition.signalAll();
		} finally {
			addingLock.unlock();
		}
	}

	public SelectionKey registerChannel(SocketChannel channel) throws RPCServerException {
		try {
			addingLock.lock();
			return channel.register(readSelector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			throw new RPCServerException("read事件注册异常", e);
		} finally {
			addingLock.unlock();
		}
	}

	public synchronized void run() {
		try {
			addingLock.lock();
			while (context.running) {
				while (adding) {
					addingCondition.await();
				}
				int num = readSelector.select();
				if (num <= 0)
					continue;
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
			addingLock.unlock();
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
