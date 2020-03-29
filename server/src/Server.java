import entity.FileInfo;
import entity.MyPacketHead;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Server {
    private static final int BLOCK_SIZE = 1204;
    private static String home = "/usr/local/SecureFileTransfer/";

    public static void main(String[] args) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.socket().bind(new InetSocketAddress(13579));
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    new HandlerThread(socketChannel).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ByteBuffer getFilenameList(int userId) { // 静态方法每个线程一份
        File myDir = new File(home + userId);
        int cnt = 0;
        ByteBuffer buf = ByteBuffer.allocate(2048);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            for (File file : myDir.listFiles()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilename(file.getName());
                fileInfo.setSize(file.length());
                fileInfo.setTime(file.lastModified());
                oos.writeObject(fileInfo);
                cnt++;
            }
            buf.putInt(cnt);
            buf.put(bos.toByteArray()).flip();
            return buf;
        } catch (IOException e) {
            buf.putInt(cnt).flip();
            return buf;
        }
    }

    private static class HandlerThread extends Thread {
        private SocketChannel socketChannel;

        HandlerThread(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            ByteBuffer buf = ByteBuffer.allocate(14);
            try {
                buf.clear();
                buf.limit(14);
                if (socketChannel.read(buf) == -1) {
                    socketChannel.close();
                    return;
                }
                buf.flip();
                MyPacketHead packet = new MyPacketHead();
                packet.fromByteBuffer(buf);
                switch (packet.getType()) {
                    case 1: // 下载文件列表
                        socketChannel.write(getFilenameList(packet.getUserId()));
                        break;
                    case 2: // 客户端请求上传文件
                        buf = ByteBuffer.allocate(packet.getSize());
                        socketChannel.read(buf);
                        buf.flip();
                        byte[] filenameBytes = new byte[packet.getSize()];
                        buf.get(filenameBytes);
                        buf = ByteBuffer.allocate(1 * 1024 * 1024); // 重新分配大容量。buf越大，上传越快
                        String filename = new String(filenameBytes);
                        try(RandomAccessFile file = new RandomAccessFile(home + packet.getUserId() + "/" + filename, "rw")) {
                            FileChannel fileChannel = file.getChannel();
                            while (socketChannel.read(buf) != -1) {
                                buf.flip();
                                fileChannel.write(buf);
                                buf.flip();
                            }
                        }
                        break;
                    case 3:  // 客户端请求下载文件
                        buf = ByteBuffer.allocate(packet.getSize());
                        socketChannel.read(buf);
                        buf.flip();
                        byte[] dFilenameBytes = new byte[packet.getSize()];
                        buf.get(dFilenameBytes);
                        buf = ByteBuffer.allocate(1 * 1024 * 1024); // 重新分配大容量。buf越大，上传越快
                        String dFilename = new String(dFilenameBytes);
                        try(RandomAccessFile file = new RandomAccessFile(home + packet.getUserId() + "/" + dFilename, "r")) {
                            FileChannel fileChannel = file.getChannel();
                            while (fileChannel.read(buf) != -1) {
                                buf.flip();
                                socketChannel.write(buf);
                                buf.flip();
                            }
                        }
                        break;
                }
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
