package com.nkh.appchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.GroupChatsActivity;
import com.nkh.appchat.R;
import com.nkh.appchat.model.Group;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private Context context;
    private List<Group>groupChatList;

    public GroupAdapter(Context context, List<Group> groupChatList) {
        this.context = context;
        this.groupChatList = groupChatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_group_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groupChatList.get(position);
        final String groupId = group.getGroupId();
        String groupIcon  = group.getGroupIcon();
        String groupTitle  = group.getGroupTitle();
        holder.groupTitle.setText(groupTitle);

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        loadLastMessage(group,holder);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.group_chat).into(holder.groupImage);
        }
        catch (Exception e){
            holder.groupImage.setImageResource(R.drawable.group_chat);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(context, GroupChatsActivity.class);
            intent.putExtra("groupId",groupId);
            context.startActivity(intent);

            }
        });

    }

    private void loadLastMessage(Group group, final ViewHolder holder) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(group.getGroupId()).child("Messages").limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String message = ""+dataSnapshot.child("message").getValue();
                    String timestamp = ""+dataSnapshot.child("timestamp").getValue();
                    final String sender = ""+dataSnapshot.child("sender").getValue();
                    final String messageType = ""+dataSnapshot.child("type").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    if (messageType.equals("image")){
                        holder.messageTv.setText("đã gửi tin nhắn ảnh");
                    }
                    holder.messageTv.setText(message);
                    holder.timeTv.setText(dateTime);
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                    reference1.orderByChild("id").equalTo(sender).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                String name = ""+dataSnapshot1.child("userName").getValue();
                                holder.nameTv.setText(name);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView groupImage;
        TextView groupTitle,nameTv,messageTv,timeTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImage =itemView.findViewById(R.id.img_groupicon);
            groupTitle =itemView.findViewById(R.id.tv_name_gr);
            nameTv =itemView.findViewById(R.id.tv_nameuser);
            messageTv =itemView.findViewById(R.id.tv_masage);
            timeTv =itemView.findViewById(R.id.tv_time);



        }
    }
}
