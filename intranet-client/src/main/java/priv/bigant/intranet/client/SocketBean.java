package priv.bigant.intranet.client;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by GaoHan on 2018/5/22.
 * <p>
 * socket连接的Bean
 */
public class SocketBean {
    // 和本线程相关的Socket
    private Socket socket;
    //private BufferedReader br = null;
    //private PrintWriter pw = null;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private InetAddress inetAddress;
    private String domainName;

    public SocketBean(Socket socket) throws IOException {
        this.socket = socket;
        this.inetAddress = socket.getInetAddress();
        this.bis = new BufferedInputStream(socket.getInputStream(), 1024 * 1024 * 3);
        this.bos = new BufferedOutputStream(socket.getOutputStream(), 1024 * 1024 * 3);

        //this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //this.pw = new PrintWriter(socket.getOutputStream());
    }


    public void close() throws IOException {
        /*if (br != null)
            br.close();

        if (pw != null)

            pw.close();*/

        if (bos != null)
            bos.close();

        if (bis != null)
            bis.close();

        if (socket != null)
            socket.close();

    }

    public BufferedInputStream getBis() {
        return bis;
    }

    public BufferedOutputStream getBos() {
        return bos;
    }

    /**
     * 此方法只能读取1024个字节用于简单通信
     *
     * @throws IOException
     */
    public byte[] readBytes() throws IOException {
        byte[] bytes = new byte[1024];
        int read = bis.read(bytes);
        return Arrays.copyOfRange(bytes, 0, read);
    }

    public void write(byte[] bytes) throws IOException {
        bos.write(bytes);
        bos.flush();
    }


    /**
     * 获取一行内容
     *
     * @throws IOException
     */
    /*public String readLine() throws IOException {
        return br.readLine();
    }*/

    /**
     * 获取一行内容
     *
     * @throws IOException
     */
    /*public String readAll() throws IOException {
        return br.readLine();
    }*/

    /*public String readAll() throws IOException {

    }*/

    /**
     * 输出内容
     */
    /*public void write(String msg) {
        pw.write(msg);
        pw.flush();
    }*/
    public String getHostName() {
        return inetAddress.getHostName();
    }

    public String getDomainName() {
        return domainName;
    }

    /*public PrintWriter getPw() {
        return pw;
    }

    public BufferedReader getBr() {
        return br;
    }*/

    public Socket getSocket() {
        return socket;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /*public String getResponse() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        String s = br.readLine();
        if (s != null)
            do {
                stringBuffer.append(s);
            } while (null != (s = br.readLine()));
        return stringBuffer.toString();
    }*/
}
