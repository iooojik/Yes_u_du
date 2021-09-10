package com.yes_u_du.zuyger.ui.chat_process.users;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.constants.FirebaseStatic;
import com.yes_u_du.zuyger.models.ChatMessage;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.chat_process.ChatParentFragment;
import com.yes_u_du.zuyger.ui.dialogs.AcceptDialog;
import com.yes_u_du.zuyger.ui.dialogs.ComplainDialog;
import com.yes_u_du.zuyger.ui.dialogs.EditMessageDialog;
import com.yes_u_du.zuyger.ui.dialogs.GoToAdminDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ChatFragment extends ChatParentFragment {
    public static final String KEY_TO_RECEIVER_UUID = "recevierID";
    public static final String KEY_TO_RECEIVER_PHOTO_URL = "recevierPHOTO_URL";
    public static final String KEY_TO_BLOCK_USER = "block_user";
    public static final int GO_TO_ADMIN_REQUEST = 1010;
    public static final int COMPLAIN_REQUEST = 2020;
    public static final int KEY_ACCEPT_BLOCK_USER = 101;
    public static final int KEY_ACCEPT_UNBLOCK_USER = 103;
    public static final int KEY_ACCEPT_DELETE_CHAT = 102;
    private ValueEventListener setChatListener;
    private String seenText;
    private DatabaseReference referenceWriting;
    private ChatMessageAdapter adapter;
    private ValueEventListener seenListener;
    private boolean setChatListenerConnected;
    private ImageView verifiedImage, blockUserImage, notificationAdminImage;
    private ValueEventListener blockChatListener;
    private ValueEventListener deleteMessageListener;
    private String firstKeyToAdmin, secondKeyToAdmin, block_user;
    private ValueEventListener adminMessagesListener;
    private boolean blocked = false;

    public static ChatFragment newInstance(String toUserUUID, String photo_url, String block_user) {
        ChatFragment fragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TO_RECEIVER_UUID, toUserUUID);
        bundle.putString(KEY_TO_RECEIVER_PHOTO_URL, photo_url);
        bundle.putString(KEY_TO_BLOCK_USER, block_user);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CallBack) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setChatListenerConnected = false;
        MobileAds.initialize(getActivity(), initializationStatus -> {
        });
        delete_string = getResources().getString(R.string.delete_users);
        admin_string = getResources().getString(R.string.admin);
        seenText = getResources().getString(R.string.seen_text);
        View v = inflater.inflate(R.layout.chat_fragment, container, false);
        receiverUuid = getArguments().getString(KEY_TO_RECEIVER_UUID);
        receiverPhotoUrl = getArguments().getString(KEY_TO_RECEIVER_PHOTO_URL);
        block_user = getArguments().getString(KEY_TO_BLOCK_USER);
        toolbar = v.findViewById(R.id.toolbarFr);
        verifiedImage = v.findViewById(R.id.verified_image_chat);
        blockUserImage = v.findViewById(R.id.block_image_chat);
        notificationAdminImage = v.findViewById(R.id.notification_admin);
        complainView = v.findViewById(R.id.complain_button);
        complainView.setOnClickListener(v1 -> {
            if (notificationAdminImage.getVisibility() == View.VISIBLE) {
                activity.goToAdmin();
            } else {
                GoToAdminDialog dialog = new GoToAdminDialog();
                dialog.setTargetFragment(ChatFragment.this, GO_TO_ADMIN_REQUEST);
                dialog.show(getFragmentManager(), null);
            }
            //activity.goToAdmin();
        });
        if (receiverUuid.equals(getResources().getString(R.string.admin_key))) {
            complainView.setVisibility(View.GONE);
            toolbar.setEnabled(false);
        } else setToolbarToAcc();
        statusText = v.findViewById(R.id.online_text_in_chat);
        recyclerView = v.findViewById(R.id.list_of_messages);
        fab = v.findViewById(R.id.send_message_button);
        send_image = v.findViewById(R.id.attach_file_button);
        send_image.setOnClickListener(this);
        input = v.findViewById(R.id.input);
        input.addTextChangedListener(this);
        fab.setOnClickListener(this);
        reference = FirebaseDatabase.getInstance().getReference("chats");
        referenceWriting = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("ChatImage");
        username = v.findViewById(R.id.username_text);
        circleImageView = v.findViewById(R.id.circle_image_chat);
        if (receiverPhotoUrl != null)
            if (receiverPhotoUrl.equals("default")) {
                circleImageView.setImageResource(R.drawable.admin_icon);
            } else {
                Glide.with(this).load(receiverPhotoUrl).into(circleImageView);
            }

        blockListener = reference.child(generateKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean b = false;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getKey().equals("firstBlock") && snapshot1.getValue().equals("block")) {
                        try {
                            input.setText(getActivity().getString(R.string.blocked_chat));
                            blockClick();
                            b = true;
                        } catch (Exception e) {
                            blockClick();
                        }
                    } else if (snapshot1.getKey().equals("secondBlock") && snapshot1.getValue().equals("block")) {
                        try {
                            input.setText(getActivity().getString(R.string.blocked_chat));
                            blockClick();
                            b = true;
                        } catch (Exception e) {
                            blockClick();
                        }
                    }// else unblockClick();
                    if (snapshot1.getKey().equals("isRefusePhotos")) {
                        Log.e(TAG, "onDataChange: is " + snapshot1.getValue());
                        if (send_image != null && !Boolean.getBoolean
                                (Objects.requireNonNull(snapshot1.getValue()).toString())) {
                            Log.e(TAG, "onDataChange2: is " + snapshot1.getValue());
                            send_image.hide();
                        } else {
                            Log.e(TAG, "onDataChange3: is " + snapshot1.getValue());
                            send_image.show();
                        }
                    }
                }
                if (!b) unblockClick();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        adminMessagesListener = reference.child(generateKeyToAdminChat()).child("message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ChatMessage message = snapshot1.getValue(ChatMessage.class);
                    if (UserModel.getCurrentUser().getUuid().equals(firstKeyToAdmin)) {
                        if (message.getFromUserUUID().equals(secondKeyToAdmin)) {
                            if (message.getFirstSeen().equals(getActivity().getString(R.string.not_seen_text))) {
                                notificationAdminImage.setVisibility(View.VISIBLE);
                            } else notificationAdminImage.setVisibility(View.INVISIBLE);
                        }
                    } else if (UserModel.getCurrentUser().getUuid().equals(secondKeyToAdmin)) {
                        if (message.getFromUserUUID().equals(firstKeyToAdmin)) {
                            if (message.getSecondSeen().equals(getActivity().getString(R.string.not_seen_text))) {
                                notificationAdminImage.setVisibility(View.VISIBLE);
                            } else notificationAdminImage.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (block_user != null)
            if (UserModel.getCurrentUser().getAdmin_block().equals("block") && !receiverUuid.equals(getActivity().getString(R.string.admin_key))) {
                input.setText(getActivity().getString(R.string.blocked_by_admin));
                blockClick();
            } else if (block_user.equals("block")) {
                input.setText(getActivity().getString(R.string.blocked_by_admin_user));
                blockUserImage.setVisibility(View.VISIBLE);
                blockClick();
            }
        if (receiverUuid != null)
            if (receiverUuid.equals(getActivity().getResources().getString(R.string.admin_key))) {
                statusText.setText(getActivity().getString(R.string.app_name));
                username.setText(admin_string);
            } else setStatus();
        displayChatMessages();

        adView = v.findViewById(R.id.adViewChat);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        setSetChatListener();
        return v;
    }

    private void setSetChatListener() {
        reference.child(generateKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getKey().equals("refusePhotos")) {
                        if (send_image != null && snapshot1.getValue().equals(true)) {
                            toolbar.getMenu().getItem(3).setVisible(false);
                            send_image.hide();
                        } else {
                            toolbar.getMenu().getItem(2).setVisible(false);
                            send_image.show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void blockClick() {
        blocked = true;
        setToolbarToAcc();
        input.setEnabled(false);
        fab.setEnabled(false);
        send_image.setEnabled(false);
        toolbar.setEnabled(false);
    }

    public void unblockClick() {
        blocked = false;
        setToolbarToAcc();
        input.setEnabled(true);
        input.getText().clear();
        fab.setEnabled(true);
        send_image.setEnabled(true);
        toolbar.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        seenMessage();
        setChatListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity = null;
    }

    @Override
    protected void setToolbarToAcc() {
        super.setToolbarToAcc();
        if (!blocked) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.chat_menu_block);
            toolbar.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.delete_chat: {
                        AcceptDialog acceptDialog =
                                new AcceptDialog(reference, deleteMessageListener, KEY_ACCEPT_DELETE_CHAT, receiverUuid, username.getText().toString(), getChildFragmentManager());
                        acceptDialog.show(getFragmentManager(), null);
                        //deleteChat();
                        return true;
                    }
                    case R.id.block_chat: {
                        AcceptDialog acceptDialog = new AcceptDialog(reference, blockChatListener, KEY_ACCEPT_BLOCK_USER, receiverUuid, username.getText().toString());
                        acceptDialog.show(getFragmentManager(), null);
                        //blockChat();
                        return true;
                    }
                    case R.id.accept_getting_photos: {
                        FirebaseDatabase.getInstance().getReference("chats")
                                .child(generateKey()).child("refusePhotos").setValue(false);
                        toolbar.getMenu().getItem(2).setVisible(true);
                        toolbar.getMenu().getItem(3).setVisible(false);
                        return true;
                    }
                    case R.id.refuse_getting_photos: {
                        FirebaseDatabase.getInstance().getReference("chats")
                                .child(generateKey()).child("refusePhotos").setValue(true);
                        toolbar.getMenu().getItem(3).setVisible(false);
                        toolbar.getMenu().getItem(2).setVisible(true);
                        return true;
                    }
                }
                return false;
            });
        } else {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.chat_menu_unblock);
            toolbar.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.delete_chat: {
                        AcceptDialog acceptDialog =
                                new AcceptDialog(reference, deleteMessageListener, KEY_ACCEPT_DELETE_CHAT, receiverUuid, username.getText().toString(), getChildFragmentManager());
                        acceptDialog.show(getFragmentManager(), null);
                        //deleteChat();
                        return true;
                    }
                    case R.id.unblock_chat: {
                        AcceptDialog acceptDialog = new
                                AcceptDialog(reference, blockChatListener, KEY_ACCEPT_UNBLOCK_USER, receiverUuid, username.getText().toString());
                        acceptDialog.show(getFragmentManager(), null);
                        //blockChat();
                        return true;
                    }
                }
                return false;
            });
        }
    }

    @Override
    public void displayChatMessages() {/*
        List<ChatMessage> messages = Collections.emptyList();
        adapter = new ChatMessageAdapter2(messages,getContext(), getFragmentManager(), ChatFragment.this, EditMessageDialog.TYPE_OF_USER_USUAL);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //layoutManager.setStackFromEnd(true);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        recyclerView.setAnimation(null);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);*/

        adapter = new ChatMessageAdapter(ChatMessage.class, R.layout.chat_list_item_right, ChatMessageAdapter.ChatMessageHolder.class,
                FirebaseDatabase.getInstance().getReference("chats").child(generateKey()).child("message"),
                receiverUuid, getActivity(), getFragmentManager(), ChatFragment.this, EditMessageDialog.TYPE_OF_USER_USUAL);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void seenMessage() {
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                    for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                        if (!snapshot2.getKey().equals("firstBlock") && !snapshot2.getKey().equals("secondBlock")) {
                            for (DataSnapshot snapshot3 : snapshot2.getChildren()) {
                                ChatMessage message = snapshot3.getValue(ChatMessage.class);
                                if ((message.getFromUserUUID().equals(UserModel.getCurrentUser().getUuid()) && message.getToUserUUID().equals(getArguments().getString(KEY_TO_RECEIVER_UUID))) ||
                                        (message.getFromUserUUID().equals(getArguments().getString(KEY_TO_RECEIVER_UUID)) && message.getToUserUUID().equals(UserModel.getCurrentUser().getUuid()))) {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    if ((message.getToUserUUID().equals(UserModel.getCurrentUser().getUuid())) && (UserModel.getCurrentUser().getUuid().equals(firstKey)))
                                        hashMap.put("firstSeen", seenText);
                                    else if ((message.getToUserUUID().equals(UserModel.getCurrentUser().getUuid())) && (UserModel.getCurrentUser().getUuid().equals(secondKey)))
                                        hashMap.put("secondSeen", seenText);
                                    snapshot3.getRef().updateChildren(hashMap);
                                }
                            }
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send_message_button) {
            saveMessageToDB();
        }
        if (v.getId() == R.id.attach_file_button) {
            openImage();
        }
    }

    public void saveMessageToDB() {
        if (image_rui != null) {

            if (!setChatListenerConnected) {
                reference.child(generateKey()).addValueEventListener(setChatListener);
                setChatListenerConnected = true;
            }
            reference.child(generateKey()).child("message")
                    .push()
                    .setValue(new ChatMessage(input.getText().toString(),
                            UserModel.getCurrentUser().getName(), UserModel.getCurrentUser().getUuid(), receiverUuid, getActivity().getString(R.string.not_seen_text),
                            getActivity().getString(R.string.not_seen_text), (image_rui != null) ? image_rui.toString() : null, "no delete", "no delete", "no"));
        }

        //!input.getText().toString().equals("")
//|| !input.getText().toString().trim().equals("")

        else if (!input.getText().toString().trim().equals("")) {
            //setChatListener();
            if (!setChatListenerConnected) {
                reference.child(generateKey()).addValueEventListener(setChatListener);
                setChatListenerConnected = true;
            }
            reference.child(generateKey()).child("message")
                    .push()
                    .setValue(new ChatMessage(input.getText().toString(),
                            UserModel.getCurrentUser().getName(), UserModel.getCurrentUser().getUuid(), receiverUuid, getActivity().getString(R.string.not_seen_text),
                            getActivity().getString(R.string.not_seen_text), (image_rui != null) ? image_rui.toString() : null, "no delete", "no delete", "no"));
        }
        image_rui = null;
        input.setText("");
    }

    @Override
    protected void setChatListener() {
        HashMap<String, Object> map = new HashMap<>();
        setChatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    map.put("firstBlock", "no block");
                    map.put("secondBlock", "no block");
                    map.put("firstFavorites", "no");
                    map.put("secondFavorites", "no");
                    reference.child(generateKey()).updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    @Override
    protected String generateKey() {
        ArrayList<String> tempList = new ArrayList<>();
        tempList.add(UserModel.getCurrentUser().getUuid());
        tempList.add(receiverUuid);
        Collections.sort(tempList);
        firstKey = tempList.get(0);
        secondKey = tempList.get(1);
        return tempList.get(0) + tempList.get(1);
    }

    private String generateKeyToAdminChat() {
        ArrayList<String> templist = new ArrayList<>();
        templist.add(UserModel.getCurrentUser().getUuid());
        templist.add(getActivity().getString(R.string.admin_key));
        Collections.sort(templist);
        firstKeyToAdmin = templist.get(0);
        secondKeyToAdmin = templist.get(1);
        return templist.get(0) + templist.get(1);
    }

    @Override
    protected void setStatus() {
        FirebaseDatabase.getInstance().getReference(FirebaseStatic.USERS_REFERENCE).child(receiverUuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                try {
                    if (userModel.getTyping().equals(UserModel.getCurrentUser().getUuid()) && userModel.getAdmin_block().equals("unblock")) {
                        statusText.setText(R.string.typing);
                    } else if (userModel.getStatus().equals(getResources().getString(R.string.label_offline))) {
                        String dateDayMonthYear = (String) DateFormat.format("dd MMMM yyyy", userModel.getOnline_time());
                        if (dateDayMonthYear.charAt(0) == '0') {
                            dateDayMonthYear = dateDayMonthYear.substring(1);

                        }
                        statusText.setText(getStatus(userModel.getOnline_time(), userModel));
                        /*
                        if (user.getSex().equals("Женский"))
                            statusText.setText(getActivity().getString(R.string.she_was) + " " + dateDayMonthYear + " " +
                                    getActivity().getString(R.string.in) + " " + DateFormat.format("HH:mm", user.getOnline_time()));
                        else statusText.setText(getActivity().getString(R.string.was) + " " + dateDayMonthYear + " " +
                                getActivity().getString(R.string.in) + " " + DateFormat.format("HH:mm", user.getOnline_time()));*/
                    } else statusText.setText(userModel.getStatus());
                    username.setText(userModel.getName());
                    if (userModel.getVerified().equals("yes")) {
                        verifiedImage.setVisibility(View.VISIBLE);
                    } else verifiedImage.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    statusText.setText(delete_string);
                    username.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getStatus(long onlineTime, UserModel userModel) {
        Date onlineDate = new Date();
        onlineDate.setTime(onlineTime);
        Date today = new Date();
        long ms = today.getTime() - onlineDate.getTime();
        int days = (int) (ms / (24 * 60 * 60 * 1000));
        String sexWas = "";
        if (userModel.getSex().equals("Женский"))
            sexWas = getResources().getString(R.string.she_was);
        else sexWas = getResources().getString(R.string.was);
        StringBuilder builder = new StringBuilder();
        builder.append(sexWas);
        builder.append(" ");
        if (days == 0) return builder.append(getStr(R.string.today)).toString();
        if (days <= 1) return builder.append(getStr(R.string.yesterday)).toString();
        if (days <= 7) return builder.append(getStr(R.string.week_ago)).toString();
        if (days >= 25 && days <= 35) return builder.append(getStr(R.string.month_ago)).toString();
        else return builder.append(onlineTime).toString();
    }

    private String getStr(int id) {
        return getResources().getString(id);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.removeAllListener();
        setWriting("unwriting");
        if (deleteMessageListener != null)
            reference.child(generateKey()).child("message").removeEventListener(deleteMessageListener);
        if (blockChatListener != null)
            reference.child(generateKey()).removeEventListener(blockChatListener);
        if (adminMessagesListener != null)
            reference.child(generateKeyToAdminChat()).child("message").removeEventListener(adminMessagesListener);
    }

    @Override
    protected void removeAllListener() {
        super.removeAllListener();
        if (seenListener != null)
            reference.removeEventListener(seenListener);
        seenListener = null;
    }

    @Override
    protected void setWriting(String writing) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("typing", writing);
        referenceWriting.child(UserModel.getCurrentUser().getUuid()).updateChildren(map);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim().length() == 0) {
            setWriting("unwriting");
        } else if (!s.toString().equals(getActivity().getString(R.string.blocked_chat))
                && !s.toString().equals(getActivity().getString(R.string.blocked_by_admin))) {
            setWriting(receiverUuid);
            Log.d("tut_writing", "tuttttt");
            adView.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GO_TO_ADMIN_REQUEST) {
                switch (data.getIntExtra(GoToAdminDialog.BTN_CODE, -1)) {
                    case GoToAdminDialog.CHAT_BTN_CODE:
                        activity.goToAdmin();
                        break;
                    case GoToAdminDialog.COMPLAIN_BTN_CODE: {
                        ComplainDialog dialog = new
                                ComplainDialog(ComplainDialog.BASE_COMPLAIN_CODE, getFragmentManager(), ChatFragment.this);
                        dialog.setTargetFragment(ChatFragment.this, COMPLAIN_REQUEST);
                        dialog.show(getFragmentManager(), null);
                    }
                }
            } else if (requestCode == COMPLAIN_REQUEST) {
                if (!data.getStringExtra(ComplainDialog.COMPLAIN_CODE).equals(getActivity().getString(R.string.another_reason_title))) {
                    String complaint = data.getStringExtra(ComplainDialog.COMPLAIN_CODE);
                    if (complaint.equals(getActivity().getString(R.string.false_name))
                            || complaint.equals(getActivity().getString(R.string.false_age))
                            || complaint.equals(getActivity().getString(R.string.false_city))
                            || complaint.equals(getActivity().getString(R.string.false_country))
                            || complaint.equals(getActivity().getString(R.string.false_country_and_country))) {
                        complaint = getActivity().getString(R.string.wrong) + " " + data.getStringExtra(ComplainDialog.COMPLAIN_CODE);
                    } else if (!complaint.equals(getActivity().getString(R.string.advertising_title))
                            && !complaint.equals(getActivity().getString(R.string.fishing_title))
                            && !complaint.equals(getActivity().getString(R.string.illegal_substance_title))
                            && !complaint.equals(getActivity().getString(R.string.obscene_content_title))
                            && !complaint.equals(getActivity().getString(R.string.extrimism_title))
                            && !complaint.equals(getActivity().getString(R.string.pornographic_content_title))
                            && !complaint.equals(getActivity().getString(R.string.threats_title))
                            && !complaint.equals(getActivity().getString(R.string.married))
                            && !complaint.equals(getActivity().getString(R.string.underage))
                    ) {
                        complaint = getActivity().getString(R.string.illegal_photos) + " " + data.getStringExtra(ComplainDialog.COMPLAIN_CODE);
                    }
                    String text_complaint = getActivity().getString(R.string.complaint_beginning) + "  " + complaint +
                            getActivity().getString(R.string.complaint_ending) + "  " + username.getText() +
                            getActivity().getString(R.string.complaint_id) + "  " + receiverUuid;

                    sendToAdmin(text_complaint);
                } else {
                    activity.goToAdmin();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendToAdmin(String str) {
        reference.child(generateKeyToAdminChat()).child("message")
                .push()
                .setValue(new ChatMessage(str,
                        UserModel.getCurrentUser().getName(), UserModel.getCurrentUser().getUuid(), getActivity().getString(R.string.admin_key), getActivity().getString(R.string.not_seen_text),
                        getActivity().getString(R.string.not_seen_text), receiverPhotoUrl, "no delete", "no delete", "no"));
        Toast.makeText(getActivity(), getActivity().getString(R.string.complain_completed), Toast.LENGTH_SHORT).show();
    }

    public interface CallBack {
        void goToAdmin();
    }

    /*private void blockChat(){
            blockChatListener = reference.child(generateKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                        if (snapshot1.getKey().equals("firstBlock") && User.getCurrentUser().getUuid().equals(firstKey)){
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("firstBlock","block");
                            snapshot.getRef().updateChildren(map);
                        }
                        else if (snapshot1.getKey().equals("secondBlock") && User.getCurrentUser().getUuid().equals(secondKey)){
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("secondBlock","block");
                            snapshot.getRef().updateChildren(map);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    private void deleteChat(){
            deleteMessageListener=reference.child(generateKey()).child("message").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                        if (User.getCurrentUser().getUuid().equals(firstKey)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("firstDelete", "delete");
                            snapshot1.getRef().updateChildren(hashMap);
                        }
                        else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("secondDelete", "delete");
                            snapshot1.getRef().updateChildren(hashMap);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }*/

}