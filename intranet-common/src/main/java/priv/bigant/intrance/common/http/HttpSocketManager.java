package priv.bigant.intrance.common.http;


import priv.bigant.intrance.common.SocketBeanss;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GaoHan on 2018/5/22.
 */
public class HttpSocketManager {

    private static Map<String, SocketBeanss> throughMap;

    static {
        throughMap = new HashMap<String, SocketBeanss>();
    }

    public static void add(SocketBeanss socketBean) {
        throughMap.put(socketBean.getDomainName(), socketBean);
    }

    public static SocketBeanss get(String host) {
        return throughMap.get(host);
    }

    /**
     * 验证是否存在
     */
    public static boolean isExist(String host) {
        return !(throughMap.get(host) == null);
    }
}
