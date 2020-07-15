package com.sinyang.miraigbf;

import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.console.plugins.ConfigSection;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageUtils;
import org.jsoup.helper.HttpConnection;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MessageProcessor {
    final private static String PRAISE_API_URL = "https://chp.shadiao.app/api.php?from=" + PluginMain.shadiaoappIdentifier;
    final private static String ABUSE_MIN_API_URL = "https://nmsl.shadiao.app/api.php?level=min&from=" + PluginMain.shadiaoappIdentifier;
    final private static String ABUSE_MAX_API_URL = "https://nmsl.shadiao.app/api.php?from=" + PluginMain.shadiaoappIdentifier;

    enum ShadiaoType {
        Praise,
        AbuseMin,
        AbuseMax
    }

    public static boolean processAutoReply(GroupMessage event, String message, ConfigSection autoReplyMap) {
        for (String keyword : autoReplyMap.keySet()) {
            if (message.contains(keyword)){
                event.getGroup().sendMessage(autoReplyMap.get(keyword).toString());

                return true;
            }
        }

        return false;
    }

    public static boolean processPraiseAbuse(GroupMessage event, ShadiaoType shadiaoType) {
        String result = null;
        URL url;

        try {
            switch (shadiaoType) {
                case Praise:
                    url = new URL(PRAISE_API_URL);

                case AbuseMin:
                    url = new URL(ABUSE_MIN_API_URL);

                case AbuseMax:
                    url = new URL(ABUSE_MAX_API_URL);

                default:
                    url = new URL(PRAISE_API_URL);
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();

            while ((result = reader.readLine()) != null) {
                sb.append(result);
            }

            if (reader != null) {
                reader.close();
            }

            connection.disconnect();
            event.getGroup().sendMessage(
                    new At(event.getSender()) //At sender
                            .plus("\n")
                            .plus(sb.toString()));

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    public static boolean processPremiumDraw(GroupMessage event, List<ConfigSection> rList, List<ConfigSection> srList, List<ConfigSection> ssrList) {
        PremiumDrawMachine machine = new PremiumDrawMachine();
        BufferedImage mergedImage = machine.doPremiumDraw(false, rList, srList, ssrList);
        final Image image = event.getGroup().uploadImage(mergedImage);
        event.getGroup().sendMessage(
                new At(event.getSender())
                .plus("\n")
                .plus(image));

        return true;
    }
}
