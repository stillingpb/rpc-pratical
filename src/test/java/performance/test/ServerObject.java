package performance.test;

import rpc.io.Text;
import rpc.ipc.RPC;
import rpc.ipc.server.ServerStub;
import rpc.ipc.util.RPCServerException;

import java.util.Random;

public class ServerObject implements ServerProtocol {

    public static void main(String[] args) {
        try {
            ServerStub s = RPC.getServer(new ServerObject(), "127.0.0.1", 2345);
            s.start();
        } catch (RPCServerException e) {
            e.printStackTrace();
        }
    }

    int count = 0;

    @Override
    public Text echo(Text t) {
        System.out.println(count++);
        int len = 500 + new Random().nextInt(200);
        String str = "";
        for (int i = 0; i < len; i++) {
            str += ((char) ('a' + (i % 26)));
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Text(str);
    }
}
