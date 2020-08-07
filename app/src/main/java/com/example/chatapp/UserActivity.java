package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUserList;
    private DatabaseReference mDatabaseReference;
    private FirebaseRecyclerAdapter<Users, UserViewHolder> firebaseAdapter;
    private FirebaseRecyclerOptions<Users> options;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // set the toolbar
        mToolbar = findViewById(R.id.user_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("User List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //database
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);

        firebaseAuth = FirebaseAuth.getInstance();

        //recycler view reference
        mUserList = (RecyclerView) findViewById(R.id.user_list);
        mUserList.setLayoutManager(new LinearLayoutManager(this));


        //FireBase query
          Query query = FirebaseDatabase.getInstance().getReference()
               .child("Users")
             .limitToLast(50);

          //FireBaseRecycler option read the data from firebse and give to firebasereycler adapter
        options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class)
                .build();
        //Firebase recycler adapter get the data from firebase recycler option and set to the UI layout
        firebaseAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>
                (options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {
                holder.setUserName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setImage(getApplication(),model.getThumb_nail());

                final String user_id = getRef(position).getKey();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        Intent profileIntent = new Intent(UserActivity.this , ProfileActivity.class);
                        profileIntent.putExtra("user_id" , user_id);
                        startActivity(profileIntent);
                        Toast.makeText(UserActivity.this, " view Onclick method Clicked ", Toast.LENGTH_SHORT).show();

                    }
                });

            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                View view = inflater.inflate(R.layout.user_single_item, null);

                return new UserViewHolder(view);
            }
        };
        mUserList.setAdapter(firebaseAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAdapter.startListening();
        mDatabaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAdapter != null) {
            firebaseAdapter.stopListening();
        }


    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserName(String name) {
            TextView userName = mView.findViewById(R.id.user_display_name);
            userName.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatus = mView.findViewById(R.id.user_status);
            userStatus.setText(status);
        }

        public void setImage(Context context, String image) {
            CircleImageView userImage = mView.findViewById(R.id.user_image);
            Glide.with(context).load(image).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.logged).into(userImage);
        }

    }
}
