package com.yes_u_du.zuyger.ui.reg_and_login_utils;

import androidx.fragment.app.Fragment;

import com.yes_u_du.zuyger.BaseActivity;
import com.yes_u_du.zuyger.R;
import com.yes_u_du.zuyger.constants.Static;
import com.yes_u_du.zuyger.services.Services;
import com.yes_u_du.zuyger.ui.reg_and_login_utils.auth.LoginFragment;
import com.yes_u_du.zuyger.ui.reg_and_login_utils.auth.RegisterFragment;
import com.yes_u_du.zuyger.utils.Permissions;


public class AuthorizationActivity extends BaseActivity implements LoginFragment.Callback, RegisterFragment.Callbacks {

    @Override
    public Fragment getFragment() {
        return LoginFragment.Companion.newFragment(null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Permissions(getApplicationContext(), this).requestPermissions();
        new Services(this, Static.Companion.getMAIN_SERVICES()).start();
    }

    @Override
    public void onRegisterClicked() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void returnLoginFragment(String email, String password) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, LoginFragment.Companion.newFragment(email, password))
                .commit();
    }
}
