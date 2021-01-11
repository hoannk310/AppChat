package com.nkh.appchat.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.chat.ChatsActivity;
import com.nkh.appchat.R;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactsFragment extends Fragment {
    private static ContactsFragment INSTANCE;
    public static ContactsFragment getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE = new ContactsFragment();

        }return INSTANCE;
    }

    private ContactsFragment() {
    }

    private RecyclerView rvContact;
    private DatabaseReference contactReference, userReference;
    private FirebaseAuth auth;
    private String currentUserId;
    private  View contactView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       contactView = inflater.inflate(R.layout.fragment_contacts, container, false);
        rvContact = contactView.findViewById(R.id.rv_contact);
        rvContact.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        contactReference = FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUserId);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        return contactView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>().setIndexedQuery(contactReference,userReference, User.class).build();

        final FirebaseRecyclerAdapter<User, ContactViewHolder> adapter = new FirebaseRecyclerAdapter<User, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull User model) {
                final String usersId = getRef(position).getKey();
                userReference.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.child("userState").hasChild("state")){
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();
                                if (state.equals("online")){
                                    holder.imgOnline.setVisibility(View.VISIBLE);
                                }else if (state.equals("offline"))
                                {
                                    holder.imgOnline.setVisibility(View.INVISIBLE);
                                }
                            }

                            else {
                                holder.imgOnline.setVisibility(View.INVISIBLE);

                            }


                            final String profileImage = snapshot.child("imageURL").getValue().toString();
                            final String profileName = snapshot.child("userName").getValue().toString();
                            String profileStatus = snapshot.child("status").getValue().toString();
                            if (snapshot.hasChild("imageURL")) {

                                holder.tvUserName.setText(profileName);
                                holder.tvStatus.setText(profileStatus);
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.ciProfile);

                            } else {

                                holder.tvUserName.setText(profileName);
                                holder.tvStatus.setText(profileStatus);
                            }
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatsActivity.class);
                                    chatIntent.putExtra("userid",usersId);

                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
                return new ContactViewHolder(view);
            }
        };

        rvContact.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvStatus;
        CircleImageView ciProfile;
        ImageView imgOnline;

        public ContactViewHolder(@NonNull View itemView) {

            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_find);
            tvStatus = itemView.findViewById(R.id.tv_status_find);
            ciProfile = itemView.findViewById(R.id.ci_users_profile);
            imgOnline = itemView.findViewById(R.id.img_online);
        }
    }
}