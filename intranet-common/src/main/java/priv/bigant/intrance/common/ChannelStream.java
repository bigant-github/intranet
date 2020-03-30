package priv.bigant.intrance.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChannelStream {

    private SocketChannel socketChannel;
    private ByteBuffer byteBuffer;

    private boolean isFirst = true;

    public ChannelStream(SocketChannel socketChannel, int bufferSize) throws IOException {
        this.socketChannel = socketChannel;
        this.byteBuffer = ByteBuffer.allocateDirect(bufferSize);

    }



    public char readChar() throws IOException {
        if (!hasNext()) {
            return 0;
        }
        return byteBuffer.getChar();
    }

    public byte read() throws IOException {
        if (!hasNext()) {
            return 0;
        }
        return byteBuffer.get();
    }


    protected int fill() throws IOException {
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);
        byteBuffer.flip();
        return read;
    }


    public boolean hasNext() throws IOException {
        if (isFirst) {
            isFirst = false;
            if (fill() < 1) return false;
        }
        return byteBuffer.position() < byteBuffer.limit() || fill() > 0;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
