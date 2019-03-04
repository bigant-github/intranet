package priv.bigant.intrance.common.thread;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by GaoHan on 2018/5/22.
 */
public class ThroughManager {

    private static Map<String, SocketBean> throughMap;
    private static Map<String, RequestSocketBean> requestThroughMap;

    static {
        throughMap = new HashMap<String, SocketBean>();
        requestThroughMap = new HashMap<String, RequestSocketBean>();
    }

    public static void add(SocketBean socketBean) {
        throughMap.put(socketBean.getDomainName(), socketBean);
    }

    public static SocketBean get(String host) {
        return throughMap.get(host);
    }

    /*public static void addRequestThroughMap(String msgKey, SocketBean socketBean) {
        requestThroughMap.put(msgKey, socketBean);
    }*/

    public static void addRequestThroughMap(String msgKey, RequestSocketBean requestSocketBean) {
        requestThroughMap.put(msgKey, requestSocketBean);
    }

    public static RequestSocketBean getRequestThroughMap(String msg) {
        return requestThroughMap.get(msg);
    }

    public static void removeRequestThroughMap(String msg) {
        requestThroughMap.remove(msg);
    }

    private static int keyInt;

    public synchronized static String getKey() {
        return ++keyInt + "";
    }

}
