package priv.bigant.intranet.visual;

import priv.bigant.intranet.visual.win.MainWin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

public class Start {
    final static String LOG_CONFIG = "handlers= priv.bigant.intranet.visual.win.ConsoleHandler\n" +
            ".level= INFO\n";

    public static void main(String[] args) throws IOException {
        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(LOG_CONFIG.getBytes()));
        MainWin insuranceWin = new MainWin();
    }

}
