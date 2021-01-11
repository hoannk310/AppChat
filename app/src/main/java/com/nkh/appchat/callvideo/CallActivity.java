package com.nkh.appchat.callvideo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallActivity extends AppCompatActivity {
    private TextView nameContact;
    private ImageView profileImage, cancelCallBtn, makeCallBtn;
    private String receiverUserId = "", receiverUserImage = "", receiverUserName = "";
    private String senderUserId = "", senderUserImage = "", senderUserName = "", checked = "";
    private DatabaseReference userRef;
    private String callingID = "", ringingID = "";
    private FirebaseAuth firebaseAuth;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        firebaseAuth = FirebaseAuth.getInstance();
        nameContact = findViewById(R.id.tv_name_callig);
        profileImage = findViewById(R.id.img_calling);
        makeCallBtn = findViewById(R.id.make_call);
        cancelCallBtn = findViewById(R.id.cancel_call);
        senderUserId = firebaseAuth.getUid();
        receiverUserId = getIntent().getExtras().get("userid").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mediaPlayer = MediaPlayer.create(this, R.raw.ringing);

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked = "clicked";
                mediaPlayer.stop();
                cancelCall();

            }
        });
        makeCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                final HashMap<String, Object> callingPickUp = new HashMap<>();
                callingPickUp.put("picked", "picked");
                userRef.child(senderUserId).child("Ringing").updateChildren(callingPickUp).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete()) {
                            Intent intent = new Intent(CallActivity.this, VideoCallActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        getAndSetInfo();
    }

    private void cancelCall() {
        userRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("calling")) {
                    callingID = snapshot.child("calling").getValue().toString();
                    userRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                finish();
                                userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("ringing")) {
                    ringingID = snapshot.child("ringing").getValue().toString();
                    userRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                finish();
                                userRef.child(senderUserId).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAndSetInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(receiverUserId).exists()) {
                    receiverUserImage = snapshot.child(receiverUserId).child("imageURL").getValue().toString();
                    receiverUserName = snapshot.child(receiverUserId).child("userName").getValue().toString();
                    nameContact.setText(receiverUserName);
                    try {
                        Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                    } catch (Exception e) {
                        profileImage.setImageResource(R.drawable.profile_image);
                    }
                }
                if (snapshot.child(senderUserId).exists()) {
                    senderUserImage = snapshot.child(senderUserId).child("imageURL").getValue().toString();
                    senderUserName = snapshot.child(senderUserId).child("userName").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();
        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!checked.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing")) {

                    final HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("calling", receiverUserId);
                    userRef.child(senderUserId).child("Calling").updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                final HashMap<String, Object> hashMap1 = new HashMap<>();
                                hashMap1.put("ringing", senderUserId);
                                userRef.child(receiverUserId).child("Ringing").updateChildren(hashMap1);
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(senderUserId).hasChild("Ringing") && !snapshot.child(senderUserId).hasChild("Calling")) {
                    makeCallBtn.setVisibility(View.VISIBLE);
                }if(snapshot.child(receiverUserId).child("Ringing").hasChild("picked")){
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallActivity.this, VideoCallActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}