package com.function.karaoke.hardware.utils.static_classes;

public class GenerateRandomId {

    private static final int RECORDING_ID_LENGTH = 15;
    private static final int PASSWORD_LENGTH = 5;
    private static String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789"
            + "abcdefghijklmnopqrstuvxyz";
    private static String passwordOptions = "1234567890";

    public static String generateRandomId() {
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

    public static String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index
                    = (int) (passwordOptions.length()
                    * Math.random());
            sb.append(passwordOptions
                    .charAt(index));
        }
        return sb.toString();
    }
}
