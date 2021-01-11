package com.nkh.appchat.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.R;
import com.nkh.appchat.adapter.NotificationAdapter;
import com.nkh.appchat.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationActitvity extends AppCompatActivity {
    private RecyclerView rvNotifi;
    private FirebaseAuth firebaseAuth;
    private List<Notification> arrNotifications;
    private NotificationAdapter adapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_actitvity);


        toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Thông báo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvNotifi = findViewById(R.id.rv_notifi);
        firebaseAuth = FirebaseAuth.getInstance();
        getAllNotifications();
    }

    private void getAllNotifications() {
        arrNotifications = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrNotifications.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification model = dataSnapshot.getValue(Notification.class);
                    arrNotifications.add(model);
                }
                adapter = new NotificationAdapter(arrNotifications, NotificationActitvity.this);
                rvNotifi.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}