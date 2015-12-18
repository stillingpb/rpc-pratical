package rpc.ipc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import rpc.io.ExceptionWritable;
import rpc.io.NullWritable;
import rpc.io.Writable;

public class ClientInvocation implements InvocationHandler {
    private ClientStub clientStub;
    private String host;
    private int port;

    public ClientInvocation(String host, int port) {
        this.host = host;
        this.port = port;
        clientStub = new ClientStub(host, port);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 将object数组转换成writable数组
        Writable[] args2 = new Writable[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Writable)
                args2[i] = (Writable) args[i];
            else
                throw new IllegalArgumentException("方法参数必须是Writable对象");
        }
        // 调用远程方法
        Writable result = clientStub.remoteCall(method.getName(), args2);
        if (result instanceof NullWritable) // 如果返回的是一个null,也就是NullWritable
            result = null;
        if (result instanceof ExceptionWritable)
            throw (ExceptionWritable) result;
        return result;
    }
}
