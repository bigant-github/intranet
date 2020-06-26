package priv.bigant.intranet.visual.win;

import javax.swing.*;
import java.awt.*;

public class IntranetWin extends JPanel {

    private JButton closeButton;
    private JScrollPane body;
    private JLabel flag;

    public IntranetWin() {
        super();
        setLayout(new BorderLayout());
        init();
    }

    public void init() {
        flag = new JLabel("状态");
        add(flag, BorderLayout.NORTH);
        //flag.setBounds()
        closeButton = new JButton("close");
        //add(closeButton);

        JLabel textArea = new JLabel("asdfasdf");
        textArea.setFont(new Font(null, Font.PLAIN, 30));   // 设置字体
        body = new JScrollPane(
                textArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        add(body, BorderLayout.CENTER);
    }
}
