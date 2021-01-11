package com.nkh.appchat.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.R;
import com.nkh.appchat.common.Common;
import com.nkh.appchat.model.User;

import static com.nkh.appchat.common.Common.IS_FACE_ID;


public class FingerprinFragment extends BottomSheetDialogFragment {
    private User user;
    private String myId;
    private SharedPreferences sharedPreferences;
    SwitchCompat switchCompat;
    public static final String SHARED_PREFERENCE_NAME = "SettingGame";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FingerprinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        switchCompat = getView().findViewById(R.id.switchCompat);
        // Lấy ra giá trị lưu trong máy đã bật hay tắt chức năng login bằng vân tay
        sharedPreferences = getActivity().
                getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean isVolume = sharedPreferences.getBoolean(IS_FACE_ID, false);
        // set giá trị cho switch theo biến lưu khi bật tắt chức năng vân tay
        switchCompat.setChecked(isVolume);
        switchCompat.setTrackTintList(switchCompat.isChecked() ?
                (ColorStateList.valueOf(Color.parseColor("#0CEBF3"))) :
                (ColorStateList.valueOf(Color.parseColor("#929697"))));

        // Sự kiện bật tắt chức năng login bằng vân tay
        switchCompat.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton,
                                                 boolean b) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (switchCompat.isChecked()) {
                            // BẬT CHỨC năng đồng thời lưu tài khoản vào máy
                            editor.putBoolean(IS_FACE_ID, true);
                            switchCompat.setTrackTintList
                                    (ColorStateList.valueOf(Color.parseColor("#0CEBF3")));
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            myId = auth.getCurrentUser().getUid();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference.child(myId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    user = snapshot.getValue(User.class);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            Common.saveData(getActivity(), user);

                        } else {

                            editor.putBoolean(IS_FACE_ID, false);
                            switchCompat.setTrackTintList
                                    (ColorStateList.valueOf(Color.parseColor("#929697")));
                            Common.saveData(getActivity(), null);
                        }
                        editor.commit();

                    }
                });
    }
}