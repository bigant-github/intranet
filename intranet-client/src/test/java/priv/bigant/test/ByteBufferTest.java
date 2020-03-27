package priv.bigant.test;

import java.nio.ByteBuffer;

public class ByteBufferTest {

    public static void main(String[] args) {
        ByteBuffer allocate = ByteBuffer.allocate(100);
        allocate.put((byte)1);

        ByteBuffer duplicate = allocate.duplicate();
        byte b = duplicate.get();
        System.out.println();
    }
}
