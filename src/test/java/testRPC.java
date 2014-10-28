import java.net.MalformedURLException;
import java.net.URISyntaxException;

import rpc.io.IntWritable;
import rpc.io.LongWritable;
import rpc.ipc.RPC;

public class testRPC {
	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		testRPC t = new testRPC();
		t.init();
		t.testReturnNull();
		t.testReturnVoid();
		t.testGetMessage();
		t.testGetMessage();
		t.testGetMessage();
	}

	MessageServerProtocol rpcServer;

	// @Before
	public void init() throws URISyntaxException, MalformedURLException {
		rpcServer = RPC.getClientProxy(MessageServerProtocol.class, "127.0.0.1", 2345);
	}

	// @Test
	public void testReturnNull() {
		System.out.println("client test null");
		rpcServer.returnNull(new IntWritable(13));
	}

	// @Test
	public void testReturnVoid() {
		System.out.println("client test void");
		rpcServer.returnVoid(new IntWritable(234));
	}

	// @Test
	public void testGetMessage() {
		System.out.println("client test msg");
		Message msg = rpcServer.getMessage(new LongWritable(10));
		System.out.println(msg);
	}

}
