package com.nkh.appchat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.nkh.appchat.MediaActivity;
import com.nkh.appchat.R;
import com.nkh.appchat.model.Image;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {
    private Context context;
    private List<Image> arrImages;

    public MediaAdapter(Context context, List<Image> arrImages) {
        this.context = context;
        this.arrImages = arrImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Image image = arrImages.get(position);
        try {
            Picasso.get().load(image.getImageUrl()).into(holder.imgMedia);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.profile_image).into(holder.imgMedia);
        }
        holder.imgMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setNegativeButton("Lưu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        BitmapDrawable draw = (BitmapDrawable) holder.imgMedia.getDrawable();
                        Bitmap bitmap = draw.getBitmap();

                        OutputStream outStream = null;
                        File sdCard = Environment.getExternalStorageDirectory();
                        File dir = new File(sdCard.getAbsolutePath() + "/Image");
                        dir.mkdirs();
                        String fileName = String.format("%d.jpg", System.currentTimeMillis());
                        File outFile = new File(dir, fileName);
                        try {
                            outStream = new FileOutputStream(outFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        Toast.makeText(context, "Đã lưu", Toast.LENGTH_SHORT).show();

                        try {
                            outStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                Button button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                button.setTextColor(Color.BLACK);
                alertDialog.getWindow();

            }
        });

    }


    @Override
    public int getItemCount() {
        return arrImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMedia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMedia = itemView.findViewById(R.id.img_media);
        }
    }
}
