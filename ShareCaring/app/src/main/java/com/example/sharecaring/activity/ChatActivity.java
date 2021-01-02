package com.example.sharecaring.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.TextMessage;
import com.example.sharecaring.R;
import com.example.sharecaring.model.ChatConfig;
import com.example.sharecaring.model.MessageWrapper;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.sql.CommonDataSource;

public class ChatActivity extends AppCompatActivity {

    private String receiverID;
    private String receiverType = CometChatConstants.RECEIVER_TYPE_USER;
    private MessagesListAdapter<MessageWrapper> adapter;
    private String listenerID = ChatActivity.class.getName();
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        intent = getIntent();
        if (intent != null) {
            receiverID = intent.getStringExtra("receiverUid");
        }

        init();
        addNewMessageListener();
        fetchPreviousMessages();
    }

    private void init() {
        MessageInput inputView = findViewById(R.id.input);
        MessagesList messagesList = findViewById(R.id.messagesList);

        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                sendMessage(input.toString());
                return true;
            }
        });

        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.with(ChatActivity.this).load(url).into(imageView);
            }
        };

        String senderId = CometChat.getLoggedInUser().getUid();
        adapter = new MessagesListAdapter<>(senderId, imageLoader);
        messagesList.setAdapter(adapter);
    }

    private void sendMessage(String message) {
        TextMessage textMessage = new TextMessage(receiverID, message, receiverType);

        CometChat.sendMessage(textMessage, new CometChat.CallbackListener <TextMessage> () {
            @Override
            public void onSuccess(TextMessage textMessage) {
                addMessage(textMessage);
            }
            @Override
            public void onError(CometChatException e) {
            }
        });
    }

    private void addMessage(TextMessage textMessage) {
        adapter.addToStart(new MessageWrapper(textMessage), true);  //adds new messages at the bottom
    }

    private void addNewMessageListener() {
        CometChat.addMessageListener(listenerID, new CometChat.MessageListener() {
            @Override
            public void onTextMessageReceived(TextMessage textMessage) {
                addMessage(textMessage);
            }
        });
    }

    private void fetchPreviousMessages() {
        MessagesRequest messagesRequest = new MessagesRequest.MessagesRequestBuilder().setUID(receiverID).build();
        messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
            @Override
            public void onSuccess(List<BaseMessage> baseMessages) {
                addMessages(baseMessages);
            }

            @Override
            public void onError(CometChatException e) {

            }
        });
    }

    private void addMessages(List<BaseMessage> baseMessages) {
        List<MessageWrapper> list = new ArrayList<>();
        for (BaseMessage message: baseMessages) {
            if (message instanceof TextMessage) {
                list.add(new MessageWrapper((TextMessage) message));
            }
        }

        adapter.addToEnd(list, true);   //messages in reversed order
    }
}