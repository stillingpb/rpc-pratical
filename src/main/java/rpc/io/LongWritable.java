package rpc.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongWritable implements Writable {
	private long value;

	public LongWritable() {
	}

	public LongWritable(long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(value);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		value = in.readLong();
	}
}
