import rpc.io.LongWritable;

public interface MessageServerProtocol {
	public Message getMessage(LongWritable para);
}
