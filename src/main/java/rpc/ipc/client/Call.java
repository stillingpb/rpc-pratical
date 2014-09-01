package rpc.ipc.client;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import rpc.io.Writable;

/**
 * 一次客户端调用，所用参数全部封装到Call对象中
 * 
 * @author pb
 * 
 */
class Call implements Writable {

	private String methodName;
	private Writable[] parameters;

	public Call(String methodName, Writable[] parameters) {
		this.methodName = methodName;
		this.parameters = parameters;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(methodName);
		out.writeInt(parameters.length);
		for (Writable para : parameters) {
			String paraClassName = para.getClass().getName();
			out.writeUTF(paraClassName);
			para.write(out);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// ipc客户端call对象，不需要从数据流中反序列化对象
	}
}