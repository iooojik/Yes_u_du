package com.yes_u_du.zuyger.ui.reg_and_login_utils.forgot;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.ui.reg_and_login_utils.auth.LoginFragment;

public class ForgotPass extends AppCompatActivity {
    Button fr_btn;
    EditText reset_email;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        mAuth = FirebaseAuth.getInstance();

        reset_email = findViewById(R.id.reset_email);

        fr_btn = findViewById(R.id.fr_btn);
        fr_btn.setOnClickListener(v -> {
            String userEmail = reset_email.getText().toString();

            if (TextUtils.isEmpty(userEmail)) {
                Toast.makeText(ForgotPass.this, "Поля пусты", Toast.LENGTH_LONG).show();
            } else {
                mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPass.this, "Ссылка для восстановления пароля отправлена на ваш email", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ForgotPass.this, LoginFragment.class));
                        finish();
                    } else {
                        Toast.makeText(ForgotPass.this, "Ошибка", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}