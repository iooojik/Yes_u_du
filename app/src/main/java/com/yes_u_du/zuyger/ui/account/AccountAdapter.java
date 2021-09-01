package com.yes_u_du.zuyger.ui.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yes_u_du.zuyger.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.TextHolder> {

    private final Context context;
    private final LinkedHashMap<String, String> values;

    public AccountAdapter(Context context, LinkedHashMap<String, String> values) {
        this.context = context;
        this.values = values;
    }

    @NonNull
    @Override
    public TextHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TextHolder(LayoutInflater.from(context).inflate(R.layout.item_account_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TextHolder holder, int position) {
        String key = (new ArrayList<>(values.values())).get(position);
        String value = (new ArrayList<>(values.keySet())).get(position);
        holder.valueText.setText(value);
        if (position <= values.size()) holder.keyName.setText(key);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public static class TextHolder extends RecyclerView.ViewHolder {

        private final TextView keyName;
        private final TextView valueText;

        public TextHolder(@NonNull View itemView) {
            super(itemView);
            keyName = itemView.findViewById(R.id.textView);
            valueText = itemView.findViewById(R.id.text_label);
        }
    }
}
