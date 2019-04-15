package priv.bigant.intrance.common;


import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by GaoHan on 2018/5/22.
 * <p>
 * socket连接的Bean
 */
public class SocketBeanss {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private InetAddress inetAddress;
    private String domainName;
    private String id;

    public SocketBeanss(Socket socket, String id) {
        this.socket = socket;
        this.id = id;
    }

    public SocketBeanss(Socket socket) throws IOException {
        this.socket = socket;
        this.inetAddress = socket.getInetAddress();
        is = socket.getInputStream();
        os = socket.getOutputStream();
        //this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //this.pw = new PrintWriter(socket.getOutputStream());
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
