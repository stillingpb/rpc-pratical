package rpc.ipc.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;

import rpc.io.Writable;
import rpc.ipc.util.RPCServerException;

class Call implements Writable {
    private String methodName;
    private Writable[] parameters;

    private Connection attach; // 附带的一个connection对象

    /**
     * 创建一个无参构造器，供反序列化用
     */
    public Call() {
    }

    @Override
    public void write(DataOutput out) throws IOException {
        // ipc服务器端call对象，不需要将数据序列化输出
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.methodName = in.readUTF();
        int paramNum = in.readInt();
        parameters = new Writable[paramNum];
        for (int i = 0; i < paramNum; i++) {
            String paramClassName = in.readUTF();
            Class<? extends Writable> clazz = null;
            Writable param = null;
            try {
                clazz = (Class<? extends Writable>) Class.forName(paramClassName);
                param = clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            param.readFields(in);
            parameters[i] = param;
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Writable[] getParameters() {
        return parameters;
    }

    public void setParameters(Writable[] parameters) {
        this.parameters = parameters;
    }

    public Connection getAttach() {
        return attach;
    }

    public void setAttach(Connection attach) {
        this.attach = attach;
    }
}
