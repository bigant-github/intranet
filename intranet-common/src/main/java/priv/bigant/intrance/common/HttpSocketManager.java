package priv.bigant.intrance.common;

import priv.bigant.intrance.common.communication.HttpCommunication;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by GaoHan on 2018/5/22.
 */
public class HttpSocketManager {

    private final static Map<String, HttpCommunication> throughMap;
    private static Iterator<Map.Entry<String, HttpCommunication>> iterator;
    private final static Map<String, String> keyMap = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(HttpSocketManager.class.getName());

    static {
        throughMap = new Hashtable<>();
        iterator = throughMap.entrySet().iterator();
    }

    public static void add(String host, HttpCommunication serverCommunication) {
        throughMap.put(host, serverCommunication);
    }

    public static void addKey(String key, String host) {
        keyMap.put(key, host);
        LOG.fine("添加log   key:" + key + "    host:" + host);
    }

    public static String getKey(String key) {
        return keyMap.get(key);
    }

    public static HttpCommunication get(String host) {
        HttpCommunication serverCommunication = throughMap.get(host);
        return serverCommunication;
    }

    public static HttpCommunication remove(String host) {
        return throughMap.remove(host);
    }

    /**
     * 验证是否存在
     */
    public static boolean isExist(String host) {
        return !(throughMap.get(host) == null);
    }


    public synchronized static Map.Entry<String, HttpCommunication> nextSocketBeans() {

        if (iterator.hasNext())
            return iterator.next();

        if (throughMap.isEmpty())
            return null;

        iterator = throughMap.entrySet().iterator();

        return iterator.next();
    }

}
