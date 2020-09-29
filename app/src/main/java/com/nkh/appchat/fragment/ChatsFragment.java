package com.nkh.appchat.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.ChatsActivity;
import com.nkh.appchat.R;
import com.nkh.appchat.model.Group;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private static ChatsFragment INSTANCE;

    public static ChatsFragment getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new ChatsFragment();

        }
        return INSTANCE;
    }


    private RecyclerView rvChatList;
    private DatabaseReference chatListRef, userRef;
    private FirebaseAuth auth;
    private String retImage = "default";
    private String currentUserId;

    private ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatListRef = FirebaseDatabase.getInstance().getReference().child("Message").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        rvChatList = view.findViewById(R.id.rv_chat_list);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(chatListRef, User.class).build();

        final FirebaseRecyclerAdapter<User, ChatListViewHolder> adapter = new FirebaseRecyclerAdapter<User, ChatListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatListViewHolder holder, int position, @NonNull User model) {
                final String userId = getRef(position).getKey();

                userRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild("imageURL")) {
                                retImage = snapshot.child("imageURL").getValue().toString();
                                Picasso.get().load(retImage).placeholder(R.drawable.profile_image).into(holder.ciProfile);
                            }
                            if (snapshot.child("userState").hasChild("state")) {
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                if (state.equals("online")) {
                                    holder.imgOnline.setVisibility(View.VISIBLE);
                                    holder.tvStatus.setText("Online");
                                } else if (state.equals("offline")) {
                                    holder.imgOnline.setVisibility(View.INVISIBLE);
                                    holder.tvStatus.setText("offline");
                                }
                            } else {
                                holder.imgOnline.setVisibility(View.INVISIBLE);
                                holder.tvStatus.setText("offline");

                            }
                            final String retName = snapshot.child("userName").getValue().toString();
                            final String retStatus = snapshot.child("status").getValue().toString();
                            holder.tvUserName.setText(retName);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatsActivity.class);
                                    chatIntent.putExtra("userid", userId);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setNegativeButton("Xóa", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setMessage("Bạn có chắc chắn muốn xóa");
                                        builder.setNegativeButton("Có", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Task<Void> chatReference = FirebaseDatabase.getInstance().getReference().child("Message").child(currentUserId).child(userId).removeValue();
                                                Task<Void> chatReference2 = FirebaseDatabase.getInstance().getReference().child("Message").child(userId).child(currentUserId).removeValue();
                                                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
                                                reference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                            String form = dataSnapshot.child("from").getValue().toString();
                                                            String to = dataSnapshot.child("to").getValue().toString();
                                                            if (form.equals(currentUserId) && to.equals(userId) ||
                                                                    form.equals(currentUserId) && to.equals(userId)) {
                                                                String idMes = dataSnapshot.child("messageID").getValue().toString();
                                                                reference.child(idMes).removeValue();
                                                            }

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.setPositiveButton("Không", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                        Button button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                                        Button button2 = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                        button.setTextColor(Color.BLACK);
                                        button2.setTextColor(Color.BLACK);
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                Button button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                                button.setTextColor(Color.BLACK);
                                alertDialog.getWindow();
                                return false;
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
                return new ChatListViewHolder(view);
            }
        };
        rvChatList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }


    public static class ChatListViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvStatus;
        CircleImageView ciProfile;
        ImageView imgOnline;


        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_find);
            tvStatus = itemView.findViewById(R.id.tv_status_find);
            ciProfile = itemView.findViewById(R.id.ci_users_profile);
            imgOnline = itemView.findViewById(R.id.img_online);
        }
    }

}