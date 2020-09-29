package com.nkh.appchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.R;
import com.nkh.appchat.model.Messages;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private List<Messages> arrMessages;
    private Context context;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    public MessagesAdapter(List<Messages> arrMessages) {
        this.arrMessages = arrMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout, parent, false);
        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String messageSenderID = auth.getCurrentUser().getUid();
        Messages messages = arrMessages.get(position);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("imageURL")) {
                    String receiverImage = snapshot.child("imageURL").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiveCircleImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.linearLayoutSend.setVisibility(View.GONE);
        holder.linearLayoutRece.setVisibility(View.GONE);
        holder.receiveMessages.setVisibility(View.GONE);
        holder.tvTimeReceive.setVisibility(View.GONE);
        holder.tvTimeSend.setVisibility(View.GONE);
        holder.receiveCircleImageView.setVisibility(View.GONE);
        holder.sendMessages.setVisibility(View.GONE);
        holder.chatSeen.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        switch (fromMessageType) {
            case "text":
                if (fromUserID.equals(messageSenderID)) {
                    holder.sendMessages.setVisibility(View.VISIBLE);
                    holder.linearLayoutSend.setVisibility(View.VISIBLE);
                    holder.tvTimeSend.setVisibility(View.VISIBLE);
//                holder.chatSeen.setVisibility(View.VISIBLE);
                    holder.linearLayoutSend.setBackgroundResource(R.drawable.custum_mymesage);
                    holder.sendMessages.setTextColor(Color.BLACK);
                    holder.sendMessages.setText(messages.getMessage());
                    holder.tvTimeSend.setText(messages.getTime() + " " + messages.getDate());

//                if (position == arrMessages.size() - 1) {
//                    if (messages.isSeen()) {
//                        holder.chatSeen.setText("Đã xem");
//                    } else {
//                        holder.chatSeen.setText("Đã gửi");
//                    }
//
//                }else {
//                    holder.chatSeen.setVisibility(View.GONE);
//                }
//                if (position==arrMessages.size()-1){
//                    if (messages.getIsSent().equals("true")){
//                        holder.chatSeen.setText("Đã gửi");
//                    }else {
//                        holder.chatSeen.setText("Đang gửi");
//                    }
//                }else {
//                    holder.chatSeen.setVisibility(View.GONE);
                } else {

                    holder.linearLayoutRece.setVisibility(View.VISIBLE);
                    holder.receiveCircleImageView.setVisibility(View.VISIBLE);
                    holder.receiveMessages.setVisibility(View.VISIBLE);
                    holder.linearLayoutSend.setVisibility(View.INVISIBLE);
                    holder.tvTimeReceive.setVisibility(View.VISIBLE);
                    holder.tvTimeSend.setVisibility(View.INVISIBLE);
                    holder.sendMessages.setVisibility(View.INVISIBLE);
                    holder.linearLayoutRece.setBackgroundResource(R.drawable.custom_mesage);
                    holder.receiveMessages.setTextColor(Color.BLACK);
                    holder.receiveMessages.setText(messages.getMessage());
                    holder.tvTimeReceive.setText(messages.getTime() + " " + messages.getDate());
                }
                break;
            case "image":
                if (fromUserID.equals(messageSenderID)) {
                    holder.linearLayoutSend.setVisibility(View.VISIBLE);
                    holder.messageSenderPicture.setVisibility(View.VISIBLE);
                    holder.tvTimeSend.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
                    holder.tvTimeSend.setText(messages.getTime() + " " + messages.getDate());
                } else {
                    holder.linearLayoutRece.setVisibility(View.VISIBLE);
                    holder.receiveCircleImageView.setVisibility(View.VISIBLE);
                    holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    holder.tvTimeReceive.setVisibility(View.VISIBLE);
                    holder.tvTimeSend.setVisibility(View.INVISIBLE);
                    holder.linearLayoutSend.setVisibility(View.INVISIBLE);
                    Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
                    holder.tvTimeSend.setText(messages.getTime() + " " + messages.getDate());
                }

                break;
            case "pdf":
                if (fromUserID.equals(messageSenderID)) {
                    holder.linearLayoutSend.setVisibility(View.VISIBLE);
                    holder.messageSenderPicture.setVisibility(View.VISIBLE);
                    holder.messageSenderPicture.setBackgroundResource(R.drawable.pdf);
                    holder.tvTimeSend.setVisibility(View.VISIBLE);
                    holder.tvTimeSend.setText(messages.getTime() + " " + messages.getDate());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(arrMessages.get(position).getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                } else {
                    holder.linearLayoutRece.setVisibility(View.VISIBLE);
                    holder.receiveCircleImageView.setVisibility(View.VISIBLE);
                    holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    holder.messageReceiverPicture.setBackgroundResource(R.drawable.pdf);
                    holder.tvTimeReceive.setVisibility(View.VISIBLE);
                    holder.tvTimeSend.setVisibility(View.INVISIBLE);
                    holder.tvTimeReceive.setText(messages.getTime() + " " + messages.getDate());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(arrMessages.get(position).getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                }
                break;
            default:
                if (fromUserID.equals(messageSenderID)) {
                    holder.linearLayoutSend.setVisibility(View.VISIBLE);
                    holder.messageSenderPicture.setVisibility(View.VISIBLE);
                    holder.messageSenderPicture.setBackgroundResource(R.drawable.word);
                    holder.tvTimeSend.setVisibility(View.VISIBLE);
                    holder.tvTimeSend.setText(messages.getTime() + " " + messages.getDate());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(arrMessages.get(position).getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                } else {
                    holder.linearLayoutRece.setVisibility(View.VISIBLE);
                    holder.receiveCircleImageView.setVisibility(View.VISIBLE);
                    holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                    holder.tvTimeReceive.setText(messages.getTime() + " " + messages.getDate());
                    holder.messageReceiverPicture.setBackgroundResource(R.drawable.word);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(arrMessages.get(position).getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                }

                break;
        }

    }

    @Override
    public int getItemCount() {
        return arrMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sendMessages, receiveMessages, chatSeen, tvTimeSend, tvTimeReceive;
        LinearLayout linearLayoutRece, linearLayoutSend;
        ImageView messageSenderPicture, messageReceiverPicture;
        CircleImageView receiveCircleImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            receiveCircleImageView = itemView.findViewById(R.id.ci_message_profile);
            sendMessages = itemView.findViewById(R.id.sender_message_tv);
            linearLayoutRece = itemView.findViewById(R.id.linear_layout_receiver);
            linearLayoutSend = itemView.findViewById(R.id.linear_layout_sender);
            sendMessages = itemView.findViewById(R.id.sender_message_tv);
            receiveMessages = itemView.findViewById(R.id.receiver_message_tv);
            messageSenderPicture = itemView.findViewById(R.id.sender_img_chat);
            messageReceiverPicture = itemView.findViewById(R.id.receiver_img_chat);
            chatSeen = itemView.findViewById(R.id.tv_seen);
            tvTimeSend = itemView.findViewById(R.id.tv_time_sender);
            tvTimeReceive = itemView.findViewById(R.id.tv_time_reve);

        }
    }
}
