package function.test;

import rpc.io.ExceptionWritable;
import rpc.io.IntWritable;
import rpc.io.LongWritable;
import rpc.io.Writable;
import rpc.ipc.RPC;

public class testRPC {
    public static void main(String[] args) {
        testRPC t = new testRPC();
        t.init();
        t.testGetMessage();
//        t.testReturnNull();
//        t.testReturnVoid();
//        t.testReturnException();
    }

    MessageServerProtocol rpcServer;

    // @Before
    public void init() {
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

    // @Test
    public void testReturnException() {
        System.out.println("client test exception");
        try {
            rpcServer.returnException(new IntWritable(2));
        } catch (ExceptionWritable e) {
            System.out.println(e.getMsg());
            for (Writable w : e.getParams())
                System.out.print(((IntWritable) w).getValue() + " ");
        }
    }
}
