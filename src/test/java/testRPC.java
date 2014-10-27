import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import rpc.io.LongWritable;
import rpc.ipc.RPC;

public class testRPC {
	MessageServerProtocol rpcServer;

	@Before
	public void init() throws URISyntaxException, MalformedURLException {
		rpcServer = RPC.getClientProxy(MessageServerProtocol.class, "127.0.0.1", 2345);
	}

	@Test
	public void testGetMessage() {
		Message msg = rpcServer.getMessage(new LongWritable(10));
		System.out.println(msg);
	}
}
