package com.nkh.appchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nkh.appchat.post.ProfileFriend;
import com.nkh.appchat.R;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.ViewHolder> {
    private Context context;
    private List<User> arrUser;

    public FindFriendAdapter(Context context, List<User> arrUser) {
        this.context = context;
        this.arrUser = arrUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
        return new FindFriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final User user = arrUser.get(position);
        holder.tvNameUser.setText(user.getUserName());
        holder.tvStatus.setText(user.getStatus());
        Picasso.get().load(user.getImageURL()).placeholder(R.drawable.profile_image).into(holder.ciUser);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ProfileFriend.class);
                intent.putExtra("visit_user_id",arrUser.get(position).getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ciUser;
        TextView tvNameUser, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ciUser = itemView.findViewById(R.id.ci_users_profile);
            tvNameUser = itemView.findViewById(R.id.tv_user_find);
            tvStatus = itemView.findViewById(R.id.tv_status_find);
        }
    }
}
