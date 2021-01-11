package com.nkh.appchat.groupchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nkh.appchat.R;
import com.nkh.appchat.adapter.MessageGroupAdapter;
import com.nkh.appchat.model.MessageGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class GroupChatsActivity extends AppCompatActivity {

    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private String[] storagePermission;
    private Uri imageUri = null;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private CircleImageView groupIconTv;
    private TextView groupTitleTv;
    private String groupId, myGroupRole = "";
    private ImageView attachBtn, btnSend, addFr, grOptions;
    private EditText messageEdt;
    private RecyclerView rvChatGr;
    private FirebaseAuth firebaseAuth;
    private List<MessageGroup> arrGroupChats;
    private MessageGroupAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chats);
        final Intent intent = getIntent();
        firebaseAuth = FirebaseAuth.getInstance();
        groupId = intent.getStringExtra("groupId");
        toolbar = findViewById(R.id.toolbar);
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
        groupIconTv = findViewById(R.id.icon_group);
        groupTitleTv = findViewById(R.id.tv_title_group);
        progressDialog = new ProgressDialog(this);
        attachBtn = findViewById(R.id.attach_btn);
        btnSend = findViewById(R.id.btn_send);
        messageEdt = findViewById(R.id.edt_mess_group);
        attachBtn = findViewById(R.id.attach_btn);
        rvChatGr = findViewById(R.id.rvChat);
        grOptions = findViewById(R.id.option);
        addFr = findViewById(R.id.add_member);

        addFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatsActivity.this, GroupParentAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };


        loadInfo();
        loadGroupMessage();
        loadMyGroupRole();


        grOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatsActivity.this, GroupInforActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = messageEdt.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GroupChatsActivity.this, "Tin nhắn trống", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);

                }
            }
        });
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageImportDialog();
            }
        });

    }

    private void showImageImportDialog() {
        String[] options = {"Ảnh", "Tệp", "PDF"};
        Context context;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickGallery();

                    }
                }
                if (which == 1) {

                }
                if (which == 1) {

                }
            }
        }).show();
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myGroupRole = "" + snapshot.child("role").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupMessage() {
        arrGroupChats = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrGroupChats.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageGroup messageGroup = dataSnapshot.getValue(MessageGroup.class);
                    arrGroupChats.add(messageGroup);
                }
                adapter = new MessageGroupAdapter(GroupChatsActivity.this, arrGroupChats);
                rvChatGr.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "text");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                messageEdt.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatsActivity.this, "Lỗi" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String groupTitle = "" + dataSnapshot.child("groupTitle").getValue();
                    String groupDescription = "" + dataSnapshot.child("groupDescription").getValue();
                    String groupIcon = "" + dataSnapshot.child("groupIcon").getValue();
                    String timestamp = "" + dataSnapshot.child("timestamp").getValue();
                    String createBy = "" + dataSnapshot.child("createBy").getValue();
                    groupTitleTv.setText(groupTitle);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.group_chat).into(groupIconTv);
                    } catch (Exception e) {
                        groupIconTv.setImageResource(R.drawable.group_chat);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                imageUri = data.getData();
                sendImageMessage();

            }
        }
    }

    private void sendImageMessage() {
        progressDialog.setTitle("Đang gửi");
        progressDialog.setMessage("Đơi chút...");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        String filenamePath = "ImageFile" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while ((!p_uriTask.isSuccessful()));
                Uri p_downloadUri = p_uriTask.getResult();
                if (p_uriTask.isSuccessful()){
                    String timestamp = "" + System.currentTimeMillis();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", "" + firebaseAuth.getUid());
                    hashMap.put("message", "" + p_downloadUri);
                    hashMap.put("timestamp", "" + timestamp);
                    hashMap.put("type", "" + "image");
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                    reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            messageEdt.setText("");
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GroupChatsActivity.this, "Lỗi" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (writeStorageAccepted) {
                    pickGallery();
                } else {
                    Toast.makeText(this, "chặn", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}