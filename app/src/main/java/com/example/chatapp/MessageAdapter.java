package com.example.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private List<Message> messageList;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        Message c = messageList.get(position);

        String from_user = c.getFrom();
        String message_type = c.getType();


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String user_image = dataSnapshot.child("thumb_nail").getValue().toString();

                holder.displayUserName.setText(name);

                Glide.with(holder.circleImageView.getContext()).load(user_image)
                        .placeholder(R.drawable.logged).into(holder.circleImageView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {

            holder.displayMessage.setText(c.getMessage());
            holder.imageView.setVisibility(View.INVISIBLE);


        } else {

            holder.displayMessage.setVisibility(View.INVISIBLE);
            Glide.with(holder.circleImageView.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.logged).into(holder.imageView);

        }









    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView displayMessage, displayUserName;
        private CircleImageView circleImageView;
        private ImageView imageView;
        private View myView;

        public MyViewHolder(View itemView) {
            super(itemView);
            myView = itemView;
            displayMessage = myView.findViewById(R.id.user_message);
            displayUserName = myView.findViewById(R.id.user_name_in_chat);
            circleImageView = myView.findViewById(R.id.message_user_image);
            imageView = myView.findViewById(R.id.image_message_layout);

        }




    }
}
