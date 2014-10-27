package rpc.ipc.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import rpc.io.Writable;

/**
 * 负责维护一次远程调用的连接，并控制请求数据的读，结果数据的写
 * 
 * @author pb
 * 
 */
class Connection {
	SocketChannel channel;
	/*********************************/
	// 接收调用请求相关参数
	private SelectionKey readKey;
	private ByteBuffer lenBuff;
	private ByteBuffer dataBuff;

	/*********************************/

	// 发送调用结果相关参数
	private SelectionKey writeKey;
	private ByteBuffer writeBuffer;

	/*********************************/

	public Connection(SelectionKey readKey, SocketChannel channel) {
		this.readKey = readKey;
		this.channel = channel;
		// 4字节大小的buffer，获取data的长度
		lenBuff = ByteBuffer.allocate(4);
	}

	/**
	 * 将调用结果设置到connection对象中
	 * 
	 * @param result
	 */
	public void setResult(Writable result) {
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(response);
		// 将结果序列化
		try {
			out.writeUTF(result.getClass().getName());
			result.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] res = response.toByteArray();
		writeBuffer = ByteBuffer.allocate(res.length);
		writeBuffer.put(res);
		writeBuffer.flip(); // 为读数据做准备
	}

	/**
	 * 从连接中读取数据
	 * 
	 * @return 如果一次调用没有读完数据，那么返回null，如果这次调用的数据读完了，返回一个call对象
	 * @throws IOException
	 */
	public Call readCall() throws IOException {
		if (lenBuff.remaining() > 0) {
			channel.read(lenBuff);
			return null; // 还未读取完call对象
		}
		if (dataBuff == null) {
			lenBuff.flip();
			dataBuff = ByteBuffer.allocate(lenBuff.getInt());
			lenBuff.position(4); // 重新设置回lenBuff装满数据的状态
		}
		if (dataBuff.remaining() > 0)
			channel.read(dataBuff);

		if (dataBuff.remaining() == 0) {
			dataBuff.flip();
			DataInput dis = new DataInputStream(new ByteArrayInputStream(dataBuff.array()));
			Call call = new Call();
			call.readFields(dis);
			return call;
		}
		return null;
	}

	/**
	 * 将result写出到channel
	 * 
	 * @return 是否写完结果，如果写完,返回true，如果未写完,返回false
	 * @throws IOException
	 */
	public boolean writeResult() throws IOException {
		if (writeBuffer.remaining() > 0)
			channel.write(writeBuffer);
		if (writeBuffer.remaining() <= 0)
			return true;
		else
			return false;
	}

	/**
	 * 关闭连接，并将注册的read,write事件从readselectKey，writeselectKey的selector中移除
	 * 
	 */
	public void close() {
		if (readKey != null)
			readKey.cancel();
		if (writeKey != null)
			writeKey.cancel();
		try {
			channel.close();
		} catch (IOException e) {
		}
	}

	public void setWriteSelectionKey(SelectionKey key) {
		this.writeKey = key;
	}
}