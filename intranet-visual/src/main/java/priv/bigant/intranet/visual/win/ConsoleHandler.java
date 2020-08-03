package priv.bigant.intranet.visual.win;

import org.apache.commons.lang3.ObjectUtils;
import sun.security.action.GetPropertyAction;

import javax.swing.*;
import java.security.AccessController;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ConsoleHandler extends Handler {
    private static JTextArea console;
    private static JScrollPane scroll;
    private static boolean flag = false;
    String lineSeparator = AccessController.doPrivileged(
            new GetPropertyAction("line.separator"));


    public ConsoleHandler() {

    }

    public ConsoleHandler(JTextArea console, JScrollPane scroll) {
        this.console = console;
        this.scroll = scroll;
    }

    @Override
    public void publish(LogRecord record) {
        if (flag) {
            println(record.getMessage());
        } else {
            System.out.println(record.getMessage());
        }

    }

    public void print(String message) {
        console.append(message);
        console.paintImmediately(console.getBounds());
    }

    public void println(String message) {
        console.append(message);
        console.append(lineSeparator);
        console.paintImmediately(console.getBounds());
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    public static void init(JTextArea c, JScrollPane s) {
        if (!ObjectUtils.allNotNull(c, s)) {
            throw new NullPointerException("param is null");
        }
        console = c;
        scroll = s;
        flag = true;
    }
}