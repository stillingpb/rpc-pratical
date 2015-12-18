package rpc.ipc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import rpc.ipc.util.RPCServerException;

class Listener extends Thread {
	private ServerContext context;
	private ServerSocketChannel acceptChannel;
	private Selector selector;

	public Listener(ServerContext context) throws RPCServerException {
		this.context = context;
		String host = context.getHost();
		int port = context.getPort();
		InetSocketAddress address = new InetSocketAddress(host, port);
		try {
			acceptChannel = ServerSocketChannel.open();
			acceptChannel.configureBlocking(false);
			ServerSocket socket = acceptChannel.socket();
			socket.bind(address);
			selector = Selector.open();
			acceptChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			throw new RPCServerException("listener 创建异常", e);
		}
	}

	public void run() {
		try {
			while (context.running) {
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();
					if (key.isValid() && key.isAcceptable())
						doAccept(key);
				}
			}
		} catch (IOException e) {
			RPCServerException serverException = new RPCServerException("listener 抛出异常", e);
			serverException.printStackTrace();
		} finally {
			// 关闭 listener
			try {
				selector.close();
			} catch (IOException e) {
				RPCServerException serverException = new RPCServerException("listener 抛出异常", e);
				serverException.printStackTrace();
			}
		}
	}

	/**
	 * 负责处理一个accept事件，当accept事件发生后，分配一个Reader对象来处理后续的读事件
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void doAccept(SelectionKey key) throws IOException {
		/*
		 * 当某个channel抛出异常，将这个channel去除掉，程序继续运行。。当socketChannel发生异常，程序退出， 抛出异常
		 * IOException
		 */
		ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = null;
		SelectionKey readKey = null;
		while ((channel = socketChannel.accept()) != null) {
			Reader reader = context.getReader();
			try {
				channel.configureBlocking(false);
				channel.socket().setTcpNoDelay(true);
				reader.startAdd();
				readKey = reader.registerChannel(channel);
				// Connection conn = new Connection(readKey, channel);
				Connection conn = new Connection(readKey, channel);
				readKey.attach(conn);
			} catch (Exception e) {// 释放channel和注册事件
				e.printStackTrace();
				if (readKey != null)
					readKey.cancel();
				try {
					if (channel != null)
						channel.close();
				} catch (IOException e1) {
				}
			} finally {
				reader.finishAdd();
			}
		}
	}

}
