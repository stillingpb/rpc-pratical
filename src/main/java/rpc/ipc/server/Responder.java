package rpc.ipc.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import rpc.ipc.util.RPCServerException;

/**
 * 负责将调用完成的数据输出出去
 * 
 * @author pb
 * 
 */
class Responder extends Thread {
	private ServerContext context;

	private Selector writeSelector;

	private volatile boolean adding;
	private ReentrantLock addingLock = new ReentrantLock();
	private Condition addingCondition = addingLock.newCondition();

	public Responder(ServerContext context) throws RPCServerException {
		this.context = context;
		try {
			this.writeSelector = Selector.open();
		} catch (IOException e) {
			throw new RPCServerException("responder 创建异常", e);
		}
	}

	public void startAdd() {
		adding = true;
		writeSelector.wakeup();
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
			return channel.register(writeSelector, SelectionKey.OP_WRITE);
		} catch (ClosedChannelException e) {
			throw new RPCServerException("注册responder异常", e);
		} finally {
			addingLock.unlock();
		}
	}

	public void run() {
		try {
			addingLock.lock();
			while (context.running) {
				while (adding) {
					addingCondition.await();
				}
				int num = writeSelector.select();
				if (num <= 0)
					continue;
				Iterator<SelectionKey> iter = writeSelector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();
					if (key.isValid() && key.isWritable()) {
						doResponse(key);
					}
				}
			}
		} catch (Exception e) {
			RPCServerException serverException = new RPCServerException("responder 抛出异常", e);
			serverException.printStackTrace();
		} finally {
			// 关闭 responder
			try {
				writeSelector.close();
			} catch (IOException e) {
				RPCServerException serverException = new RPCServerException("responder 抛出异常", e);
				serverException.printStackTrace();
			}
			addingLock.unlock();
		}
	}

	/**
	 * 将数据输出出去
	 * 
	 * @param key
	 */
	public void doResponse(SelectionKey key) {
		Connection conn = (Connection) key.attachment();
		boolean isWriteOver;
		try {
			isWriteOver = conn.writeResult();
			if (isWriteOver)
				conn.close();
		} catch (IOException e) {
			RPCServerException serverException = new RPCServerException("writer 写出执行结果失败", e);
			serverException.printStackTrace();
			conn.close();
		}
	}
}