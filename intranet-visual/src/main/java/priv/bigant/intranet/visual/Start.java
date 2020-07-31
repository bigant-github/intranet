package priv.bigant.intranet.visual;

import priv.bigant.intranet.visual.win.InsuranceWin;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

public class Start {
    final static String LOG_CONFIG = "handlers= priv.bigant.intranet.visual.win.ConsoleHandler\n" +
            ".level= INFO\n";

    public static void main(String[] args) throws IOException {
        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(LOG_CONFIG.getBytes()));
        //InsuranceWin insuranceWin = new InsuranceWin();
        JFrame jFrame = new JFrame("ssss");
        jFrame.getContentPane().setBackground(Color.black);
        jFrame.setBounds(60, 100, 1000, 1000);//设置位置大小
        jFrame.setVisible(true);//可视性设置
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置按x后的操作.[这个是关闭整个程序,将会关闭所有窗口]
        jFrame.setContentPane(new InsuranceWin().getCon());
        /*MainWin mainWin = new MainWin("BigAnt");
        mainWin.getContentPane().setBackground(Color.black);
        mainWin.setBounds(60, 100, 1000, 1000);//设置位置大小
        mainWin.setVisible(true);//可视性设置
        mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置按x后的操作.[这个是关闭整个程序,将会关闭所有窗口]*/
    }

}
