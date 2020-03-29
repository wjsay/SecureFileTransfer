package component;

import network.Client;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class UploadPageComponent extends JPanel {
    private final JTextField pathField = new JTextField();
    private final JButton selectBtn = new JButton("打开");
    private final JButton uploadBtn = new JButton("上传");
    private JPanel content = new JPanel(new GridBagLayout());
    private final JScrollPane jScrollPane = new JScrollPane();
    GridBagConstraints constraints = null;

    public UploadPageComponent(Client client) {
        setLayout(new BorderLayout());
        JPanel northPanel = new JPanel(new GridBagLayout());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 100;
        constraints.weighty = 100;
        constraints.insets = new Insets(20, 10, 20,10);
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.ipady = 20;
        pathField.setColumns(50);
        northPanel.add(pathField, constraints);
        selectBtn.addActionListener((e) -> {
            JFileChooser chooser = new JFileChooser("/");
            chooser.showOpenDialog(null);
            File file = chooser.getSelectedFile();
            if (file != null) {
                pathField.setText(file.getAbsolutePath());
            }
        });
        constraints.gridx = 1;
        northPanel.add(selectBtn, constraints);
        constraints.gridx = 2;
        northPanel.add(uploadBtn, constraints);
        add(northPanel, BorderLayout.NORTH);


        constraints.gridx = 0;  // 横位置
        constraints.gridy = 0;  // 纵坐标
        constraints.weighty = 0; // 不拉伸
        content.add(new JLabel("文件名"), constraints);
        constraints.gridx = 1;
        content.add(new JLabel("上传进度"), constraints);
        constraints.gridy++;

        jScrollPane.setViewportView(content);
        add(jScrollPane, BorderLayout.CENTER);

        uploadBtn.addActionListener(event -> {
            String path = pathField.getText();
            pathField.setText("");
            File file = new File(path);
            if (file.exists()) {
                if (file.length() > 0x7f7f7f7f) { // 文件太大
                    return;
                }
                JLabel filenameLabel = new JLabel(file.getName());
                JLabel progressLabel = new JLabel("0.0");
                constraints.gridx = 0;
                content.add(filenameLabel, constraints);
                constraints.gridx = 2;
                content.add(progressLabel, constraints);
                constraints.gridy++;
                client.pushFile(path, progressLabel);
            }
        });
    }
}
