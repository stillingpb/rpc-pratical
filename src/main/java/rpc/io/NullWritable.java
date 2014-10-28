package rpc.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NullWritable implements Writable {

	@Override
	public void write(DataOutput out) throws IOException {
		// do nothing
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// do nothing
	}

}
