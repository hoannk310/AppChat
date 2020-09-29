package com.nkh.appchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nkh.appchat.R;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<User> arrUsers;

    public UserAdapter(Context context, List<User> arrUsers) {
        this.context = context;
        this.arrUsers = arrUsers;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater;
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        User user = arrUsers.get(position);
        holder.tvUserName.setText(user.getUserName());
        Picasso.get().load(user.getImageURL()).placeholder(R.drawable.profile_image).into(holder.ciProfile);
    }

    @Override
    public int getItemCount() {
        return arrUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        CircleImageView ciProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_find);

            ciProfile = itemView.findViewById(R.id.ci_users_profile);
        }
    }
}
