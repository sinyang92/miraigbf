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
    final private static String PRAISE_KEYWORD = "����";
    final private static String PREMIUM_DRAW_KEYWORD = "ʮ��";

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