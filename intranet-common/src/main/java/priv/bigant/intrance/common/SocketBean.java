package priv.bigant.intrance.common;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Created by GaoHan on 2018/5/22.
 * <p>
 * socket连接的Bean
 */
public class SocketBean {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private InetAddress inetAddress;
    private String domainName;
    private String id;

    public SocketBean(Socket socket, String id) {
        this.socket = socket;
        this.id = id;
    }

    public SocketBean(SocketChannel socketChannel) throws IOException {
        Socket socket = socketChannel.socket();
        this.socket = socket;
        this.inetAddress = this.socket.getInetAddress();
        is = this.socket.getInputStream();
        os = this.socket.getOutputStream();
    }

    public SocketBean(SocketChannel socketChannel, String id) throws IOException {
        Socket socket = socketChannel.socket();
        this.id = id;
        this.socket = socket;
        this.inetAddress = this.socket.getInetAddress();
        is = this.socket.getInputStream();
        os = this.socket.getOutputStream();
    }

    public SocketBean(Socket socket) throws IOException {
        this.socket = socket;
        this.inetAddress = socket.getInetAddress();
        is = socket.getInputStream();
        os = socket.getOutputStream();
    }

    public void close() {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                os = null;
            }
        }

        if (is != null) {
            try {
                skip();
                is.close();
            } catch (IOException e) {
                os = null;
            }
        }


        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                socket = null;
            }
        }
    }


    public void skip() throws IOException {
        int available = is.available();
        // skip any unread (bogus) bytes
        if (available > 0) {
            is.skip(available);
        }
    }

    public boolean sendUrgentData() {
        try {
            socket.sendUrgentData(0);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public InputStream getIs() {
        return is;
    }

    public void setIs(InputStream is) {
        this.is = is;
    }

    public OutputStream getOs() {
        return os;
    }

    public void setOs(OutputStream os) {
        this.os = os;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
