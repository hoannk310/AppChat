package com.nkh.appchat.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.R;
import com.nkh.appchat.model.MessageGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MessageGroupAdapter extends RecyclerView.Adapter<MessageGroupAdapter.ViewHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private FirebaseAuth firebaseAuth;
    private Context context;
    private List<MessageGroup> arrGroupsChat;

    public MessageGroupAdapter(Context context, List<MessageGroup> arrGroupsChat) {
        this.context = context;
        this.arrGroupsChat = arrGroupsChat;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_left, parent, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MessageGroup messageGroup = arrGroupsChat.get(position);
        String message = messageGroup.getMessage();
        String senderUid = messageGroup.getSender();
        String messageType = messageGroup.getType();
        String timestamp = messageGroup.getTimestamp();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


        if (messageType.equals("text")){
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);


        }else {
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            try {
            Picasso.get().load(message).placeholder(R.drawable.profile_image).into(holder.messageIv);
            }catch (Exception e){
                holder.messageIv.setImageResource(R.drawable.profile_image);
            }

        }

        holder.timeTv.setText(dateTime);
        setUserName(messageGroup, holder);

    }

    private void setUserName(MessageGroup messageGroup, final ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("id").equalTo(messageGroup.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = "" + dataSnapshot.child("userName").getValue();
                    holder.nameTv.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return arrGroupsChat.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseAuth = FirebaseAuth.getInstance();
        if (arrGroupsChat.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, messageTv, timeTv;
        ImageView messageIv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.tv_name_grc);
            messageTv = itemView.findViewById(R.id.tv_mesage_grc);
            timeTv = itemView.findViewById(R.id.time_chat_gr);
            messageIv = itemView.findViewById(R.id.message_picture_group);

        }
    }
}
