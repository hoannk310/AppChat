package com.nkh.appchat.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nkh.appchat.ActivitySettings;
import com.nkh.appchat.AddStatusActivity;
import com.nkh.appchat.ProfileFriend;
import com.nkh.appchat.R;
import com.nkh.appchat.model.Post;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> arrPost;
    private Context context;
    private String myUid;

    public PostAdapter(List<Post> arrPost, Context context) {
        this.arrPost = arrPost;
        this.context = context;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_posts, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final String uid = arrPost.get(position).getUid();
        String uEmail = arrPost.get(position).getuEmail();
        String uName = arrPost.get(position).getuName();
        String uDp = arrPost.get(position).getuDp();
        final String pId = arrPost.get(position).getpId();
        String pDesc = arrPost.get(position).getpDescr();
        final String pImage = arrPost.get(position).getpImage();
        String pTimeStamp = arrPost.get(position).getpTime();

        Locale aLocale;
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.tvName.setText(uName);
        holder.tvTime.setText(pTime);
        holder.tvStatus.setText(pDesc);
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.profile_image).into(holder.imgProfile);
        } catch (Exception e) {

        }
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

            }
        });
        holder.tvComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                }
                else if (id==1){
                    Intent intent = new Intent(context, AddStatusActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
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
        TextView tvName, tvTime, tvStatus, tvNumLikes, tvLikes, tvComments, tvShares;
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
            tvComments = itemView.findViewById(R.id.tv_comment);
            tvShares = itemView.findViewById(R.id.tv_share);
            profileLayout = itemView.findViewById(R.id.layou_profile);
        }
    }
}
