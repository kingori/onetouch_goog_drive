package com.orisider.oneclickgoogd;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

public class GoogDriveOpen extends RoboSherlockActivity {

	/**
	 * DRIVE_OPEN Intent action.
	 */
	private static final String ACTION_DRIVE_OPEN = "com.google.android.apps.drive.DRIVE_OPEN";
	/**
	 * Drive file ID key.
	 */
	private static final String EXTRA_FILE_ID = "resourceId";

	/**
	 * Drive file ID.
	 */
	private String mFileId;
	private static final String LOG_TAG = "goog_drive_open";


	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();

		// Get the action that triggered the intent filter for this Activity
		final Intent intent = getIntent();
		final String action = intent.getAction();

		// Make sure the Action is DRIVE_OPEN.
		if (ACTION_DRIVE_OPEN.equals(action)) {
			// Get the Drive file ID.
			mFileId = intent.getStringExtra(EXTRA_FILE_ID);
			try {
				getUserAccountAndProcessFile();
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		} else {
			// Unknown action.
			finish();
		}
	}

	/**
	 * Prompt the user to choose the account to use and process the file using the
	 * Drive file ID stored in mFileId.
	 */
	private void getUserAccountAndProcessFile() throws IOException {

		final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";

		new Thread(new Runnable() {
			@Override
			public void run() {
				final NetHttpTransport transport = new NetHttpTransport();

				AccountManager acntMgr = AccountManager.get(getApplicationContext());
				Account acnt = acntMgr.getAccounts()[4];
				AccountManagerFuture<Bundle> oneTouchGDrive = acntMgr.getAuthToken(acnt, AUTH_TOKEN_TYPE, null, GoogDriveOpen.this, new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> result) {
//
						Bundle bundle;
						try {
							bundle = result.getResult();
							String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

							Log.d(LOG_TAG, "auth token:" + authToken);

							Credential c = new GoogleCredential.Builder()
									.setServiceAccountScopes("https://www.googleapis.com/auth/drive")
									.setClientSecrets("445798695489.apps.googleusercontent.com", "3SYikZExgd0T2fg2QJsHTK_L").build();
							c.setAccessToken(authToken);

							final Drive drive = new Drive.Builder(transport, new AndroidJsonFactory(), c).setApplicationName("OneTouchGDrive").build();

							new Thread(
									new Runnable() {
										@Override
										public void run() {
											File file = null;
											try {
												file = drive.files().get(mFileId).execute();
												Log.d(LOG_TAG, "title:" + file.getTitle());
												Log.d(LOG_TAG, "download url:" + file.getDownloadUrl());
												Log.d(LOG_TAG, "kind:" + file.getKind());
											} catch (IOException e) {
												e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
											}

										}
									}


							).start();


						} catch (Throwable e) {
							e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
						}


					}
				}, handler);


			}
		}).start();


//		AccountManager acntMgr = AccountManager.get(getApplicationContext());
//
//		Account acnt =  acntMgr.getAccounts()[0];
//		Log.d(LOG_TAG, "acnt name:" +acnt.name );
//		acntMgr.getAuthToken(acnt, )


//		// Implement the method.
//		throw new UnsupportedOperationException(
//				"The getUserAccountAndProcessFile method has not been implemented");
	}
}
