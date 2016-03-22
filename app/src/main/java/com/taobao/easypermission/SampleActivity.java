package com.taobao.easypermission;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);


        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if(fragment == null){
            fragment = new BlankFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container,fragment).commit();
        }
    }
}
