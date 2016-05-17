package com.whitefly.plutocrat.models;

/**
 * Created by Satjapot on 5/17/16 AD.
 */
public class BuyoutModel {
    public static final int DEBUG_NO_PROFILE_PICTURE = 0;

    public enum BuyoutStatus {
        Initiate, Threat
    }

    public enum GameStatus {
        Playing, Win, Lose
    }

    // Attributes
    public String name;
    public int shares;
    public int hours;
    public BuyoutStatus status;
    public GameStatus gameStatus;
    public int picProfile = DEBUG_NO_PROFILE_PICTURE;

    // Methods
    public String getPicName() {
        String result;
        char firstCh, secondCh;

        if(name == null) {
            result = "";
        } else {
            String[] names = name.split("\\s+");
            if(names.length > 1) {
                firstCh = names[0].charAt(0);
                secondCh = names[1].charAt(0);
                result = String.format("%s%s", firstCh, secondCh).toUpperCase();
            } else {
                firstCh = name.charAt(0);
                secondCh = name.length() > 1 ? name.charAt(1) : Character.MIN_VALUE;
                result = String.format("%s%s", firstCh, secondCh).toUpperCase();
            }
        }

        return result;
    }

    public String getPeriod() {
        // For debug
        return String.format("%d hours", hours);
    }
}
