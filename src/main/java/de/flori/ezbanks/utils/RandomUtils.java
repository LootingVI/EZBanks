package de.flori.ezbanks.utils;


import java.util.Random;

public class RandomUtils {

    private static final Random RANDOM = new Random();

    public static int getRandomNumber() {
        return RANDOM.nextInt();
    }

    public static int getRandomNumber(int max) {
        return RANDOM.nextInt(max);
    }

    public static int getRandomNumber(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }
}