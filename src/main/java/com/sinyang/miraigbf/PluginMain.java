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
    // Config
    final private static String CONFIG_FILE = "config.yml";

    // Shadiaoapp identifier
    public static String shadiaoappIdentifier;

    // Keywords
    final private static String PRAISE_KEYWORD = "夸我";
    final private static String ABUSE_MIN_KEYWORD = "骂我";
    final private static String ABUSE_MAX_KEYWORD = "用力骂我";
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

        shadiaoappIdentifier = this.loadConfig(CONFIG_FILE).getString("shadiaoapp_identifier");

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
        });
    }

    private void processGroupMessage(GroupMessage event, String message) {
        // Auto Reply does not require exact match
        // while others do
        if (MessageProcessor.processAutoReply(event, message, this.autoReplyMap)) {
            getLogger().info("Processed auto reply. Skipping...");
        } else if (message.equalsIgnoreCase(PRAISE_KEYWORD)) {
            getScheduler().async(() -> {
                MessageProcessor.processPraiseAbuse(event, MessageProcessor.ShadiaoType.Praise);
            });
        } else if (message.equalsIgnoreCase(ABUSE_MIN_KEYWORD)) {
            getScheduler().async(() -> {
                MessageProcessor.processPraiseAbuse(event, MessageProcessor.ShadiaoType.AbuseMin);
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