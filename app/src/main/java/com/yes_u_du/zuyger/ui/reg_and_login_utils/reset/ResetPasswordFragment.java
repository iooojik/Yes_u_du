package com.yes_u_du.zuyger.ui.reg_and_login_utils.reset;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yes_u_du.zuyger.R;

public class ResetPasswordFragment extends Fragment {

    private FirebaseUser user;
    private EditText oldPassword;
    private EditText newPassword;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reset_password, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        oldPassword = view.findViewById(R.id.old_password);
        newPassword = view.findViewById(R.id.new_password);
        toolbar = view.findViewById(R.id.toolbarFr);
        settingToolbar();
        return view;
    }

    private void settingToolbar() {
        toolbar.inflateMenu(R.menu.reset_password);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.saved_password) {
                if (!oldPassword.getText().toString().isEmpty())
                    passwordReconciliation();
            }
            return true;
        });
    }

    private void passwordReconciliation() {
        final String email = user.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword.getText().toString());

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updatePassword();
                } else {
                    Toast.makeText(getActivity(), R.string.incorrect_old_password, Toast.LENGTH_SHORT).show();
                    Log.e("RESET PASSWORD", "INCORRECT OLD PASSWORD");
                }
            });
        }
    }

    private void updatePassword() {
        newPassword.setVisibility(View.VISIBLE);
        if (newPassword.getText().length() > 5) {
            user.updatePassword(newPassword.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), R.string.password_reset, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.password_not_reset, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), R.string.password_length_short, Toast.LENGTH_SHORT).show();
        }
    }

}
