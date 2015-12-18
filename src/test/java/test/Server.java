package test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

/**
 * TCP/IP的NIO非阻塞方式
 * 服务器端
 * */
public class Server implements Runnable {

    //第一个端口
    private Integer port1 = 8099;
    //第二个端口
    private Integer port2 = 9099;
    //第一个服务器通道 服务A
    private ServerSocketChannel serversocket1;
    //第二个服务器通道 服务B
    private ServerSocketChannel serversocket2;
    //连接1
    private SocketChannel clientchannel1;
    //连接2
    private SocketChannel clientchannel2;

    //选择器，主要用来监控各个通道的事件
    private Selector selector;

    //缓冲区
    private ByteBuffer buf = ByteBuffer.allocate(512);

    public Server() {
        init();
    }

    /**
     * 这个method的作用
     * 1：是初始化选择器
     * 2：打开两个通道
     * 3：给通道上绑定一个socket
     * 4：将选择器注册到通道上
     * */
    public void init() {
        try {
            //创建选择器
            this.selector = SelectorProvider.provider().openSelector();
            //打开第一个服务器通道
            this.serversocket1 = ServerSocketChannel.open();
            //告诉程序现在不是阻塞方式的
            this.serversocket1.configureBlocking(false);
            //获取现在与该通道关联的套接字
            this.serversocket1.socket().bind(new InetSocketAddress("localhost", this.port1));
            //将选择器注册到通道上，返回一个选择键
            //OP_ACCEPT用于套接字接受操作的操作集位
            this.serversocket1.register(this.selector, SelectionKey.OP_ACCEPT);

            //然后初始化第二个服务端
            this.serversocket2 = ServerSocketChannel.open();
            this.serversocket2.configureBlocking(false);
            this.serversocket2.socket().bind(new InetSocketAddress("localhost", this.port2));
            this.serversocket2.register(this.selector, SelectionKey.OP_ACCEPT);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 这个方法是连接
     * 客户端连接服务器
     * @throws IOException
     * */
    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        if (server.equals(serversocket1)) {
            clientchannel1 = server.accept();
            clientchannel1.configureBlocking(false);
            //OP_READ用于读取操作的操作集位
            clientchannel1.register(this.selector, SelectionKey.OP_READ);
        } else {
            clientchannel2 = server.accept();
            clientchannel2.configureBlocking(false);
            //OP_READ用于读取操作的操作集位
            clientchannel2.register(this.selector, SelectionKey.OP_READ);
        }
    }

    /**
     * 从通道中读取数据
     * 并且判断是给那个服务通道的
     * @throws IOException
     * */
    public void read(SelectionKey key) throws IOException {

        this.buf.clear();
        //通过选择键来找到之前注册的通道
        //但是这里注册的是ServerSocketChannel为什么会返回一个SocketChannel？？
        SocketChannel channel = (SocketChannel) key.channel();
        //从通道里面读取数据到缓冲区并返回读取字节数
        int count = channel.read(this.buf);

        if (count == -1) {
            //取消这个通道的注册
            key.channel().close();
            key.cancel();
            return;
        }

        //将数据从缓冲区中拿出来
        String input = new String(this.buf.array()).trim();
        //那么现在判断是连接的那种服务
        if (channel.equals(this.clientchannel1)) {
            System.out.println("欢迎您使用服务A");
            System.out.println("您的输入为：" + input);
        } else {
            System.out.println("欢迎您使用服务B");
            System.out.println("您的输入为：" + input);
        }

    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("running ... ");
                //选择一组键，其相应的通道已为 I/O 操作准备就绪。
                this.selector.select();

                //返回此选择器的已选择键集
                //public abstract Set<SelectionKey> selectedKeys()
                Iterator selectorKeys = this.selector.selectedKeys().iterator();
                while (selectorKeys.hasNext()) {
                    System.out.println("running2 ... ");
                    //这里找到当前的选择键
                    SelectionKey key = (SelectionKey) selectorKeys.next();
                    //然后将它从返回键队列中删除
                    selectorKeys.remove();
                    if (!key.isValid()) { // 选择键无效
                        continue;
                    }
                    if (key.isAcceptable()) {
                        //如果遇到请求那么就响应
                        this.accept(key);
                    } else if (key.isReadable()) {
                        //读取客户端的数据
                        this.read(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        Thread thread = new Thread(server);
        thread.start();
    }
}
