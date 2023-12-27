package com.example.whatsappdemo.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappdemo.R;
import com.example.whatsappdemo.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> messagesList;
    Context context;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    FirebaseAuth auth;
    String senderId;
    String receiverId;

    public ChatAdapter(ArrayList<MessageModel> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
        auth = FirebaseAuth.getInstance();
        senderId = auth.getUid();
    }

    public ChatAdapter(ArrayList<MessageModel> messagesList, Context context, String receiverId) {
        this.messagesList = messagesList;
        this.context = context;
        this.receiverId = receiverId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = messagesList.get(position);
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        String timeText = formatter.format(new Date(messageModel.getTimestamp()));

        if (holder.getClass() == SenderViewHolder.class) {
            ((SenderViewHolder)holder).senderMessageView.setText(messageModel.getMessage());
            ((SenderViewHolder)holder).senderTimeView.setText(timeText);
        } else {
            ((ReceiverViewHolder)holder).receiverMessageView.setText(messageModel.getMessage());
            ((ReceiverViewHolder)holder).receiverTimeView.setText(timeText);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = senderId + receiverId;
                                database.getReference()
                                        .child("Chats")
                                        .child(senderRoom)
                                        .child(messageModel.getMessageId())
                                        .setValue(null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).getuId().equals(senderId)) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMessageView, receiverTimeView;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMessageView = itemView.findViewById(R.id.tvReceiverMessage);
            receiverTimeView = itemView.findViewById(R.id.tvRecevierMessageTime);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMessageView, senderTimeView;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageView = itemView.findViewById(R.id.tvSenderMessage);
            senderTimeView = itemView.findViewById(R.id.tvSenderMessageTime);
        }
    }

}
