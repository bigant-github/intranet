package priv.bigant.intrance.common.log;

import java.util.logging.Logger;

public class LogUtil {

    public static final String LOG_NAME = "BigAnt-Log";

    private static final Logger LOG = Logger.getLogger(LOG_NAME);

    public static Logger getLog() {
        return LOG;
    }


}
