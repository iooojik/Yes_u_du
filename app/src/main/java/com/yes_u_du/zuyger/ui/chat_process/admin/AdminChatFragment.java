package com.yes_u_du.zuyger.ui.chat_process.admin;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.models.ChatMessage;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.chat_process.ChatParentFragment;
import com.yes_u_du.zuyger.ui.dialogs.EditMessageDialog;

import java.util.ArrayList;
import java.util.Collections;

public class AdminChatFragment extends ChatParentFragment {

    private AdminChatMessageAdapter adapter;
    private boolean setChatListenerConnected;

    public static AdminChatFragment newInstance(String toUserUUID, String photo_url) {
        AdminChatFragment fragment = new AdminChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TO_RECEIVER_UUID, toUserUUID);
        bundle.putString(KEY_TO_RECEIVER_PHOTO_URL, photo_url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        delete_string = getResources().getString(R.string.delete_users);
        admin_string = getResources().getString(R.string.admin);
        setChatListenerConnected = false;

        View v = inflater.inflate(R.layout.chat_fragment, container, false);
        MobileAds.initialize(getActivity(), initializationStatus -> {
        });
        receiverUuid = getArguments().getString(KEY_TO_RECEIVER_UUID);
        receiverPhotoUrl = getArguments().getString(KEY_TO_RECEIVER_PHOTO_URL);
        toolbar = v.findViewById(R.id.toolbarFr);
        complainView = v.findViewById(R.id.complain_button);
        setToolbarToAcc();
        complainView.setVisibility(View.GONE);
        statusText = v.findViewById(R.id.online_text_in_chat);
        recyclerView = v.findViewById(R.id.list_of_messages);
        fab = v.findViewById(R.id.send_message_button);
        send_image = v.findViewById(R.id.attach_file_button);
        send_image.setOnClickListener(this);
        input = v.findViewById(R.id.input);
        input.addTextChangedListener(this);
        fab.setOnClickListener(this);
        reference = FirebaseDatabase.getInstance().getReference("chats");
        username = v.findViewById(R.id.username_text);
        circleImageView = v.findViewById(R.id.circle_image_chat);
        if (receiverPhotoUrl.equals("default")) {
            circleImageView.setImageResource(R.drawable.admin_icon);
            circleImageView.setBackgroundResource(R.color.authui_colorAccent);
        } else {
            Glide.with(this).load(receiverPhotoUrl).into(circleImageView);
        }

        blockListener = reference.child(generateKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                    if (snapshot1.getKey().equals("firstBlock") && UserModel.getCurrentUser().getUuid().equals(secondKey) && snapshot1.getValue().equals("block")) {
                        input.setText(getResources().getString(R.string.blocked_chat));
                        input.setEnabled(false);
                        fab.setEnabled(false);
                        send_image.setEnabled(false);
                    } else if (snapshot1.getKey().equals("secondBlock") && UserModel.getCurrentUser().getUuid().equals(firstKey) && snapshot1.getValue().equals("block")) {
                        input.setText(getResources().getString(R.string.blocked_chat));
                        input.setEnabled(false);
                        fab.setEnabled(false);
                        send_image.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        if (UserModel.getCurrentUser().getAdmin_block().equals("block")) {
            input.setText(getResources().getString(R.string.blocked_by_admin));
            input.setEnabled(false);
            fab.setEnabled(false);
            send_image.setEnabled(false);
        }

        if (receiverUuid.equals(getActivity().getResources().getString(R.string.admin_key))) {
            statusText.setText(getActivity().getString(R.string.app_name));
            username.setText(admin_string);
        } else setStatus();
        displayChatMessages();
        adView = v.findViewById(R.id.adViewChat);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setChatListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeAllListener();
    }

    @Override
    public void saveMessageToDB() {
        if (image_rui != null) {
            //reference = FirebaseDatabase.getInstance().getReference("chats").child(generateKey());
            if (!setChatListenerConnected) {
                reference.child(generateKey()).addValueEventListener(setChatListener);
                setChatListenerConnected = true;
            }

            reference.child(generateKey()).child("message")
                    .push()
                    .setValue(new ChatMessage(input.getText().toString(),
                            getActivity().getResources().getString(R.string.admin), getActivity().getString(R.string.admin_key), receiverUuid, getResources().getString(R.string.not_seen_text),
                            getResources().getString(R.string.not_seen_text), (image_rui != null) ? image_rui.toString() : null, "no delete", "no delete", "no"));
        } else if (!input.getText().toString().equals("")) {
            // reference = FirebaseDatabase.getInstance().getReference("chats").child(generateKey());
            if (!setChatListenerConnected) {
                reference.child(generateKey()).addValueEventListener(setChatListener);
                setChatListenerConnected = true;
            }

            reference.child(generateKey()).child("message")
                    .push()
                    .setValue(new ChatMessage(input.getText().toString(),
                            getActivity().getResources().getString(R.string.admin), getActivity().getString(R.string.admin_key), receiverUuid, getResources().getString(R.string.not_seen_text),
                            getResources().getString(R.string.not_seen_text), (image_rui != null) ? image_rui.toString() : null, "no delete", "no delete", "no"));
        }
        image_rui = null;
        input.setText("");
    }

    public void displayChatMessages() {
        adapter = new AdminChatMessageAdapter(ChatMessage.class, R.layout.chat_list_item_right, AdminChatMessageAdapter.ChatMessageHolder.class,
                FirebaseDatabase.getInstance().getReference("chats").child(generateKey()).child("message"),
                receiverUuid, getActivity(), getFragmentManager(), AdminChatFragment.this, EditMessageDialog.TYPE_OF_USER_ADMIN);
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
        recyclerView.setAnimation(null);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    protected String generateKey() {
        ArrayList<String> templist = new ArrayList<>();
        templist.add(getActivity().getResources().getString(R.string.admin_key));
        templist.add(receiverUuid);
        Collections.sort(templist);
        firstKey = templist.get(0);
        secondKey = templist.get(1);
        return templist.get(0) + templist.get(1);
    }

    @Override
    protected void setWriting(String writing) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
