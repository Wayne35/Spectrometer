package com.example.demo1.Spectrometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import widget.MyDialog_Image;
import widget.MyDialog_Mode;

public class CompareActivity extends AppCompatActivity {

    private ListView listView;
    private SurfaceView mSurface;
    private Button mBtnCompare, mBtnDelete, mBtnAlign;
    private List<ListItem> checkedListItemList = new ArrayList<ListItem>();
    private List<ListItem> listItemList = new ArrayList<ListItem>();
    private float[][] mPoints;
    private boolean isAnalysed = false;
    private String mode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        initAlcohol();
        MyAdapter myAdapter = new MyAdapter(CompareActivity.this, R.layout.layout_list, listItemList);
        listView = findViewById(R.id.lv_compare);
        listView.setAdapter(myAdapter);

        mSurface = findViewById(R.id.sv_compare);
        mBtnCompare = findViewById(R.id.btn_compare);
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnAlign = findViewById(R.id.btn_align);


        //获取手机屏幕的像素，并模拟光谱显示区域的高宽像素值
        Display display = getWindowManager().getDefaultDisplay();
        float sv_width = (float) (display.getWidth() * 0.9);
        float sv_height = (float) (display.getHeight() * 0.28);//模拟显示光谱区域的高宽像素值
        //绘制SurfaceView底板
        drawBlank();
        //获取R坐标数据
        drawGraphByChannel(sv_width, sv_height);
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog_Delete myDialogDelete = new MyDialog_Delete(CompareActivity.this).setMessage("确认删除该组数据么？");
                myDialogDelete.show();
                myDialogDelete.setYesOnclickListener(new MyDialog_Delete.onYesOnclickListener() {
                    @Override
                    public void onYesOnclick() {
                        deleteData();
                        myDialogDelete.dismiss();
                        Toast.makeText(CompareActivity.this, "成功删除数据！", Toast.LENGTH_SHORT).show();
                    }
                });
                myDialogDelete.setNoOnclickListener(new MyDialog_Delete.onNoOnclickListener() {
                    @Override
                    public void onNoOnclick() {
                        myDialogDelete.dismiss();
                        Toast.makeText(CompareActivity.this, "取消删除数据！", Toast.LENGTH_SHORT).show();
                    }
                });

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
                    mPoints[i][j] = Float.parseFloat(strings[j]);
                } catch (Exception e) {
                    //  Toast.makeText(CompareActivity.this,"数据转化出错！",Toast.LENGTH_SHORT).show();
                }
            }
        }
        return mPoints;
    }

    //绘制单一通道光成分曲线
    private void drawGraphByChannel(float sv_width, float sv_height) {
        SurfaceHolder surfaceHolder = mSurface.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mBtnCompare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isAnalysed = true;
                        MyDialog_Mode myDialogMode = new MyDialog_Mode(CompareActivity.this).setMessage("请选择一种模式");
                        checkedListItemList.clear();
                        //找出被选中的酒精数据
                        findSelected(listItemList);
                        if (checkedListItemList.size() != 0 && checkedListItemList.size() <= 3) {
                            myDialogMode.show();
                        } else if(checkedListItemList.size() > 3){
                            Toast.makeText(CompareActivity.this, "最多只能选取3组数据", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(CompareActivity.this, "请至少选择一组数据", Toast.LENGTH_SHORT).show();
                        }
                        myDialogMode.setYesOnclickListener(new MyDialog_Mode.onYesOnclickListener() {
                            @Override
                            public void onYesOnclick() {
                                mode = myDialogMode.mode;
                                mPoints = getDataBySingleChannel(checkedListItemList, myDialogMode.mode);
                                myDialogMode.dismiss();
                                //必须在该方法中获取Canvas对象，才能保证SurfaceView可用
                                Canvas canvas = holder.lockCanvas(); //获得canvas对象
                                //使用Canvas绘图
                                //画布使用白色填充
                                canvas.drawColor(Color.LTGRAY);
                                Paint paint = new Paint();
                                //创建画笔，宽度为5，绘制相关点
                                paint.setStrokeWidth(5);
                                if (myDialogMode.mode.equals("R")) {
                                    paint.setColor(Color.RED);
                                } else if (myDialogMode.mode.equals("G")) {
                                    paint.setColor(Color.GREEN);
                                } else if (myDialogMode.mode.equals("B")) {
                                    paint.setColor(Color.BLUE);
                                } else if (myDialogMode.mode.equals("grey")) {
                                    paint.setColor(Color.GRAY);
                                }else {
                                    paint.setColor(Color.BLACK);
                                }
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
                            }
                        });
                        myDialogMode.setNoOnclickListener(new MyDialog_Mode.onNoOnclickListener() {
                            @Override
                            public void onNoOnclick() {
                                myDialogMode.dismiss();
                            }
                        });
                    }
                });
                mBtnAlign.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isAnalysed){
                        alignData(mPoints);
                        checkedListItemList.clear();
                        //找出被选中的酒精数据
                        findSelected(listItemList);
                        if (checkedListItemList.size() == 0){
                            Toast.makeText(CompareActivity.this, "请先分析", Toast.LENGTH_SHORT).show();
                        }else if(checkedListItemList.size() > 3){
                            Toast.makeText(CompareActivity.this, "最多只能选取3组数据", Toast.LENGTH_SHORT).show();
                        }else {
                            //必须在该方法中获取Canvas对象，才能保证SurfaceView可用
                            Canvas canvas = holder.lockCanvas(); //获得canvas对象
                            //使用Canvas绘图
                            //画布使用白色填充
                            canvas.drawColor(Color.LTGRAY);
                            Paint paint = new Paint();
                            if (mode.equals("R")) {
                                paint.setColor(Color.RED);
                            } else if (mode.equals("G")) {
                                paint.setColor(Color.GREEN);
                            } else if (mode.equals("B")) {
                                paint.setColor(Color.BLUE);
                            } else if (mode.equals("grey")) {
                                paint.setColor(Color.GRAY);
                            } else {
                                paint.setColor(Color.BLACK);
                            }
                            for (int i = 0; i < checkedListItemList.size(); i++) {
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
                                }
                            }
                            holder.unlockCanvasAndPost(canvas); //释放canvas对象
                        }
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

    private void deleteData() {
        findSelected(listItemList);
        SharedPreferences sharedPreferences = getSharedPreferences("list_name", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int i = 0; i < checkedListItemList.size(); i++) {
            editor.remove(checkedListItemList.get(i).getName());
        }
        editor.commit();
        listItemList.clear();
        initAlcohol();
        listView.setAdapter(new MyAdapter(CompareActivity.this, R.layout.layout_list, listItemList));
    }

    private void alignData(float[][] data) {
        int min_Position = 0;
        int row = 0;
        int column = 0;
        if(data != null){
         row = data.length;
         column = data[0].length;
        }
        int[] position = new int[row];
        for (int i = 0; i < row; i++) {
            float max = 0;
            for (int j = 0; j < column; j++) {
                if (data[i][j] > max) {
                    max = data[i][j];
                    position[i] = j;
                }
            }
            Log.d("position" + "[" + i + "]", "" + position[i]);
        }

        for (int j = 0; j < row; j++) {
            int[] temp_p = position.clone();
            Arrays.sort(temp_p);
            min_Position = temp_p[0];
        }
        for (int k = 0; k < row; k++) {
            int shift = position[k] - min_Position;
            if (shift != 0) {
                for (int m = 0; m < data[k].length - shift; m++) {
                    data[k][m] = data[k][m + shift];
                }
                for (int n = data[k].length - shift; n < data[k].length; n++) {
                    data[k][n] = 0;
                }
            }
        }
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
}