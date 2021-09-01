package com.yes_u_du.zuyger.ui.rules_and_policy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yes_u_du.zuyger.R;

public class InformationFragment extends Fragment {
    private static final String KEY_INFORMATION = "informationText";

    public static InformationFragment newInstance(String informationText) {
        InformationFragment fragment = new InformationFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_INFORMATION, informationText);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.information_fragment, container, false);
        TextView informationText = view.findViewById(R.id.information_text);
        if (getArguments() != null)
            informationText.setText(getArguments().getString(KEY_INFORMATION));
        return view;
    }
}
