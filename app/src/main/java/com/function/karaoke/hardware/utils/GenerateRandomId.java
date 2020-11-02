package com.function.karaoke.hardware.utils;

public class GenerateRandomId {

    private static final int RECORDING_ID_LENGTH = 15;
    private static String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789"
            + "abcdefghijklmnopqrstuvxyz";

    public static String generateRandomId(){
        StringBuilder sb = new StringBuilder(RECORDING_ID_LENGTH);

        for (int i = 0; i < RECORDING_ID_LENGTH; i++) {
            int index
                    = (int) (alphaNumericString.length()
                    * Math.random());

            sb.append(alphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
