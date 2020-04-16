package priv.bigant.intrance.common.manager;

import priv.bigant.intrance.common.Connector;

import java.util.ArrayList;
import java.util.Objects;

/**
 * NIO 连接器管理中心
 * 主要是停止使用的
 */
public class ConnectorManager {
    private static ArrayList<Connector> list = new ArrayList<>();

    public static void add(Connector connector) {
        list.add(connector);
    }

    public static Connector getByName(String name) {
        for (Connector connector : list) {
            if (name.equals(connector.getName())) {
                return connector;
            }
        }
        return null;
    }

    public static void remove(Connector connector) {
        list.remove(connector);
    }


    public static void showdownByName(String name) {
        Objects.requireNonNull(getByName(name)).showdown();
    }

    public static void showdownAll() {
        for (Connector connector : list) {
            connector.showdown();
        }
    }

}
