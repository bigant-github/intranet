package priv.bigant.test;


import priv.bigant.intrance.common.util.bcel.classfile.ConstantDouble;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChunkedTest {

    public static void main(String[] args) throws IOException {

        SocketChannel open = SocketChannel.open(new InetSocketAddress("127.0.0.1", 80));
        ByteBuffer allocate = ByteBuffer.allocate(2048);
        String chunkedHttp = "GET /test HTTP/1.1\r\n" +
                "Host: www.bigant.club\r\n" +
                "Accept-Encoding: gzip\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "b\r\n" +
                "01234567890\r\n" +
                "5\r\n" +
                "12345\r\n" +
                "0\r\n" +
                "\r\n";
        allocate.put(chunkedHttp.getBytes());
        allocate.flip();
        open.write(allocate);

        System.out.println("123");
    }

}
