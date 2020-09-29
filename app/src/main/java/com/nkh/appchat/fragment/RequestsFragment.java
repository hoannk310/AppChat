package com.nkh.appchat.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.nkh.appchat.R;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {
    private static RequestsFragment INSTANCE;
    public static RequestsFragment getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE = new RequestsFragment();

        }return INSTANCE;
    }

    private RequestsFragment() {
    }
    private RecyclerView rvRequests;
    private DatabaseReference chatRequestRef, userRef,contactRef;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chatRequest");
        contactRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        rvRequests = view.findViewById(R.id.rv_request);
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(chatRequestRef.child(currentUserId), User.class).build();
        FirebaseRecyclerAdapter<User, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<User, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull User model) {
                holder.itemView.findViewById(R.id.btn_accept_friend).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.btn_cancel_friend).setVisibility(View.VISIBLE);
                final String listUserID = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();


                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String type = snapshot.getValue().toString();
                            if (type.equals("received")) {
                                userRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("imageURL")) {

                                            final String requestProfileImage = snapshot.child("imageURL").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.ciProfile);
                                        }
                                        final String requestUserName = snapshot.child("userName").getValue().toString();

                                        holder.tvName.setText(requestUserName);
                                        holder.tvStatus.setText("Yêu cầu kết bạn với bạn");
                                        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                    contactRef.child(currentUserId).child(listUserID).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                contactRef.child(listUserID).child(currentUserId).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            chatRequestRef.child(currentUserId).child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){

                                                                                        chatRequestRef.child(listUserID).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(),"Đã chấp nhận kết bạn",Toast.LENGTH_LONG).show();

                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                            }
                                        });
                                        holder.btnCanelFriend.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                chatRequestRef.child(currentUserId).child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){

                                                            chatRequestRef.child(listUserID).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(getContext(),"Đã hủy",Toast.LENGTH_LONG).show();

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else if (type.equals("sent")){
                                final Button requestSent = holder.itemView.findViewById(R.id.btn_accept_friend);
                                requestSent.setText("Hủy yêu cầu");
                                holder.itemView.findViewById(R.id.btn_cancel_friend).setVisibility(View.INVISIBLE);
                                userRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("imageURL")) {

                                            final String requestProfileImage = snapshot.child("imageURL").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.ciProfile);
                                        }
                                        final String requestUserName = snapshot.child("userName").getValue().toString();

                                        holder.tvName.setText(requestUserName);
                                        holder.tvStatus.setText("Bạn gửi lời mời kết bạn đến"+requestUserName);
                                        requestSent.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                chatRequestRef.child(currentUserId).child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            chatRequestRef.child(listUserID).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(getContext(), "Đã hủy", Toast.LENGTH_LONG).show();

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itiem_user, parent, false);
                return new RequestViewHolder(view);
            }
        };
        rvRequests.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvStatus;
        CircleImageView ciProfile;
        Button btnAccept, btnCanelFriend;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_find);
            tvStatus = itemView.findViewById(R.id.tv_status_find);
            btnAccept = itemView.findViewById(R.id.btn_accept_friend);
            btnCanelFriend = itemView.findViewById(R.id.btn_cancel_friend);
            ciProfile = itemView.findViewById(R.id.ci_users_profile);
        }
    }
}