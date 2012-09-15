package com.orisider.oneclickgoogd;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
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
import com.orisider.oneclickgoogd.model.AccessToken;
import roboguice.util.RoboAsyncTask;

import java.io.IOException;

public class GoogDriveOpen extends RoboSherlockActivity {

    /**
     * Drive file ID.
     */
    private String mFileId;

    /**
     * Drive file ID key.
     */
    String EXTRA_FILE_ID = "resourceId";

    Handler handler;
    AccessToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (handler == null) {
            handler = new Handler();
        }

        // Get the action that triggered the intent filter for this Activity
        final Intent intent = getIntent();
        final String action = intent.getAction();

        token = SessionStore.getAccountAuthToken();

        // Make sure the Action is DRIVE_OPEN.
        if (Constant.ACTION_DRIVE_OPEN.equals(action)) {
            // Get the Drive file ID.
            mFileId = intent.getStringExtra(EXTRA_FILE_ID);
            processFile();
        } else {
            Util.showToast(R.string.warn_no_file_found);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.REQ_CODE_ACNT && resultCode == Activity.RESULT_OK) {
            token = (AccessToken) data.getSerializableExtra(Constant.BUNDLE_KEY_ACCESS_TOKEN);
            if (token != null) {
                processFile();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void processFile() {
        if (token == null) {
            startActivityForResult(new Intent(this, GetAccountActivity.class), Constant.REQ_CODE_ACNT);
            return;
        }

        final HttpTransport transport = AndroidHttp.newCompatibleTransport();
        final JsonFactory jsonFactory = new AndroidJsonFactory();
        GoogleCredential credential =
                new GoogleCredential.Builder()
                        .setClientSecrets(Constant.CLIENT_API_ID, Constant.CLIENT_API_SECRET).build();

        credential.setAccessToken(token.accessToken);
        final Drive drive = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName("OneTouchGDrive")
                .setJsonHttpRequestInitializer(new GoogleKeyInitializer(Constant.SIMPLE_API_KEY))
                .build();

        new RoboAsyncTask<String>(this, handler){

            @Override
            public String call() throws Exception {

                try {

                    PermissionList list = drive.permissions().list(mFileId).execute();
                    for (Object v : list.values()) {
                        Log.d(Constant.LOG_TAG, "perm item:" + v);
                    }

                    Permission anyoneReadPerm = new Permission();
                    //anyoneReadPerm.setValue("anyone");
                    anyoneReadPerm.setType("anyone");
                    anyoneReadPerm.setRole("reader");
                    drive.permissions().insert(mFileId, anyoneReadPerm).execute();

                    File file = drive.files().get(mFileId).execute();
                    Log.d(Constant.LOG_TAG, "title:" + file.getTitle());
                    Log.d(Constant.LOG_TAG, "download url:" + file.getDownloadUrl());
                    Log.d(Constant.LOG_TAG, "kind:" + file.getKind());
                    Log.d(Constant.LOG_TAG, "web content link:" + file.getWebContentLink());

                    return file.getWebContentLink();
                } catch (IOException e) {
                    Log.w(Constant.LOG_TAG, "failed to get file", e);
                    return null;
                }
            }

            @Override
            protected void onSuccess(String shareUrl) throws Exception {
                Util.showToast("url:"+shareUrl);
            }

            @Override
            protected void onThrowable(Throwable t) throws RuntimeException {
                Util.showToast("failed:"+t);
            }
        }.execute();


    }

}
