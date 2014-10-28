import rpc.io.IntWritable;
import rpc.io.LongWritable;

public interface MessageServerProtocol {
	public Message getMessage(LongWritable para);
	
	public void returnVoid(IntWritable para);

	public IntWritable returnNull(IntWritable intWritable);
}
