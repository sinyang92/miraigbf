package com.sinyang.miraigbf;

import kotlinx.coroutines.Job;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class PluginMain extends PluginBase {

    public void onLoad() {
        getLogger().info("Plugin loaded!");
    }

    public void onEnable() {
        getLogger().info("Plugin enabled!");

        this.getEventListener().subscribeAlways(GroupMessage.class, (GroupMessage event) -> {
            String content = event.getMessage().contentToString();
            if (content.contains("reply")) {
                // ���ûظ�
                final QuoteReply quote = MessageUtils.quote(event.getMessage());
                event.getGroup().sendMessage(quote.plus("���ûظ�"));

            } else if (content.contains("at")) {
                // at
                event.getGroup().sendMessage(new At(event.getSender()));

            } else if (content.contains("permission")) {
                // ��ԱȨ��
                event.getGroup().sendMessage(event.getPermission().toString());

            } else if (content.contains("mixed")) {
                // ������Ϣ, ͨ�� .plus ����������Ϣ
                event.getGroup().sendMessage(
                        MessageUtils.newImage("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png") // ��ʾͼƬ, �����ѹ���
                                .plus("Hello") // �ı���Ϣ
                                .plus(new At(event.getSender())) // at Ⱥ��Ա
                                .plus(AtAll.INSTANCE) // at ȫ���Ա
                );

            } else if (content.contains("recall1")) {
                event.getGroup().sendMessage("�㿴����������Ϣ").recall();
                // ������Ϣ���Ͼͳ���. ���ٶ�̫��, �ͻ��˽������������Ϣ.

            } else if (content.contains("recall2")) {
                final Job job = event.getGroup().sendMessage("3��󳷻�").recall(3000);

                // job.cancel(new CancellationException()); // ��ȡ���������

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

}          