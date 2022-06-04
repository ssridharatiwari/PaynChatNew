package com.startup.paynchat.utils;

import com.startup.paynchat.R;

import java.util.Random;


public class RandomColorGenerator {

    private static final int[] COLORS = new int[] {R.color.blue,
            R.color.purple,
            R.color.green,
            R.color.orange,
            R.color.red,
            R.color.darkBlue,
            R.color.darkPurple,
            R.color.darkGreen,
            R.color.darkOrange,
            R.color.darkRed};

    private static final Random RANDOM = new Random();

    public static Integer getColor() {
        return COLORS[RANDOM.nextInt(COLORS.length)];
    }
}
