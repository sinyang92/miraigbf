package com.sinyang.miraigbf;

public class Utils {
    public static int firstDigit(int n) {
        while (n > 9) {
            n /= 10;
        }
        return n;
    }
}
