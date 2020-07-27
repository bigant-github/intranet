package priv.bigant.intranet.visual.win;

import priv.bigant.intranet.client.ClientConfig;
import priv.bigant.intranet.client.Domain;

import javax.swing.*;
import java.awt.*;

public class MainWin extends JFrame {

    private JButton addButton;
    private JTabbedPane tabbedPane;//选项卡面板
    private JMenuBar menuBar;//菜单条

    public MainWin(String title) throws HeadlessException {
        super(title);
        init();
    }

    private void init() {
        menuBar = new JMenuBar();
        JMenuItem menu = new JMenuItem("新建");

        menu.addActionListener(e -> {
            new AddInsuranceWin(this, (hostName, ip, port) -> {
                ClientConfig clientConfig = new ClientConfig();
                clientConfig.setHostName(hostName);
                clientConfig.setLocalHost(ip);
                clientConfig.setLocalPort(port);
                clientConfig.setLogName(hostName);
                Domain domain = new Domain(clientConfig);
                tabbedPane.add(hostName, new InsuranceWin(clientConfig, domain, x -> tabbedPane.remove(x)).getCon());
            });
        });

        menuBar.add(menu);
        setJMenuBar(menuBar);

        tabbedPane = new JTabbedPane();//实例化选项卡面板
        //将两个自定义的面板加入到选项卡面板下，通过选项卡可进行切换
        //设置这个MixedForm的布局模式为BorderLayout
        //将这个选项卡面板添加入该MixedForm的中区域
        add(tabbedPane);
        //设置MixedForm的相关属性
        setBounds(10, 10, 570, 390);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                /*addButton = new JButton("链接");
        this.add(addButton);

*/
        /*JDesktopPane desktopPane = new JDesktopPane();
        JInternalFrame internalFrame = createInternalFrame();
        desktopPane.add(internalFrame);
        this.add(desktopPane);*/
    }

    private static JInternalFrame createInternalFrame() {
        // 创建一个内部窗口
        JInternalFrame internalFrame = new JInternalFrame(
                "内部窗口",  // title
                true,       // resizable
                true,       // closable
                true,       // maximizable
                true        // iconifiable
        );

        // 设置窗口的宽高
        internalFrame.setSize(200, 200);
        // 设置窗口的显示位置
        internalFrame.setLocation(50, 50);
        // 内部窗口的关闭按钮动作默认就是销毁窗口，所有不用设置
        // internalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // 创建内容面板
        JPanel panel = new JPanel();

        // 添加组件到面板
        panel.add(new JLabel("Label001"));
        panel.add(new JButton("JButton001"));

        // 设置内部窗口的内容面板
        internalFrame.setContentPane(panel);

        /*
         * 对于内部窗口，还可以不需要手动设置内容面板，直接把窗口当做普通面板使用，
         * 即直接设置布局，然后通过 add 添加组件，如下代码:
         *     internalFrame.setLayout(new FlowLayout());
         *     internalFrame.add(new JLabel("Label001"));
         *     internalFrame.add(new JButton("JButton001"));
         */

        // 显示内部窗口
        internalFrame.setVisible(true);

        return internalFrame;
    }

}


