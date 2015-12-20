package test;

import rpc.ipc.util.RPCServerException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class mockServer {
    public static void main(String[] args) throws RPCServerException, IOException {
        ServerSocketChannel acceptChannel;
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 3456);
        Selector selector;
        try {
            acceptChannel = ServerSocketChannel.open();
            acceptChannel.configureBlocking(false);
            ServerSocket socket = acceptChannel.socket();
            socket.bind(address);
            selector = Selector.open();
            acceptChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RPCServerException("listener ¥¥Ω®“Ï≥£", e);
        }

        selector.select();
        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove();
            if (key.isValid() && key.isAcceptable())
                doAccept(key);
        }
    }

    private static void doAccept(SelectionKey key) throws IOException {
        SocketChannel channel = null;
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        if ((channel = socketChannel.accept()) != null) {
            channel.configureBlocking(false);
            channel.socket().setTcpNoDelay(true);
        }
        mockServer.selector = Selector.open();
        SelectionKey keys = channel.register(mockServer.selector, SelectionKey.OP_READ);
        keys.attach(channel);

        new Thread(new Reader()).start();
    }

    static Selector selector = null;

    static class Reader implements Runnable {
        public void run() {
            try {
                int count = 0;
                while (true) {
                    int num = selector.select();
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isValid() && key.isReadable()) {
                            doRead(key);
                        }
                        if (!key.isValid()) {
                            System.out.println("error");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private ByteBuffer buffer = ByteBuffer.allocate(2000);

        private void doRead(SelectionKey key) {
            SocketChannel channel = (SocketChannel) key.attachment();
            try {
                int i = channel.read(buffer);
                if(i == -1){
                    key.cancel();
                }
                System.out.println(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
