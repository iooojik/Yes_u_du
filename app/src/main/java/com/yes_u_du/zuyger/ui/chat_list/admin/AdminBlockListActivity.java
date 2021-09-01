package com.yes_u_du.zuyger.ui.chat_list.admin;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.yes_u_du.zuyger.BaseActivity;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.ui.chat_list.admin.block.AdminPermBlockListFragment;
import com.yes_u_du.zuyger.ui.chat_list.admin.block.AdminTimeBlockListFragment;

public class AdminBlockListActivity extends BaseActivity {


    public static final String TIMED_CODE_KEY = "timed_or_perm_code";

    public static Intent newInstance(Context context, int code) {
        Intent intent = new Intent(context, AdminBlockListActivity.class);
        intent.putExtra(TIMED_CODE_KEY, code);
        return intent;
    }

    @Override
    public Fragment getFragment() {
        switch (getIntent().getIntExtra(TIMED_CODE_KEY, -1)) {
            case AdminTimeBlockListFragment.BLOCK_CODE: {
                return new AdminTimeBlockListFragment();
            }

            case AdminPermBlockListFragment.BLOCK_CODE: {
                return new AdminPermBlockListFragment();
            }
        }
        return null;
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
