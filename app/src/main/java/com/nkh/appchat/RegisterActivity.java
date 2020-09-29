package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private MaterialEditText userName, email, matkhau;
    private Button btnRegister;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        Context context;
        progressDialog = new ProgressDialog(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Đăng ký");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userName = findViewById(R.id.edt_use);
        email = findViewById(R.id.edt_email);
        matkhau = findViewById(R.id.edt_pass);
        btnRegister = findViewById(R.id.btn_regis);

        auth = FirebaseAuth.getInstance();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("hoan", "btnRegister");
                String txtUsername = userName.getText().toString();
                String txtEmail = email.getText().toString();
                String txtPassword = matkhau.getText().toString();
                if (TextUtils.isEmpty(txtUsername) || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)) {
                    Toast.makeText(RegisterActivity.this, "ALl filed are requied", Toast.LENGTH_SHORT).show();

                } else if (txtPassword.length() < 6) {

                    Toast.makeText(RegisterActivity.this, "password must be at least 6 characters ", Toast.LENGTH_SHORT).show();
                } else {
                    register(txtUsername, txtEmail, txtPassword);

                    Log.e("hoan", "btnRegister1");
                }

            }
        });
    }

    private void register(final String userName, String email, String password) {
        progressDialog.setTitle("Create New Account");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseAuth = auth.getCurrentUser();
                    assert firebaseAuth != null;
                    String userid = firebaseAuth.getUid();

                    Log.e("hoan", "btnRegister2");
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    String setStatus = "xin chào";
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("userName", userName);
                    hashMap.put("imageURL", "default");
                    hashMap.put("status", setStatus);
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Log.e("nkh", "btnRegister3");
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "you can't register with this email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}