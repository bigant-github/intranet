package priv.bigant.intranet.visual;

import priv.bigant.intranet.visual.win.MainWin;

import javax.swing.*;
import java.awt.*;

public class Start {
    public static void main(String[] args) {
        MainWin mainWin = new MainWin("BigAnt");
        mainWin.getContentPane().setBackground(Color.black);
        mainWin.setBounds(60, 100, 1000, 1000);//设置位置大小
        mainWin.setVisible(true);//可视性设置
        mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置按x后的操作.[这个是关闭整个程序,将会关闭所有窗口]
    }

}
