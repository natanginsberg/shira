package com.function.karaoke.interaction.activities.Model;

public class Keys {
    private String accessKeyId;
    private String privateKey;

    public Keys() {
    }

    public Keys(String id, String secretKey) {
        this.accessKeyId = id;
        this.privateKey = secretKey;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
