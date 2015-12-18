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

import org.apache.log4j.Logger;
import rpc.ipc.util.RPCServerException;

class Reader extends Thread {
    private Logger log4j = Logger.getLogger(Listener.class.getClass());
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
            log4j.fatal("reader 创建异常", e);
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
                    if (key.isValid() && key.isReadable()) {
                        doRead(key);
                    }
                }
            }
        } catch (Exception e) {
            log4j.fatal("reader 运行异常", e);
        } finally { // 关闭 reader
            try {
                readSelector.close();
            } catch (IOException e) {
                log4j.fatal("reader 关闭异常", e);
            }
            addingLock.unlock();
        }
    }

    private void doRead(SelectionKey key) {
        Connection conn = (Connection) key.attachment();
        try {
            int count = conn.readCall(callQueue);
            if (count == -1) { //客户端连接已经关闭，取消这个通道的注册
                key.channel().close();
                key.cancel();
            }
        } catch (IOException e) {
            log4j.error("reader 读取client调用请求失败", e);
            conn.close();
        }
    }
}
