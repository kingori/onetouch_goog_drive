package com.orisider.oneclickgoogd.model;

import java.io.Serializable;

public class AccessToken implements Serializable {
    public final String accessToken;
    public final String accountName;

    public AccessToken(String accountName, String accessToken) {
        this.accountName= accountName;
        this.accessToken = accessToken;
    }
}
