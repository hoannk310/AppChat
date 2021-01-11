package com.nkh.appchat.groupchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.MainActivity;
import com.nkh.appchat.R;
import com.nkh.appchat.adapter.AddPersonAdapter;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInforActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView groupIconTv;
    private TextView descriptionTv, createByTv, editGroupTv, addMemberTv, memberGroupsTv, leaveGroupTv,tvMedia;
    private RecyclerView rvGroupInfo;
    private String groupId, myGroupRole;
    private FirebaseAuth firebaseAuth;
    private ArrayList<User>arrUsers;
    private AddPersonAdapter adapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_infor);
        progressDialog = new ProgressDialog(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        groupId = getIntent().getStringExtra("groupId");

        groupIconTv = findViewById(R.id.group_icon);
        descriptionTv = findViewById(R.id.decription_group);
        createByTv = findViewById(R.id.create_gr);
        editGroupTv = findViewById(R.id.tv_edt_gr);
        tvMedia = findViewById(R.id.tv_media);
        addMemberTv = findViewById(R.id.tv_add_membergr);
        leaveGroupTv = findViewById(R.id.tv_out_gr);
        rvGroupInfo = findViewById(R.id.rv_group_info);
        memberGroupsTv = findViewById(R.id.tv_member);

        firebaseAuth = FirebaseAuth.getInstance();
        tvMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInforActivity.this, MediaGroupActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInforActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";
                if (myGroupRole.equals("creator")){
                    dialogTitle = "Xóa nhóm";
                    dialogDescription = "Bạn có chắc chắn muốn xóa";
                    positiveButtonTitle = "Xóa";
                }else {
                    dialogTitle = "Rời nhóm";
                    dialogDescription = "Bạn có chắc chắn muốn rời";
                    positiveButtonTitle = "Rời";
                }
                Context context;
                final AlertDialog.Builder builder = new AlertDialog.Builder(GroupInforActivity.this);
                builder.setTitle(dialogTitle).setMessage(dialogDescription).setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (myGroupRole.equals("creator")){
                            deletGroup();
                        }else{
                            leaveGroup();
                        }
                    }
                }).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
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
        addMemberTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInforActivity.this,GroupParentAddActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
        loadGroupInfo();
        loadMyGroupRole();

    }

    private void leaveGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(firebaseAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(GroupInforActivity.this, MainActivity.class));
            finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void deletGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              startActivity(new Intent(GroupInforActivity.this,MainActivity.class));
            finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myGroupRole = "" + dataSnapshot.child("role").getValue();
                    if (myGroupRole.equals("participant")) {
                        editGroupTv.setVisibility(View.GONE);
                        addMemberTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Rời nhóm");
                    }else if (myGroupRole.equals("admin")){
                        editGroupTv.setVisibility(View.GONE);
                        addMemberTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Rời nhóm");
                    }else if (myGroupRole.equals("creator")){
                        editGroupTv.setVisibility(View.VISIBLE);
                        addMemberTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Xóa nhóm");
                    }
                }
                loadMember();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMember() {
        arrUsers = new ArrayList<>();
        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrUsers.clear();
              for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                  String uid = ""+dataSnapshot.child("uid").getValue();
                  DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                  ref.orderByChild("id").equalTo(uid).addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {
                          for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                              User user = dataSnapshot.getValue(User.class);
                              arrUsers.add(user);
                          }
                          adapter = new AddPersonAdapter(GroupInforActivity.this,arrUsers,groupId,myGroupRole);
                          memberGroupsTv.setText("Thành viên ("+arrUsers.size()+")");
                          rvGroupInfo.setAdapter(adapter);
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

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String groupId = "" + dataSnapshot.child("groupId");
                    String groupTitle = "" + dataSnapshot.child("groupTitle").getValue();
                    String groupDescription = "" + dataSnapshot.child("groupDescription").getValue();
                    String groupIcon = "" + dataSnapshot.child("groupIcon").getValue();
                    String createBy = "" + dataSnapshot.child("createBy").getValue();
                    String timestamp = "" + dataSnapshot.child("timestamp").getValue();
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                    toolbar.setTitle(groupTitle);
                    descriptionTv.setText(groupDescription);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.profile_image).into(groupIconTv);
                    }catch (Exception e){
                        groupIconTv.setImageResource(R.drawable.profile_image);
                    }

                    loadCreatorInfo(dateTime, createBy);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCreatorInfo(final String dateTime, String createBy) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("id").equalTo(createBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = "" + dataSnapshot.child("userName").getValue();
                    createByTv.setText("Tạo bởi " + name + " on " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}