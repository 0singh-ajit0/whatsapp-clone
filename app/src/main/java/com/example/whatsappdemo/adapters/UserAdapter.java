package com.example.whatsappdemo.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappdemo.ChatDetailActivity;
import com.example.whatsappdemo.R;
import com.example.whatsappdemo.models.MessageModel;
import com.example.whatsappdemo.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    ArrayList<UserModel> list;
    Context context;

    public UserAdapter(ArrayList<UserModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = list.get(position);
        Picasso.get().load(user.getProfilePicture()).placeholder(R.drawable.user).into(holder.profilePicView);
        holder.userNameView.setText(user.getUserName());
        // Updating last message
        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(FirebaseAuth.getInstance().getUid() + user.getUserId())
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                MessageModel model = dataSnapshot.getValue(MessageModel.class);
                                Log.i("INFO", "Message: " + model.getMessage());
                                holder.lastMessageView.setText(model.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId", user.getUserId());
                intent.putExtra("username", user.getUserName());
                intent.putExtra("profilePic", user.getProfilePicture());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePicView;
        TextView userNameView, lastMessageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicView = itemView.findViewById(R.id.ivProfileImage);
            userNameView = itemView.findViewById(R.id.tvChatUserName);
            lastMessageView = itemView.findViewById(R.id.tvChatLastMsg);
        }
    }

}
