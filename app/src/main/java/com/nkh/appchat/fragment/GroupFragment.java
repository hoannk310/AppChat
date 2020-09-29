package com.nkh.appchat.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.LoginActivity;
import com.nkh.appchat.R;
import com.nkh.appchat.adapter.GroupAdapter;
import com.nkh.appchat.model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {
    private static GroupFragment INSTANCE;
    public static GroupFragment getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE = new GroupFragment();

        }return INSTANCE;
    }

    private GroupFragment() {

    }
    private RecyclerView rvGroup;
    private List<Group> arrGroup;
    private GroupAdapter adapter;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        rvGroup = view.findViewById(R.id.rv_group);
        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupChatsList();
        return view;
    }

    private void loadGroupChatsList() {
        arrGroup = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrGroup.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if (dataSnapshot.child("Participants").child(firebaseAuth.getUid()).exists()){
                        Group group =  dataSnapshot.getValue(Group.class);
                        arrGroup.add(group);
                    }
                }
                adapter = new GroupAdapter(getActivity(),arrGroup);
                rvGroup.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void seachGroupChatsList(final String query) {
        arrGroup = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrGroup.size();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if (dataSnapshot.child("Participants").child(firebaseAuth.getUid()).exists()){
                        if (dataSnapshot.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
                            Group group = dataSnapshot.getValue(Group.class);
                            arrGroup.add(group);
                        }
                    }
                }
                adapter = new GroupAdapter(getActivity(),arrGroup);
                adapter.notifyDataSetChanged();
                rvGroup.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

   private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
   }
}