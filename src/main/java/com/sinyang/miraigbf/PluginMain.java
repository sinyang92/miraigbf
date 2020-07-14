package com.sinyang.miraigbf;

import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.ConfigSection;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class PluginMain extends PluginBase {
    // Keywords
    final private static String PRAISE_KEYWORD = "夸我";
    final private static String PREMIUM_DRAW_KEYWORD = "十连";

    // Auto Reply
    private Config autoReplyConfig;
    private ConfigSection autoReplyMap;
    final private String AUTOREPLY_CONFIG = "autoreply.yml";
    final private String AUTOREPLY = "autoreply";

    // Premium draw pool
    private Config premiumDrawPoolConfig;
    private List<ConfigSection> poolItemsList;
    private List<ConfigSection> rList = new ArrayList<>();
    private List<ConfigSection> srList = new ArrayList<>();
    private List<ConfigSection> ssrList = new ArrayList<>();
    final private String PREMIUM_DRAW_POOL = "premium_draw_pool.yml";
    final private String ITEMS = "items";

    public void onLoad() {
        super.onLoad();

        // Load Auto Reply
        this.autoReplyConfig = this.loadConfig(AUTOREPLY_CONFIG);
        this.autoReplyMap = this.autoReplyConfig.getConfigSection(AUTOREPLY);

        // Load premium draw pool
        this.premiumDrawPoolConfig = this.loadConfig(PREMIUM_DRAW_POOL);
        this.poolItemsList = this.premiumDrawPoolConfig.getConfigSectionList(ITEMS);
        splitPoolItemList();
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
        if (MessageProcessor.processAutoReply(event, message, this.autoReplyMap)) {
            getLogger().info("Processed auto reply. Skipping...");
        } else if (message.contains(PRAISE_KEYWORD)) {
            getScheduler().async(() -> {
                MessageProcessor.processPraise(event);
            });
        } else if (message.equalsIgnoreCase(PREMIUM_DRAW_KEYWORD)) {
            getScheduler().async(() -> {
                MessageProcessor.processPremiumDraw(event, rList, srList, ssrList);
            });
        }
    }

    private void splitPoolItemList() {
        for (int i = 0; i < poolItemsList.size(); i++) {
            String rare = poolItemsList.get(i).getString("Rare");

            if (rare.equalsIgnoreCase("R")) {
                this.rList.add(poolItemsList.get(i));
            } else if (rare.equalsIgnoreCase("SR")) {
                this.srList.add(poolItemsList.get(i));
            } else if (rare.equalsIgnoreCase("SSR")) {
                this.ssrList.add(poolItemsList.get(i));
            }
        }
    }
}          