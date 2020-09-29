package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.adapter.AddPersonAdapter;
import com.nkh.appchat.model.User;

import java.util.ArrayList;
import java.util.List;

public class GroupParentAddActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private RecyclerView rvAddMember;
    private FirebaseAuth firebaseAuth;
    private String groupId;
    private String myGroupRole, myId, hisId;
    private ArrayList<User> arrUsers;
    private AddPersonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_parent_add);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Thêm thành viên");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rvAddMember = findViewById(R.id.rv_add_member);
        firebaseAuth = FirebaseAuth.getInstance();
        groupId = getIntent().getStringExtra("groupId");
        myId = firebaseAuth.getUid();
        Log.e("hoan", myId);
        loadGroupInfo();


    }

    private void getAllUsers() {
        arrUsers = new ArrayList<>();

        final List<String> id = new ArrayList<>();
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("contacts");
        ref1.child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String hisId = dataSnapshot.getKey();
                    id.add(hisId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (!myId.equals(user.getId())) {
                        for (int i = 0; i < id.size(); i++) {
                            if (user.getId().equals(id.get(i))){
                                arrUsers.add(user);
                            }

                        }

                    }
                }
                adapter = new AddPersonAdapter(GroupParentAddActivity.this, arrUsers, "" + groupId, "" + myGroupRole);
                rvAddMember.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadGroupInfo() {
        final DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String groupId = "" + dataSnapshot.child("groupId").getValue();
                    final String groupTitle = "" + dataSnapshot.child("groupTitle").getValue();
                    String groupDescription = "" + dataSnapshot.child("groupDescription").getValue();
                    String groupIcon = "" + dataSnapshot.child("groupIcon").getValue();
                    String timestamp = "" + dataSnapshot.child("timestamp").getValue();
                    String createdBy = "" + dataSnapshot.child("createBy").getValue();
                    toolbar.setTitle("Thêm thành viên");
                    reference1.child(groupId).child("Participants").child(myId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                myGroupRole = "" + snapshot.child("role").getValue();
                                Log.d("hoan", myGroupRole);
                                toolbar.setTitle(groupTitle + "(" + myGroupRole + ")");
                                getAllUsers();

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
}