package test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class test {
    public static void main(String[] args) {
        ByteBuffer buff = ByteBuffer.allocateDirect(1024);
        buff.position(0).limit(512);
        IntBuffer intBuffer = buff.asIntBuffer();
        for(int i=0; i<128; i++){
            intBuffer.put(i);
            System.out.println(i);
        }

        ByteBuffer buff2 = buff.duplicate();
        buff2.position(512).limit(1024);
        intBuffer = buff2.asIntBuffer();
        for(int i=0; i<128; i++){
            intBuffer.put(i);
            System.out.println(i);
        }
    }
}
