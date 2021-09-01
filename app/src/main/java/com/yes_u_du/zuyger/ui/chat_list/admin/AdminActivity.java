package com.yes_u_du.zuyger.ui.chat_list.admin;

import androidx.fragment.app.Fragment;

import com.yes_u_du.zuyger.BaseActivity;
import com.yes_u_du.zuyger.R;

public class AdminActivity extends BaseActivity {
    @Override
    public Fragment getFragment() {
        return new AdminFragment();
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
