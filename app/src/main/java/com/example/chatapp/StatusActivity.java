package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveButton;
    //firebase
    private DatabaseReference mStatusReference;
    private FirebaseUser mUser;
    //progress dialog
    private ProgressDialog mDialog;
    String status_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //add the toolbar
        mToolbar = findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //intent to get the data from accont setting activity
        Intent intent = getIntent();
        status_value = intent.getStringExtra("status");
        //textInput layout
        mStatus = (TextInputLayout) findViewById(R.id.status_text_input_layout);

        //set previous status
        mStatus.getEditText().setText(status_value);
        // firebase
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = mUser.getUid();
        mStatusReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        // progress
        mDialog = new ProgressDialog(this);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDialog.setTitle("Status Changing");
                mDialog.setMessage("Please wait while change your status...");
                mDialog.show();

                String status = mStatus.getEditText().getText().toString();

                mStatusReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            Toast.makeText(StatusActivity.this, "Status Change Successfully..", Toast.LENGTH_SHORT).show();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(StatusActivity.this, "Fail to change Status..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mStatusReference.child("online").setValue("true");
    }


}
