package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class ResetPassActivity extends AppCompatActivity {
    MaterialEditText edtEmail;
    Button btnReset;

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cấp lại mật khẩu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        edtEmail = findViewById(R.id.edt_email);
        btnReset = findViewById(R.id.btn_reset);
        firebaseAuth =FirebaseAuth.getInstance();
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                if (email.equals("")){
                    Toast.makeText(ResetPassActivity.this,"Email trống",Toast.LENGTH_SHORT).show();

                }
                else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ResetPassActivity.this,"Kiểm tra trong email của bạn",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPassActivity.this,LoginActivity.class));

                            }else {
                                Toast.makeText(ResetPassActivity.this,"Lỗi",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}