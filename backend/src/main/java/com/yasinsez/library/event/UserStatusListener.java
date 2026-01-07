package com.yasinsez.library.event;

import com.yasinsez.library.model.User;

public interface UserStatusListener {
    void onUserStatusChange(User user, String oldStatus, String newStatus);
}
