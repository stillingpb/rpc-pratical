package rpc.ipc.server;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

public class ServerContext {
    private static Logger log4j = Logger.getLogger(ServerContext.class.getClass());

    /**
     * 控制服务器启停的变量
     */
    volatile boolean running = true;

    static int DEFAULT_READER_NUM = 2;
    static int DEFAULT_HANDLER_NUM = 2;
    static int DEFAULT_RESPONDER_NUM = 2;

    private String host;
    private int port;
    private Object instance;
    private BlockingQueue<Call> callQueue;
    private Reader[] readers;
    private Responder[] responders;
    private Integer currentReader = 0;
    private Integer currentResponder = 0;

    Reader getReader() {
        synchronized (currentReader) {
            currentReader = (currentReader + 1) % readers.length;
            return readers[currentReader];
        }
    }

    Responder getResponder() {
        synchronized (currentResponder) {
            currentResponder = (currentResponder + 1) % responders.length;
            return responders[currentResponder];
        }
    }

    static void closeChannel(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            log4j.error("channel关闭异常", e);
        }
    }

    public Responder[] getResponders() {
        return responders;
    }

    public void setResponders(Responder[] responders) {
        this.responders = responders;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public void setCallQueue(BlockingQueue<Call> callQueue) {
        this.callQueue = callQueue;
    }

    public BlockingQueue<Call> getCallQueue() {
        return this.callQueue;
    }

    public void setReaders(Reader[] readers) {
        this.readers = readers;
    }

    public Reader[] getReaders() {
        return readers;
    }
}
