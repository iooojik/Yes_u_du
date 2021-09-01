package com.yes_u_du.zuyger.ui.chat_list.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yes_u_du.zuyger.models.ChatMessage;
import com.yes_u_du.zuyger.models.UserModel;

import java.util.ArrayList;

public class BlockListFragment extends ChatListFragment {

    public static final int KEY_TO_UNBLOCK = 0;
    public static final int TYPE_DIALOG = 0;

    private ValueEventListener listener;
    private DatabaseReference reference;

    private void setChatsFromMsg() {
        listener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> usersID = new ArrayList<>();
                usersID.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String genKey = snapshot1.getKey();
                    for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                        if (snapshot2.getKey().equals("firstBlock") && snapshot2.getValue().equals("block") && genKey.startsWith(UserModel.getCurrentUser().getUuid()) ||
                                snapshot2.getKey().equals("secondBlock") && snapshot2.getValue().equals("block") && !genKey.startsWith(UserModel.getCurrentUser().getUuid())) {
                            for (DataSnapshot snapshot3 : snapshot1.getChildren()) {
                                if (!snapshot3.getKey().equals("firstBlock") && !snapshot3.getKey().equals("secondBlock")) {
                                    for (DataSnapshot snapshot4 : snapshot3.getChildren()) {
                                        ChatMessage msg = snapshot4.getValue(ChatMessage.class);
                                        if (msg.getFromUserUUID().equals(FirebaseAuth.getInstance().getUid())) {
                                            usersID.add(msg.getToUserUUID());
                                        }
                                        if (msg.getToUserUUID() != null && msg.getToUserUUID().equals(FirebaseAuth.getInstance().getUid())) {
                                            usersID.add(msg.getFromUserUUID());
                                        }
                                    }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) reference.removeEventListener(listener);
    }

    protected void setUsersFromChats(ArrayList<String> usersWithMsgId) {
        ArrayList<UserModel> usersList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    UserModel userModel = snapshot1.getValue(UserModel.class);
                    userModel.setUuid(snapshot1.getKey());
                    for (String id : usersWithMsgId) {
                        if (userModel.getUuid().equals(id)) {
                            if (!usersList.contains(userModel)) usersList.add(userModel);
                        }
                    }
                }
                ChatRecViewAdapter adapter = new ChatRecViewAdapter(usersList, getActivity(), getFragmentManager(), ChatRecViewAdapter.BlockListItemHolder.VIEW_TYPE, TYPE_DIALOG);
                chatRecView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == KEY_TO_UNBLOCK) {
                reference = FirebaseDatabase.getInstance().getReference("chats");
                setChatsFromMsg();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference = FirebaseDatabase.getInstance().getReference("chats");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setLayoutManagerForRecView() {
        chatRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    protected void setChats() {
        setChatsFromMsg();
    }

    @Override
    protected void getToolbarMenu() {
    }

    @Override
    protected boolean clickToolbarItems(MenuItem item) {
        return false;
    }

    @Override
    public void update() {
    }
}
