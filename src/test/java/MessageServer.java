import rpc.io.IntWritable;
import rpc.io.LongWritable;
import rpc.ipc.RPC;
import rpc.ipc.server.ServerStub;
import rpc.ipc.util.RPCServerException;

public class MessageServer implements MessageServerProtocol {

	public static void main(String[] args) {
		try {
			ServerStub s = RPC.getServer(new MessageServer(), "127.0.0.1", 2345);
			s.start();
		} catch (RPCServerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Message getMessage(LongWritable para) {
		System.out.println("server return msg");
		Message msg = new Message(12, 23, "pb");
		return msg;
	}

	@Override
	public void returnVoid(IntWritable para) {
		System.out.println("server return void " + para.getValue());
	}

	@Override
	public IntWritable returnNull(IntWritable intWritable) {
		System.out.println("server return null " + intWritable.getValue());
		return null;
	}

}
