package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends Activity {

    private String currentUserId;
    private ArrayAdapter<String> namesArrayAdapter;
    private ArrayList<String> names;
    private ListView usersListView;
    private Button logoutButton;
    private ProgressDialog progressDialog;
    private BroadcastReceiver receiver = null;
    private MessageService.MessageServiceInterface sinchService;
    private Boolean bound = false;
    private ServiceConnection serviceConnection = new MyServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        if (!bound) {
            bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);
            bound = true;
        }

        logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(getApplicationContext(), MessageService.class));
                ParseUser.logOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        setConversationsList();
    }

    //display clickable a list of all users
    private void setConversationsList() {
        currentUserId = ParseUser.getCurrentUser().getObjectId();
        names = new ArrayList<String>();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, ParseException e) {
                if (e == null) {
                    for (int i=0; i<userList.size(); i++) {
                        names.add(userList.get(i).getUsername().toString());
                    }

                    usersListView = (ListView)findViewById(R.id.usersListView);
                    namesArrayAdapter =
                        new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.user_list_item, names);
                    usersListView.setAdapter(namesArrayAdapter);

                    usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                            openConversation(names, i);
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(),
                        "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //open a conversation with one person
    public void openConversation(ArrayList<String> names, int pos) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", names.get(pos));
        query.findInBackground(new FindCallback<ParseUser>() {
           public void done(List<ParseUser> user, ParseException e) {
               if (e == null) {
                   Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
                   intent.putExtra("RECIPIENT_ID", user.get(0).getObjectId());
                   startActivity(intent);
               } else {
                   Toast.makeText(getApplicationContext(),
                       "Error finding that user",
                           Toast.LENGTH_SHORT).show();
               }
           }
        });
    }

    //show a loading spinner while the sinch client starts
    private void showSpinner() {
        if (!sinchService.isSinchClientStarted()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Boolean success = intent.getBooleanExtra("success", false);
                    progressDialog.dismiss();
                    if (!success) {
                        Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                    }
                }
            };

            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.sinch.messagingtutorial.app.ListUsersActivity"));
        }
    }

    @Override
    public void onResume() {
        if (!bound) {
            bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);
            bound = true;
        }
        setConversationsList();
        super.onResume();
    }

    @Override
    public void onPause() {
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }

        super.onDestroy();
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sinchService = (MessageService.MessageServiceInterface) iBinder;
            showSpinner();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            sinchService = null;
        }
    }
}


