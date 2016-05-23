package com.whitefly.plutocrat.models;

/**
 * Created by Satjapot on 5/17/16 AD.
 */
public class TargetModel {
    public enum TargetStatus {
        Normal, UnderThreat
    }

    // Attributes
    public String name;
    public int numBuyouts;
    public int numThreats;
    public int daySurvived;
    public TargetStatus status;
    public int picProfile = 0;
    public boolean isPlutocrat = false;

    // Methods
    public String getPicName() {
        String result;
        if(name == null) {
            result = "";
        } else {
            String[] names = name.split("\\s+");
            if(names.length > 1) {
                char first = names[0].charAt(0);
                char second = names[1].charAt(0);
                result = String.format("%s%s", first, second).toUpperCase();
            } else {
                char first = name.charAt(0);
                char second = name.length() > 1 ? name.charAt(1) : Character.MIN_VALUE;
                result = String.format("%s%s", first, second).toUpperCase();
            }
        }

        return result;
    }
}
