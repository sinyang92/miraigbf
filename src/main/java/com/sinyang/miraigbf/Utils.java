package com.sinyang.miraigbf;

import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.console.plugins.ConfigSection;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.MessageUtils;
import org.jsoup.helper.HttpConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
    final private static String PRAISE_API_URL = "https://chp.shadiao.app/api.php";
    final private static String PRAISE_KEYWORD = "ø‰Œ“";

    public static void processAutoReply(GroupMessage event, String message, ConfigSection autoReplyMap) {
        for (String keyword : autoReplyMap.keySet()) {
            if (message.contains(keyword)){
                event.getGroup().sendMessage(autoReplyMap.get(keyword).toString());
            }
        }
    }

    public static void processPraise(GroupMessage event, String message) {
        if (message.contains(PRAISE_KEYWORD)) {
            String result = null;

            try {
                URL url = new URL(PRAISE_API_URL);
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

                System.out.println("result: " + sb.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
