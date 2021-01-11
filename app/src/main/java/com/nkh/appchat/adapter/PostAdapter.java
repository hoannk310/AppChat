package com.nkh.appchat.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nkh.appchat.ActivitySettings;
import com.nkh.appchat.post.AddStatusActivity;
import com.nkh.appchat.post.PostDetailActivity;
import com.nkh.appchat.post.PostLikeActivity;
import com.nkh.appchat.post.ProfileFriend;
import com.nkh.appchat.R;
import com.nkh.appchat.model.Post;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> arrPost;
    private Context context;
    private String myUid;
    private DatabaseReference likeRef;
    private DatabaseReference postsRef;
    boolean mProcessLike = false;

    public PostAdapter(List<Post> arrPost, Context context) {
        this.arrPost = arrPost;
        this.context = context;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_posts, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Post post = arrPost.get(position);
        final String uid = arrPost.get(position).getUid();
        String uEmail = arrPost.get(position).getuEmail();
        String uName = arrPost.get(position).getuName();
        String uDp = arrPost.get(position).getuDp();
        final String pId = arrPost.get(position).getpId();
        String pDesc = arrPost.get(position).getpDescr();
        final String pImage = arrPost.get(position).getpImage();
        String pTimeStamp = arrPost.get(position).getpTime();
        String pLikes = arrPost.get(position).getpLikes();
        String pComments = arrPost.get(position).getpComments();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        holder.tvTime.setText(pTime);
        holder.tvStatus.setText(pDesc);
        holder.tvNumLikes.setText(pLikes + " Thích");
        holder.tvNumComments.setText(pComments + " Bình luận");

        setLikes(holder, pId);
        setUser(holder, post);
        if (pImage.equals("noImage")) {
            holder.imgStt.setVisibility(View.GONE);
        } else {
            holder.imgStt.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).placeholder(R.drawable.profile_image).into(holder.imgStt);
            } catch (Exception e) {

            }
        }
        holder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.imgMore, uid, myUid, pId, pImage);
            }
        });
        holder.tvLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pLikes = Integer.parseInt(arrPost.get(position).getpLikes());
                mProcessLike = true;
                final String postIde = arrPost.get(position).getpId();
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike) {
                            if (snapshot.child(postIde).hasChild(myUid)) {
                                postsRef.child(postIde).child("pLikes").setValue("" + (pLikes - 1));
                                likeRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            } else {
                                postsRef.child(postIde).child("pLikes").setValue("" + (pLikes + 1));
                                likeRef.child(postIde).child(myUid).setValue("Bỏ thích");
                                mProcessLike = false;
                                addToHisNotifications(""+uid,""+pId,"Thích bài viết của bạn");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        holder.tvComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });
        holder.tvShares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth firebaseAuth;
                firebaseAuth = FirebaseAuth.getInstance();
                String id = firebaseAuth.getUid();
                if (!id.equals(uid)) {

                    Intent intent = new Intent(context, ProfileFriend.class);
                    intent.putExtra("visit_user_id", uid);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, ActivitySettings.class);

                    context.startActivity(intent);
                }
            }
        });
        holder.tvNumLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostLikeActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });
    }

    private void setUser(final ViewHolder holder, Post post) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("id").equalTo(post.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = "" + dataSnapshot.child("userName").getValue();
                    String img = "" + dataSnapshot.child("imageURL").getValue();
                    try {
                        Picasso.get().load(img).placeholder(R.drawable.profile_image).into(holder.imgProfile);
                    } catch (Exception e) {

                    }
                    holder.tvName.setText(name);
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes(final ViewHolder holder, final String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)) {
                    holder.tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_24, 0, 0, 0);
                    holder.tvLikes.setText("Bỏ thích");
                } else {
                    holder.tvLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_244, 0, 0, 0);
                    holder.tvLikes.setText("Thích");
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

    private void showMoreOptions(ImageView imgMore, String uid, String myUid, final String pId, final String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, imgMore, Gravity.END);
        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Sửa");
        }


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    beginDelete(pId, pImage);
                } else if (id == 1) {
                    Intent intent = new Intent(context, AddStatusActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")) {
            deleteWithoutImag(pId);
        } else {
            deleteWithImg(pId, pImage);
        }
    }

    private void deleteWithImg(final String pId, String pImage) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Đang xóa...");
        StorageReference pic = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        pic.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fqQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fqQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            dataSnapshot.getRef().removeValue();
                        }
                        Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImag(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Đang xóa...");
        Query fqQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fqQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue();
                }
                Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return arrPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgProfile;
        ImageView imgStt, imgMore;
        TextView tvName, tvTime, tvStatus, tvNumLikes, tvLikes, tvComments, tvShares, tvNumComments;
        LinearLayout profileLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.ci_stt_profile);
            imgStt = itemView.findViewById(R.id.img_stt);
            imgMore = itemView.findViewById(R.id.btn_more_profile);
            tvName = itemView.findViewById(R.id.tv_name_stt);
            tvTime = itemView.findViewById(R.id.tv_time_stt);
            tvStatus = itemView.findViewById(R.id.tv_stt);
            tvNumLikes = itemView.findViewById(R.id.tv_num_like);
            tvLikes = itemView.findViewById(R.id.tv_like);
            tvNumComments = itemView.findViewById(R.id.tv_num_cmt);
            tvComments = itemView.findViewById(R.id.tv_comment);
            tvShares = itemView.findViewById(R.id.tv_share);
            profileLayout = itemView.findViewById(R.id.layou_profile);
        }
    }
}
