package com.nkh.appchat.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.R;
import com.nkh.appchat.adapter.MediaAdapter;
import com.nkh.appchat.model.Image;

import java.util.ArrayList;
import java.util.List;

public class MediaActivity extends AppCompatActivity {
    private String senderId, receiverId;
    private Toolbar toolbar;
    private RecyclerView rvMedia;
    private MediaAdapter adapter;
    private List<Image> arrImages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);


        senderId = getIntent().getStringExtra("visit_sender_id");
        receiverId = getIntent().getStringExtra("visit_receiver_id");
        toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kho ảnh");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvMedia = findViewById(R.id.rv_media);
        rvMedia.setLayoutManager(new GridLayoutManager(this, 2));
        loadMedia();

    }

    private void loadMedia() {
        arrImages = new ArrayList<>();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d("hoan", String.valueOf(dataSnapshot));
                    String idSend = dataSnapshot.child("from").getValue().toString();
                    String idReceiver = dataSnapshot.child("to").getValue().toString();
                    String type = dataSnapshot.child("type").getValue().toString();
                    Log.d("hoan", idSend);
                    Log.d("hoan", idReceiver);
                    Log.d("hoan", type);

                    if (idSend.equals(senderId) && idReceiver.equals(receiverId) || idSend.equals(receiverId) && idReceiver.equals(senderId)) {
                        if (type.equals("image")) {
                            String image = dataSnapshot.child("message").getValue().toString();
                            Log.d("hoan", image);
                            arrImages.add(new Image(image));
                            Log.d("hoan", String.valueOf(arrImages));

                        }

                    }
                }
                adapter = new MediaAdapter(MediaActivity.this, arrImages);
                rvMedia.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}