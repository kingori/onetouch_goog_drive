package com.orisider.oneclickgoogd;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import com.orisider.oneclickgoogd.model.AccessToken;

public class Util {

    public static void showToast(int textResId) {
        showToast(GoogDrvShareApp.ctx.getResources().getString(textResId));
    }

    public static void showToast(String text) {
        Toast.makeText(GoogDrvShareApp.ctx, text, Toast.LENGTH_SHORT).show();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() < 1;
    }


    public static void getAccountToken(AccountManager acntMgr, final Account acnt, Activity act, Handler handler,
                                       final AccessTokenCallback callback) {
        acntMgr.getAuthToken(acnt, Constant.AUTH_TOKEN_TYPE, null, act, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();
                    String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                    if (!Util.isEmpty(authToken)) {
                        AccessToken token = new AccessToken(acnt.name, authToken);
                        SessionStore.saveAccountAuthToken(token);
                        callback.onTokenGetSuccess(token);
                        return;
                    }

                    callback.onTokenGetFailed(new Exception("token is empty"));
                } catch (Throwable e) {
                    callback.onTokenGetFailed(e);
                }
            }
        }, handler);
    }

    public static interface AccessTokenCallback {
        public void onTokenGetSuccess(AccessToken token);

        public void onTokenGetFailed(Throwable e);
    }
}
