package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupEditActivity extends AppCompatActivity {

    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_CODE = 200;
    private String[] storagePermissions;
    private Uri imgUri = null;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    private String groupId;
    private CircleImageView imgGroup;
    private EditText edtNameGr, edtStatusGr;
    private Button btnUpdateGr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        progressDialog = new ProgressDialog(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sửa group");
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

        imgGroup = findViewById(R.id.img_group);
        edtNameGr = findViewById(R.id.edt_name_group);
        edtStatusGr = findViewById(R.id.edt_status_group);
        btnUpdateGr = findViewById(R.id.btn_update_gr);
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        firebaseAuth = FirebaseAuth.getInstance();

        loadGroupInfo();
        imgGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStoragePermission()) {
                    requestStoragePermissions();
                } else {
                    pickFromGalley();
                }
            }
        });
        btnUpdateGr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUploadGroup();
            }
        });

    }

    private void startUploadGroup() {
        progressDialog.setTitle("Đang tạo");
        progressDialog.setMessage("Đơi chút...");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        final String groupTitle = edtNameGr.getText().toString().trim();
        final String groupDescription = edtStatusGr.getText().toString().trim();
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, "Tên trống", Toast.LENGTH_SHORT).show();
        }
        if (imgUri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();

            hashMap.put("groupTitle", groupTitle);
            hashMap.put("groupDescription", groupDescription);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
            reference.child(groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(GroupEditActivity.this, "Đã xong", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                }
            });
        } else {
            String timestamp = "" + System.currentTimeMillis();
            String filePathAndName = "Group_Imgs/" + "image" + timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!p_uriTask.isSuccessful()) ;
                    Uri p_downloadUri = p_uriTask.getResult();
                    if (p_uriTask.isSuccessful()) {
                        HashMap<String, Object> hashMap = new HashMap<>();

                        hashMap.put("groupTitle", groupTitle);
                        hashMap.put("groupDescription", groupDescription);
                        hashMap.put("groupIcon",""+ p_downloadUri);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                        reference.child(groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(GroupEditActivity.this, "Đã xong", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
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


                    edtNameGr.setText(groupTitle);
                    edtStatusGr.setText(groupDescription);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.profile_image).into(imgGroup);
                    } catch (Exception e) {
                        imgGroup.setImageResource(R.drawable.profile_image);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pickFromGalley() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            boolean storageAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (storageAccept) {
                pickFromGalley();
            } else {
                Toast.makeText(this, "Bị Chặn", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                imgUri = data.getData();
                imgGroup.setImageURI(imgUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}