package priv.bigant.intranet.visual.win;

import org.apache.commons.lang3.StringUtils;
import sun.swing.StringUIClientPropertyKey;

import javax.swing.*;
import java.awt.event.*;

public class AddInsuranceWin extends JDialog {
    private JPanel contentPane;
    private JTextField textField1;
    private JTextField ipTextField;
    private JTextField 端口TextField;
    private JButton 提交Button;

    public AddInsuranceWin() {
        setContentPane(contentPane);
        setModal(true);
        pack();
        setVisible(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        提交Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(123123123);
            }
        });
        /*提交Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });*/
    }

    public AddInsuranceWin(JFrame jFrame, AddInsuranceAction actionListener) {
        super(jFrame, "添加内网穿透", false);
        setContentPane(contentPane);
        //setModal(true);
        pack();
        setVisible(true);
        //setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        提交Button.addActionListener(e -> {
            String toolTipText = textField1.getToolTipText();
            if (StringUtils.isEmpty(toolTipText)) {
                JOptionPane.showMessageDialog(this, "请输入域名", "域名输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (toolTipText.length() > 20)
                JOptionPane.showMessageDialog(this, "请输入域名", "请输入域名", JOptionPane.ERROR_MESSAGE);

            JOptionPane.showMessageDialog(this, "请输入域名", "请输入域名", JOptionPane.ERROR_MESSAGE);

            actionListener.action("", "", "");

        });
    }
}

interface AddInsuranceAction {
    void action(String host, String ip, String port);
}
