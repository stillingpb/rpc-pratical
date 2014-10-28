package rpc.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 如果想要返回异常，那么异常类需要继承这个类
 * 
 * @author pb
 * 
 */
public class ExceptionWritable extends Exception implements Writable {

	private String msg;
	private Writable[] params;

	public ExceptionWritable() {

	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Writable[] getParams() {
		return params;
	}

	public void setParams(Writable[] params) {
		this.params = params;
	}

	public ExceptionWritable(String msg, Writable... params) {
		this.msg = msg;
		this.params = params;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(msg);
		out.writeInt(params.length);
		for (Writable para : params) {
			if (para == null)
				para = new NullWritable();
			String paraClassName = para.getClass().getName();
			out.writeUTF(paraClassName);
			para.write(out);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		msg = in.readUTF();
		int len = in.readInt();
		params = new Writable[len];
		for (int i = 0; i < len; i++) {
			String paramClassName = in.readUTF();
			Class<? extends Writable> clazz = null;
			try {
				clazz = (Class<? extends Writable>) Class.forName(paramClassName);
				params[i] = clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			params[i].readFields(in);
			if (params[i] instanceof NullWritable)
				params[i] = null;
		}
	}

}
