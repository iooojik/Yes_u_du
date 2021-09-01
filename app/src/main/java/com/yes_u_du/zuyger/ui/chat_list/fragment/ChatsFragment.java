package com.yes_u_du.zuyger.ui.chat_list.fragment;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.constants.FirebaseStatic;
import com.yes_u_du.zuyger.models.ChatMessage;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.dialogs.FilterDialog;

import java.util.ArrayList;


public class ChatsFragment extends ChatListFragment {

    private Callback activityCallBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        MobileAds.initialize(getActivity(), initializationStatus -> {
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void setLayoutManagerForRecView() {
        chatRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activityCallBack = (ChatsFragment.Callback) context;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        activityCallBack = null;
    }

    protected boolean clickToolbarItems(MenuItem item) {
        if (item.getItemId() == R.id.find_item) {
            if (UserModel.getCurrentUser().getAdmin_block().equals("unblock")) {
                FilterDialog dialog = new FilterDialog();
                dialog.setTargetFragment(this, CODE_TO_FILTER_DIALOG);
                dialog.show(getFragmentManager(), null);
            } else {
                Toast.makeText(getActivity(), R.string.blocked_by_admin, Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    protected void setChats() {
        FirebaseDatabase.getInstance().getReference("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> usersID = new ArrayList<>();
                usersID.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String genKey = snapshot1.getKey();
                    for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                        if (!snapshot2.getKey().equals("firstBlock") && !snapshot2.getKey().equals("secondBlock")) {
                            for (DataSnapshot snapshot3 : snapshot2.getChildren()) {
                                ChatMessage msg = snapshot3.getValue(ChatMessage.class);
                                if (msg.getFromUserUUID().equals(FirebaseAuth.getInstance().getUid()) && !(
                                        msg.getFirstDelete().equals("delete") && genKey.startsWith(FirebaseAuth.getInstance().getUid()) ||
                                                (msg.getSecondDelete().equals("delete") && !genKey.startsWith(FirebaseAuth.getInstance().getUid())))) {
                                    usersID.add(msg.getToUserUUID());
                                }
                                if (msg.getToUserUUID() != null && msg.getToUserUUID().equals(FirebaseAuth.getInstance().getUid()) && !(
                                        msg.getFirstDelete().equals("delete") && genKey.startsWith(FirebaseAuth.getInstance().getUid()) ||
                                                (msg.getSecondDelete().equals("delete") && !genKey.startsWith(FirebaseAuth.getInstance().getUid())))) {
                                    usersID.add(msg.getFromUserUUID());
                                }
                            }
                        }
                    }
                }
                setUsersFromChats(usersID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
    }

    protected void setUsersFromChats(ArrayList<String> usersWithMsgId) {
        ArrayList<UserModel> usersList = new ArrayList<>();
        Log.e(TAG, "setUsersFromChats: " + usersWithMsgId);

        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                    UserModel userModel = snapshot1.getValue(UserModel.class);
                    if (userModel != null) {
                        userModel.setUuid(snapshot1.getKey());
                        for (String id : usersWithMsgId) {
                            if (userModel.getUuid().equals(id)) {
                                if (!usersList.contains(userModel)) usersList.add(userModel);
                            }
                        }
                    }
                }

                Log.e(TAG, "onDataChange: " + usersList);
                if (isAdded() && getActivity() != null) {
                    usersList.add(new UserModel(FirebaseStatic.ADMIN, getActivity()
                            .getResources().getString(R.string.admin)));
                    ChatRecViewAdapter adapter =
                            new ChatRecViewAdapter(usersList, getActivity(),
                                    requireActivity().getSupportFragmentManager(),
                                    ChatRecViewAdapter.ChatItemHolder.VIEW_TYPE, true);
                    chatRecView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CODE_TO_FILTER_DIALOG) {
            activityCallBack.onUsersFilter(data);
        }
    }

    @Override
    public void update() {
        setChats();
    }

    public interface Callback {
        void onUsersFilter(Intent data);
    }
}
