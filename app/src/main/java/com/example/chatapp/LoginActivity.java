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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mEmail , mPassword;
    private Button mLoginButton;
    // Toolbar instance
    private Toolbar mToolbar;
    private DatabaseReference mDatabase;
    //firebase auth instance
    private FirebaseAuth mAuth;
    //progress dialog instance
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = (TextInputLayout) findViewById(R.id.login_email);
        mPassword = (TextInputLayout) findViewById(R.id.login_password);

        mLoginButton = findViewById(R.id.login_Button);

        //Toolbar reference
        mToolbar = (Toolbar) findViewById(R.id.login_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //firebase auth reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //progressbar reference
        mProgress = new ProgressDialog(this);


        // login button set onclick method
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString().trim();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
                {
                    mEmail.setError("Email Required ....");
                    mPassword.setError("Password Required ...");
                }

                else
                {
                    mProgress.setTitle("Login into your account...");
                    mProgress.setMessage("Please wait while login to your account ...!");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                   userLogin(email , password);
                }




            }
        });
    }



    //user login method
    private void userLogin(String email , String password)
    {

        mAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    mProgress.dismiss();

                    final String current_user = mAuth.getCurrentUser().getUid();
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {

                            String device_token = instanceIdResult.getToken();
                            mDatabase.child(current_user).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent mainIntent = new Intent(LoginActivity.this , MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });
                        }
                    });


                }
                else
                {
                    mProgress.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication Error..", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
