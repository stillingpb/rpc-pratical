package test;

import java.io.IOException;
import java.net.Socket;

public class test {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket s = new Socket("127.0.0.1", 3456);
        s.setTcpNoDelay(true);
        s.getOutputStream().write("hello".getBytes());
        s.getOutputStream().flush();
        s.getOutputStream().close();
        s.close();
    }
}
