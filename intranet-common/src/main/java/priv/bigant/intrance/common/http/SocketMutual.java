package priv.bigant.intrance.common.http;


import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketMutual {

    public static void mutual(SocketInputStream socketInputStream, int contentLength, Socket socket) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(socketInputStream.byteBuffer);
        byte[] bytes = new byte[contentLength];
        int by = socketInputStream.is.read(bytes);
        String s = new String(ArrayUtils.addAll(socketInputStream.byteBuffer, bytes), "UTF-8");
        System.out.println(s);
        os.write(bytes);
    }
}