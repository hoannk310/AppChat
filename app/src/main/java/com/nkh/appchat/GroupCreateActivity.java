package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupCreateActivity extends AppCompatActivity {

    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_CODE = 200;
    private String[] storagePermissions;
    private Uri imgUri = null;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    private CircleImageView imgGroup;
    private EditText edtNameGr, edtStatusGr;
    private Button btnCreateGr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tạo nhóm");
        progressDialog = new ProgressDialog(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgGroup = findViewById(R.id.img_group);
        edtNameGr = findViewById(R.id.edt_name_group);
        edtStatusGr = findViewById(R.id.edt_status_group);
        btnCreateGr = findViewById(R.id.btn_create_gr);
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        firebaseAuth = FirebaseAuth.getInstance();
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
        btnCreateGr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateGroup();
            }
        });
    }

    private void startCreateGroup() {
        progressDialog.setTitle("Đang tạo");
        progressDialog.setMessage("Đơi chút...");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        final String nameGroup = edtNameGr.getText().toString().trim();
        final String statusGroup = edtStatusGr.getText().toString().trim();
        if (TextUtils.isEmpty(nameGroup)) {
            Toast.makeText(this, "tên không để trổng", Toast.LENGTH_SHORT).show();
            return;
        }
        final String timeGroup = ""+ System.currentTimeMillis();
        if (imgUri == null) {
            createGroup("" + timeGroup, "" + nameGroup, "" + statusGroup,"" );
        }
        else {
            String fileNamePath ="Group_Imgs/"+"image"+timeGroup;
            StorageReference storageReference  = FirebaseStorage.getInstance().getReference(fileNamePath);
            storageReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> pUriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!pUriTask.isSuccessful());
                    Uri downloadUri = pUriTask.getResult();
                    if (pUriTask.isSuccessful()) {
                        createGroup("" + timeGroup, "" + nameGroup, "" + statusGroup, "" + downloadUri);
                    }
                }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
        }
    }

    private void createGroup(final String timeGroup, String groupTitle, String groupDescription, String groupIcon) {
        progressDialog.setTitle("Đang tạo");
        progressDialog.setMessage("Đơi chút...");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupId", "" + timeGroup);
        hashMap.put("groupTitle", "" + groupTitle);
        hashMap.put("groupDescription", "" + groupDescription);
        hashMap.put("groupIcon", "" + groupIcon);
        hashMap.put("timestamp", "" + timeGroup);
        hashMap.put("createBy", "" + firebaseAuth.getUid());
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(timeGroup).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               HashMap<String, String>hashMap1=new HashMap<>();
               hashMap1.put("uid",firebaseAuth.getUid());
               hashMap1.put("role","creator");
               hashMap1.put("timestamp",timeGroup);
               DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Groups");
               ref2.child(timeGroup).child("Participants").child(firebaseAuth.getUid()).setValue(hashMap1)
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       progressDialog.dismiss();
                       Toast.makeText(GroupCreateActivity.this, "Đã tạo", Toast.LENGTH_SHORT).show();
                       finish();
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       progressDialog.dismiss();
                   }
               });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            progressDialog.dismiss();
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