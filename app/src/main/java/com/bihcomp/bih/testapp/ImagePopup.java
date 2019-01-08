package com.bihcomp.bih.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.bihcomp.bih.testapp.Fragment2;
import com.bihcomp.bih.testapp.R;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Picasso;

public class ImagePopup extends FragmentActivity implements OnClickListener {
    private Context mContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_popup);
        mContext = this;
        PhotoViewAttacher photoViewAttacher;


        // 전송메시지
        Intent i = ((FragmentActivity) mContext).getIntent();
        Bundle extras = i.getExtras();
        String imgPath = extras.getString("filename");

        ImageView iv = (ImageView) findViewById(R.id.photo_view);

        Picasso.with(mContext).load(imgPath).into(iv);
        photoViewAttacher = new PhotoViewAttacher(iv);



    }

    public void onClick(View v) {

    }
}
