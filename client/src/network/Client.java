package network;

import entity.FileInfo;
import entity.MyPacketHead;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static String serverIP = "127.0.0.1";
    private static final int serverPort = 13579;
    private static final int BLOCK_SIZE = 1448;

    private int id;
    private List<FileInfo> myFiles = new ArrayList<>();
    private List<String> downloadList = new ArrayList<>();
    private JPanel jPanel我的 = null;
    private JPanel jPanel下载 = null;

    public Client (int id) {
        this.id = id;
    }

    public void pullFileList() {
        List<FileInfo> ret = new ArrayList<>();
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(serverIP, serverPort));
            MyPacketHead packetHead = new MyPacketHead();
            packetHead.setType((short) 1);
            packetHead.setUserId(id);
            ByteBuffer buf = packetHead.toByteBuffer();
            socketChannel.write(buf);
            buf = ByteBuffer.allocate(2048);
            socketChannel.read(buf);
            buf.flip();
            int cnt = buf.getInt();
            if (cnt > 0) {
                byte[] data = new byte[buf.limit() - buf.position()];
                buf.get(data, 0, data.length);
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bis);
                for (int i = 0; i < cnt; ++i) {
                    FileInfo fileInfo = (FileInfo) ois.readObject();
                    produce(fileInfo);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void pushFile(String filepath, JLabel label) { // bug.二次上传少了2个字节。原因：SocketChannel#read的问题，已修复
        new Thread(() -> {  // 不能阻塞UI线程
            MyPacketHead packetHead = new MyPacketHead();
            ByteBuffer buf = ByteBuffer.allocate(BLOCK_SIZE);
            try (SocketChannel socketChannel = SocketChannel.open()) {
                socketChannel.connect(new InetSocketAddress(serverIP, serverPort));
                RandomAccessFile aFile = new RandomAccessFile(filepath, "r");
                String[] tmp = filepath.split("/|\\\\");
                String filename = tmp[tmp.length - 1];
                FileChannel fileChannel = aFile.getChannel();
                packetHead.setType((short) 2);
                packetHead.setUserId(id);
                int total = ((int) aFile.length() + BLOCK_SIZE - 1) / BLOCK_SIZE;
                byte[] filenameBytes = filename.getBytes();
                packetHead.setSize(filenameBytes.length);
                socketChannel.write(packetHead.toByteBuffer());
                socketChannel.write(ByteBuffer.wrap(filenameBytes));
                do {
                    buf.clear();
                    int cnt = fileChannel.read(buf);
                    if (cnt == -1) break;
                    buf.flip();
                    packetHead.incrSeq();
                    socketChannel.write(buf);
                    label.setText(String.format("%.2f%%", 100.0 * packetHead.getSeq() / total));
                } while (true);
                for (FileInfo fileInfo : myFiles) {
                    if (fileInfo.getFilename().equals(filename))
                        return;
                }
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilename(filename);
                fileInfo.setTime(System.currentTimeMillis());
                fileInfo.setSize(aFile.length());
                produce(fileInfo);
            } catch (IOException e) {
                e.printStackTrace();
                label.setText("访问不到服务器");
            }
        }).start();
    }

    public void pullFile(FileInfo fileInfo, String filepath) {
        final JLabel jLabel = new JLabel("0.0");
        produceDownloadInfo(fileInfo.getFilename(), jLabel);
        new Thread(()->{
            MyPacketHead packetHead = new MyPacketHead();
            ByteBuffer buf = ByteBuffer.allocate(20 * 1024 * 1024);
            try (SocketChannel socketChannel = SocketChannel.open(); RandomAccessFile aFile = new RandomAccessFile(filepath, "rw")) {
                socketChannel.connect(new InetSocketAddress(serverIP, serverPort));
                FileChannel fileChannel = aFile.getChannel();
                packetHead.setType((short) 3);
                packetHead.setUserId(id);
                byte[] filenameBytes = fileInfo.getFilename().getBytes();
                packetHead.setSize(filenameBytes.length);
                buf.put(packetHead.toByteBuffer());
                buf.put(filenameBytes);
                buf.flip();
                socketChannel.write(buf);
                buf.clear();
                long cnt = 0;
                while(socketChannel.read(buf) != -1) {
                    buf.flip();
                    fileChannel.write(buf);
                    cnt += buf.limit();
                    jLabel.setText(String.format("%.2f%%", 100.0 * cnt / fileInfo.getSize()));
                    buf.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void produce(FileInfo fileInfo) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 100;
        constraints.weighty = 0; // 不拉伸
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.ipady = 20;
        constraints.fill = GridBagConstraints.NONE;
        myFiles.add(fileInfo);
        while (jPanel我的 == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        constraints.gridy = jPanel我的.getComponentCount() / 3;
        constraints.gridx = 0;
        JLabel filenameLabel = new JLabel(fileInfo.getFilename());
        filenameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser(System.getProperties().getProperty("user.home"));
                chooser.setSelectedFile(new File(filenameLabel.getText()));  // 默认文件名
                chooser.showSaveDialog(null);
                File file = chooser.getSelectedFile();
                if (file != null) {
                    pullFile(fileInfo, file.getAbsolutePath());
                }
            }
        });
        jPanel我的.add(filenameLabel, constraints);
        constraints.gridx = 1;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime time = LocalDateTime.ofEpochSecond(fileInfo.getTime() / 1000, (int) (fileInfo.getTime() % 1000), ZoneOffset.of("+8"));
        jPanel我的.add(new JLabel(dtf.format(time)), constraints);
        constraints.gridx = 2;
        String size = (fileInfo.getSize() + 1023) / 1024 + "KB";
        jPanel我的.add(new JLabel(size), constraints);
    }

    public void produceDownloadInfo(String filename, JLabel jLabel) {
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
        constraints.gridy = downloadList.size();
        downloadList.add(filename);
        jPanel下载.add(new JLabel(filename), constraints);
        constraints.gridx = 1;
        jPanel下载.add(jLabel, constraints);
    }

    public void setjPanel我的(JPanel jPanel) {
        jPanel我的 = jPanel;
    }
    public void setjPanel下载(JPanel jPanel) {
        jPanel下载 = jPanel;
    }

    public static void setServerIP(String serverIP) {
        Client.serverIP = serverIP;
    }

}
