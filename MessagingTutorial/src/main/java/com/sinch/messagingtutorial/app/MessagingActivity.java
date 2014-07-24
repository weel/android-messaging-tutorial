package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

    private String recipientId;
    private Button sendButton;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private MessageAdapter messageAdapter;
    private ListView messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);

        doBind();

        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);

        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);
        sendButton = (Button) findViewById(R.id.sendButton);

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

    @Override
    public void onDestroy() {
        unbindService(this);
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        //Define the messaging service and add a listener
        messageService = (MessageService.MessageServiceInterface) iBinder;
        messageService.addMessageClientListener(this);
        if (!messageService.isSinchClientStarted()) {
            Toast.makeText(this, "The message client did not start."
                ,Toast.LENGTH_LONG).show();
        }
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
        messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
    }

    @Override
    public void onMessageSent(MessageClient client, Message message, String recipientId) {
        messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
    }

    @Override
    public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {
        //Intentionally left blank
    }
}
