package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.nkh.appchat.adapter.MessagesAdapter;
import com.nkh.appchat.model.Messages;
import com.nkh.appchat.model.Tracking;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsActivity extends AppCompatActivity{
    private String messageReceiverID, userName, userImage, messageSenderID;
    private TextView chatName, chatLastSeen, chatSent;
    private FirebaseAuth auth;
    private CircleImageView ciChats;
    private EditText edtMessage;
    private ImageView imgMessage, imgFile, imgMedia, imgCallVdieo;
    private DatabaseReference reference;

    private ProgressDialog progressDialog;
    private final List<Messages> arrMessage = new ArrayList<>();
    private MessagesAdapter messagesAdapter;
    private RecyclerView rvChatsLayout;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;
    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myURL = "";
    private StorageTask uploadTask;
    private DatabaseReference referenceSeen;
    private Uri fileUri;
    private ValueEventListener seenLisener;
    private LocationManager locationManager;
    private String myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        auth = FirebaseAuth.getInstance();
        messageSenderID = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();

        chatName = findViewById(R.id.tv_name_chat);
        chatLastSeen = findViewById(R.id.tv_last_seen);
        ciChats = findViewById(R.id.chat_img);
        edtMessage = findViewById(R.id.edt_message);
        imgMessage = findViewById(R.id.img_send);
        imgCallVdieo = findViewById(R.id.btn_video_call);
        imgFile = findViewById(R.id.img_file);
        imgMedia = findViewById(R.id.btn_media);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        progressDialog = new ProgressDialog(this);


        imgMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, MediaActivity.class);
                intent.putExtra("visit_sender_id", messageSenderID);
                intent.putExtra("visit_receiver_id", messageReceiverID);
                startActivity(intent);
            }
        });
        ciChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, ProfileFriend.class);
                intent.putExtra("visit_user_id", messageReceiverID);
                startActivity(intent);
            }
        });

        messageReceiverID = getIntent().getExtras().get("userid").toString();
        reference.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                chatName.setText(user.getUserName());
                if (snapshot.hasChild("imageURL")) {
                    String receiverImage = snapshot.child("imageURL").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(ciChats);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        chatName.setText(userName);
        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(ciChats);
        imgMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessager();
            }
        });
        imgFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Ảnh",
                        "Tệp PDF",
                        "Tệp Word",
                        "Vị trí"
                };
                Context context;
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatsActivity.this);
                builder.setTitle("Chọn");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Chọn Ảnh"), 420);
                        }
                        if (which == 1) {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Chọn tệp"), 420);
                        }
                        if (which == 2) {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Chọn tệp"), 420);

                        }
                        if (which == 3) {
                            checker = "location";
                                sendLocation();
                        }


                    }
                });
                builder.show();

            }


        });
        imgCallVdieo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, CallActivity.class);
                intent.putExtra("userid", messageReceiverID);
                startActivity(intent);
            }
        });
        messagesAdapter = new MessagesAdapter(arrMessage,ChatsActivity.this);
        rvChatsLayout = findViewById(R.id.rv_chat_layout);
        Context context;
        linearLayoutManager = new LinearLayoutManager(this);
        rvChatsLayout.setLayoutManager(linearLayoutManager);
        rvChatsLayout.setAdapter(messagesAdapter);
        displayLastSeen();
        reference.child("Message").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                arrMessage.add(messages);
                messagesAdapter.notifyDataSetChanged();
                rvChatsLayout.smoothScrollToPosition(rvChatsLayout.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendLocation() {

        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("Locations");

        locationRef.child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lat = snapshot.child("lat").getValue().toString();
                String lng = snapshot.child("lng").getValue().toString();
                myLocation = lat + "," + lng;
                Log.d("hoan", lat + "," + lng);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (TextUtils.isEmpty(myLocation)) {
            Toast.makeText(this, "Bạn không có quyền", Toast.LENGTH_SHORT).show();
        } else {
            String messageSendRef = "Message/" + messageSenderID + "/" + messageReceiverID;
            String messageReceivedRef = "Message/" + messageReceiverID + "/" + messageSenderID;
            DatabaseReference userMessageKeyRef = reference.child("Message").child(messageSenderID).child(messageReceivedRef).push();
            String messagePushID = userMessageKeyRef.getKey();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message", myLocation);
            messageTextBody.put("type", checker);
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("isSeen", false);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSendRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceivedRef + "/" + messagePushID, messageTextBody);
            reference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatsActivity.this, "tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatsActivity.this, "tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
                    }
                    edtMessage.setText("");
                }
            });

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
            ref.child(messagePushID).setValue(messageTextBody).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }
    }


    private void seenMessage(final String userId) {
        referenceSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenLisener = referenceSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    if (messages.getTo().equals(messageSenderID) && messages.getFrom().equals(messageReceiverID)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);

                        snapshot.getRef().updateChildren(hashMap);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //   private void seenChat (String userId){
//        final DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chats");
//        reference1.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    Messages messages = snapshot.getValue(Messages.class);
//                    if (messages.getTo().equals(messageSenderID) || messages.getFrom().equals(messageReceiverID)) {
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("isSeen", true);
//                        dataSnapshot.getRef().updateChildren(hashMap);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 420 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            progressDialog.setTitle("Đang gửi");
            progressDialog.setMessage("Đơi chút...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            fileUri = data.getData();
            if (!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document File");
                final String messageSendRef = "Message/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceivedRef = "Message/" + messageReceiverID + "/" + messageSenderID;
                DatabaseReference userMessageKeyRef = reference.child("Message").child(messageSenderID).child(messageReceiverID).push();
                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", downloadUrl);
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("isSeen", false);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSendRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put(messageReceivedRef + "/" + messagePushID, messageTextBody);
                                edtMessage.setText("");
                                reference.updateChildren(messageBodyDetails);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
                                ref.child(messagePushID).setValue(messageTextBody).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                                progressDialog.dismiss();

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage((int) p + "% Uploading...");
                    }
                });


            } else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image File");
                final String messageSendRef = "Message/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceivedRef = "Message/" + messageReceiverID + "/" + messageSenderID;
                DatabaseReference userMessageKeyRef = reference.child("Message").child(messageSenderID).child(messageReceiverID).push();
                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myURL = downloadUrl.toString();
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myURL);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("isSeen", false);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);
                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSendRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceivedRef + "/" + messagePushID, messageTextBody);
                            edtMessage.setText("");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
                            ref.child(messagePushID).setValue(messageTextBody).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                            reference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatsActivity.this, "tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();

                                    } else {
                                        Toast.makeText(ChatsActivity.this, "chưa gửi được tin nhắn", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }

                                }
                            });
                        }
                    }
                });


            } else {
                Toast.makeText(this, "erro", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

        }
    }

    private void sendMessager() {
        String messageText = edtMessage.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "tin nhắn trống", Toast.LENGTH_SHORT).show();
        } else {
            String messageSendRef = "Message/" + messageSenderID + "/" + messageReceiverID;
            String messageReceivedRef = "Message/" + messageReceiverID + "/" + messageSenderID;
            DatabaseReference userMessageKeyRef = reference.child("Message").child(messageSenderID).child(messageReceivedRef).push();
            String messagePushID = userMessageKeyRef.getKey();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("isSeen", false);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSendRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceivedRef + "/" + messagePushID, messageTextBody);
            reference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatsActivity.this, "tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatsActivity.this, "tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
                    }
                    edtMessage.setText("");
                }
            });

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
            ref.child(messagePushID).setValue(messageTextBody).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }
    }

    private void displayLastSeen() {
        reference.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("state")) {
                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String date = snapshot.child("userState").child("date").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();
                    if (state.equals("online")) {
                        chatLastSeen.setText("online");
                    } else if (state.equals("offline")) {
                        chatLastSeen.setText("Hoạt động :" + date + " " + time);
                    }
                } else {

                    chatLastSeen.setText("offline");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}