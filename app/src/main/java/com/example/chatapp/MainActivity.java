package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    //view pager instance
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase auth reference
        mAuth = FirebaseAuth.getInstance();
        //tool bar reference
        mToolbar = (Toolbar) findViewById(R.id.main_page_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Simple Chat App");

        //view pager reference
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mSectionPagerAdapter  = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        // tablayout
        mTabLayout  = findViewById(R.id.tablayout);
        mTabLayout.setupWithViewPager(mViewPager);

        if(mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
           sendToBack();
        }
        else
        {
            mUserRef.child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }



    public void sendToBack()
    {
        startActivity(new Intent(MainActivity.this , StartScreen.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if(item.getItemId() == R.id.logout_main_page)
        {
            mAuth.signOut();
            sendToBack();
        }
        else if(item.getItemId() == R.id.acount_setting)
        {
            startActivity(new Intent(MainActivity.this , AccountSettingActivity.class));
        }
        else if(item.getItemId() == R.id.all_users)
        {
         startActivity(new Intent(MainActivity.this , UserActivity.class));
        }


        return true;
    }
}
