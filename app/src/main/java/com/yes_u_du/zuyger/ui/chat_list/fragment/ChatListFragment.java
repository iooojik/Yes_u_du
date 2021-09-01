package com.yes_u_du.zuyger.ui.chat_list.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.ui.chat_list.Updatable;
import com.yes_u_du.zuyger.ui.dialogs.FilterDialog;

import java.util.ArrayList;

public abstract class ChatListFragment extends Fragment implements Updatable {

    public static final int KEY_DELETE_DIALOG = -1;
    protected static final int CODE_TO_FILTER_DIALOG = 0;
    protected RecyclerView chatRecView;
    protected Toolbar toolbar;
    protected AdView adView;

    protected void getToolbarMenu() {
        toolbar.inflateMenu(R.menu.filter_users_menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_users_list, container, false);
        chatRecView = v.findViewById(R.id.chat_recycler_view);
        toolbar = v.findViewById(R.id.toolbarFr);
        adView = v.findViewById(R.id.adViewListUser);
        getToolbarMenu();
        toolbar.setOnMenuItemClickListener(item -> clickToolbarItems(item));
        setLayoutManagerForRecView();
        setChats();
        return v;
    }


    public abstract void setLayoutManagerForRecView();

    protected boolean clickToolbarItems(MenuItem item) {
        if (item.getItemId() == R.id.find_item) {
            FilterDialog dialog = new FilterDialog();
            dialog.setTargetFragment(this, CODE_TO_FILTER_DIALOG);
            dialog.show(getFragmentManager(), null);
        }
        return true;
    }

    protected abstract void setUsersFromChats(ArrayList<String> usersWithMsgId);

    protected abstract void setChats();
}
