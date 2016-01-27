package performance.test;

import rpc.io.Text;
import rpc.ipc.RPC;
import rpc.ipc.util.RPCClientException;

import java.util.Random;
import java.util.concurrent.*;

public class ClientObject {
    public static void main(String[] args) throws InterruptedException {
        ClientObject client = new ClientObject();
        client.init();
//        client.singleThreadLoadTest(10);
        client.multiThreadLoadTest(21, 10);
    }

    static ServerProtocol rpcServer;
    static String host = "127.0.0.1";
    static int port = 2345;

    public void init() {
        rpcServer = RPC.getClientProxy(ServerProtocol.class, host, port);
    }

    public void multiThreadLoadTest(int nThread, final int times) {
        ExecutorService executor = Executors.newFixedThreadPool(nThread);
        CyclicBarrier barrier = new CyclicBarrier(nThread);
        for (int i = 0; i < nThread; i++) {
            executor.execute(new TestThread(barrier, i, times));
        }
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        shutdownClient();
    }

    private void shutdownClient() {
        try {
            RPC.shutdownClientSoftly(host, port);
            while (!RPC.isTerminited(host, port)) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void singleThreadLoadTest(int times) {
        new Thread(new TestThread(new CyclicBarrier(1), 0, times)).start();
    }

    private static Text generateText() {
        int len = 500 + new Random().nextInt(200);
        String str = "";
        for (int i = 0; i < len; i++) {
            str += ((char) ('a' + (i % 26)));
        }
        return new Text(str);
    }

    static class TestThread implements Runnable {

        private int RATE_PERCENT = 10;
        private boolean rateVisited[] = new boolean[RATE_PERCENT];

        private int threadId;
        private int times;
        private CyclicBarrier barrier;

        public TestThread(CyclicBarrier barrier, int threadId, int times) {
            this.barrier = barrier;
            this.threadId = threadId;
            this.times = times;
        }

        private void printResult(int threadId, int times, long beginTime) {
            long endTime = System.currentTimeMillis();
            System.out.println("thread" + threadId + ": test is over. runtime: " +
                    (endTime - beginTime) + "ms\tavgTime: " +
                    (endTime - beginTime) / times + "ms");
        }

        private void printRate(int threadId, int times, int curRound, long beginTime) {
            int rate = (curRound * RATE_PERCENT) / times;
            if (rate > 0 && !rateVisited[rate]) {
                long endTime = System.currentTimeMillis();
                System.out.println("thread" + threadId + ": " +
                        ((int) (100 * rate / RATE_PERCENT)) + "%\t" +
                        curRound + "/" + times + "\truntime: " +
                        (endTime - beginTime) + "ms\tavgTime: " +
                        (endTime - beginTime) / curRound + "ms");
                rateVisited[rate] = true;
            }
        }

        @Override
        public void run() {
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            long beginTime = System.currentTimeMillis();
            for (int i = 0; i < times; i++) {
                Text param = generateText();
                Text result = rpcServer.echo(param);
                result = null;
                printRate(threadId, times, i, beginTime);
            }
            printResult(threadId, times, beginTime);
        }
    }
}
