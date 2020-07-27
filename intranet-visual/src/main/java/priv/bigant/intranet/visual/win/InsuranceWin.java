package priv.bigant.intranet.visual.win;

import priv.bigant.intranet.client.ClientConfig;
import priv.bigant.intranet.client.Domain;
import priv.bigant.intranet.client.ex.ServerConnectException;

import javax.swing.*;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class InsuranceWin {
    private JButton showdownBtn;
    private JPanel con;
    private JTextArea console;
    private JPanel consolePlane;
    private Domain domain;
    private ClientConfig clientConfig;
    private Logger logger;

    public InsuranceWin() {
    }

    public InsuranceWin(ClientConfig clientConfig, Domain domain, Consumer<JPanel> closeAction) {
        this();
        this.domain = domain;
        this.clientConfig = clientConfig;


        console = new JTextArea();
        console.setEditable(false);
        consolePlane.add(new JScrollPane(console));
        console.setLineWrap(true);

        logger = Logger.getLogger(clientConfig.getHostName());
        logger.addHandler(new ConsoleHandler(console));
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINE);

        domain.setReturnError(x -> showdown());

        showdownBtn.addActionListener(x -> {
            if (showdownBtn.getText().equals("链接")) {
                connect();
            } else if (showdownBtn.getText().equals("断开")) {
                showdown();
            }
        });
    }


    private void connect() {
        try {
            logger.info("链接中");
            domain.connect();
            showdownBtn.setText("断开");
        } catch (IOException | ServerConnectException e) {
            JOptionPane.showMessageDialog(con, e.getMessage(), "服务器链接失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showdown() {
        domain.showdown();
        showdownBtn.setText("链接");
    }

    public JPanel getCon() {
        return con;
    }

    class ConsoleHandler extends Handler {
        private JTextArea console;
        String lineSeparator = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("line.separator"));

        public ConsoleHandler(JTextArea console) {
            this.console = console;
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
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }
}

