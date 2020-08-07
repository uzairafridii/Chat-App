package com.example.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private Intent intent;
    private String name, id, image;
    private Toolbar mToolbar;
    private TextView display_name, last_seen;
    private CircleImageView user_image;
    private DatabaseReference chatDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private EditText messageEditText;
    private ImageButton add_image_btn;
    private ImageButton send_message_btn;

    private RecyclerView messageList;
    private LinearLayoutManager layoutManager;
    private MessageAdapter messageAdapter;
    private List<Message> listOfMessages = new ArrayList();

    private SwipeRefreshLayout refreshLayout;

    private static final int TOTAL_ITEMS = 10;
    private int item_count = 1;

    private static final int GALLERY_RESULT = 1;

    private int itemPos = 0;
    private String lastKey = "";
    private String prevKey = "";

    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        intent = getIntent();
        name = intent.getStringExtra("user_name");
        id = intent.getStringExtra("user_id");


        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.custom_app_bar, null);

        actionBar.setCustomView(mView);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        chatDatabase = FirebaseDatabase.getInstance().getReference();
        chatDatabase.keepSynced(true);
        mStorageReference = FirebaseStorage.getInstance().getReference();

        //------------------custom app bar views---------------------//

        display_name = findViewById(R.id.custom_display_name);
        last_seen = findViewById(R.id.custom_last_seen);
        user_image = findViewById(R.id.custom_app_bar_image);
        //-------------------------------------------------------------//

        //------------- send message views inflate---------------------//

        messageEditText = findViewById(R.id.message_edit_text);
        add_image_btn = findViewById(R.id.add_btn_in_chat);
        send_message_btn = findViewById(R.id.send_btn_in_chat);

        //--------------Recycler view to retrieve the messages---------------//
        messageAdapter = new MessageAdapter(listOfMessages);
        messageList = findViewById(R.id.messages_list_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        messageList.setHasFixedSize(true);
        refreshLayout = findViewById(R.id.swipe_layout);

        messageList.setLayoutManager(layoutManager);

        messageList.setAdapter(messageAdapter);

        loadMessages();

        display_name.setText(name);

        chatDatabase.child("Users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                image = dataSnapshot.child("image").getValue().toString();
                String onlineStatus = dataSnapshot.child("online").getValue().toString();

                if (onlineStatus.equals("true")) {
                    last_seen.setText("online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(onlineStatus);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    last_seen.setText("" + lastSeenTime);

                }

                Glide.with(getApplicationContext()).load(image).diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.drawable.logged).into(user_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        chatDatabase.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(id)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + id, chatAddMap);
                    chatUserMap.put("Chat/" + id + "/" + mCurrentUserId, chatAddMap);

                    chatDatabase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Log.e("Chat App", databaseError.getMessage());
                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        send_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });

        add_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");

                startActivityForResult(Intent.createChooser(galleryIntent , "SELECT IMAGE") , GALLERY_RESULT);
            }
        });


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                item_count++;
                itemPos = 0;
                loadMoreMessages();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_RESULT && resultCode == RESULT_OK)
        {
           Uri uri = data.getData();

           final String currentUser = "messages/" + mCurrentUserId + "/" + id;
           final String chatUserId = "messages/" + id + "/" + mCurrentUserId;

           DatabaseReference database = chatDatabase.child("messages/").child(mCurrentUserId).child(id).push();

           final String pushKey = database.getKey();

           final StorageReference filePath = mStorageReference.child("message_images").child(pushKey + ".jpg");

           filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String downloadUrl = uri.toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message", downloadUrl);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                            messageMap.put("from" , mCurrentUserId);


                            Map messageUserMap = new HashMap();
                            messageUserMap.put(currentUser + "/" + pushKey, messageMap);
                            messageUserMap.put(chatUserId + "/" + pushKey, messageMap);


                            chatDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    if(databaseError != null)
                                    {
                                        Toast.makeText(ChatActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });



                        }
                    });
               }
           });


        }


    }

    private void loadMoreMessages()
    {
        DatabaseReference messageRef = chatDatabase.child("messages").child(mCurrentUserId).child(id);
        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(TOTAL_ITEMS);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Message message = dataSnapshot.getValue(Message.class);
                String messageKey = dataSnapshot.getKey();

                if(!prevKey.equals(messageKey)) {
                    listOfMessages.add(itemPos++, message);
                }
                else
                {
                    prevKey = lastKey;
                }

                if(itemPos == 1)
                {

                    lastKey = messageKey;
                }

                messageAdapter.notifyDataSetChanged();

                messageList.scrollToPosition(listOfMessages.size() - 1);

                refreshLayout.setRefreshing(false);
                layoutManager.scrollToPositionWithOffset(10 , 0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = chatDatabase.child("messages").child(mCurrentUserId).child(id);
        Query messageQuery = messageRef.limitToLast(item_count * TOTAL_ITEMS);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Message message = dataSnapshot.getValue(Message.class);
                itemPos++;

                if(itemPos == 1)
                {
                    String messageKey = dataSnapshot.getKey();
                    lastKey = messageKey;
                    prevKey = messageKey;
                }
                listOfMessages.add(message);
                messageAdapter.notifyDataSetChanged();

                messageList.scrollToPosition(listOfMessages.size() - 1);

                refreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void sendMessage() {
        final String message = messageEditText.getText().toString();

        if (!TextUtils.isEmpty(message)) {
            String currentUserRef = "messages/" + mCurrentUserId + "/" + id;
            String chatUserRef = "messages/" + id + "/" + mCurrentUserId;

            DatabaseReference user_push = chatDatabase.child("messages")
                    .child(mCurrentUserId).child(id).push();
            String pushId = user_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("timestamp", ServerValue.TIMESTAMP);
            messageMap.put("from" , mCurrentUserId);


            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
            messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

            chatDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e("Chat App", databaseError.getMessage());
                    }

                }
            });
            messageEditText.setText("");
        }
       else
        {
            messageEditText.setError("Required");
            messageEditText.setText("");
        }

    }
}
