package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;

import java.util.List;

public class MessagingActivity extends Activity implements ServiceConnection, MessageClientListener {

    private String currentUserId;
    private String recipientId;
    private Button sendButton;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);

        doBind();

        currentUserId = ParseUser.getCurrentUser().getObjectId().toString();
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");

        messageBodyField = (EditText) findViewById(R.id.txtTextBody);
        sendButton = (Button) findViewById(R.id.btnSend);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        messageBody = messageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }

        messageService.sendMessage(recipientId, messageBody);
        messageBodyField.setText("");
    }

    private void doBind() {
        Intent serviceIntent = new Intent(this, MessageService.class);
        bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

    private void doUnbind() {
        if (messageService != null) {
            messageService.removeMessageClientListener(this);
        }
        unbindService(this);
    }

    @Override
    public void onDestroy() {
        doUnbind();
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        //Define the messaging service and add a listener
        messageService = (MessageService.MessageServiceInterface) iBinder;
        messageService.addMessageClientListener(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        messageService = null;
    }

    @Override
    public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
        //Intentionally  left blank
    }

    @Override
    public void onMessageFailed(MessageClient client, Message message,
                                MessageFailureInfo failureInfo) {
        //Notify the user if message fails to send
        Toast.makeText(this, "Message failed to send.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onIncomingMessage(MessageClient client, Message message) {
        //This is where we will update the UI
    }

    @Override
    public void onMessageSent(MessageClient client, Message message, String recipientId) {
        //We will also update the UI here
    }

    @Override
    public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {
        //Intentionally left blank
    }
}
