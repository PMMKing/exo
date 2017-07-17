package com.haolb.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.haolb.client.R;
import com.haolb.client.utils.cache.ImageLoader;


public class TestAct extends Activity {

    private ImageView imageHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test);
        super.onCreate(savedInstanceState);
        imageHeader = (ImageView)findViewById(R.id.image_header);
        ImageLoader.getInstance(this).loadImage("http://115.28.235.86:8081/bang2/portrait/2015/07/29/13/54/10/84..jpg", imageHeader, R.drawable.default_avatar);
//        Picasso.with(this).load("http://115.28.235.86:8081/bang2/portrait/2015/07/29/13/54/10/84..jpg").placeholder(R.drawable.default_avatar).into(imageHeader);
    }


}