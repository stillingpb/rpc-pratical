import java.nio.ByteBuffer;

public class test {
    public static void main(String[] args) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(23);
        System.out.println(b.remaining());
        b.flip();
        int num = b.getInt();
        System.out.println(b.remaining());
    }
}
