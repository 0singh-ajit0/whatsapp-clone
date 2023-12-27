package com.example.whatsappdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.whatsappdemo.adapters.ChatAdapter;
import com.example.whatsappdemo.databinding.ActivityChatDetailBinding;
import com.example.whatsappdemo.models.MessageModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class GroupChatActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    Boolean justOpenedChat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        binding.ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter adapter = new ChatAdapter(messageModels, this);
        binding.rvChatDetail.setAdapter(adapter);
        binding.rvChatDetail.setLayoutManager(new LinearLayoutManager(this));

        final String senderId = auth.getUid();
        binding.tvChatDetailUsername.setText("Friend's Group");

        database.getReference()
                .child("Group Chat")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshots) {
                        messageModels.clear();
                        for (DataSnapshot snapshot : snapshots.getChildren()) {
                            MessageModel model = snapshot.getValue(MessageModel.class);
                            messageModels.add(model);
                        }
                        adapter.notifyDataSetChanged();
                        if (justOpenedChat) {
                            scrollChatViewToBottom();
                            justOpenedChat = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.ivSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = binding.etUserMessage.getText().toString();
                final MessageModel messageModel = new MessageModel(senderId, message, new Date().getTime());
                binding.etUserMessage.setText("");

                database.getReference()
                        .child("Group Chat")
                        .push()
                        .setValue(messageModel)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
            }
        });
    }

    private void scrollChatViewToBottom() {
        final LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvChatDetail.getLayoutManager();
        final RecyclerView.Adapter adapter = binding.rvChatDetail.getAdapter();
        if (adapter == null)
            return;
        final int lastItemPosition = adapter.getItemCount() - 1;

        if (layoutManager == null)
            return;
        layoutManager.scrollToPositionWithOffset(lastItemPosition, 0);
        binding.rvChatDetail.post(new Runnable() {
            @Override
            public void run() {
                View target = layoutManager.findViewByPosition(lastItemPosition);
                if (target != null) {
                    int offset = binding.rvChatDetail.getMeasuredHeight() - target.getMeasuredHeight();
                    layoutManager.scrollToPositionWithOffset(lastItemPosition, offset);
                }
            }
        });
    }
}