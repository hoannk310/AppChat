package com.nkh.appchat.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.nkh.appchat.R;
import com.nkh.appchat.adapter.CommentAdapter;
import com.nkh.appchat.model.Comment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    boolean mProcessComment = false;
    boolean mProcessLike = false;


    String myUid, myEmail, myName, myDp, postId, pLikes, pImage, hisDp, hisName, hisId;

    private CircleImageView ciAvatarCmt;
    private ImageView imgSendCmt;
    private EditText edtSendCmt;


    CircleImageView imgProfile;
    ImageView imgStt, imgMore;
    TextView tvName, tvTime, tvStatus, tvNumLikes, tvLikes, tvNumComments, tvShares;
    LinearLayout profileLayout;
    RecyclerView rvCmtList;
    List<Comment> arrComments;
    CommentAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bình luận");
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


        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myUid = firebaseUser.getUid();
        myEmail = firebaseUser.getEmail();

        ciAvatarCmt = findViewById(R.id.ciAvatar);
        imgSendCmt = findViewById(R.id.btn_sendcmt);
        edtSendCmt = findViewById(R.id.edt_cmt);
        imgProfile = findViewById(R.id.ci_stt_profile);
        imgStt = findViewById(R.id.img_stt);
        imgMore = findViewById(R.id.btn_more_profile);
        tvName = findViewById(R.id.tv_name_stt);
        tvTime = findViewById(R.id.tv_time_stt);
        tvStatus = findViewById(R.id.tv_stt);
        tvNumLikes = findViewById(R.id.tv_num_like);

        tvNumComments = findViewById(R.id.tv_num_cmt);
        tvNumLikes = findViewById(R.id.tv_num_like);
        tvLikes = findViewById(R.id.tv_like);
        tvShares = findViewById(R.id.tv_share);
        profileLayout = findViewById(R.id.layou_profile);
        rvCmtList = findViewById(R.id.rv_cmt);


        loadPostInfor();
        loadUserInfor();
        setLike();
        loadComment();

        imgSendCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();

            }
        });
        tvLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
        tvNumLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, PostLikeActivity.class);
                intent.putExtra("postId", postId);
                startActivity(intent);
            }
        });
    }

    private void loadComment() {
        Context context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvCmtList.setLayoutManager(linearLayoutManager);
        arrComments = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrComments.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    arrComments.add(comment);
                    adapter = new CommentAdapter(arrComments, getApplicationContext(), myUid, postId);
                    rvCmtList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, imgMore, Gravity.END);
        if (hisId.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Sửa");
        }


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    beginDelete();
                } else if (id == 1) {
                    Intent intent = new Intent(PostDetailActivity.this, AddStatusActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if (pImage.equals("noImage")) {
            deleteWithoutImag();
        } else {
            deleteWithImg();
        }
    }

    private void deleteWithImg() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang xóa...");
        Query fqQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fqQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteWithoutImag() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang xóa...");
        StorageReference pic = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        pic.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fqQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fqQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            dataSnapshot.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLike() {
        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)) {
                    tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_24, 0, 0, 0);
                    tvLikes.setText("Bỏ thích");
                } else {
                    tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_244, 0, 0, 0);
                    tvLikes.setText("Thích");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {

        mProcessLike = true;
        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike) {
                    if (snapshot.child(postId).hasChild(myUid)) {
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) - 1));
                        likeRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;
                        tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_244, 0, 0, 0);
                        tvLikes.setText("Thích");
                    } else {
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) + 1));
                        likeRef.child(postId).child(myUid).setValue("Bỏ thích");
                        mProcessLike = false;

                        tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_24, 0, 0, 0);
                        tvLikes.setText("Bỏ thích");
                        addToHisNotifications("" + hisId, "" + postId, "Thích bài đăng của bạn");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void postComment() {
        final String comment = edtSendCmt.getText().toString().trim();

        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(PostDetailActivity.this, "Nội dung trống", Toast.LENGTH_SHORT).show();
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", timestamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        reference.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                edtSendCmt.setText("");
                updateCommentCount();
                addToHisNotifications("" + hisId, "" + postId, "Bình luận bài viết của bạn");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostDetailActivity.this, "Chưa gửi được", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateCommentCount() {
        mProcessComment = true;
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment) {
                    String comments = "" + snapshot.child("pComments").getValue();
                    int newCommentCount = Integer.parseInt(comments) + 1;
                    reference.child("pComments").setValue("" + newCommentCount);
                    mProcessComment = false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfor() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("id").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myName = "" + dataSnapshot.child("userName").getValue();
                    myDp = "" + dataSnapshot.child("imageURL").getValue();
                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.profile_image).into(imgStt);
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfor() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(postId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String pDescr = "" + dataSnapshot.child("pDescr").getValue();
                    pLikes = "" + dataSnapshot.child("pLikes").getValue();
                    String pTimestamp = "" + dataSnapshot.child("pTime").getValue();
                    pImage = "" + dataSnapshot.child("pImage").getValue();
                    hisDp = "" + dataSnapshot.child("uDp").getValue();
                    hisId = "" + dataSnapshot.child("uid").getValue();
                    String uEmail = "" + dataSnapshot.child("uEmail").getValue();
                    hisName = "" + dataSnapshot.child("uName").getValue();
                    String cmtCout = "" + dataSnapshot.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimestamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    tvStatus.setText(pDescr);
                    tvNumLikes.setText(pLikes + " Thích");
                    tvTime.setText(pTime);
                    tvName.setText(hisName);
                    tvNumComments.setText(cmtCout + " Bình luận");

                    if (pImage.equals("noImage")) {
                        imgStt.setVisibility(View.GONE);
                    } else {
                        imgStt.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).placeholder(R.drawable.profile_image).into(imgStt);
                        } catch (Exception e) {

                        }
                    }

                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.profile_image).into(imgStt);
                    } catch (Exception e) {
                        Picasso.get().load(hisDp).into(imgStt);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addToHisNotifications(String hisUid, String pId, String notification) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}