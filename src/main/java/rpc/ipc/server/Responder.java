package rpc.ipc.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 负责将调用完成的数据输出出去
 * 
 * @author pb
 * 
 */
class Responder extends Thread {
	private ServerContext context;

	private Selector writeSelector;
	private boolean adding = false;

	public Responder(ServerContext context) {
		this.context = context;
		try {
			this.writeSelector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startAdd() {
		adding = true;
		writeSelector.wakeup();
	}

	public void finishAdd() {
		adding = false;
		synchronized (this) {
			this.notify();
		}
	}

	public synchronized SelectionKey registerChannel(SocketChannel channel)
			throws ClosedChannelException {
		return channel.register(writeSelector, SelectionKey.OP_WRITE);
	}

	public void run() {
		while (context.running) {
			try {
				writeSelector.select();
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (adding) {
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			Iterator<SelectionKey> iter = writeSelector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();
				if (key.isValid() && key.isWritable())
					doResponse(key);
			}
		}
	}

	/**
	 * 将数据输出出去
	 * 
	 * @param key
	 */
	public void doResponse(SelectionKey key) {
		Connection conn = (Connection) key.attachment();
		try {
			conn.writeResult();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}