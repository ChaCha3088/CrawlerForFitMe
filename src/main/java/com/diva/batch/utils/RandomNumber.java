package com.diva.batch.utils;

public class RandomNumber {
    public static int generateRandomNumberBetween(int min, int max) {
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }
}
