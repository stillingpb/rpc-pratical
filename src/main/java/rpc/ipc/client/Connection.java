package rpc.ipc.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

import rpc.io.Writable;

/**
 * 负责管理一次rpc，从发送数据，到获取数据的过程
 * 
 * @author pb
 * 
 */
class Connection implements Callable<Writable> {
	private String ipcHost;
	private int ipcPort;
	private Call call;

	private Socket socket;
	private DataInput in;
	private DataOutput out;

	Connection(Call call, String host, int port) {
		this.call = call;
		ipcHost = host;
		ipcPort = port;
	}

	void createConnection() {
		try {
			socket = new Socket(ipcHost, ipcPort);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("ipc连接异常");
			e.printStackTrace();
		}
	}

	void sendPackage() {
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
			e.printStackTrace();
		}

	}

	/**
	 * 获取调用结果
	 * 
	 * @return
	 */
	Writable getResult() {
		Writable instance = null;
		try {
			String className = in.readUTF();
			Class<? extends Writable> clazz = (Class<? extends Writable>) Class.forName(className);
			instance = clazz.getDeclaredConstructor().newInstance();
			instance.readFields(in);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	@Override
	public Writable call() throws Exception {
		createConnection();
		sendPackage();
		return getResult();
	}

}
