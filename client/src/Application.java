import component.MainPageComponent;
import network.Client;

import java.awt.*;

public class Application {
    static volatile Client client = new Client(1);
    public static void main(String[] args) {
        if (args.length > 0) {
            Client.setServerIP(args[0]);
        }
        EventQueue.invokeLater(()->{
            new MainPageComponent("安全文件传输", client).setVisible(true);
        });
        new Thread(()->{
            client.pullFileList();
        }).start();
    }
}
