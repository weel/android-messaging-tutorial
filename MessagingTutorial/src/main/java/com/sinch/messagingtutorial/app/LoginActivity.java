package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.os.Bundle;
import com.parse.Parse;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Parse.initialize(this, "IhUPJfNhqBPLns6dXn6BeW3BMfGOlcRElMoYnilM", "1tTdMRSmK74ZzZtYBAPCJFoSbahBxi8cNt6TYj9U");
    }
}
