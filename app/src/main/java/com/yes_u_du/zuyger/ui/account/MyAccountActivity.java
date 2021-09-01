package com.yes_u_du.zuyger.ui.account;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.yes_u_du.zuyger.BaseActivity;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.ui.ChatAndAccountPager;
import com.yes_u_du.zuyger.ui.chat_list.fragment.ChatsFragment;
import com.yes_u_du.zuyger.ui.photo_utils.viewpager.PhotoViewPagerItemFragment;


public class MyAccountActivity extends BaseActivity implements ChatsFragment.Callback {

    public static final int CODE_NO_FILTER = 0;
    public static final int CODE_FILTER = 1;


    @Override
    public Fragment getFragment() {
        return new ChatAndAccountPager();
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

    @Override
    public void onUsersFilter(Intent data) {
        ChatAndAccountPager fragment = (ChatAndAccountPager) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragment != null)
            fragment.checkUsersFragment(CODE_FILTER, data);
    }

    @Override
    public void onBackPressed() {
        if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof PhotoViewPagerItemFragment)) {
            ChatAndAccountPager fragment = (ChatAndAccountPager) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment != null && fragment.getViewPager().getCurrentItem() == 1 && fragment.TYPE_OF_LIST.equals("F"))
                fragment.checkUsersFragment(CODE_NO_FILTER, null);
            else super.onBackPressed();
        } else super.onBackPressed();
    }
}
