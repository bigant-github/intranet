package priv.bigant.intranet.visual.win;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public class AddInsuranceWin extends JDialog {
    private JPanel contentPane;
    private JTextField hostNameField;
    private JTextField ipTextField;
    private JTextField 端口TextField;
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
            if (hostName.length() > 20){
                JOptionPane.showMessageDialog(this, "域名长度不可超过20", "域名错误", JOptionPane.ERROR_MESSAGE);
                return;
            }



            actionListener.action(hostName, ipTextField.getText(), 端口TextField.getText());

        });
    }
}

interface AddInsuranceAction {
    void action(String host, String ip, String port);
}
