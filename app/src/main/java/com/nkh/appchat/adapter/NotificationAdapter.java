package com.nkh.appchat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.post.PostDetailActivity;
import com.nkh.appchat.R;
import com.nkh.appchat.model.Notification;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    List<Notification> arrNotifications;
    Context context;
    private FirebaseAuth firebaseAuth;

    public NotificationAdapter(List<Notification> arrNotifications, Context context) {
        this.arrNotifications = arrNotifications;
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Notification notificationModel = arrNotifications.get(position);
        String name = notificationModel.getsName();
        String notificationText = notificationModel.getNotification();
        String image = notificationModel.getsImage();
        final String timestamp = notificationModel.getTimestamp();
        String senderUid = notificationModel.getsUid();
        final String pId = notificationModel.getpId();

        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("id").equalTo(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = "" + dataSnapshot.child("userName").getValue();
                    String email = "" + dataSnapshot.child("email").getValue();
                    String img = "" + dataSnapshot.child("imageURL").getValue();
                    try {
                        Picasso.get().load(img).placeholder(R.drawable.profile_image).into(holder.ciAvatar);
                    } catch (Exception e) {

                    }
                    holder.tvName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tvTime.setText(pTime);

        holder.tvName.setText(name);
        holder.tvNotification.setText(notificationText);
        try {
            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.ciAvatar);
        } catch (Exception e) {
            holder.ciAvatar.setImageResource(R.drawable.profile_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                builder.setTitle("Xóa");
                builder.setMessage("Bạn có chắc chắn muốn xóa");
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                        reference1.child(firebaseAuth.getUid()).child("Notifications").child(timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Không thể xóa", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });
                builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                Button button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button button2 = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setTextColor(Color.BLACK);
                button2.setTextColor(Color.BLACK);
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ciAvatar;
        TextView tvName, tvNotification, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ciAvatar = itemView.findViewById(R.id.ci_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNotification = itemView.findViewById(R.id.tv_notifi);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
