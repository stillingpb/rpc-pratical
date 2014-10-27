package rpc.ipc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

class Listener extends Thread {
	private ServerContext context;
	private ServerSocketChannel acceptChannel;
	private Selector selector;

	public Listener(ServerContext context) {
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
			e.printStackTrace();
		}
	}

	public void run() {
		while (ServerStub.running) {
			try {
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();
					if (key.isValid() && key.isAcceptable())
						doAccept(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
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
		ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = null;
		while ((channel = socketChannel.accept()) != null) {
			channel.configureBlocking(false);
			channel.socket().setTcpNoDelay(true);

			Reader reader = context.getReader();
			try {
				reader.startAdd();
				SelectionKey readKey = reader.registerChannel(channel);
				Connection conn = new Connection(readKey, channel);
				readKey.attach(conn);
			} finally {
				reader.finishAdd();
			}
		}
	}

}
