package com.example.chatapp;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView chatRecycler;
    private DatabaseReference chatReference;
    private DatabaseReference messageReference;
    private DatabaseReference usersReference;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private FirebaseRecyclerOptions<Conversation> options;
    private FirebaseRecyclerAdapter<Conversation , MyChatViewHolder> firebaseRecyclerAdapter;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View chatView = inflater.inflate(R.layout.fragment_chat, container, false);
        chatRecycler = chatView.findViewById(R.id.chat_fragment_recycler);
        LinearLayoutManager layoutManager  = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        chatReference = FirebaseDatabase.getInstance().getReference().child("Chat").child(currentUserId);

        messageReference = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUserId);

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");


        //----------------------recycler adapter etc------------------------//


        final Query conversationQuery = chatReference.orderByChild("timestamp");


        options  = new FirebaseRecyclerOptions.Builder<Conversation>()
                .setQuery(conversationQuery , Conversation.class)
                .setLifecycleOwner(this)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conversation, MyChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MyChatViewHolder holder, int position, @NonNull final Conversation model) {

                final String list_user_id = getRef(position).getKey();

                Query lastMessage = messageReference.child(list_user_id).limitToLast(1);

                lastMessage.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        String message = dataSnapshot.child("message").getValue().toString();
                        String messageType = dataSnapshot.child("type").getValue().toString();
                        boolean userSeen = model.isSeen();

                        holder.setMessageView(message , userSeen , messageType);
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



                usersReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String  userName = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("thumb_nail").getValue().toString();

                        holder.setProfileImage(image);
                        holder.setNameView(userName);

                        if(dataSnapshot.hasChild("online"))
                        {
                            String onlineStatus = dataSnapshot.child("online").getValue().toString();

                            holder.setOnlineStatusImage(onlineStatus);
                        }

                        holder.myView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id" , list_user_id);
                                chatIntent.putExtra("user_name" , userName);
                                startActivity(chatIntent);
                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public MyChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.single_message_layout, parent , false);
                MyChatViewHolder holder = new MyChatViewHolder(view);
                return holder;
            }
        };

        chatRecycler.setAdapter(firebaseRecyclerAdapter);



        return chatView;
    }



    public class MyChatViewHolder extends RecyclerView.ViewHolder
    {
        private CircleImageView profileImage;
        private TextView nameView;
        private TextView messageView;
        private ImageView onlineStatusImage;
        private ImageView messageImage;
        private View myView;


        public MyChatViewHolder(View itemView) {
            super(itemView);
            myView = itemView ;
        }

        public void setNameView(String user_name)
        {
            nameView  = myView.findViewById(R.id.user_name_in_chat);
            nameView.setText(user_name);
        }

        public void setMessageView(String user_message , boolean isSeen , String textType)
        {
            messageView = myView.findViewById(R.id.user_message);
            messageImage = myView.findViewById(R.id.image_message_layout);

            if(textType.equals("text")) {
                messageView.setText(user_message);
            }else
            {
                Glide.with(getContext())
                        .load(user_message)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(messageImage); }
            if(!isSeen)
            {
                messageView.setTypeface(messageView.getTypeface() , Typeface.BOLD);
            }
            else
            {
                messageView.setTypeface(messageView.getTypeface() , Typeface.NORMAL);
            }

        }

        public void setProfileImage(String image_url)
        {
            profileImage = myView.findViewById(R.id.message_user_image);
            Glide.with(getContext()).load(image_url).diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(R.drawable.logged).into(profileImage);
        }

        public void setOnlineStatusImage(String seen)
        {
            onlineStatusImage = myView.findViewById(R.id.online_status);
            if(seen.equals("online"))
            {
                onlineStatusImage.setVisibility(View.VISIBLE);
            }
            else
            {
                onlineStatusImage.setVisibility(View.INVISIBLE);
            }
        }



    }
}
