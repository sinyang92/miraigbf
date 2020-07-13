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
                // 成员权限
                //event.getGroup().sendMessage(event.getPermission().toString());

            } else if (content.contains("mixed")) {
                // 复合消息, 通过 .plus 连接两个消息
                event.getGroup().sendMessage(
                        MessageUtils.newImage("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png") // 演示图片, 可能已过期
                                .plus("Hello") // 文本消息
                                .plus(new At(event.getSender())) // at 群成员
                                .plus(AtAll.INSTANCE) // at 全体成员
                );

            } else if (content.contains("上传图片")) {
                File file = new File("myImage.jpg");
                if (file.exists()) {
                    final Image image = event.getGroup().uploadImage(new File("myImage.jpg"));
                    // 上传一个图片并得到 Image 类型的 Message
                    event.getGroup().sendMessage(image); // 发送图片

                    final String imageId = image.getImageId(); // 可以拿到 ID
                    final Image fromId = MessageUtils.newImage(imageId); // ID 转换得到 Image

                    event.getGroup().sendMessage(fromId);
                }

            } else if (content.contains("friend")) {
                final Future<MessageReceipt<Contact>> future = event.getSender().sendMessageAsync("Async send"); // 异步发送
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