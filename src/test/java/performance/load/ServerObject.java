package performance.load;

import performance.load.ServerProtocol;
import rpc.io.ExceptionWritable;
import rpc.io.IntWritable;
import rpc.io.LongWritable;
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

    @Override
    public Text echo(Text t) {
        int len = 512 + new Random().nextInt(200);
        String str = "";
        for (int i = 0; i < len; i++) {
            str += ((char) ('a' + (i % 26)));
        }
        return new Text(str);
    }
}
