package priv.bigant.intranet.visual.win;

import priv.bigant.intranet.client.Domain;
import sun.tools.jconsole.JConsole;

import javax.swing.*;
import java.io.IOException;
import java.util.function.Consumer;

public class InsuranceWin {
    private JButton showdownBtn;
    private JPanel content;
    private JTextArea console;
    private JPanel consolePlane;
    private Domain domain;

    public InsuranceWin(Domain domain, Consumer<JPanel> closeAction) {
        this.domain = domain;

        console = new JTextArea();
        console.setEditable(false);
        consolePlane.add(new JScrollPane(console));
        console.setLineWrap(true);
        showdownBtn.addActionListener(x -> {
            if (showdownBtn.getText().equals("链接")) {
                try {
                    for (int i = 0; i < 1000; i++) {
                        console.append("asd\r\n");
                        console.paintImmediately(console.getBounds());
                    }

                    domain.connect();
                    showdownBtn.setText("断开");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(content, e.getMessage(), "服务器链接失败", JOptionPane.ERROR_MESSAGE);
                }
            } else if (showdownBtn.getText().equals("断开")) {
                domain.showdown();
                showdownBtn.setText("链接");
            }
        });
    }

    public JPanel getContent() {
        return content;
    }

}
