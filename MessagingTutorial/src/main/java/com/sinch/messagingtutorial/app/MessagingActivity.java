package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

public class MessagingActivity extends Activity {

//    private String currentUserId;
//    private String recipientId;
//    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);

//        currentUserId = ParseUser.getCurrentUser().getObjectId().toString();
//        intent = getIntent();
//        recipientId = intent.getStringExtra("RECIPIENT_ID");
    }
}
