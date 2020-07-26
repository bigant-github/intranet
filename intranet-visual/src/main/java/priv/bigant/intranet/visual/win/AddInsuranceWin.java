package priv.bigant.intranet.visual.win;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.swing.*;
import java.util.regex.Pattern;

public class AddInsuranceWin extends JDialog {
    private JPanel contentPane;
    private JTextField hostNameField;
    private JTextField ipTextField;
    private JTextField portText;
    private JButton 提交Button;


    public AddInsuranceWin(JFrame jFrame, AddInsuranceAction actionListener) {

        super(jFrame, "添加内网穿透", false);

        setContentPane(contentPane);
        //setModal(true);
        pack();
        setVisible(true);

        //setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        提交Button.addActionListener(e -> {
            String hostName = hostNameField.getText();
            if (StringUtils.isEmpty(hostName)) {
                JOptionPane.showMessageDialog(this, "请输入域名", "域名错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (hostName.length() > 20) {
                JOptionPane.showMessageDialog(this, "域名长度不可超过20", "域名错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String ip = ipTextField.getText();
            if (StringUtils.isEmpty(ip)) {
                JOptionPane.showMessageDialog(this, "请输入客户端IP", "客户端IP错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Pattern.matches("((1[0-9][0-9]\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)|([1-9][0-9]\\.)|([0-9]\\.)){3}((1[0-9][0-9])|(2[0-4][0-9])|(25[0-5])|([1-9][0-9])|([0-9]))", ip)) {
                JOptionPane.showMessageDialog(this, "客户端IP格式有误", "客户端IP错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String port = portText.getText();
            if (StringUtils.isEmpty(port)) {
                JOptionPane.showMessageDialog(this, "请输入客户端端口", "客户端端口错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!StringUtils.isNumeric(port) || Integer.parseInt(port) < 1 || Integer.parseInt(port) > 65535) {
                JOptionPane.showMessageDialog(this, "客户端端口格式不正确", "客户端端口错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            actionListener.action(hostName+".bigant.club", ipTextField.getText(), Integer.parseInt(portText.getText()));

        });
    }
}

interface AddInsuranceAction {
    void action(String host, String ip, int port);
}
