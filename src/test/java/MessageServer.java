import rpc.io.LongWritable;
import rpc.ipc.RPC;
import rpc.ipc.server.Server;

public class MessageServer implements MessageServerProtocol {

	@Override
	public Message getMessage(LongWritable para) {
		System.out.println(para.getValue() + " info");
		Message msg = new Message(12, 23, "pb");
		return msg;
	}

	public static void main(String[] args) {
		Server s = RPC.getServer(new MessageServer(), "127.0.0.1", 2345);
		s.start();
	}
}
