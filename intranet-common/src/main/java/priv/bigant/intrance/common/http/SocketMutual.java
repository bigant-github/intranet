package priv.bigant.intrance.common.http;


import java.io.IOException;
import java.io.OutputStream;

public class SocketMutual {

    private SocketInputStream socketInputStream;
    private int contentLength;
    private SocketBeanss socketBean;

    public SocketMutual(SocketInputStream socketInputStream, SocketBeanss socketBean, int contentLength) {
        this.socketInputStream = socketInputStream;
        this.contentLength = contentLength;
        this.socketBean = socketBean;
    }

    public void mutual() throws IOException {
        OutputStream os = socketBean.getOs();
        os.write(socketInputStream.byteBuffer);

        byte[] bytes = new byte[contentLength];
        int by = socketInputStream.is.read(bytes);
        os.write(by);
    }

}
