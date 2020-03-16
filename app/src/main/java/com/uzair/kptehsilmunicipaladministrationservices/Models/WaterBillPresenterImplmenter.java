package com.uzair.kptehsilmunicipaladministrationservices.Models;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.api.Response;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uzair.kptehsilmunicipaladministrationservices.Presenters.WaterBillPresenter;
import com.uzair.kptehsilmunicipaladministrationservices.Views.WaterBillView;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Target;
import java.util.Random;

public class WaterBillPresenterImplmenter implements WaterBillPresenter
{
    private WaterBillView view;
    private Context context;

    public WaterBillPresenterImplmenter(WaterBillView view , Context context) {
        this.view = view;
        this.context = context;
    }

    // get user bill on ref number
    @Override
    public void getUserBill(final DatabaseReference databaseReference , String refNumber) {

        if(databaseReference != null && !refNumber.isEmpty())
        {

            view.showProgressDialog();

            Query query = databaseReference.child(refNumber);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        String name = dataSnapshot.child("ownerName").getValue().toString();
                        String date = dataSnapshot.child("locationDate").getValue().toString();
                        String location = dataSnapshot.child("locAddress").getValue().toString();
                        String imageUrl = dataSnapshot.child("bill_image").getValue().toString();
                        view.showErrorMessage(name);
                        view.getUserRecord(name , date, location , imageUrl);
                        view.hideProgressDialog();
                        view.clearAllFields();
                    }
                    else
                    {
                        view.showErrorMessage("No record found");
                        view.hideProgressDialog();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else
        {
            view.showErrorMessage("Please enter reference number");
        }

    }



    // download bill on download button click
    @Override
    public void downloadBill(String bilImage) {

        if (!view.checkStoragePermission()) {

            view.showErrorMessage("Please enable permission");
            view.requestStoragePermission();
        }
        else {

            Glide.with(context).asBitmap().load(bilImage).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                    try {

                        String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                +"/WaterBills";

                        final Random generator = new Random();
                        int n = 10000;
                        n = generator.nextInt(n);
                        final String fname = "Your Bill-" + n + ".png";

                        File image = new File(storagePath);

                        if (!image.exists()) {
                            image.mkdirs();
                        }

                        FileOutputStream outputStream = new FileOutputStream(image+"/"+fname);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                        //write data into destination
                        outputStream.flush();

                        //close the output stream
                        outputStream.close();

                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "Image saved with success at /Pictures/WaterBills", Toast.LENGTH_LONG).show();
                }
                @Override
                public void onLoadCleared(Drawable placeholder) {
                }
            });


        }

    }



}