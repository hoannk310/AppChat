package com.nkh.appchat.adapter;

import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.nkh.appchat.fragment.ChatsFragment;
import com.nkh.appchat.fragment.ContactsFragment;
import com.nkh.appchat.fragment.GroupFragment;
import com.nkh.appchat.fragment.PostFrament;
import com.nkh.appchat.fragment.RequestsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {


    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public TabsAccessorAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return PostFrament.getINSTANCE();
            case 1:
                return ChatsFragment.getINSTANCE();
            case 2:
                return GroupFragment.getINSTANCE();
            case 3:
                return ContactsFragment.getINSTANCE();
            case 4:
                return RequestsFragment.getINSTANCE();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 5;
    }
//
//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        switch (position){
//            case 0:
//                return "";
//            case 1:
//                return "Nhóm";
//            case 2:
//                return "Danh bạ";
//            case 3:
//                return "Lời mời kết bạn";
//            default:
//                return null;
//        }
//    }
}
