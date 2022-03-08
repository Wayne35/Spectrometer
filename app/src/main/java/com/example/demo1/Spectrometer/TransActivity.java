package com.example.demo1.Spectrometer;

import static com.example.demo1.Spectrometer.SpectrumActivity.getPicFromBytes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.demo1.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import widget.ListItem;
import widget.MyAdapter;
import widget.MyDialog_Delete;
import widget.MyDialog_Mode;

public class TransActivity extends AppCompatActivity {

    private float[] Bright_spec;
    private float[][] data;
    private ImageView mAilab;
    private ListView listView;
    private SurfaceView mSurface;
    private Button mBtnCompare,mBtnTrans,mBtnBright;
    private List<ListItem> checkedListItemList = new ArrayList<ListItem>();
    private List<ListItem> listItemList = new ArrayList<ListItem>();
    private float[][] mPoints;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);
        initAlcohol();
        //定义控件
        MyAdapter myAdapter = new MyAdapter(TransActivity.this, R.layout.layout_list, listItemList);
        listView = findViewById(R.id.lv_compare);
        listView.setAdapter(myAdapter);

        mAilab = findViewById(R.id.aiLab);
        mSurface = findViewById(R.id.sv_compare);
        mBtnCompare = findViewById(R.id.btn_compare);
        mBtnTrans = findViewById(R.id.btn_trans);
        mBtnBright = findViewById(R.id.btn_bright);

        //获取手机屏幕的像素，并模拟光谱显示区域的高宽像素值
        Display display = getWindowManager().getDefaultDisplay();
        float sv_width = (float) (display.getWidth() * 0.9);
        float sv_height = (float) (display.getHeight() * 0.28);//模拟显示光谱区域的高宽像素值
        //绘制SurfaceView底板
        drawBlank();
        //获取R坐标数据
        drawGraph(sv_width, sv_height);
        drawTrans(sv_width, sv_height);

        //网页跳转
        mAilab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.ailabcqu.com");    //设置跳转的网站
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        mBtnBright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedListItemList.clear();
                findSelected(listItemList);
                if(checkedListItemList.size() == 1){
                    Bright_spec = getDataBySingleChannel(checkedListItemList, "crop")[0];
                    String s = extractNum(checkedListItemList.get(0).getName());
                    Toast.makeText(TransActivity.this, "已选择浓度为"+ s +"数据作为亮光谱", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(TransActivity.this, "请选择一个亮光谱", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void drawTrans(float sv_width, float sv_height) {
        SurfaceHolder surfaceHolder = mSurface.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mBtnTrans.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkedListItemList.clear();
                        //找出被选中的酒精数据
                        findSelected(listItemList);
                        if (checkedListItemList.size() != 0 && checkedListItemList.size() <= 3) {
                            mPoints = getTransBySingleChannel(checkedListItemList, "crop",Bright_spec);
                            //必须在该方法中获取Canvas对象，才能保证SurfaceView可用
                            Canvas canvas = holder.lockCanvas(); //获得canvas对象
                            //使用Canvas绘图
                            //画布使用白色填充
                            canvas.drawColor(Color.LTGRAY);
                            Paint paint = new Paint();
                            //创建画笔，宽度为5，绘制相关点
                            paint.setStrokeWidth(5);
                            paint.setColor(Color.GRAY);
                            for (int i = 0; i < checkedListItemList.size(); i++){
                                String str = extractNum(checkedListItemList.get(i).getName());
                                Path path = new Path();
                                //屏幕左上角（100,100）到（1000,100）画一条直线
                                path.moveTo(720, 22 + 50 * (i + 1));
                                path.lineTo(1100, 22 + 50 * (i + 1));
                                paint.setTextSize(38);
                                if (i == 0) {
                                    paint.setStrokeWidth(3);
                                    canvas.drawLine(600, 50 * (i + 1), 700, 50 * (i + 1), paint);
                                    canvas.drawTextOnPath("浓度" + str + "%", path, 0, 0, paint);
                                } else if (i == 1) {
                                    paint.setStrokeWidth(5);
                                    canvas.drawLine(600, 50 * (i + 1), 700, 50 * (i + 1), paint);
                                    canvas.drawTextOnPath("浓度" + str + "%", path, 0, 0, paint);
                                } else if (i == 2) {
                                    paint.setStrokeWidth(7);
                                    canvas.drawLine(600, 50 * (i + 1), 700, 50 * (i + 1), paint);
                                    canvas.drawTextOnPath("浓度" + str + "%", path, 0, 0, paint);
                                }
                                for (int j = 0; j < mPoints[i].length - 1; j++) {
                                    // canvas.drawPoint(j * (sv_width / mPoints[i].length), sv_height - 2 * mPoints[i][j], paint);
                                    canvas.drawLine(j * (sv_width / mPoints[i].length), sv_height - 2 * mPoints[i][j], (j + 1) * (sv_width / mPoints[i].length), sv_height - 2 * mPoints[i][j + 1], paint);
                                }}
                            holder.unlockCanvasAndPost(canvas); //释放canvas对象
                        } else if(checkedListItemList.size() > 3){
                            Toast.makeText(TransActivity.this, "最多只能选取3组数据", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(TransActivity.this, "请至少选择一组数据", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }
    //绘制单一通道光成分曲线
    private void drawGraph(float sv_width, float sv_height) {
        SurfaceHolder surfaceHolder = mSurface.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mBtnCompare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkedListItemList.clear();
                        //找出被选中的酒精数据
                        findSelected(listItemList);
                        if (checkedListItemList.size() != 0 && checkedListItemList.size() <= 3) {
                            mPoints = getDataBySingleChannel(checkedListItemList, "crop");
                            //必须在该方法中获取Canvas对象，才能保证SurfaceView可用
                            Canvas canvas = holder.lockCanvas(); //获得canvas对象
                            //使用Canvas绘图
                            //画布使用白色填充
                            canvas.drawColor(Color.LTGRAY);
                            Paint paint = new Paint();
                            //创建画笔，宽度为5，绘制相关点
                            paint.setStrokeWidth(5);
                            paint.setColor(Color.GRAY);
                            for (int i = 0; i < checkedListItemList.size(); i++){
                                String str = extractNum(checkedListItemList.get(i).getName());
                                Path path = new Path();
                                //屏幕左上角（100,100）到（1000,100）画一条直线
                                path.moveTo(720, 22 + 50 * (i + 1));
                                path.lineTo(1100, 22 + 50 * (i + 1));
                                paint.setTextSize(38);
                                if (i == 0) {
                                    paint.setStrokeWidth(3);
                                    canvas.drawLine(600, 50 * (i + 1), 700, 50 * (i + 1), paint);
                                    canvas.drawTextOnPath("浓度" + str + "%", path, 0, 0, paint);
                                } else if (i == 1) {
                                    paint.setStrokeWidth(5);
                                    canvas.drawLine(600, 50 * (i + 1), 700, 50 * (i + 1), paint);
                                    canvas.drawTextOnPath("浓度" + str + "%", path, 0, 0, paint);
                                } else if (i == 2) {
                                    paint.setStrokeWidth(7);
                                    canvas.drawLine(600, 50 * (i + 1), 700, 50 * (i + 1), paint);
                                    canvas.drawTextOnPath("浓度" + str + "%", path, 0, 0, paint);
                                }
                                for (int j = 0; j < mPoints[i].length - 1; j++) {
                                    // canvas.drawPoint(j * (sv_width / mPoints[i].length), sv_height - 2 * mPoints[i][j], paint);
                                    canvas.drawLine(j * (sv_width / mPoints[i].length), sv_height - 2 * mPoints[i][j], (j + 1) * (sv_width / mPoints[i].length), sv_height - 2 * mPoints[i][j + 1], paint);
                                }}
                            holder.unlockCanvasAndPost(canvas); //释放canvas对象
                        } else if(checkedListItemList.size() > 3){
                            Toast.makeText(TransActivity.this, "最多只能选取3组数据", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(TransActivity.this, "请至少选择一组数据", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    private String extractNum(String str) {
        str = str.trim();
        String str2 = "";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    str2 += str.charAt(i);
                }
            }
        }
        return str2;
    }

    //找出被选中的酒精数据
    private void findSelected(List<ListItem> listItemList) {
        checkedListItemList.clear();
        for (int i = 0; i < listItemList.size(); i++) {
            if (listItemList.get(i).getIsChecked()) {
                checkedListItemList.add(listItemList.get(i));
            }
        }
    }

    //绘制SurfaceView底板
    private void drawBlank() {
        //在SurfaceView上绘制
        SurfaceHolder surfaceHolder1 = mSurface.getHolder();
        surfaceHolder1.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                //必须在该方法中获取Canvas对象，才能保证SurfaceView可用
                Canvas canvas = holder.lockCanvas(); //获得canvas对象
                //使用Canvas绘图
                //画布使用白色填充
                canvas.drawColor(Color.LTGRAY);
                Paint paint = new Paint();
                //创建蓝色画笔，宽度为5，绘制相关点
                paint.setStrokeWidth(5);
                holder.unlockCanvasAndPost(canvas); //释放canvas对象
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    //初始化酒精数据
    private void initAlcohol() {
        SharedPreferences sharedPreferences = getSharedPreferences("list_name", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            ListItem listItem = new ListItem(entry.getKey(), R.drawable.glass, false);
            listItemList.add(listItem);
        }
    }

    //获取选定的数据的选定通道的数据   返回数组维数：选取数据组数X横坐标点数
    private float[][] getDataBySingleChannel(List<ListItem> checkedListItemList, String channel) {
        int len = new Gson().fromJson(getSharedPreferences(checkedListItemList.get(0).getName(), MODE_PRIVATE).getString(channel, ""), String[].class).length;
        float[][] mPoints = new float[checkedListItemList.size()][len];
        for (int i = 0; i < checkedListItemList.size(); i++) {
            SharedPreferences sharedPreferences = getSharedPreferences(checkedListItemList.get(i).getName(), MODE_PRIVATE);
            String str = sharedPreferences.getString(channel, "");
            Gson gson = new Gson();
            String[] strings = gson.fromJson(str, String[].class);
            for (int j = 0; j < strings.length; j++) {
                try {
                    mPoints[i][j] = 3*Float.parseFloat(strings[j]);
                } catch (Exception e) {
                    //  Toast.makeText(CompareActivity.this,"数据转化出错！",Toast.LENGTH_SHORT).show();
                }
            }
        }
        return mPoints;
    }

    private float[][] getTransBySingleChannel(List<ListItem> checkedListItemList, String channel,float Bright[]) {
        int len = new Gson().fromJson(getSharedPreferences(checkedListItemList.get(0).getName(), MODE_PRIVATE).getString(channel, ""), String[].class).length;
        float[][] mPoints = new float[checkedListItemList.size()][len];
        for (int i = 0; i < checkedListItemList.size(); i++) {
            SharedPreferences sharedPreferences = getSharedPreferences(checkedListItemList.get(i).getName(), MODE_PRIVATE);
            String str = sharedPreferences.getString(channel, "");
            Gson gson = new Gson();
            String[] strings = gson.fromJson(str, String[].class);
//            float st[] = new float[strings.length];
//            for(int k = 0 ; k < strings.length ;k++){
//                st[k] = Float.parseFloat(strings[k]);
//            }
            //st = smoothPoints(st);
 //           Bright = smoothPoints(Bright);
            for (int j = 0; j < strings.length; j++) {
                try {
                    int offset = 45;
                    //mPoints[i][j] = ((3*Float.parseFloat(strings[j])<=Bright[j])?600*Float.parseFloat(strings[j])/Bright[j]:200);
                    mPoints[i][j] = 600*Float.parseFloat(strings[j])/Bright[j]; //没限制阈值，可能会大于1
                    mPoints[i][j] = (float) (mPoints[i][j] + (40/(1+0.01*0.01*0.2*j*j*j)) - offset) ;
                    mPoints[i][j] = (mPoints[i][j]<=(200-offset))?mPoints[i][j]:200;
                   // mPoints[i][j] = (600*(st[j]))/Bright[j];
                    Log.d("["+j+"]",""+mPoints[i][j]);
                } catch (Exception e) {
                    //  Toast.makeText(CompareActivity.this,"数据转化出错！",Toast.LENGTH_SHORT).show();
                }
            }
            mPoints[i] = smoothPoints(mPoints[i]);
        }
        return mPoints;
    }

    //均值平滑数组
    public static float[] smoothPoints(float magnitude[]){
        int length = magnitude.length;
        float smooth[] = new float[length];
        smooth[0] = magnitude[0];
        smooth[1] = magnitude[1];
        smooth[length-2] = magnitude[length-2];
        smooth[length-1] = magnitude[length-1];

        for (int i = 2; i<length-2; i++){
            smooth[i] = (magnitude[i-2]+magnitude[i-1]+magnitude[i]+magnitude[i+1]+magnitude[i+2])/5;
        }
        return smooth;
    }
}