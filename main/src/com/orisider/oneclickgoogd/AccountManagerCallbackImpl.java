package com.orisider.oneclickgoogd;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.util.Log;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;

import java.io.IOException;

public class AccountManagerCallbackImpl implements AccountManagerCallback<Bundle> {

    private final String mFileId;

    AccountManagerCallbackImpl(String mFileId) {
        this.mFileId = mFileId;
    }
    @Override
    public void run(AccountManagerFuture<Bundle> future) {
        Bundle bundle;
        try {
            bundle = future.getResult();
            String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

            Log.d(Constant.LOG_TAG, "auth token:" + authToken);

            final HttpTransport transport = AndroidHttp.newCompatibleTransport();
            final JsonFactory jsonFactory = new AndroidJsonFactory();
            GoogleCredential credential =
                    new GoogleCredential.Builder()
                            .setClientSecrets(Constant.CLIENT_API_ID, Constant.CLIENT_API_SECRET).build();


            credential.setAccessToken(authToken);
            final Drive drive = new Drive.Builder(transport, jsonFactory, credential)
                    .setApplicationName("OneTouchGDrive")
                    .setJsonHttpRequestInitializer(new GoogleKeyInitializer(Constant.SIMPLE_API_KEY))

                    .build();

            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            File file = null;
                            try {

                                PermissionList list = drive.permissions().list(mFileId).execute();
                                for(Object v: list.values() ) {
                                    Log.d(Constant.LOG_TAG, "perm item:"+v);
                                }

                                Permission anyoneReadPerm = new Permission();
                                //anyoneReadPerm.setValue("anyone");
                                anyoneReadPerm.setType("anyone");
                                anyoneReadPerm.setRole("reader");
                                drive.permissions().insert(mFileId,anyoneReadPerm ).execute();

                                file = drive.files().get(mFileId).execute();
                                Log.d(Constant.LOG_TAG, "title:" + file.getTitle());
                                Log.d(Constant.LOG_TAG, "download url:" + file.getDownloadUrl());
                                Log.d(Constant.LOG_TAG, "kind:" + file.getKind());
                                Log.d(Constant.LOG_TAG, "web content link:" + file.getWebContentLink());
                            } catch (IOException e) {
                                Log.w(Constant.LOG_TAG, "failed to get file", e);
                            }
                        }
                    }
            ).start();
        } catch (Throwable e) {
            Log.w(Constant.LOG_TAG, "error", e);
        }
    }
}
