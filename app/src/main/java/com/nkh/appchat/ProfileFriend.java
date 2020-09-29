package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFriend extends AppCompatActivity {
    private CircleImageView cvFriends;
    private String userID, myUserId, currentState;
    private TextView tvNameFriend, tvStatusFriend;
    private Button btnSendFriend, btnCancelFriend, btnSendMes;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference, chatRequestReference, contactReference, notificationReference;
    private FirebaseAuth auth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_friend);
        cvFriends = findViewById(R.id.img_friend);
        tvNameFriend = findViewById(R.id.tv_name_friends);
        tvStatusFriend = findViewById(R.id.tv_status_friends);
        btnSendFriend = findViewById(R.id.btn_send);
        btnCancelFriend = findViewById(R.id.btn_cancel);
        btnSendMes = findViewById(R.id.btn_send_mes);
        btnSendMes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(ProfileFriend.this, ChatsActivity.class);
                chatIntent.putExtra("userid",userID);

                startActivity(chatIntent);
            }
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trang cá nhân");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        userID = intent.getStringExtra("visit_user_id");
        auth = FirebaseAuth.getInstance();
        myUserId = auth.getCurrentUser().getUid();
        currentState = "new";
        // myUserId = intent.getStringExtra("myUser");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        chatRequestReference = FirebaseDatabase.getInstance().getReference("chatRequest");
        contactReference = FirebaseDatabase.getInstance().getReference("contacts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                tvNameFriend.setText(user.getUserName());
                tvStatusFriend.setText(user.getStatus());
                Picasso.get().load(user.getImageURL()).placeholder(R.drawable.profile_image).into(cvFriends);
                managerChatRequest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void managerChatRequest() {
        chatRequestReference.child(myUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(userID)) {
                    String request_type = snapshot.child(userID).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        currentState = "request_type";
                        btnSendFriend.setText("Hủy kết bạn");

                    } else if (request_type.equals("received")) {
                        currentState = "request_received";
                        btnSendFriend.setText("đồng ý kết bạn");
                        btnCancelFriend.setVisibility(View.VISIBLE);
                        btnCancelFriend.setEnabled(true);
                        btnCancelFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelSendChatRequest();
                            }
                        });
                    }
                } else {
                    contactReference.child(myUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(userID)) {
                                currentState = "friends";
                                btnSendFriend.setText("xóa bạn");
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

        btnSendFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendFriend.setEnabled(false);
                if (currentState.equals("new")) {
                    sendChatRequest();
                }
                if (currentState.equals("request_type")) {
                    cancelSendChatRequest();
                }
                if (currentState.equals("request_received")) {
                    acceptChatRequest();
                }
                if (currentState.equals("friends")) {
                    removeFriends();
                }
            }
        });


    }

    private void removeFriends() {
        contactReference.child(myUserId).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactReference.child(userID).child(myUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                btnSendFriend.setEnabled(true);
                                currentState = "new";
                                btnSendFriend.setText("gửi lời mời kết bạn");
                                btnCancelFriend.setVisibility(View.INVISIBLE);
                                btnCancelFriend.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptChatRequest() {
        contactReference.child(myUserId).child(userID).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactReference.child(userID).child(myUserId).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                chatRequestReference.child(myUserId).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            chatRequestReference.child(userID).child(myUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        btnSendFriend.setEnabled(true);
                                                        currentState = "friends";
                                                        btnSendFriend.setText("xóa bạn");
                                                        btnCancelFriend.setVisibility(View.INVISIBLE);
                                                        btnCancelFriend.setEnabled(false);
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

    private void cancelSendChatRequest() {
        chatRequestReference.child(myUserId).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatRequestReference.child(userID).child(myUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                btnSendFriend.setEnabled(true);
                                currentState = "new";
                                btnSendFriend.setText("gửi lời mời kết bạn");
                                btnCancelFriend.setVisibility(View.INVISIBLE);
                                btnCancelFriend.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        chatRequestReference.child(myUserId).child(userID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatRequestReference.child(userID).child(myUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                HashMap<String, String> chatNotification = new HashMap<>();
                                chatNotification.put("from", myUserId);
                                chatNotification.put("type", "request");
                                btnSendFriend.setEnabled(true);
                                currentState = "request_type";
                                btnSendFriend.setText("Hủy kết bạn");


                            }
                        }
                    });
                }
            }
        });
    }
}