package com.yes_u_du.zuyger.ui.account;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.yes_u_du.zuyger.BaseActivity;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.account.fragment.AdminAccountFragment;
import com.yes_u_du.zuyger.ui.account.fragment.UserAccountFragment;

public class UserAccountActivity extends BaseActivity {
    public static Intent newIntent(Context context, String toUserUUID) {
        Intent intent = new Intent(context, UserAccountActivity.class);
        intent.putExtra(AdminAccountFragment.KEY_TO_RECEIVER_UUID, toUserUUID);
        return intent;
    }

    @Override
    public Fragment getFragment() {
        if (UserModel.getCurrentUser().isAdmin()) {
            return AdminAccountFragment.Companion.newInstance(getIntent().getStringExtra(AdminAccountFragment.KEY_TO_RECEIVER_UUID));
        }
        return UserAccountFragment.Companion.newInstance(getIntent().getStringExtra(AdminAccountFragment.KEY_TO_RECEIVER_UUID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(getResources().getString(R.string.label_offline));
    }

    @Override
    protected void onResume() {
        super.onResume();
        status(getResources().getString(R.string.label_online));
    }
}
