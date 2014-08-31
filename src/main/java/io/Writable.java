package io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 如果对象需要进行网络传送，需要实现本接口，来实现序列化
 * 注意，实现该接口的类，必须实现一个无参数的构造器，否者不能够顺利的反序列化
 * @author pb
 *
 */
public interface Writable {
	/**
	 * 序列化输出
	 * @param out
	 * @throws IOException
	 */
	public void write(DataOutput out) throws IOException;
	/**
	 * 序列化输入
	 * @param in
	 * @throws IOException
	 */
	public void readFields(DataInput in) throws IOException;
}
