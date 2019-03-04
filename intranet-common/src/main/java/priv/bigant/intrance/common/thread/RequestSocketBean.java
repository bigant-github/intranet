package priv.bigant.intrance.common.thread;


/**
 * Created by GaoHan on 2018/5/22.
 * <p>
 * 此类暂且无用
 */
public class RequestSocketBean {

    private byte[] data;
    private SocketBean requestSocketBean;

    public RequestSocketBean(byte[] data, SocketBean requestSocketBean) {
        this.data = data;
        this.requestSocketBean = requestSocketBean;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public SocketBean getRequestSocketBean() {
        return requestSocketBean;
    }

    public void setRequestSocketBean(SocketBean requestSocketBean) {
        this.requestSocketBean = requestSocketBean;
    }

   /* public String readLine() throws IOException {
        String s = socketBean.getBr().readLine();
        if (s != null) {
            stringBuffer.append(s);
        }
        return s;
    }

    public String readAll() throws IOException {
        String s = socketBean.getBr().readLine();
        if (s != null)
            do {
                stringBuffer.append(s);
            } while (null != (s = socketBean.getBr().readLine()));
        return stringBuffer.toString();
    }*/

    public String getHost() {
        String string = new String(data);
        String s = string.substring(string.indexOf("Host:") + 5);
        return s.split("\r\n")[0].trim();
    }
}
