package com.sinyang.miraigbf;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.ConfigSection;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class PluginMain extends PluginBase {

    private Config autoReplyConfig;
    private ConfigSection autoReplyMap;
    final private String AUTOREPLY_CONFIG = "autoreply.yml";
    final private String AUTOREPLY = "autoreply";

    public void onLoad() {
        super.onLoad();
        this.autoReplyConfig = this.loadConfig(AUTOREPLY_CONFIG);
        this.autoReplyMap = this.autoReplyConfig.getConfigSection(AUTOREPLY);
    }

    public void onEnable() {
        getLogger().info("Plugin enabled!");

        this.getEventListener().subscribeAlways(GroupMessage.class, (GroupMessage event) -> {
            String content = event.getMessage().contentToString();

            processGroupMessage(event, content);

            if (content.contains("at")) {
                // at
                event.getGroup().sendMessage(new At(event.getSender()));

            } else if (content.contains("permission")) {
                // Commented due to method being unsupported in miral-console 0.5.2
                // ��ԱȨ��
                //event.getGroup().sendMessage(event.getPermission().toString());

            } else if (content.contains("mixed")) {
                // ������Ϣ, ͨ�� .plus ����������Ϣ
                event.getGroup().sendMessage(
                        MessageUtils.newImage("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png") // ��ʾͼƬ, �����ѹ���
                                .plus("Hello") // �ı���Ϣ
                                .plus(new At(event.getSender())) // at Ⱥ��Ա
                                .plus(AtAll.INSTANCE) // at ȫ���Ա
                );

            } else if (content.contains("�ϴ�ͼƬ")) {
                File file = new File("myImage.jpg");
                if (file.exists()) {
                    final Image image = event.getGroup().uploadImage(new File("myImage.jpg"));
                    // �ϴ�һ��ͼƬ���õ� Image ���͵� Message
                    event.getGroup().sendMessage(image); // ����ͼƬ

                    final String imageId = image.getImageId(); // �����õ� ID
                    final Image fromId = MessageUtils.newImage(imageId); // ID ת���õ� Image

                    event.getGroup().sendMessage(fromId);
                }

            } else if (content.contains("friend")) {
                final Future<MessageReceipt<Contact>> future = event.getSender().sendMessageAsync("Async send"); // �첽����
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void processGroupMessage(GroupMessage event, String message) {
        Utils.processAutoReply(event, message, this.autoReplyMap);
        Utils.processPraise(event, message);
    }
}          