package rpc.ipc.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

class Reader extends Thread {
	Selector readSelector;
	private volatile boolean adding;
	private BlockingQueue<Call> callQueue;

	public Reader(ServerContext context) {
		this.callQueue = context.getCallQueue();
		try {
			readSelector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
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
			throws ClosedChannelException {
		return channel.register(readSelector, SelectionKey.OP_READ);
	}

	public void run() {
		while (ServerStub.running) {
			try {
				readSelector.select();
			} catch (IOException e1) {
				e1.printStackTrace();
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
			Iterator<SelectionKey> iter = readSelector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();
				if (key.isValid() && key.isReadable())
					doRead(key);
			}
		}
	}

	private void doRead(SelectionKey key) {
		Connection c = (Connection) key.attachment();
		try {
			Call call = c.readCall();
			if (call != null) {
				call.setAttach(c); // 将connection对象附属到call对象中，供后续返回时使用
				callQueue.add(call);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
