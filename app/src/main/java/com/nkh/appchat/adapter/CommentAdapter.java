package com.nkh.appchat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private String myUid, postId;

    public CommentAdapter(List<Comment> arrComments, Context context, String myUid, String postId) {
        this.arrComments = arrComments;
        this.context = context;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_coment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final String uid = arrComments.get(position).getUid();
        String name = arrComments.get(position).getuName();
        String email = arrComments.get(position).getuEmail();
        String image = arrComments.get(position).getuDp();
        final String cid = arrComments.get(position).getcId();
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myUid.equals(uid)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Xóa");
                    builder.setMessage("Bạn có chắc chắn muốn xóa");
                    builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(cid);
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
                } else {
                    Toast.makeText(context, "Không thể xóa", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void deleteComment(String cid) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.child("Comments").child(cid).removeValue();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pComments").getValue();
                int newCommentCount = Integer.parseInt(comments) - 1;
                reference.child("pComments").setValue("" + newCommentCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
