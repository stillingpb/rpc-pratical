package rpc.ipc.client.connection;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import rpc.io.Writable;
import rpc.ipc.client.Call;
import rpc.ipc.util.RPCClientException;

/**
 * 负责管理一次rpc，从发送数据，到获取数据的过程
 *
 * @author pb
 */
class ConnectionImpl implements Connection {
    private Call call;

    private Socket socket;
    private DataInput in;
    private DataOutput out;

    public ConnectionImpl(Socket socket) throws RPCClientException {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RPCClientException("获取socket的输入输出流异常");
        }
    }

    private void sendPackage(Call call) throws RPCClientException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        DataOutput dataOut = new DataOutputStream(buf);
        try {
            call.write(dataOut);
            int callLen = buf.size();
            out.writeInt(callLen);
            byte[] callData = buf.toByteArray();
            out.write(callData);
            socket.getOutputStream().flush();
        } catch (IOException e) {
            throw new RPCClientException("远程调用失败，发生数据包时失败：" + call.toString(), e);
        }

    }

    /**
     * 获取调用结果
     *
     * @return result.
     */
    private Writable getResult() throws RPCClientException {
        String className = null;
        try {
            Writable instance = null;
            className = in.readUTF();
            Class<? extends Writable> clazz = null;
            clazz = (Class<? extends Writable>) Class.forName(className);
            instance = clazz.getDeclaredConstructor().newInstance();
            instance.readFields(in);
            return instance;
        } catch (InstantiationException e) {
            throw new RPCClientException("构造反射对象失败:" + className, e);
        } catch (InvocationTargetException e) {
            throw new RPCClientException("构造反射对象失败:" + className, e);
        } catch (IllegalAccessException e) {
            throw new RPCClientException("构造反射对象失败:" + className, e);
        } catch (ClassNotFoundException e) {
            throw new RPCClientException("无法完成反序列化，找不到类:" + className, e);
        } catch (NoSuchMethodException e) {
            throw new RPCClientException("构造反射对象失败,找不到类" + className + "的默认构造器", e);
        } catch (IOException e) {
            throw new RPCClientException("获取远程结果失败", e);
        }
    }

    public Writable remoteCall(Call call) throws RPCClientException {
        sendPackage(call);
        return getResult();
    }

    public void close() throws RPCClientException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RPCClientException("关闭socket异常", e);
        }
    }
}
