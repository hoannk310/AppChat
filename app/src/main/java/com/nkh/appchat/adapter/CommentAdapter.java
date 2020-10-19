package com.nkh.appchat.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nkh.appchat.R;
import com.nkh.appchat.model.Comment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> arrComments;
    private Context context;

    public CommentAdapter(List<Comment> arrComments, Context context) {
        this.arrComments = arrComments;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_coment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String uid = arrComments.get(position).getUid();
        String name = arrComments.get(position).getuName();
        String email = arrComments.get(position).getuEmail();
        String image = arrComments.get(position).getuDp();
        String cid = arrComments.get(position).getcId();
        String comment = arrComments.get(position).getComment();
        String timestamp = arrComments.get(position).getTimestamp();


        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.tvNameCmt.setText(name);
        holder.tvCmt.setText(comment);
        holder.tvTimeCmt.setText(pTime);

        try {
            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.ciAvtCmt);
        } catch (Exception e) {
        }


    }

    @Override
    public int getItemCount() {
        return arrComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView ciAvtCmt;
        TextView tvNameCmt, tvCmt, tvTimeCmt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ciAvtCmt = itemView.findViewById(R.id.avatar_cmt);
            tvNameCmt = itemView.findViewById(R.id.tv_name_cmt);
            tvCmt = itemView.findViewById(R.id.tv_cmt);
            tvTimeCmt = itemView.findViewById(R.id.tv_time_cmt);
        }
    }
}
