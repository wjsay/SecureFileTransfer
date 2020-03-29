package component;

import network.Client;

import javax.swing.*;
import java.awt.*;

public class DownloadPageComponent extends JPanel {
    private JPanel jPanel = new JPanel();
    private final JScrollPane jScrollPane = new JScrollPane();
    public DownloadPageComponent(Client client) {
        setLayout(new BorderLayout());
        client.setjPanel下载(jPanel);
        JPanel item = new JPanel(new GridBagLayout()); // 默认FlowLayout
        jPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;  // 横位置
        constraints.gridy = 0;  // 纵坐标
        constraints.gridwidth = 1;  // 宽度
        constraints.gridheight = 1; // 高度
        constraints.weightx = 100; // 拉伸权重
        constraints.weighty = 0; // 不拉伸
        constraints.anchor = GridBagConstraints.NORTH; // 对齐方式
        constraints.ipady = 20; // 最小高度
        constraints.fill = GridBagConstraints.NONE; // 填充方式
        item.add(new JLabel("文件名"), constraints);
        constraints.gridx = 1;
        item.add(new JLabel("下载进度"), constraints);
        add(item, BorderLayout.NORTH);
        jScrollPane.setViewportView(jPanel);
        add(jScrollPane, BorderLayout.CENTER);
    }
}
