package com.yes_u_du.zuyger.ui.chat_list.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.dialogs.FilterDialog;

import java.util.ArrayList;
import java.util.Locale;

public class FilteredUsersSearchFragment extends ChatListFragment {

    public static final String KEY_TO_INTENT_DATA = "key_to_data";
    public static final String FILTER_VIEW_TYPE = "filter_view";
    private static final String TYPE_HOLDER = "type_holder";

    public static FilteredUsersSearchFragment newInstance(Intent data) {
        FilteredUsersSearchFragment fragment = new FilteredUsersSearchFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_TO_INTENT_DATA, data);
        //args.putSerializable(TYPE_HOLDER, type);
        fragment.setArguments(args);
        return fragment;
    }

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
        chatRecView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    }

    @Override
    protected void setUsersFromChats(ArrayList<String> usersWithMsgId) {
    }

    @Override
    protected void setChats() {
        getFilterInfoAndFilter(getArguments().getParcelable(KEY_TO_INTENT_DATA));
    }

    private void getFilterInfoAndFilter(Intent data) {
        String nameFilter = data.getStringExtra(FilterDialog.KEY_TO_NAME_FILTER);
        String sexFilter = data.getStringExtra(FilterDialog.KEY_TO_SEX_FILTER);
        Log.d("tut_filter", sexFilter);
        String ageFilter = data.getStringExtra(FilterDialog.KEY_TO_AGE_FILTER);
        String cityFilter = data.getStringExtra(FilterDialog.KEY_TO_CITY_FILTER);
        String onlineFilter = data.getStringExtra(FilterDialog.KEY_TO_ONLINE_FILTER);
        String photoFilter = data.getStringExtra(FilterDialog.KEY_TO_PHOTO_FILTER);
        String countryFilter = data.getStringExtra(FilterDialog.KEY_TO_COUNTRY_FILTER);
        String regionFilter = data.getStringExtra(FilterDialog.KEY_TO_REGION_FILTER);
        filterUsers(nameFilter, sexFilter, ageFilter, cityFilter, onlineFilter, photoFilter, countryFilter, regionFilter);
    }

    private void filterUsers(String nameFilter,
                             String sexFilter,
                             String ageFilter,
                             String cityFilter,
                             String onlineFilter,
                             String photoFilter,
                             String countryFilter,
                             String regionFilter) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addValueEventListener(new ValueEventListener() {
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
                    filterByAdminBlock(userModels, userModel);
                    filterByMe(userModels, userModel);
                    //filterUsersByPhoto(users,user);
                }

                ChatRecViewAdapter adapter = new ChatRecViewAdapter(userModels, getActivity(), getFragmentManager(), ChatRecViewAdapter.ChatItemHolder.VIEW_TYPE, FILTER_VIEW_TYPE);
                chatRecView.setAdapter(adapter);
                ref.removeEventListener(this);
            }

            private void filterUsersByRegion(ArrayList<UserModel> userModels, UserModel userModel) {
                /*
                if (userModel.getRegion() != null && !regionFilter.equals("0")) {
                    if (!userModel.getRegion().equals(regionFilter)) {
                        userModels.remove(userModel);
                    }
                }todo*/
            }

            private void filterByAdminBlock(ArrayList<UserModel> userModels, UserModel userModel) {
                if (userModel.getAdmin_block() != null)
                    if (!userModel.getAdmin_block().equals("unblock")) userModels.remove(userModel);
            }

            private void filterUsersByCountry(ArrayList<UserModel> userModels, UserModel userModel) {
                /*
                if (userModel.getCountry() != null && !countryFilter.equals("0")) {
                    if (!userModel.getCountry().equals(countryFilter)) {
                        userModels.remove(userModel);
                    }
                }todo*/
            }

            private void filterUsersByPhoto(ArrayList<UserModel> userModels, UserModel userModel) {
                if (userModel.getPhoto_url() != null && !photoFilter.isEmpty()) {
                    if (userModel.getPhoto_url().equals("default")) {
                        userModels.remove(userModel);
                    }
                }
            }

            private void filterUsersByOnline(ArrayList<UserModel> userModels, UserModel userModel) {
                if (userModel.getStatus() != null && !onlineFilter.isEmpty()) {
                    if (!(userModel.getStatus().equals(onlineFilter))) {
                        userModels.remove(userModel);
                    }
                }
            }

            private void filterUsersByCity(ArrayList<UserModel> userModels, UserModel userModel) {
                /*todo
                if (userModel.getCity() != null && !(cityFilter.isEmpty())) {
                    if (!userModel.getCity().toLowerCase(Locale.ROOT).equals(cityFilter.toLowerCase(Locale.ROOT))) {
                        userModels.remove(userModel);
                    }
                }*/
            }

            private void filterUsersByAge(ArrayList<UserModel> userModels, UserModel userModel) {
                if (ageFilter != null && !ageFilter.isEmpty() && userModel.getAge() != null) {
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
                if (userModel.getSex() != null && !sexFilter.isEmpty()) {
                    if (!(userModel.getSex().equals(sexFilter))) {
                        userModels.remove(userModel);
                    }
                }
            }

            private void filterUsersByName(ArrayList<UserModel> userModels, UserModel userModel) {
                if (userModel.getName() != null && !nameFilter.isEmpty()) {
                    if (!(userModel.getName().toLowerCase(Locale.ROOT).equals(nameFilter.toLowerCase(Locale.ROOT)))) {
                        userModels.remove(userModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
    }

    private void filterByMe(ArrayList<UserModel> userModels, UserModel userModel) {
        if (userModel.getUuid() != null)
            if (userModel.getUuid().equals(UserModel.getCurrentUser().getUuid())) userModels.remove(userModel);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CODE_TO_FILTER_DIALOG && data != null) {
                getFilterInfoAndFilter(data);
            }
        }
    }

    @Override
    public void update() {

    }
}