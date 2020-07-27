package priv.bigant.intrance.common.log;

import org.apache.commons.lang3.StringUtils;

import java.util.logging.Logger;

public class LogUtil {

    public static Logger getLog(String name, Class clazz) {

        if (StringUtils.isEmpty(name)) return Logger.getLogger(clazz.getName());

        else return Logger.getLogger(name);

    }


}
