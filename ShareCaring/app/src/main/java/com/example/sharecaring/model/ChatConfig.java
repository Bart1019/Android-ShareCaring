package com.example.sharecaring.model;

import android.content.Context;

import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;

public class ChatConfig {
    private static final String APP_ID = "27297bcc6d53412";
    private static final String API_KEY = "3c022a14c1229184e2b757e57962f87a21eb0570";
    private static final String REGION = "eu";

    public static void initCometChat(Context context) {
        AppSettings appSettings=new AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(ChatConfig.REGION).build();

        CometChat.init(context, ChatConfig.APP_ID, appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {}

            @Override
            public void onError(CometChatException e) {}
        });
    }

    public static void createUser(String uId, String name) {
        User user = new User();
        user.setUid(uId); // Replace with the UID for the user to be created
        user.setName(name); // Replace with the name of the user

        CometChat.createUser(user, API_KEY, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {}

            @Override
            public void onError(CometChatException e) {}
        });
    }

    public static void loginToChat(String uId) {
        if (CometChat.getLoggedInUser() == null) {
            CometChat.login(uId, API_KEY, new CometChat.CallbackListener<User>() {

                @Override
                public void onSuccess(User user) {}

                @Override
                public void onError(CometChatException e) {}
            });
        } else {
            // User already logged in
        }
    }

    public void logoutFromChat() {
        CometChat.logout(new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {}
            @Override
            public void onError(CometChatException e) {}
        });
    }
}
