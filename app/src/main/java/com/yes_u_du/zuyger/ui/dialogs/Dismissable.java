package com.yes_u_du.zuyger.ui.dialogs;

public interface Dismissable<T> {
    void onDismiss(T code);

    String chooseOption(T code);
}
