package com.example.chatapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView userProfileName, userProfileStatus, totalFriends;
    private Button sendFriendReqBtn, declineFriendReqBtn;
    private ImageView profile_image;
    private DatabaseReference databaseReference;
    private DatabaseReference friendRequestDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String currentUser, userId;
    private String currentState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userProfileName = findViewById(R.id.profileName);
        userProfileStatus = findViewById(R.id.profileStatus);
        totalFriends = findViewById(R.id.totalFriends);

        sendFriendReqBtn = findViewById(R.id.sendFriendReq);
        declineFriendReqBtn = findViewById(R.id.decline);

        profile_image = findViewById(R.id.profileImage);
        currentState = "not_friend";
        userId = getIntent().getStringExtra("user_id");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        currentUser = mUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        databaseReference.keepSynced(true);
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        friendRequestDatabase.keepSynced(true);
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        friendDatabase.keepSynced(true);
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
        rootRef = FirebaseDatabase.getInstance().getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String uStatus = (String) dataSnapshot.child("status").getValue();
                String profileImage = (String) dataSnapshot.child("image").getValue();

                userProfileName.setText(name);
                userProfileStatus.setText(uStatus);
                Glide.with(getApplicationContext()).load(profileImage).diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.logged).into(profile_image);

                //------- friend list -------//

                friendRequestDatabase.child(currentUser).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)) {
                            String reqState = (String) dataSnapshot.child(userId).child("request_state").getValue();
                            if (reqState.equals("received")) {
                                currentState = "req_received";
                                sendFriendReqBtn.setText("Accept Friend Request");

                                declineFriendReqBtn.setVisibility(View.VISIBLE);
                                declineFriendReqBtn.setEnabled(true);

                            } else if (reqState.equals("sent")) {
                                currentState = "req_sent";
                                sendFriendReqBtn.setText("Cancel Friend Request");

                                declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                declineFriendReqBtn.setEnabled(false);
                            }
                        } else {
                            if (dataSnapshot.hasChild(userId)) {
                                currentState = "friends";
                                sendFriendReqBtn.setText("Unfriend this person");

                                declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                declineFriendReqBtn.setEnabled(false);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        sendFriendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendFriendReqBtn.setEnabled(false);

                //------------- Not Friend ----------------//
                if (currentState.equals("not_friend")) {

                    DatabaseReference newNotificationRef = rootRef.child("notification").child(userId).push();
                    String notificationKey = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser);
                    notificationData.put("type", "request");


                    Map requestMap = new HashMap();
                    requestMap.put("friend_requests/" + currentUser + "/" + userId + "/" + "request_state", "sent");
                    requestMap.put("friend_requests/" + userId + "/" + currentUser + "/" + "request_state", "received");
                    requestMap.put("notification/" + userId + "/" + notificationKey, notificationData);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "There was something error in sending friend request", Toast.LENGTH_SHORT).show();
                            }

                            sendFriendReqBtn.setEnabled(true);
                            currentState = "req_sent";
                            sendFriendReqBtn.setText("cancel friend request");
                        }
                    });


                }


                //------------ Cancel Friend Request ---------//

                if (currentState.equals("req_sent")) {
                    friendRequestDatabase.child(currentUser).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            friendRequestDatabase.child(userId).child(currentUser).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    sendFriendReqBtn.setText("Send Friend Request");
                                    currentState = "not_friend";
                                    sendFriendReqBtn.setEnabled(true);

                                    declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                    declineFriendReqBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }


                //------------- Request Received ----------------//
                if (currentState.equals("req_received")) {

                    final String date = DateFormat.getDateTimeInstance().format(new Date());

                    friendDatabase.child(currentUser).child(userId).child("date").setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            friendDatabase.child(userId).child(currentUser).child("date").setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    friendRequestDatabase.child(currentUser).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            friendRequestDatabase.child(userId).child(currentUser).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    currentState = "friends";
                                                    sendFriendReqBtn.setText("Unfriend this person");
                                                    sendFriendReqBtn.setEnabled(true);

                                                    declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                                    declineFriendReqBtn.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }


                //------------UnFriends-----------//
                if(currentState.equals("friends"))
                {
                    Map unFriends = new HashMap();
                    unFriends.put("friends/" + currentUser + "/" + userId , null);
                    unFriends.put("friends/" + userId + "/" +currentUser , null);

                    rootRef.updateChildren(unFriends, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null)
                            {

                                currentState = "not_friend";
                                sendFriendReqBtn.setText("send friend request");

                                declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                declineFriendReqBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            declineFriendReqBtn.setEnabled(true);

                        }
                    });


                }



                //send btn request brackets//
                 }
        });


        declineFriendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(currentState.equals("req_received"))
                {
                    Map unFriends = new HashMap();
                    unFriends.put("friend_requests/" + currentUser + "/" + userId , null);
                    unFriends.put("friend_requests/" + userId + "/" +currentUser , null);

                    rootRef.updateChildren(unFriends, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null)
                            {

                                currentState = "not_friend";
                                sendFriendReqBtn.setText("send friend request");

                                declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                declineFriendReqBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            sendFriendReqBtn.setEnabled(true);

                        }
                    });


                }


            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.child("online").setValue("true");
    }


}
