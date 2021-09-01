package com.yes_u_du.zuyger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.ui.account.MyAccountActivity;
import com.yes_u_du.zuyger.ui.account.fragment.MyAccountFragment;
import com.yes_u_du.zuyger.ui.chat_list.Updatable;
import com.yes_u_du.zuyger.ui.chat_list.fragment.ChatListFragment;
import com.yes_u_du.zuyger.ui.chat_list.fragment.ChatsFragment;
import com.yes_u_du.zuyger.ui.chat_list.fragment.FilteredUsersSearchFragment;

import java.util.Objects;

public class ChatAndAccountPager extends Fragment {
    public String TYPE_OF_LIST;
    private ViewPager viewPager;
    private int activity_code;
    private Intent activity_data;
    private ChatListFragment currentChatFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_pager_fragment, container, false);
        activity_code = MyAccountActivity.CODE_NO_FILTER;
        TYPE_OF_LIST = "NO_F";
        TabLayout tabLayout = v.findViewById(R.id.tab_layout);
        viewPager = v.findViewById(R.id.view_pager);
        FragmentStatePagerAdapter pagerAdapter = setAdapter();
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        return v;
    }

    private FragmentStatePagerAdapter setAdapter() {
        return new FragmentStatePagerAdapter(requireFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return new MyAccountFragment();
                } else {
                    if (activity_code == MyAccountActivity.CODE_NO_FILTER) {
                        TYPE_OF_LIST = "N0_F";
                        currentChatFragment = new ChatsFragment();
                    } else {
                        TYPE_OF_LIST = "F";
                        currentChatFragment = FilteredUsersSearchFragment.newInstance(activity_data);
                    }
                    return currentChatFragment;
                }
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                if (object instanceof MyAccountFragment) {
                    return POSITION_UNCHANGED;
                }
                return POSITION_NONE;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) {
                    return getResources().getString(R.string.myAccount);
                } else {
                    return getResources().getString(R.string.chat_list);
                }
            }
        };
    }

    public void checkUsersFragment(int code, Intent data) {
        activity_code = code;
        activity_data = data;
        Objects.requireNonNull(viewPager.getAdapter()).notifyDataSetChanged();
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ChatListFragment.KEY_DELETE_DIALOG) {
            try {
                Updatable upd = currentChatFragment;
                upd.update();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
