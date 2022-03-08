package com.example.demo1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.demo1.Spectrometer.AnalysisActivity;

public class MainActivity extends AppCompatActivity {

    private Button mAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAnalysis = findViewById(R.id.btn_spec);
        MyListener();
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