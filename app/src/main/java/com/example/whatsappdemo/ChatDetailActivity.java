package com.example.whatsappdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.whatsappdemo.adapters.ChatAdapter;
import com.example.whatsappdemo.databinding.ActivityChatDetailBinding;
import com.example.whatsappdemo.models.MessageModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    Boolean scrolledUp = false;
    Boolean justOpenedChat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        final String senderId = auth.getUid();
        String receiverId = getIntent().getStringExtra("userId");
        String username = getIntent().getStringExtra("username");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.tvChatDetailUsername.setText(username);
        Picasso.get().load(profilePic).placeholder(R.drawable.user).into(binding.ivProfileImage);

        binding.ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //TODO Scroll to bottom when user opens keyboard if the user is at the bottom message
//        binding.rvChatDetail.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                Log.i("SCROLL_INFO", "onLayoutChange called");
//                if (!scrolledUp) {
//                    Log.i("SCROLL_INFO", "will do scroll to bottom");
//                    scrollChatViewToBottom();
//                }
//            }
//        });

        binding.rvChatDetail.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View lastView = binding.rvChatDetail.getChildAt(binding.rvChatDetail.getChildCount() - 1);
                scrolledUp = lastView.getBottom() != binding.rvChatDetail.getHeight();
            }
        });

        final ArrayList<MessageModel> messageList = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageList, this, receiverId);

        binding.rvChatDetail.setAdapter(chatAdapter);
        binding.rvChatDetail.setLayoutManager(new LinearLayoutManager(this));

        final String senderRoom = senderId + receiverId;
        final String receiverRoom = receiverId + senderId;

        database.getReference()
                .child("Chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshots) {
                        messageList.clear(); //TODO - Not that much efficient
                        for (DataSnapshot snapshot : snapshots.getChildren()) {
                            MessageModel messageModel = snapshot.getValue(MessageModel.class);
                            messageModel.setMessageId(snapshot.getKey());
                            messageList.add(messageModel);
                        }
                        chatAdapter.notifyItemInserted(messageList.size() - 1);
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
                String message = binding.etUserMessage.getText().toString();
                final MessageModel messageModel = new MessageModel(senderId, message, new Date().getTime());
                binding.etUserMessage.setText("");

                database.getReference()
                        .child("Chats")
                        .child(senderRoom)
                        .push()
                        .setValue(messageModel)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference()
                                        .child("Chats")
                                        .child(receiverRoom)
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