package com.example.chatapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friend_list_recycler;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference friendDatabaseReference;
    private DatabaseReference allUserList;
    private FirebaseUser currentUser;
    private String userId;
    private FirebaseRecyclerOptions<Friends> options;
    private FirebaseRecyclerAdapter<Friends , MyViewHolder> firebaseRecyclerAdapter;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View friendsView = inflater.inflate(R.layout.fragment_friends, container, false);
        friend_list_recycler = friendsView.findViewById(R.id.friend_List);
        friend_list_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        userId = currentUser.getUid();

        friendDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends").child(userId);
        friendDatabaseReference.keepSynced(true);
        allUserList = FirebaseDatabase.getInstance().getReference().child("Users");
        allUserList.keepSynced(true);


        options  = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendDatabaseReference , Friends.class)
                .setLifecycleOwner(this)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull Friends model)
            {
                holder.setDate(model.getDate());

                final String list = getRef(position).getKey();
                allUserList.child(list).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String name = (String) dataSnapshot.child("name").getValue();
                        final String image = (String) dataSnapshot.child("thumb_nail").getValue();

                        if(dataSnapshot.hasChild("online")) {
                            String onlineStatus =  dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(onlineStatus);
                        }

                        holder.setUserName(name);
                        holder.setImage(image);



                        holder.myView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence option[] = new CharSequence[]{"User Profile","Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if(i == 0)
                                        {
                                            Intent profileIntent = new Intent(getActivity() , ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list);
                                            startActivity(profileIntent);
                                        }
                                        else if(i == 1)
                                        {
                                            Intent chatIntent = new Intent(getActivity() , ChatActivity.class);
                                            chatIntent.putExtra("user_id", list);
                                            chatIntent.putExtra("user_name",name);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });
                                builder.show();


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
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View myView = LayoutInflater.from(getContext()).inflate(R.layout.user_single_item , null);
                MyViewHolder holder = new MyViewHolder(myView);

                return holder;
            }
        };

        friend_list_recycler.setAdapter(firebaseRecyclerAdapter);


        return friendsView;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder
    {
        private TextView date;
        private View myView;
        private CircleImageView image;
        private TextView name;

        public MyViewHolder(View itemView) {
            super(itemView);
            myView = itemView;
        }

        public void setDate(String userDate)
        {
            date = myView.findViewById(R.id.user_status);
            date.setText(userDate);
        }

        public void setUserName(String userName)
        {
            name = myView.findViewById(R.id.user_display_name);
            name.setText(userName);
        }

        public void setImage(String userImage)
        {
            image = myView.findViewById(R.id.user_image);
            Glide.with(getContext())
                    .load(userImage)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(R.drawable.logged)
                    .into(image);
        }

        public void setUserOnline(String userOnline)
        {
            ImageView imageView = myView.findViewById(R.id.online_icon);
            if(userOnline.equals("true"))
            {
                imageView.setVisibility(View.VISIBLE);
            }
            else
            {
                imageView.setVisibility(View.INVISIBLE);
            }
        }

    }
}
