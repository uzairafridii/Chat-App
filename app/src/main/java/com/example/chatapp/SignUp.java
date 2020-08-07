package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    //edit text
    private TextInputLayout mDisplayName, mEmail, mPassword;
    //create account button
    private Button mCreateButton;
    //firebase auth
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    // toolbar
    private Toolbar mToolbar;
    //progressbar dialog
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);

        mCreateButton = findViewById(R.id.signButton);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.register_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Progress Dialog
        mProgress = new ProgressDialog(this);


        // firebase auth instance or initialization
        mAuth = FirebaseAuth.getInstance();


        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (TextUtils.isEmpty(name)) {
                    mDisplayName.setError("Name Required...");

                } else if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email Required...");
                } else if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password Required...");
                } else {
                    mProgress.setTitle("Registering your account....");
                    mProgress.setMessage("Please wait while creating your account...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    // call user register method
                    registerUsers(name, email, password);
                }


            }
        });
    }

    // method for registering user or create user account
    private void registerUsers(final String displayName, final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currenUser = mAuth.getCurrentUser();
                            String userId = currenUser.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {

                                    String device_token = instanceIdResult.getToken();
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", displayName);
                                    hashMap.put("status", "Hi there,i'm using chat app..");
                                    hashMap.put("image", "default");
                                    hashMap.put("thumb_nail", "default");
                                    hashMap.put("device_token", device_token);

                                    mDatabase.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                                startActivity(intent);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                finish();
                                                Toast.makeText(SignUp.this, "Authentication Success.", Toast.LENGTH_SHORT).show();
                                                mProgress.dismiss();
                                            }

                                        }
                                    });

                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();

                        }

                        // ...
                    }
                });
    }
}
