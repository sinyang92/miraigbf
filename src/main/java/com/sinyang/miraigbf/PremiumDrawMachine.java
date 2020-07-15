package com.sinyang.miraigbf;

import net.coobird.thumbnailator.Thumbnails;
import net.mamoe.mirai.console.plugins.ConfigSection;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class PremiumDrawMachine {
    final private String WEAPON_URL_PREFIX = "http://game-a1.granbluefantasy.jp/assets/img/sp/assets/weapon/m/";
    final private String SUMMON_URL_SUFFIX = "http://game-a1.granbluefantasy.jp/assets/img/sp/assets/summon/m/";
    final private String EXTENSION_JPG = ".jpg";

    enum Rarity {
        R,
        SR,
        SSR
    }

    enum DrawType {
        normalSingle,
        normalLast,
        fesSingle,
        fesLast
    }

    // レジェンド
    private double[] normalSingleDrawRarities = new double[] {
            0.82, // R
            0.15, // SR
            0.03  // SSR
    };

    // Sレア以上確定
    private double[] normalLastDrawRarities = new double[] {
            0.00, // R
            0.97, // SR
            0.03  // SSR
    };

    // フェス
    private double[] fesSingleDrawRarities = new double[] {
            0.79, // R
            0.15, // SR
            0.06  // SSR
    };

    // フェスSレア以上確定
    private double[] fesLastDrawRarities = new double[] {
            0.00, // R
            0.94, // SR
            0.06, // SSR
    };

    private Rarity getDrawRarity(DrawType drawType) {
        Random random = new Random();
        int randomInt = random.nextInt(100);
        int lower = 0;
        int upper = 0;
        Rarity rarity = Rarity.R;
        double[] pool = normalSingleDrawRarities; // Default pool is normal single draw

        if (drawType == DrawType.normalLast) {
            pool = normalLastDrawRarities;
        } else if (drawType == DrawType.fesSingle) {
            pool = fesSingleDrawRarities;
        } else if (drawType == DrawType.fesLast) {
            pool = fesLastDrawRarities;
        }

        for (int i = 0; i < pool.length; i++) {
            lower = upper;
            int areaLength = (int)Math.round(pool[i] * 100);
            upper += areaLength;

            if (lower <= randomInt && randomInt < upper) {
                rarity = Rarity.values()[i];
                break;
            }
        }

        return rarity;
    }

    private List<Rarity> getPremiumDrawResults(boolean isFes) {
        List<Rarity> resultsList = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            if (isFes) {
                resultsList.add(getDrawRarity(DrawType.fesSingle));
            } else {
                resultsList.add(getDrawRarity(DrawType.normalSingle));
            }
        }

        if (isFes) {
            resultsList.add(getDrawRarity(DrawType.fesLast));
        } else {
            resultsList.add(getDrawRarity(DrawType.normalLast));
        }

        return resultsList;
    }

    private int getImageIdByRarity(List<ConfigSection> rList, List<ConfigSection> srList, List<ConfigSection> ssrList, Rarity rarity) {
        int id = 2020017000; //Set default id to shark
        int index;
        Random random = new Random();

        if (rarity == Rarity.R) {
            index = random.nextInt(rList.size());
            id = rList.get(index).getInt("Id");
        } else if (rarity == Rarity.SR) {
            index = random.nextInt(srList.size());
            id = srList.get(index).getInt("Id");
        } else if (rarity == Rarity.SSR) {
            index = random.nextInt(ssrList.size());
            id = ssrList.get(index).getInt("Id");
        }

        return id;
    }

    private static BufferedImage compressImage(BufferedImage source) {
        try {
            return Thumbnails.of(source).scale(1).outputQuality(0.5f).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
            return source;
        }
    }

    public BufferedImage doPremiumDraw(boolean isFes, List<ConfigSection> rList, List<ConfigSection> srList, List<ConfigSection> ssrList) {
        List<Rarity> resultsList = getPremiumDrawResults(isFes);
        int[][] imageArray = new int[10][];
        int newHeight = 0;
        int newWidth = 0;
        BufferedImage[] images = new BufferedImage[10];

        for (int i = 0; i < resultsList.size(); i++) {
            int id = getImageIdByRarity(rList, srList, ssrList, resultsList.get(i));
            BufferedImage image = null;
            String imageUrl = Utils.firstDigit(id) == 1 ? WEAPON_URL_PREFIX + id + EXTENSION_JPG : SUMMON_URL_SUFFIX + id + EXTENSION_JPG;

            try {
                URL url = new URL(imageUrl);
                image = ImageIO.read(url);
                image = compressImage(image);
                images[i] = image;
            } catch (Exception e) {
                e.printStackTrace();
            }

            int width = image.getWidth();
            int height = image.getHeight();
            imageArray[i] = new int[width * height];
            imageArray[i] = image.getRGB(0, 0, width, height, imageArray[i], 0, width);

            newWidth = newWidth > width ? newWidth : width;
            newHeight += height;
        }

        try {
            BufferedImage mergedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            int height_i = 0;
            int width_i = 0;

            for (int i = 0; i < 10; i++) {
                mergedImage.setRGB(0, height_i, newWidth, images[i].getHeight(), imageArray[i], 0, newWidth);
                height_i += images[i].getHeight();
            }

            return mergedImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
