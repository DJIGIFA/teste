package com.isp_bamako;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Loading extends AppCompatActivity {

    private Button btn_try;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);


        initViw();
        //animation
        ImageView ImageLogo = (ImageView) findViewById(R.id.intro_logo_card);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(ImageLogo, "alpha",  1f, .3f);
        fadeOut.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(ImageLogo, "alpha", .3f, 1f);
        fadeIn.setDuration(2000);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
            }
        });
        mAnimationSet.start();
        /////////////////////////////////////////////////





        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(Loading.this,MainActivity.class));
                    finish();
                }
            },4000);
        }
        else{
            //connected = false;
            Toast.makeText(Loading.this,"Connection internet non trouvée !!",Toast.LENGTH_LONG).show();
            Toast.makeText(Loading.this,"Connection internet non trouvée !!",Toast.LENGTH_LONG).show();
            btn_try.setVisibility(View.VISIBLE);
            btn_try.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Loading.this,Loading.class));
                    finish();
                }
            });
        }



    }

    private void initViw()
    {
        btn_try  = findViewById(R.id.btn_try);
    }



}