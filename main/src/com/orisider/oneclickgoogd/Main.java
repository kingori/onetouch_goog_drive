package com.orisider.oneclickgoogd;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.orisider.oneclickgoogd.model.AccessToken;
import roboguice.inject.InjectView;

public class Main extends RoboSherlockActivity implements View.OnClickListener {


    @InjectView(R.id.saved_acnt_info)
    View savedAcntInfo;

    @InjectView(R.id.saved_acnt_name)
    TextView savedAcntName;

    @InjectView(R.id.saved_acnt_token)
    TextView savedAcntToken;

    @InjectView(R.id.add_acnt_btn)
    View addAcntBtn;

    AccessToken token;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (token == null) {
            token = SessionStore.getAccountAuthToken();
        }

        initView();
        showPanel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == Constant.REQ_CODE_ACNT && resultCode == Activity.RESULT_OK) {
            token = (AccessToken) data.getSerializableExtra(Constant.BUNDLE_KEY_ACCESS_TOKEN);
            showPanel();
        }
    }

    private void initView() {
        addAcntBtn.setOnClickListener(this);
    }

    private void showPanel() {
        if (token == null) {
            addAcntBtn.setVisibility(View.VISIBLE);
            savedAcntInfo.setVisibility(View.GONE);
        } else {
            addAcntBtn.setVisibility(View.GONE);
            savedAcntInfo.setVisibility(View.VISIBLE);
            savedAcntName.setText(token.accountName);
            savedAcntToken.setText(token.accessToken);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == addAcntBtn) {
            startActivityForResult(new Intent(this, GetAccountActivity.class), Constant.REQ_CODE_ACNT);
        }
    }
}

