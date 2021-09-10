package com.yes_u_du.zuyger.ui.chat_list.admin.block;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.chat_list.fragment.ChatListFragment;
import com.yes_u_du.zuyger.ui.chat_list.fragment.ChatRecViewAdapter;

import java.util.ArrayList;

public class AdminPermBlockListFragment extends ChatListFragment {

    public static final int BLOCK_CODE = -10;
    private DatabaseReference mReference;

    @Override
    public void setLayoutManagerForRecView() {
        chatRecView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    }


    //chats
    //setUsersFromChats(usersID);
    @Override
    protected void setChats() {
        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> usersID = new ArrayList<>();
                usersID.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    UserModel userModel = snapshot1.getValue(UserModel.class);
                    userModel.setUuid(snapshot1.getKey());
                    Log.d("NULL?", String.valueOf(userModel.getPerm_block() == null));
                    if (userModel.getPerm_block().equals("block")) usersID.add(userModel.getUuid());
                }
                setUsersFromChats(usersID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //ну я и ебач
    protected void setUsersFromChats(ArrayList<String> arrayList) {
        ArrayList<UserModel> usersList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    UserModel userModel = snapshot1.getValue(UserModel.class);
                    userModel.setUuid(snapshot1.getKey());
                    for (String id : arrayList) {
                        if (userModel.getUuid().equals(id)) {
                            if (!usersList.contains(userModel)) usersList.add(userModel);
                        }
                    }
                }
                ChatRecViewAdapter adapter = new ChatRecViewAdapter(usersList, getActivity(), getFragmentManager(), ChatRecViewAdapter.AdminBanListItemHolder.VIEW_TYPE, BLOCK_CODE);
                chatRecView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    @Override
    public void update() {

    }
}
