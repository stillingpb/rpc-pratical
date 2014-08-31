package io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * 实现了Writable接口的类，如果需要反序列化的话， 可以通过该类提供的readObject()方法，反序列化出一个对象
 * 
 * @author pb
 * 
 */
public class ObjectWritable implements Writable {

	public ObjectWritable() {
	}

	/**
	 * 从输入流中创建一个对象，并将值封装到该对象中
	 * 
	 * @param in
	 *            输入流
	 * @param clazz
	 *            将要获取的对象所对应的类
	 * @return 一个对象
	 * @throws IOException
	 */
	public static Writable readObject(DataInput in,
			Class<? extends Writable> clazz) throws IOException {
		return readObject(in, clazz, null);
	}

	/**
	 * 
	 * 从输入流中创建一个对象，并将值封装到该对象中
	 * 
	 * @param in
	 *            输入流
	 * @param clazz
	 *            将要获取的对象所对应的类
	 * @param obj
	 *            如果对象不为null，将数据封装到obj中
	 * @return 一个对象
	 * @throws IOException
	 */
	private static Writable readObject(DataInput in,
			Class<? extends Writable> clazz, ObjectWritable obj)
			throws IOException {
		Writable instance = null;

		if (obj != null) {
			instance = obj;
		} else { // 通过反射，创建一个Writable的对象
			Constructor<? extends Writable> constructor;
			try {
				constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				instance = constructor.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		instance.readFields(in); // 反序列化数据

		return instance;
	}

	public static void writeObject(DataOutput out, Writable obj)
			throws IOException {
		obj.write(out);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		writeObject(out, this);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		readObject(in, ObjectWritable.class, this);
	}

}
