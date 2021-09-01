package com.yes_u_du.zuyger.ui.chat_list.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.constants.FirebaseStatic;
import com.yes_u_du.zuyger.models.ChatMessage;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.account.UserAccountActivity;
import com.yes_u_du.zuyger.ui.chat_process.ChatActivity;
import com.yes_u_du.zuyger.ui.dialogs.CancelDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRecViewAdapter extends RecyclerView.Adapter<ChatRecViewAdapter.ChatItemHolder> {

    public static String filtered;
    private final List<UserModel> userModelList;
    private final Context context;
    private final FragmentManager fragmentManager;
    private final int viewType;
    private int type_dialog;
    private boolean isChatDialogs = false;
    private Activity activity;

    public ChatRecViewAdapter(List<UserModel> list, Context context, FragmentManager manager, int viewType, boolean isChatDialogs) {
        this.userModelList = list;
        this.context = context;
        this.fragmentManager = manager;
        this.viewType = viewType;
        filtered = "none";
        this.isChatDialogs = isChatDialogs;
    }

    public ChatRecViewAdapter(List<UserModel> list, Context context, FragmentManager manager, int viewType, String filtered) {
        this.userModelList = list;
        this.context = context;
        this.fragmentManager = manager;
        this.viewType = viewType;
        ChatRecViewAdapter.filtered = filtered;
    }

    public ChatRecViewAdapter(List<UserModel> list, Context context, FragmentManager manager, int viewType, int type_dialog) {
        this.userModelList = list;
        this.context = context;
        this.fragmentManager = manager;
        this.viewType = viewType;
        this.type_dialog = type_dialog;
        filtered = "none";
    }

    @NonNull
    @Override
    public ChatItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (filtered.equals("none") && viewType != AdminBanListItemHolder.VIEW_TYPE) {
            v = LayoutInflater.from(context).inflate(R.layout.users_list_item, parent, false);
        } else {
            v = LayoutInflater.from(context).inflate(R.layout.users_filtered_list_item, parent, false);
        }

        switch (viewType) {
            case ChatItemHolder.VIEW_TYPE:
                return new ChatItemHolder(v, context, fragmentManager);
            case BlockListItemHolder.VIEW_TYPE:
                return new BlockListItemHolder(v, context, fragmentManager, type_dialog);
            case AdminChatItemHolder.VIEW_TYPE:
                return new AdminChatItemHolder(v, context, fragmentManager);
            case FavoriteListItemHolder.VIEW_TYPE:
                return new FavoriteListItemHolder(v, context, fragmentManager, type_dialog);
            case AdminBanListItemHolder.VIEW_TYPE:
                return new AdminBanListItemHolder(v, context, fragmentManager, type_dialog);
            default:
                throw new NullPointerException("HOLDER TYPE IS INVALID");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatItemHolder holder, int position) {
        //Log.e("users_inAdapter_id", userList.get(position).getUuid());
        UserModel userModel = userModelList.get(position);
        Log.e("ss", String.valueOf((userModel.getName() != null)));
        if (userModel.getName() == null) {
            userModelList.remove(position);
            return;
        }
        if (userModel.getName() != null && !userModel.getName().trim().isEmpty()) {
            holder.onBind(userModelList.get(position), isChatDialogs);
            if (UserModel.getCurrentUser() != null && filtered.equals("none") && viewType != AdminBanListItemHolder.VIEW_TYPE) {
                Log.d("if_bind", "here");
                if (userModel.getType() != null && !userModel.getType().equals(FirebaseStatic.ADMIN))
                    holder.setLastMsg(holder.userModel.getUuid(), holder.userText);
                else holder.setLastMsg(FirebaseStatic.ADMIN, holder.userText);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }


    public static class ChatItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public static final int VIEW_TYPE = 0;

        protected UserModel userModel;
        protected Context context;
        protected FragmentManager fragmentManager;
        private final TextView userName;
        private final TextView userDate;
        private final TextView userText;
        private final TextView userStatus;
        private final ImageView verifiedImage;
        private final ImageView blockedUserImage;
        private final CircleImageView photoImageView;
        private final LinearLayout linearLayout;

        public ChatItemHolder(@NonNull View itemView, Context context, FragmentManager manager) {
            //обычные чаты
            super(itemView);
            linearLayout = itemView.findViewById(R.id.chat_list_item_layout);
            this.context = context;
            this.fragmentManager = manager;
            userName = itemView.findViewById(R.id.user_name);
            userDate = itemView.findViewById(R.id.user_date);
            userText = itemView.findViewById(R.id.user_text);
            userStatus = itemView.findViewById(R.id.text_online_list);
            photoImageView = itemView.findViewById(R.id.circle_image_user);
            verifiedImage = itemView.findViewById(R.id.verified_image_item);
            blockedUserImage = itemView.findViewById(R.id.blocked_image_item);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        protected String generateKey(String receiverUuid) {
            ArrayList<String> templist = new ArrayList<>();
            templist.add(UserModel.getCurrentUser().getUuid());
            templist.add(receiverUuid);
            Collections.sort(templist);
            String firstKey = templist.get(0);
            return firstKey;
        }

        public void setLastMsg(String id, TextView view) {
            if (view != null)
                FirebaseDatabase.getInstance().getReference("chats").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren())
                            for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                                if (!snapshot2.getKey().equals("firstBlock") && !snapshot2.getKey().equals("secondBlock")) {
                                    for (DataSnapshot snapshot3 : snapshot2.getChildren()) {
                                        ChatMessage message = snapshot3.getValue(ChatMessage.class);
                                        //если я первый
                                        if (UserModel.getCurrentUser().getUuid().equals(generateKey(id))) {
                                            if ((message.getToUserUUID().equals(UserModel.getCurrentUser().getUuid()) && message.getFromUserUUID().equals(id) ||
                                                    message.getToUserUUID().equals(id) && message.getFromUserUUID().equals(UserModel.getCurrentUser().getUuid()))
                                            ) {
                                                if (message.getFirstDelete().equals("delete")) {
                                                    view.setText("");
                                                } else {

                                                    view.setText(message.getMessageText());
                                                }
                                                if (message.getSecondSeen().equals(context.getString(R.string.not_seen_text))) {
                                                    linearLayout.setBackgroundResource(R.color.no_seen);
                                                }
                                                if (message.getSecondSeen().equals(context.getString(R.string.seen_text)) || message.getFirstSeen().equals(context.getString(R.string.seen_text)))
                                                    linearLayout.setBackgroundColor(Color.WHITE);
                                            }
                                        } else {
                                            if ((message.getToUserUUID().equals(UserModel.getCurrentUser().getUuid()) && message.getFromUserUUID().equals(id) ||
                                                    message.getToUserUUID().equals(id) && message.getFromUserUUID().equals(UserModel.getCurrentUser().getUuid()))) {
                                                if (message.getSecondDelete().equals("delete")) {
                                                    view.setText("");
                                                } else
                                                    view.setText(message.getMessageText());
                                                if (message.getFirstSeen().equals(context.getString(R.string.not_seen_text))) {
                                                    //TODO здесь красится лейаут
                                                    linearLayout.setBackgroundResource(R.color.no_seen);
                                                }
                                                if (message.getSecondSeen().equals(context.getString(R.string.seen_text)) || message.getFirstSeen().equals(context.getString(R.string.seen_text)))
                                                    linearLayout.setBackgroundColor(Color.WHITE);
                                            }
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

        void onBind(UserModel userModel, boolean isChatDialogs) {
            this.userModel = userModel;
            if (userModel.getType() != null && userModel.getType().equals(FirebaseStatic.ADMIN)) {
                userName.setText(context.getString(R.string.admin));
                photoImageView.setImageResource(R.drawable.admin_icon);

            } else {
                if (isChatDialogs) {
                    Glide.with(context).load(userModel.getPhoto_url()).into(photoImageView);
                    userName.setText(userModel.getName());
                }
                else
                    //userName.setText(userModel.getName() + ", " + userModel.getAge() + "\n" + userModel.getCity());todo
                    if (userModel.getPerm_block() != null
                            && userModel.getAdmin_block() != null
                            && userModel.getPhoto_url() != null
                            && userModel.getStatus() != null
                            && userModel.getVerified() != null) {

                        if (filtered.equals(FilteredUsersSearchFragment.FILTER_VIEW_TYPE)) {
                            if (userModel.getPerm_block().equals("block")) {
                                setBlockedListeners(context.getString(R.string.perm_blocked_by_admin_on_chatlist_title));
                                blockedUserImage.setVisibility(View.VISIBLE);
                            } else if (userModel.getAdmin_block().equals("block")) {
                                setBlockedListeners(context.getString(R.string.blocked_by_admin_on_chatlist_title));
                                blockedUserImage.setVisibility(View.VISIBLE);
                            }
                        }
                        if (userModel.getPhoto_url().equals("default")) {
                            photoImageView.setImageResource(R.drawable.unnamed);
                        } else {
                            Glide.with(context).load(userModel.getPhoto_url()).into(photoImageView);
                        }
                        if (userModel.getStatus().equals(context.getResources().getString(R.string.label_online))) {
                            userStatus.setText(context.getResources().getString(R.string.label_online));
                        } else
                            userStatus.setText(context.getResources().getString(R.string.label_offline));
                        if (userModel.getVerified().equals("yes")) {
                            verifiedImage.setVisibility(View.VISIBLE);
                        } else verifiedImage.setVisibility(View.INVISIBLE);
                    }
            }
        }

        private void setBlockedListeners(String text) {
            itemView.setOnClickListener(v -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            if (userModel.getType() != null && userModel.getType().equals(FirebaseStatic.ADMIN)) {
                intent = ChatActivity.newIntent(
                        context, UserModel.getCurrentUserModel().getUuid(),
                        UserModel.getCurrentUserModel().getPhoto_url(), "unblock", 3);
            } else {
                intent = ChatActivity.newIntent(
                        context.getApplicationContext(), userModel.getUuid(),
                        userModel.getPhoto_url(), userModel.getAdmin_block(), VIEW_TYPE);
            }
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    public static class BlockListItemHolder extends ChatItemHolder {
        public static final int VIEW_TYPE = 1;
        private final int type_dialog;

        public BlockListItemHolder(@NonNull View itemView, Context context, FragmentManager manager, int type_dialog) {
            super(itemView, context, manager);
            this.type_dialog = type_dialog;
        }

        @Override
        public boolean onLongClick(View v) {
            CancelDialog dialog = new CancelDialog(userModel.getUuid(), type_dialog);
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
            dialog.setTargetFragment(fragment, BlockListFragment.KEY_TO_UNBLOCK);
            dialog.show(fragmentManager, null);
            return true;
        }
    }

    public static class FavoriteListItemHolder extends ChatItemHolder {
        public static final int VIEW_TYPE = 3;
        private final int type_dialog;

        public FavoriteListItemHolder(@NonNull View itemView, Context context, FragmentManager manager, int type_dialog) {
            super(itemView, context, manager);
            this.type_dialog = type_dialog;
        }

        @Override
        public boolean onLongClick(View v) {
            CancelDialog dialog = new CancelDialog(userModel.getUuid(), type_dialog);
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
            dialog.setTargetFragment(fragment, BlockListFragment.KEY_TO_UNBLOCK);
            dialog.show(fragmentManager, null);
            return true;
        }
    }

    public static class AdminBanListItemHolder extends ChatItemHolder {
        public static final int VIEW_TYPE = 4;
        private final int list_type;
        //private DatabaseReference mReference;

        public AdminBanListItemHolder(@NonNull View itemView, Context context, FragmentManager manager, int list_type) {
            super(itemView, context, manager);
            this.list_type = list_type;
            //Log.d("holder_userID",user.getUuid());
            //mReference=FirebaseDatabase.getInstance().getReference("users").child(user.getUuid());
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

        @Override
        public void onClick(View view) {
            Intent intent = UserAccountActivity.newIntent(context, userModel.getUuid());
            FragmentActivity activity = (FragmentActivity) context;
            context.startActivity(intent);
            activity.finish();
        }
    }

    public static class AdminChatItemHolder extends ChatItemHolder {

        public static final int VIEW_TYPE = 2;

        public AdminChatItemHolder(@NonNull View itemView, Context context, FragmentManager manager) {
            //список чатов администрации с пользователями
            super(itemView, context, manager);
            itemView.findViewById(R.id.user_creds).setOnClickListener(this);
            itemView.findViewById(R.id.circle_image_user).setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (view.getId() == R.id.user_creds || view.getId() == R.id.circle_image_user) {
                Intent intent = UserAccountActivity.newIntent(context, userModel.getUuid());
                context.startActivity(intent);
            } else {
                Intent intent2 = ChatActivity.newIntent(context,
                        userModel.getUuid(), userModel.getPhoto_url(), userModel.getAdmin_block(), VIEW_TYPE);
                context.startActivity(intent2);
            }

        }

        @Override
        public void setLastMsg(String id, TextView view) {
            FirebaseDatabase.getInstance().getReference("chats").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                        for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                            if (!snapshot2.getKey().equals("firstBlock") && !snapshot2.getKey().equals("secondBlock")) {
                                for (DataSnapshot snapshot3 : snapshot2.getChildren()) {
                                    ChatMessage message = snapshot3.getValue(ChatMessage.class);

                                    //если я первый
                                    if (context.getResources().getString(R.string.admin_key).equals(generateKey(id))) {
                                        if ((message.getToUserUUID().equals(context.getResources().getString(R.string.admin_key)) && message.getFromUserUUID().equals(id) ||
                                                message.getToUserUUID().equals(id) && message.getFromUserUUID().equals(context.getResources().getString(R.string.admin_key)))) {
                                            if (message.getFirstDelete().equals("delete")) {
                                                view.setText("");
                                            } else
                                                view.setText(message.getMessageText());
                                        }
                                    } else {
                                        if ((message.getToUserUUID().equals(context.getResources().getString(R.string.admin_key)) && message.getFromUserUUID().equals(id) ||
                                                message.getToUserUUID().equals(id) && message.getFromUserUUID().equals(context.getResources().getString(R.string.admin_key)))) {
                                            if (message.getSecondDelete().equals("delete")) {
                                                view.setText("");
                                            } else
                                                view.setText(message.getMessageText());
                                        }
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
        protected String generateKey(String receiverUuid) {
            ArrayList<String> templist = new ArrayList<>();
            templist.add(context.getResources().getString(R.string.admin_key));
            templist.add(receiverUuid);
            Collections.sort(templist);
            String firstKey = templist.get(0);
            return firstKey;
        }
    }

}