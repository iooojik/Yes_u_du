package com.yes_u_du.zuyger.ui.chat_list.admin;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.ui.chat_list.fragment.ChatRecViewAdapter;
import com.yes_u_du.zuyger.ui.chat_list.fragment.ChatListFragment;
import com.yes_u_du.zuyger.models.ChatMessage;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.dialogs.FilterDialog;

import java.util.ArrayList;


//панель администратора
public class AdminFragment extends ChatListFragment {

    private DatabaseReference reference;
    private ValueEventListener userListener;
    private String admin_key_string;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference = FirebaseDatabase.getInstance().getReference("users");
        admin_key_string = getResources().getString(R.string.admin_key);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            toolbar.setTitle(getActivity().getString(R.string.panel_admin));
            toolbar.setBackground(null);
            toolbar.setBackgroundResource(R.color.colorToolbar);
        }
    }

    @Override
    public void setLayoutManagerForRecView() {
        chatRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    protected boolean clickToolbarItems(MenuItem item) {
        if (item.getItemId() == R.id.find_item) {

        }
        return super.clickToolbarItems(item);
    }

    protected void setChats() {
        userListener = FirebaseDatabase.getInstance().getReference("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> usersID = new ArrayList<>();
                usersID.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    // String genKey=snapshot1.getKey();
                    // usersID.add(msg.getToUserUUID());
                    for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                        if (!snapshot2.getKey().equals("firstBlock") && !snapshot2.getKey().equals("secondBlock")) {
                            for (DataSnapshot snapshot3 : snapshot2.getChildren()) {
                                ChatMessage msg = snapshot3.getValue(ChatMessage.class);
                                if (msg.getFromUserUUID().equals(admin_key_string))
                                    usersID.add(msg.getToUserUUID());
                                if (msg.getToUserUUID().equals(admin_key_string))
                                    usersID.add(msg.getFromUserUUID());
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
                ChatRecViewAdapter adapter = new ChatRecViewAdapter(usersList, getActivity(), getFragmentManager(), ChatRecViewAdapter.AdminChatItemHolder.VIEW_TYPE, false);
                chatRecView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        reference.removeEventListener(userListener);
    }

    private void getFilterInfoAndFilter(Intent data) {
        String nameFilter = data.getStringExtra(FilterDialog.KEY_TO_NAME_FILTER);
        String sexFilter = data.getStringExtra(FilterDialog.KEY_TO_SEX_FILTER);
        String ageFilter = data.getStringExtra(FilterDialog.KEY_TO_AGE_FILTER);
        String cityFilter = data.getStringExtra(FilterDialog.KEY_TO_CITY_FILTER);
        String onlineFilter = data.getStringExtra(FilterDialog.KEY_TO_ONLINE_FILTER);
        String photoFilter = data.getStringExtra(FilterDialog.KEY_TO_PHOTO_FILTER);
        String countryFilter = data.getStringExtra(FilterDialog.KEY_TO_COUNTRY_FILTER);
        String regionFilter = data.getStringExtra(FilterDialog.KEY_TO_REGION_FILTER);
        filterUsers(nameFilter, sexFilter, ageFilter, cityFilter, onlineFilter, photoFilter, countryFilter, regionFilter);
    }

    private void filterUsers(String nameFilter, String sexFilter, String ageFilter, String cityFilter, String onlineFilter, String photoFilter, String countryFilter, String regionFilter) {
        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<UserModel> userModels = new ArrayList<>();
                userModels.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    UserModel userModel = snapshot1.getValue(UserModel.class);
                    userModel.setUuid(snapshot1.getKey());
                    userModels.add(userModel);
                    filterUsersByName(userModels, userModel);
                    filterUserBySex(userModels, userModel);
                    filterUsersByAge(userModels, userModel);
                    filterUsersByCity(userModels, userModel);
                    filterUsersByOnline(userModels, userModel);
                    filterUsersByCountry(userModels, userModel);
                    filterUsersByRegion(userModels, userModel);
                    //filterUsersByPhoto(users,user);
                }
                ChatRecViewAdapter adapter = new ChatRecViewAdapter(userModels, getActivity(), getFragmentManager(), ChatRecViewAdapter.AdminChatItemHolder.VIEW_TYPE, false);
                chatRecView.setAdapter(adapter);
                reference.removeEventListener(this);
            }

            private void filterUsersByRegion(ArrayList<UserModel> userModels, UserModel userModel) {
                if (!regionFilter.equals("0")) {
                    /*todo
                    if (!userModel.getRegion().equals(regionFilter)) {
                        userModels.remove(userModel);
                    }
                     */
                }
            }

            private void filterUsersByCountry(ArrayList<UserModel> userModels, UserModel userModel) {
                if (!countryFilter.equals("0")) {
                    /*todo
                    if (!userModel.getCountry().equals(countryFilter)) {
                        userModels.remove(userModel);
                    }*/
                }
            }

            private void filterUsersByPhoto(ArrayList<UserModel> userModels, UserModel userModel) {
                if (!photoFilter.isEmpty()) {
                    if (userModel.getPhoto_url().equals("default")) {
                        userModels.remove(userModel);
                    }
                }
            }

            private void filterUsersByOnline(ArrayList<UserModel> userModels, UserModel userModel) {
                if (!onlineFilter.isEmpty()) {
                    if (!(userModel.getStatus().equals(onlineFilter))) {
                        userModels.remove(userModel);
                    }
                }
            }

            private void filterUsersByCity(ArrayList<UserModel> userModels, UserModel userModel) {
                if (!(cityFilter.isEmpty())) {
                    /*todo
                    if (!userModel.getCity().equals(cityFilter)) {
                        userModels.remove(userModel);
                    }*/
                }
            }

            private void filterUsersByAge(ArrayList<UserModel> userModels, UserModel userModel) {
                if (ageFilter != null) {
                    if (ageFilter.equals(getResources().getStringArray(R.array.age_for_spinner)[0]) && !(Integer.parseInt(userModel.getAge()) < 18)) {
                        userModels.remove(userModel);
                    } else if (ageFilter.equals(getResources().getStringArray(R.array.age_for_spinner)[1]) && !(Integer.parseInt(userModel.getAge()) >= 18 && Integer.parseInt(userModel.getAge()) < 30)) {
                        userModels.remove(userModel);
                    } else if (ageFilter.equals(getResources().getStringArray(R.array.age_for_spinner)[2]) && !(Integer.parseInt(userModel.getAge()) >= 30 && Integer.parseInt(userModel.getAge()) < 45)) {
                        userModels.remove(userModel);
                    } else if (ageFilter.equals(getResources().getStringArray(R.array.age_for_spinner)[3]) && !(Integer.parseInt(userModel.getAge()) >= 45 && Integer.parseInt(userModel.getAge()) < 60)) {
                        userModels.remove(userModel);
                    } else if (ageFilter.equals(getResources().getStringArray(R.array.age_for_spinner)[4]) && !(Integer.parseInt(userModel.getAge()) >= 60)) {
                        userModels.remove(userModel);
                    }
                }
            }

            private void filterUserBySex(ArrayList<UserModel> userModels, UserModel userModel) {
                if (!sexFilter.isEmpty()) {
                    if (!(userModel.getSex().equals(sexFilter))) {
                        userModels.remove(userModel);
                    }
                }
            }

            private void filterUsersByName(ArrayList<UserModel> userModels, UserModel userModel) {
                if (!nameFilter.isEmpty()) {
                    if (!(userModel.getName().equals(nameFilter))) {
                        userModels.remove(userModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        else {
            if (requestCode == CODE_TO_FILTER_DIALOG) {
                getFilterInfoAndFilter(data);
            }
        }
    }

    @Override
    public void update() {

    }
}