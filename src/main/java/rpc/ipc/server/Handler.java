package rpc.ipc.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import rpc.io.NullWritable;
import rpc.io.Writable;
import rpc.ipc.util.RPCServerException;

class Handler extends Thread {
    private Logger log4j = Logger.getLogger(Listener.class.getClass());

    private ServerContext context;
    private BlockingQueue<Call> callQueue;
    private Object instance;
    private Responder responder;

    public Handler(ServerContext context) {
        this.context = context;
        this.instance = context.getInstance();
        this.callQueue = context.getCallQueue();
        this.responder = context.getResponder();
    }

    public void run() {
        while (context.running) {
            try {
                Call call = callQueue.take(); // 如果callQueue中没有数据，将会阻塞
                Writable result = invokeMethod(call);
                processResult(call.getAttach(), result);
            } catch (InterruptedException e) {
                log4j.fatal("handler 运行异常", e);
            }
        }
    }

    /**
     * 执行方法调用
     *
     * @param call
     * @return result 调用结果
     */
    private Writable invokeMethod(Call call) {
        String methodName = call.getMethodName();
        Writable[] parameters = call.getParameters();
        Class<? extends Writable> paramClass[] = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<? extends Writable> clazz = parameters[i].getClass();
            paramClass[i] = clazz;
        }
        Writable result = null;
        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, paramClass);
            method.setAccessible(true);
            result = (Writable) method.invoke(instance, parameters);
            if (result == null) // 如果调用结果是null
                result = new NullWritable();
        } catch (Exception e) {
            Connection conn = call.getAttach();
            conn.close();
            log4j.error(methodName + " 调用异常", e);
        }
        return result;
    }

    /**
     * 将结果存储到connection对象中，并在channel上注册一个write事件
     *
     * @param conn
     * @param result
     */
    private void processResult(Connection conn, Writable result) {
        try {
            responder.startAdd();
            conn.setResult(result);
            SelectionKey key = responder.registerChannel(conn.channel);
            conn.setWriteSelectionKey(key);
            key.attach(conn);
        } catch (Exception e) { // 关闭connection
            conn.close();
            log4j.error("向responder注册写事件出现异常", e);
        } finally {
            responder.finishAdd();
        }
    }
}