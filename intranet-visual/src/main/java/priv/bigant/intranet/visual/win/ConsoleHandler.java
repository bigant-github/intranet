package priv.bigant.intranet.visual.win;

import sun.security.action.GetPropertyAction;

import javax.swing.*;
import java.security.AccessController;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ConsoleHandler extends Handler {
    public static JTextArea console;
    public static JScrollPane scroll;
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
        println(record.getMessage());
    }

    public void print(String message) {
        console.append(message);
        console.paintImmediately(console.getBounds());
    }

    public void println(String message) {
        console.append(message);
        console.append(lineSeparator);
        console.paintImmediately(console.getBounds());
        JScrollBar sbar = scroll.getVerticalScrollBar();
        sbar.setValue(sbar.getMaximum());
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}