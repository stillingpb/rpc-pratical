package io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntWritable implements Writable {
	private int value;

	public IntWritable() {
	}

	public IntWritable(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(value);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		value = in.readInt();
	}
}
