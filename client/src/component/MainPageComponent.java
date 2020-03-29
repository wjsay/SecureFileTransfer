package component;

import network.Client;

import javax.swing.*;
import java.awt.*;

public class MainPageComponent extends JFrame {
    Container[] containers = new Container[3];
    JPanel minePanel = new JPanel(new GridBagLayout());
    String[] category = {"文件名", "修改日期", "大小"};
    JScrollPane mineScrollPane = new JScrollPane();

    public MainPageComponent(String title, Client client) {
        setSize(800, 600);
        setTitle(title);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 100;
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.ipady = 20;
        JPanel myFilePage = new JPanel(new BorderLayout());
        JPanel item = new JPanel(new GridBagLayout());
        for (int i = 0; i < category.length; ++i) {
            constraints.gridx = i;
            item.add(new JLabel(category[i]), constraints);
        }
        myFilePage.add(item, BorderLayout.NORTH);
        mineScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mineScrollPane.setViewportView(minePanel); //mineScrollPane.setBorder(null);
        myFilePage.add(mineScrollPane, BorderLayout.CENTER);
        containers[0] = myFilePage;
        containers[1] = new UploadPageComponent(client);
        containers[2] = new DownloadPageComponent(client);
        client.setjPanel我的(minePanel);
        add(myFilePage, BorderLayout.CENTER);
        JMenuBar menuBar = new MenuBarComponent(this.getContentPane(), containers);
        setJMenuBar(menuBar);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
