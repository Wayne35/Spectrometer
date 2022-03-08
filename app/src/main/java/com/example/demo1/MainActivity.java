package com.example.demo1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.demo1.Spectrometer.AnalysisActivity;

public class MainActivity extends AppCompatActivity {

    private Button mAnalysis;
    private ImageView mCQU,mAilab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAnalysis = findViewById(R.id.btn_spec);
        mCQU = findViewById(R.id.iv);
        mAilab = findViewById(R.id.aiLab);
        MyListener();

        //网页跳转
        mAilab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.ailabcqu.com");    //设置跳转的网站
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        //网页跳转
        mCQU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://cqu.edu.cn");    //设置跳转的网站
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void MyListener(){
        OnClick onclick = new OnClick();
        mAnalysis.setOnClickListener(onclick);
    }

    private class OnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.btn_spec:
                    intent = new Intent(MainActivity.this, AnalysisActivity.class);
                    break;

            }
            startActivity(intent);
        }
    }
}