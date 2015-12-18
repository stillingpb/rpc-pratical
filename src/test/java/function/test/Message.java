package function.test;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import rpc.io.Writable;

public class Message implements Writable {
	int a;
	long b;
	String c;

	public Message() {
	}

	public Message(int a, long b, String c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(a);
		out.writeLong(b);
		out.writeUTF(c);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		a = in.readInt();
		b = in.readLong();
		c = in.readUTF();
	}

	public String toString() {
		return a + " " + b + " " + c;
	}
}
