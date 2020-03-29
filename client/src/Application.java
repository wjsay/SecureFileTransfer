import component.MainPageComponent;
import network.Client;

import java.awt.*;

public class Application {
    static volatile Client client = new Client();
    public static void main(String[] args) {
        EventQueue.invokeLater(()->{
            new MainPageComponent("安全文件传输", client).setVisible(true);
        });
        new Thread(()->{
            client.pullFileList();
        }).start();
    }
}
