package com.nkh.appchat.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.R;
import com.nkh.appchat.adapter.UserAdapter;
import com.nkh.appchat.model.User;

import java.util.ArrayList;
import java.util.List;

public class PostLikeActivity extends AppCompatActivity {
    String postId;
    FirebaseAuth auth;
    String myId;
    private Toolbar toolbar;
    private RecyclerView rvLike;
    private List<User> arrUsers;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_like);
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvLike = findViewById(R.id.rv_like);
        arrUsers = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes");
        reference.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String hisUid = "" + dataSnapshot.getRef().getKey();
                    getUser(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUser(final String hisUid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user.getId() != null && user.getId().equals(hisUid)) {
                        arrUsers.add(user);
                    }

                }
                adapter = new UserAdapter(PostLikeActivity.this, arrUsers);
                adapter.notifyDataSetChanged();
                rvLike.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}