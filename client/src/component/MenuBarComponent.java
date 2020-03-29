package component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuBarComponent extends JMenuBar {
    private String[] ss = {"我的文件", "上传文件", "下载列表"};
    JMenu[] menus = new JMenu[ss.length];
    public MenuBarComponent(Container root, Container[] jComponents) {
        for (int i = 0; i < ss.length; ++i) {
            menus[i] = new JMenu(ss[i]);
            final Container component = jComponents[i];
            menus[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    root.removeAll();
                    root.add(component, BorderLayout.CENTER);
                    root.validate();
                    root.repaint(); // 记得重绘
                }
            });
            add(menus[i]);
        }
    }
}
