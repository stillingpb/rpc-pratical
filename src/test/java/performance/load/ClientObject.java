package performance.load;

import rpc.io.Text;
import rpc.ipc.RPC;
import java.util.Random;

public class ClientObject {
    public static void main(String[] args) {
        ClientObject client = new ClientObject();
        client.init();
        client.singleThreadLoadTest(10);
    }

    ServerProtocol rpcServer;

    public void init() {
        rpcServer = RPC.getClientProxy(ServerProtocol.class, "127.0.0.1", 2345);
    }

    public void singleThreadLoadTest(int times) {
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            Text param = generateText();
            Text result = rpcServer.echo(param);
            result = null;
            printRate(times, i, beginTime);
        }
        printResult(times, beginTime);
    }

    private int RATE_PERCENT = 10;
    private boolean rateVisited[] = new boolean[RATE_PERCENT];

    private void printResult(int times, long beginTime) {
        long endTime = System.currentTimeMillis();
        System.out.println("test is over. runtime: " +
                (endTime - beginTime) / 1000 + "s\tavgTime: " +
                (endTime - beginTime) / times + "ms");
    }

    private void printRate(int times, int curRound, long beginTime) {
        int rate = (curRound * RATE_PERCENT) / times;
        if (rate > 0 && !rateVisited[rate]) {
            long endTime = System.currentTimeMillis();
            System.out.println(((int) (100 * rate / RATE_PERCENT)) + "%\t" +
                    curRound + "/" + times + "\truntime: " +
                    (endTime - beginTime) / 1000 + "s\tavgTime: " +
                    (endTime - beginTime) / curRound + "ms");
            rateVisited[rate] = true;
        }
    }

    private Text generateText() {
        int len = 512 + new Random().nextInt(200);
        String str = "";
        for (int i = 0; i < len; i++) {
            str += ((char) ('a' + (i % 26)));
        }
        return new Text(str);
    }
}
