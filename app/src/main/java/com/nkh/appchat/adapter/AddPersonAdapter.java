package com.nkh.appchat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.R;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddPersonAdapter extends RecyclerView.Adapter<AddPersonAdapter.ViewHolder> {
    private Context context;
    private List<User> arrUsers;
    private String groupId, myGroupRole;

    public AddPersonAdapter(Context context, List<User> arrUsers, String groupId, String myGroupRole) {
        this.context = context;
        this.arrUsers = arrUsers;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_add, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = arrUsers.get(position);
        String name = user.getUserName();
        final String uid = user.getId();
        String email = user.getEmail();
        String image = user.getImageURL();
        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try {
            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.avatarTv);
        } catch (Exception e) {
            holder.avatarTv.setImageResource(R.drawable.profile_image);
        }
        checkIfAlrealyExits(user, holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                reference.child(groupId).child("Participants").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String previousRole = "" + snapshot.child("role").getValue();
                            String[] options;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Chọn");
                            if (myGroupRole.equals("creator")) {
                                if (previousRole.equals("admin")) {
                                    options = new String[]{"Remove Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                removeAdmin(user);
                                            } else {
                                                removeUser(user);

                                            }
                                        }
                                    }).show();
                                } else if (previousRole.equals("participant")) {
                                    options = new String[]{"Make Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                makeAdmin(user);
                                            } else {
                                                removeUser(user);

                                            }
                                        }
                                    }).show();
                                }
                            } else if (myGroupRole.equals("admin")) {
                                if (previousRole.equals("creator")) {
                                    Toast.makeText(context, "Người tạo group", Toast.LENGTH_SHORT).show();
                                } else if (previousRole.equals("admin")) {
                                    options = new String[]{"Remove Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                removeAdmin(user);
                                            } else {
                                                removeUser(user);

                                            }
                                        }
                                    }).show();
                                } else if (previousRole.equals("participant")) {
                                    options = new String[]{"Make Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                makeAdmin(user);
                                            } else {
                                                removeUser(user);

                                            }
                                        }
                                    }).show();
                                }
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Thêm Thành Viên").setMessage("Bạn có muốn thêm người này ?").setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addParticipants(user);
                                }
                            }).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
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

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void removeAdmin(User user) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(user.getId()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Đã xóa quyền admin", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(User user) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(user.getId()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Đã xét quyền thành admin", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeUser(User user) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(user.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addParticipants(User user) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", user.getId());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", "" + timestamp);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(user.getId()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Đã thêm", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlrealyExits(User user, final ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(user.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String hisRole = "" + snapshot.child("role").getValue();
                    holder.statusTv.setText(hisRole);
                } else {
                    holder.statusTv.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return arrUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarTv;
        TextView nameTv, emailTv, statusTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarTv = itemView.findViewById(R.id.ci_users_profile);
            nameTv = itemView.findViewById(R.id.tv_user_find);
            emailTv = itemView.findViewById(R.id.tv_email_find);
            statusTv = itemView.findViewById(R.id.tv_status_find);
        }
    }
}
