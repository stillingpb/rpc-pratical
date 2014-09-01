package rpc.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Text implements Writable {

	private String str;

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(str);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		str = in.readUTF();
	}

}
