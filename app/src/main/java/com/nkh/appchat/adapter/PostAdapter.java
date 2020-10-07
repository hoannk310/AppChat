package com.nkh.appchat.adapter;

import android.content.Context;
import android.graphics.Point;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
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

    public PostAdapter(List<Post> arrPost, Context context) {
        this.arrPost = arrPost;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_posts, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String uid = arrPost.get(position).getUid();
        String uEmail = arrPost.get(position).getuEmail();
        String uName = arrPost.get(position).getuName();
        String uDp = arrPost.get(position).getuDp();
        String pId = arrPost.get(position).getpId();
        String pDesc = arrPost.get(position).getpDescr();
        String pImage = arrPost.get(position).getpImage();
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
            try {
                Picasso.get().load(pImage).placeholder(R.drawable.profile_image).into(holder.imgStt);
            } catch (Exception e) {

            }
        }
        holder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
    }

    @Override
    public int getItemCount() {
        return arrPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgProfile;
        ImageView imgStt, imgMore;
        TextView tvName, tvTime, tvStatus, tvNumLikes, tvLikes, tvComments, tvShares;

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
        }
    }
}
