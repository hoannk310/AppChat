package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nkh.appchat.adapter.TabsAccessorAdapter;
import com.nkh.appchat.callvideo.CallActivity;
import com.nkh.appchat.fragment.FingerprinFragment;
import com.nkh.appchat.groupchat.GroupCreateActivity;
import com.nkh.appchat.model.Tracking;
import com.nkh.appchat.model.User;
import com.nkh.appchat.post.NotificationActitvity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RES_REQUEST = 7172;
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 1000;
    private static final int DISTANCE = 10;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mgGoogleApiClient;
    private Location mLastLocation;


    CircleImageView profileImage, navImage;
    FirebaseUser firebaseUser;
    TextView tvUser, tvStatus, tvFragment;
    ImageView addFr, addGr, notifiCmt;
    DatabaseReference reference, locationReference;
    private ViewPager myViewPager;
    private BottomNavigationView bottomNav;
    private TabsAccessorAdapter tabsAccessorAdapter;
    private FirebaseAuth auth;
    private NavigationView navView;
    private DrawerLayout drawerLayout;
    private String mUID;
    private String calledBy = "";
    private MenuItem prevMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        utilsActivity();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        locationReference = FirebaseDatabase.getInstance().getReference("Locations");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                tvUser.setText(user.getUserName());
                tvStatus.setText(user.getStatus());
                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.drawable.profile_image);
                } else {
                    try {
                        Glide.with(MainActivity.this).load(user.getImageURL()).error(R.drawable.profile_image).into(profileImage);
                    } catch (Exception e) {

                    }

                }
                Picasso.get().load(user.getImageURL()).placeholder(R.drawable.profile_image).into(navImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mgGoogleApiClient);
        if (mLastLocation != null) {
            locationReference.child(firebaseUser.getUid()).setValue(new Tracking(firebaseUser.getUid(),
                    String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude())));

        } else {
            Toast.makeText(this, "hihi1", Toast.LENGTH_SHORT).show();
        }
    }

    private void createLocationRequest() {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void buildGoogleApiClient() {

        mgGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mgGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RES_REQUEST).show();
            } else {
                Toast.makeText(this, "hihi2", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
            }
        }
    }

    private void utilsActivity() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        myViewPager = findViewById(R.id.main_tabs_pager);
        addFr = findViewById(R.id.add_friend);
        addFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFindFriends();
            }
        });

        addGr = findViewById(R.id.add_group);
        addGr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GroupCreateActivity.class));
            }
        });
        tvFragment = findViewById(R.id.tv_frg_name);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(tabsAccessorAdapter);
        bottomNav = findViewById(R.id.bottom_nav);
        notifiCmt = findViewById(R.id.add_noti);
        notifiCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NotificationActitvity.class));
            }
        });
        final String[] strings = {"Nhật Ký", "Tin Nhắn", "Nhóm", "Danh Bạ", "Lời Mời"};
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.post_nav:
                        myViewPager.setCurrentItem(0);
                        tvFragment.setText(strings[0]);
                        item.setChecked(true);
                        break;
                    case R.id.message_nav:
                        myViewPager.setCurrentItem(1);
                        tvFragment.setText(strings[1]);
                        item.setChecked(true);
                        break;
                    case R.id.group_chat_nav:
                        myViewPager.setCurrentItem(2);
                        item.setChecked(true);
                        tvFragment.setText(strings[2]);
                        break;
                    case R.id.contac_nav:
                        myViewPager.setCurrentItem(3);
                        item.setChecked(true);

                        tvFragment.setText(strings[3]);
                        break;
                    case R.id.addfr_nav:
                        myViewPager.setCurrentItem(4);
                        item.setChecked(true);
                        tvFragment.setText(strings[4]);
                        break;
                }
                return false;
            }
        });
        myViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNav.getMenu().getItem(0).setChecked(false);
                }
                Log.d("page", "" + position);
                bottomNav.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNav.getMenu().getItem(position);
                tvFragment.setText(strings[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        navView = findViewById(R.id.nav_menu);
        navView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawer);
        View view = navView.getHeaderView(0);
        navImage = view.findViewById(R.id.ci_header);
        tvUser = view.findViewById(R.id.tv_name);
        tvStatus = view.findViewById(R.id.tv_status);
        profileImage = findViewById(R.id.profile_img);

        auth = FirebaseAuth.getInstance();
        mUID = auth.getCurrentUser().getUid();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                finish();
                return true;
            case R.id.menu_settings:
                menuSettings();
                return true;
            case R.id.menu_addgroup:
                startActivity(new Intent(MainActivity.this, GroupCreateActivity.class));
                return true;
            case R.id.menu_setting:
                FingerprinFragment dialog = new FingerprinFragment();
                dialog.show(getSupportFragmentManager(), dialog.getTag());
                return true;
            case R.id.menu_ffrient:
                openFindFriends();
                return true;
        }
        return false;
    }

    private void openFindFriends() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendActivity.class);
        startActivity(findFriendsIntent);
    }


    private void menuSettings() {
        startActivity(new Intent(MainActivity.this, ActivitySettings.class));
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", saveCurrentDate);
        onlineState.put("state", state);
        DatabaseReference refOnl = FirebaseDatabase.getInstance().getReference();
        refOnl.child("Users").child(mUID).child("userState").updateChildren(onlineState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseUser != null) {
            updateUserStatus("online");
        }
        checkForRecevingCall();
        if (mgGoogleApiClient != null) {
            mgGoogleApiClient.connect();
        }


    }

    private void checkForRecevingCall() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(mUID).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("ringing")) {
                    calledBy = snapshot.child("ringing").getValue().toString();
                    Intent intent = new Intent(MainActivity.this, CallActivity.class);
                    intent.putExtra("userid", calledBy);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseUser != null) {
            updateUserStatus("offline");


        }
        if (mgGoogleApiClient != null) {
            mgGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mgGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}