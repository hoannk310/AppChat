package com.nkh.appchat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.ActivitySettings;
import com.nkh.appchat.AddStatusActivity;
import com.nkh.appchat.MainActivity;
import com.nkh.appchat.R;
import com.nkh.appchat.adapter.PostAdapter;
import com.nkh.appchat.model.Post;
import com.nkh.appchat.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostFrament extends Fragment {

    private static PostFrament INSTANCE;

    public static PostFrament getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new PostFrament();

        }
        return INSTANCE;
    }

    private PostFrament() {
    }


    private CircleImageView ciProfile;
    private TextView tvUpText;
    private Button btnUpImg;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private RecyclerView rvStt;
    private String myId;
    private List<Post> arrPosts;
    private PostAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        ciProfile = view.findViewById(R.id.ci_profile);
        tvUpText = view.findViewById(R.id.tv_uptext);
        btnUpImg = view.findViewById(R.id.btn_upimage);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myId = FirebaseAuth.getInstance().getUid();


        rvStt = view.findViewById(R.id.rv_stt);
        Context context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvStt.setLayoutManager(linearLayoutManager);
        arrPosts = new ArrayList<>();
        LoadPost();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user.getImageURL().equals("default")) {
                    ciProfile.setImageResource(R.drawable.profile_image);
                } else {
                    try {
                        Glide.with(getContext()).load(user.getImageURL()).error(R.drawable.profile_image).into(ciProfile);
                    } catch (Exception e) {

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ciProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivitySettings.class));
            }
        });
        tvUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddStatusActivity.class));
            }
        });

        return view;
    }

    private void LoadPost() {

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrPosts.clear();
                for (final DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    arrPosts.add(post);
                    adapter = new PostAdapter(arrPosts, getActivity());
                    rvStt.setAdapter(adapter);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seachPosts(String searchQuery) {


    }

}