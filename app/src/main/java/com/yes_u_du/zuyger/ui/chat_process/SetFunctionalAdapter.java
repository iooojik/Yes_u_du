package com.yes_u_du.zuyger.ui.chat_process;

import com.yes_u_du.zuyger.models.ChatMessage;

public interface SetFunctionalAdapter {
    void setMessageLayout(ChatMessage chatMessage);

    String generateKey();
}
