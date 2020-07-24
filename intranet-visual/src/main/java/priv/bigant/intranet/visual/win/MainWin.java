package priv.bigant.intranet.visual.win;

import priv.bigant.intrance.common.Config;
import priv.bigant.intranet.client.ClientConfig;
import priv.bigant.intranet.client.Domain;

import javax.swing.*;
import java.awt.*;

public class MainWin extends JFrame {

    private JSplitPane jSplitPane;
    private JButton addButton;
    private GridPanel gridPanel;
    private JTabbedPane tabbedPane;//选项卡面板
    private JMenuBar menuBar;//菜单条

    public MainWin(String title) throws HeadlessException {
        super(title);
        init();
    }

    /**
     * 格子布局演示
     */
    public class GridPanel extends JPanel {
        public GridPanel() {
            GridLayout gridLayout = new GridLayout(12, 12);//生成格子布局对象。构造时设置格子
            setLayout(gridLayout);//为该panel设置布局
            JLabel[][] labels = new JLabel[12][12];//格子中的组件
            for (int i = 0; i <= 11; i++) {
                for (int j = 0; j <= 11; j++) {
                    labels[i][j] = new JLabel();
                    if ((i + j) % 2 == 0)
                        labels[i][j].setText("A");
                    else
                        labels[i][j].setText("B");
                    add(labels[i][j]);//将该组件加入到面板中
                }
            }
        }
    }

    /**
     * 自定义的空布局面板
     */
    public class NullPanel extends JPanel {
        JButton button;
        JTextField textField;

        public NullPanel() {
            setLayout(null);//设置布局类型
            button = new JButton("确定");//实例化组件
            textField = new JTextField();//实例化组件
            //将组件加入该面板
            add(button);
            add(textField);
            //设置他们大小和位置
            textField.setBounds(100, 30, 90, 30);
            button.setBounds(190, 30, 66, 30);
        }
    }

    private void init() {
        //setLayout(new FlowLayout());//设置布局

        menuBar = new JMenuBar();
        JMenuItem menu = new JMenuItem("新建");

        menu.addActionListener(e -> {
            AddInsuranceWin addInsuranceWin = new AddInsuranceWin(this, (x, y, z) -> {
                ClientConfig clientConfig = new ClientConfig();
                clientConfig.setHostName("mmm.mmm.mmm");
                clientConfig.setLocalHost("192.168.201.90");
                clientConfig.setLocalPort(80);
                Config.config = clientConfig;
                Domain domain = new Domain(clientConfig);
                try {
                    domain.connect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                domain.startListener();
            });
        });

        menuBar.add(menu);
        setJMenuBar(menuBar);

        gridPanel = new GridPanel();//实例化格子面板对象
        tabbedPane = new JTabbedPane();//实例化选项卡面板
        //将两个自定义的面板加入到选项卡面板下，通过选项卡可进行切换
        tabbedPane.add("格子布局面板", gridPanel);
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


