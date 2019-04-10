package priv.bigant.intrance.common.http;


import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketMutual {

    public static void mutual(SocketInputStream socketInputStream, int contentLength, Socket socket) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(socketInputStream.byteBuffer);
        String s = new String(socketInputStream.byteBuffer, StandardCharsets.UTF_8);
        System.out.print(s);
        byte[] bytes = new byte[1024];
        if (contentLength > 0) {
            int readSize = 0;
            int by;
            do {
                by = socketInputStream.is.read(bytes);
                s = new String(bytes, 0, by, StandardCharsets.UTF_8);
                System.out.print(s);
                readSize += by;
                os.write(bytes, 0, by);
            } while (readSize < contentLength && !(by < 1024));
        }
        System.out.println();
    }
}