package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.style.EasyEditSpan;
import android.text.style.SubscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.security.PublicKey;
import java.util.logging.Logger;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoCallActivity extends AppCompatActivity implements Session.SessionListener, Publisher.PublisherListener {
    private static String API_key = "46893574";
    private static String SESSION_ID = "1_MX40Njg5MzU3NH5-MTU5ODA3ODkxMTM1NH5uVkRDa1c4TmZnRTlic1o1Y2xZMlAzYyt-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Njg5MzU3NCZzaWc9ZmVjNDVlZDY5MzdjMWZkYWZhMTlmMWFmMWExY2Q0M2Q4YWZkZjA1MTpzZXNzaW9uX2lkPTFfTVg0ME5qZzVNelUzTkg1LU1UVTVPREEzT0RreE1UTTFOSDV1VmtSRGExYzRUbVpuUlRsaWMxbzFZMnhaTWxBell5dC1mZyZjcmVhdGVfdGltZT0xNTk4MDc5MDMyJm5vbmNlPTAuNjkzMjI1MDgyMTY2NTA4MiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjAwNjcxMDMwJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoCallActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private ImageView closeVideoChatBtn;
    private DatabaseReference userRef;
    private FirebaseAuth firebaseAuth;
    private String userId = "";
    private FrameLayout mPublishViewController;
    private FrameLayout mSubscribeViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getCurrentUser().getUid();
        Context context;


        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        closeVideoChatBtn = findViewById(R.id.close_video);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(userId).hasChild("Ringing")) {
                            userRef.child(userId).child("Ringing").removeValue();
                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null){
                                mSubscriber.destroy();
                            }
                            finish();
                        }
                        if (snapshot.child(userId).hasChild("Calling")) {
                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null){
                                mSubscriber.destroy();
                            }
                            userRef.child(userId).child("Calling").removeValue();
                            finish();
                        } else {
                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null){
                                mSubscriber.destroy();
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoCallActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            mPublishViewController = findViewById(R.id.publisher_container);
            mSubscribeViewController = findViewById(R.id.sub_container);
            mSession = new Session.Builder(this, API_key, SESSION_ID).build();
            mSession.setSessionListener(VideoCallActivity.this);
            mSession.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "Ứng dụng này cần mic và camera", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {

        Log.i(LOG_TAG, "Session conected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoCallActivity.this);
        mPublishViewController.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscribeViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        Log.i(LOG_TAG, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscribeViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}