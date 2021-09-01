package com.yes_u_du.zuyger.ui.chat_process;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.ui.account.UserAccountActivity;
import com.yes_u_du.zuyger.models.ChatMessage;
import com.yes_u_du.zuyger.models.UserModel;
import com.yes_u_du.zuyger.ui.chat_process.users.ChatFragment;
import com.yes_u_du.zuyger.ui.dialogs.EditMessageDialog;
import com.yes_u_du.zuyger.ui.photo_utils.viewpager.PhotoViewPagerItemFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class ChatParentFragment extends Fragment implements View.OnClickListener, TextWatcher {
    public static final String KEY_TO_RECEIVER_UUID = "recevierID";
    public static final String KEY_TO_RECEIVER_PHOTO_URL = "recevierPHOTO_URL";
    public static final int EDIT_MSG_DIALOG_CODE = 0;
    private static final int IMAGE_REQUEST = 1;
    public boolean isEditing;
    protected String receiverUuid;
    protected String receiverPhotoUrl;
    protected FloatingActionButton fab, send_image;
    protected Toolbar toolbar;
    protected EditText input;
    protected TextView username;
    protected TextView statusText;
    protected ImageView complainView;
    protected RecyclerView recyclerView;
    protected CircleImageView circleImageView;
    protected DatabaseReference reference;
    protected String firstKey, secondKey;
    protected ValueEventListener blockListener;
    protected ValueEventListener setChatListener;
    protected StorageReference storageReference;
    protected ChatFragment.CallBack activity;
    protected Uri image_rui;
    protected String delete_string;
    protected String admin_string;
    protected AdView adView;
    protected BottomSheetImageSelector bottomSheetImageSelector;
    private StorageTask uploadTask;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ChatFragment.CallBack) context;
        bottomSheetImageSelector = new
                BottomSheetImageSelector(requireContext(), requireActivity(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity = null;
    }

    public abstract void displayChatMessages();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            saveMessageToDB();
        }
        if (v.getId() == R.id.send_image_button) {
            openImage();
        }
    }

    protected void openImage() {
        bottomSheetImageSelector.show();
    }

    public abstract void saveMessageToDB();

    private void saveMessageToDB(String key) {

        String txt_message = input.getText().toString();
        if (TextUtils.isEmpty(txt_message)) {
            Toast.makeText(getContext(), "Отправка пустого сообщения невозможна!", Toast.LENGTH_LONG).show();
        } else if (txt_message.length() < 1) {
            Toast.makeText(getContext(), "Отправка пустого сообщения невозможна!", Toast.LENGTH_LONG).show();
        } else if (txt_message.trim().isEmpty()) {
            Toast.makeText(getContext(), "Отправка пустого сообщения невозможна!", Toast.LENGTH_LONG).show();
        } else {
            DatabaseReference referenceDB = reference.child(generateKey()).child("message").child(key);
            HashMap<String, Object> map = new HashMap<>();
            map.put("messageText", txt_message);
            map.put("edited", "yes");
            referenceDB.updateChildren(map);
        }

        input.setText("");
        fab.setImageResource(R.drawable.baseline_send_black_24dp);
        fab.setOnClickListener(this);

    }

    // abstract void clickMessage(View v, DatabaseReference reference, String messageText,int type);


    protected String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    protected void setClickListenerOnImage(ChatMessage model, ImageView imageView) {
        imageView.setOnClickListener(v -> {
            Fragment newDetail = PhotoViewPagerItemFragment.newInstance(model.getImage_url(), imageView);
            getFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container, newDetail)
                    .commit();
            imageView.setEnabled(false);
        });
    }

    protected void uploadImage(Bitmap bitmap) throws IOException {
        Log.e(TAG, "uploadImage: " + (bitmap == null));
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage(getResources().getString(R.string.uploading));
        pd.show();

        if (image_rui != null || bitmap != null) {
            StorageReference fileReference;
            if (image_rui != null) {
                storageReference = FirebaseStorage.getInstance().getReference("ChatImage");
                fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(image_rui));
                uploadTask = fileReference.putFile(image_rui);
            } else {
                //Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
                byte[] bitmapdata = bos.toByteArray();
                fileReference = storageReference.child(System.currentTimeMillis() + ".jpeg");
                uploadTask = fileReference.putBytes(bitmapdata);
            }
            StorageReference finalFileReference = fileReference;
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return finalFileReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    image_rui = task.getResult();
                    Toast.makeText(getContext(), R.string.image_attach, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.failed_update_photo, Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(getContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();
        }
    }

    protected void setToolbarToAcc() {
        toolbar.setOnClickListener(v -> {
            Intent intent = UserAccountActivity.newIntent(getContext(), receiverUuid);
            startActivity(intent);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == EDIT_MSG_DIALOG_CODE) {
            if (!isEditing) toolbar.inflateMenu(R.menu.edit_menu);
            // toolbar.getMenu().getItem(0).setEnabled(false);
            MenuItem item = toolbar.getMenu().getItem(0);
            item.setEnabled(false);
            toolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.colorToolbar));
            username.setVisibility(View.GONE);
            statusText.setVisibility(View.GONE);
            complainView.setVisibility(View.GONE);
            send_image.setEnabled(false);
            isEditing = true;
            toolbar.setOnClickListener(null);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.cancel_edit_item) {
                        setupEditCancel();
                        return true;
                    }
                    return false;
                }
            });
            input.setText(data.getStringExtra(EditMessageDialog.KEY_TO_MSG_TEXT));
            fab.setImageResource(R.drawable.edit_msg_icon);
            fab.setOnClickListener(v -> {
                Log.e("KEY_TO_REF:", data.getStringExtra(EditMessageDialog.KEY_TO_REF));
                saveMessageToDB(data.getStringExtra(EditMessageDialog.KEY_TO_REF));
                setupEditCancel();
            });

        }

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null &&
                (data.getData() != null || data.getExtras().get("data") != null)) {
            bottomSheetImageSelector.hide();
            if (data.getData() != null) {
                image_rui = data.getData();
                try {
                    uploadImage(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (data.getExtras().get("data") != null) {
                try {
                    uploadImage((Bitmap) data.getExtras().get("data"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void setupEditCancel() {
        username.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.VISIBLE);
        complainView.setVisibility(View.VISIBLE);
        isEditing = false;
        //circleImageView.setVisibility(View.VISIBLE);
        toolbar.setTitle("");
        toolbar.getMenu().clear();
        input.setText("");
        send_image.setEnabled(true);
        setToolbarToAcc();
    }

    protected abstract String generateKey();

    protected void setStatus() {
        FirebaseDatabase.getInstance().getReference("users").child(receiverUuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                try {
                    if (userModel.getStatus().equals(getResources().getString(R.string.label_offline))) {
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

    protected abstract void setWriting(String writing);

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

    protected void removeAllListener() {
        if (blockListener != null)
            reference.child(generateKey()).removeEventListener(blockListener);
        blockListener = null;
        if (setChatListener != null)
            reference.child(generateKey()).removeEventListener(setChatListener);
        setChatListener = null;
    }
}
