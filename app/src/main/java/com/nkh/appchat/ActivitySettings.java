package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.nkh.appchat.adapter.PostAdapter;
import com.nkh.appchat.model.Post;
import com.nkh.appchat.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivitySettings extends AppCompatActivity {
    private Button btnUpdate;
    private EditText edtUserName;
    private EditText edtUserStatus;
    private CircleImageView imgProfile;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private Uri imageUri;
    private FirebaseAuth auth;
    private String currentUserId;
    private StorageTask uploadTask;
    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;

    private RecyclerView rvSttPr;
    private List<Post> arrPosts;
    private PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        btnUpdate = findViewById(R.id.btn_update);
        edtUserName = findViewById(R.id.edt_use_profile);
        edtUserStatus = findViewById(R.id.edt_status_profile);
        imgProfile = findViewById(R.id.profile_img);
        rvSttPr = findViewById(R.id.rv_stt_pr);
        arrPosts = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();


        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                edtUserName.setText(user.getUserName());
                edtUserStatus.setText(user.getStatus());
                if (user.getImageURL().equals("default")) {
                    imgProfile.setImageResource(R.drawable.profile_image);
                } else {
                    try {
                        Glide.with(ActivitySettings.this).load(user.getImageURL()).into(imgProfile);
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_REQUEST);
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upLoadProfile();
            }
        });

        loatMyPost();
    }

    private void loatMyPost() {
        Context context;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        rvSttPr.setLayoutManager(layoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(currentUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrPosts.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    arrPosts.add(post);
                    adapter = new PostAdapter(arrPosts, ActivitySettings.this);
                    rvSttPr.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void upLoadProfile() {
        final ProgressDialog progressDialog = new ProgressDialog(ActivitySettings.this);
        progressDialog.setMessage("uploading...");
        progressDialog.show();
        String setUserName = edtUserName.getText().toString();
        String setStatus = edtUserStatus.getText().toString();
        if (TextUtils.isEmpty(setStatus)) {
            setStatus = "xin chào";
        }
        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Tên Không được để trống", Toast.LENGTH_SHORT).show();
        } else {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
            HashMap<String, Object> profileHash = new HashMap<>();
            profileHash.put("userName", setUserName);
            profileHash.put("status", setStatus);
            reference.updateChildren(profileHash).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(ActivitySettings.this, "đã cập nhập", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = (ActivitySettings.this).getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        Context context;
        final ProgressDialog progressDialog = new ProgressDialog(ActivitySettings.this);
        progressDialog.setMessage("uploading...");
        progressDialog.show();
        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downUri = task.getResult();
                        String mUri = downUri.toString();
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        reference.updateChildren(map);
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(ActivitySettings.this, "Failed", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ActivitySettings.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(ActivitySettings.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ActivitySettings.this, "Upload is preogress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }
}