package com.example.demo1.Spectrometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo1.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import widget.MyDialog_Concentration;
import widget.MyDialog_Delete;
import widget.VerticalSeekBar;

public class SpectrumActivity extends AppCompatActivity {

    private ImageView mPassPic;
    private SurfaceView mSpectrum;
    private Button mBtnSpec,mBtnSmooth,mBtnSave,mBtnGrey;
    private  Bitmap mBitmap;//转换过的Bitmap
    private VerticalSeekBar mSeekBar;
    private TextView mRGB_max;
    private EditText mHeight;
    private ArrayList<String> arrPackage_R,arrPackage_G,arrPackage_B,arrPackage_grey;
    private SharedPreferences sharedPreferences,sharedPreferences_con;
    boolean isAnalysed = false;
    public ArrayList<String> strArray = new ArrayList<String> ();
    String imagePath;
    String mode = null;
    float [][] mPoints;
    int mProgress;
    int concentration; //保存一组数据时的命名
    private byte[] res;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectrum);
        //引用布局文件中的控件
        mPassPic = findViewById(R.id.passPic);//关联ImageView
        mSpectrum = findViewById(R.id.spectrum);//关联Button
        mBtnSpec = findViewById(R.id.btn_spec);
        mBtnSave = findViewById(R.id.btn_save);
        mBtnSmooth = findViewById(R.id.btn_smooth);
        mBtnGrey = findViewById(R.id.btn_grey);
        mBtnSave = findViewById(R.id.btn_save);
        mSpectrum = findViewById(R.id.spectrum);//关联SurfaceView
        mSeekBar = findViewById(R.id.position);//关联SeekBar
        mRGB_max = findViewById(R.id.RGB_max);//
        mHeight = findViewById(R.id.height);

        //处理前一界面传来的图片，并加以展示
        imagePath = getIntent().getStringExtra("imagePath");//获得传递的byte数组
        res = getIntent().getByteArrayExtra("Bitmap");

        if(imagePath != null){
            mBitmap = BitmapFactory.decodeFile(imagePath);
        }else if(res != null){
            mBitmap = getPicFromBytes(res,null);
        }

        mPassPic.setImageBitmap(mBitmap);
        mPoints = new float[4][mBitmap.getWidth()];

        //获取手机屏幕的像素，并模拟光谱显示区域的高宽像素值
        Display display = getWindowManager().getDefaultDisplay();
        float sv_width = (float) (display.getWidth()*0.9);
        float sv_height = (float) (display.getHeight()*0.28);//模拟显示光谱区域的高宽像素值

        //初始化sharedPreferences_con
        sharedPreferences_con = getSharedPreferences("list_name",MODE_APPEND);

        //设置拖动按钮的初始进度
        mSeekBar.setProgress(50);
        mHeight.setText(""+mSeekBar.getProgress());
        mRGB_max.setText("R_max:0"+"\n"+"G_max:0"+"\n"+"B_max:0");
        mHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if("".equals(str)){str = "0";}
                try {
                    if(Integer.parseInt(str)>=0 && Integer.parseInt(str)<=100){
                        mSeekBar.setProgress(Integer.parseInt(str));
                    }else{
                        mSeekBar.setProgress(100);
                        mHeight.setText(""+mSeekBar.getProgress());
                    }

                }catch (Exception e){
                    Toast.makeText(SpectrumActivity.this,"输入无效",Toast.LENGTH_SHORT).show();
                    mSeekBar.setProgress(50);
                    mHeight.setText(""+mSeekBar.getProgress());
                };
            }
        });
        mSeekBar.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
            @Override
            public void onStart(VerticalSeekBar slideView, int progress) {

            }

            @Override
            public void onProgress(VerticalSeekBar slideView, int progress) {
                mHeight.setText(""+mSeekBar.getProgress());
            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {

            }
        });
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAnalysed){
                    Toast.makeText(SpectrumActivity.this,"请进行分析！",Toast.LENGTH_SHORT).show();
                }else{
                MyDialog_Concentration myDialogConcentration = new MyDialog_Concentration(SpectrumActivity.this);
                myDialogConcentration.show();
                myDialogConcentration.setYesOnclickListener(new MyDialog_Concentration.onYesOnclickListener() {
                    @Override
                    public void onYesOnclick() {
                            myDialogConcentration.dismiss();
                            concentration = myDialogConcentration.getConcentration();
                        //检查是否有重名
                        Boolean coverFlag = false;
                        Map<String, ?> allEntries = getSharedPreferences("list_name",MODE_PRIVATE).getAll();
                        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                            if(entry.getKey().equals("浓度为"+concentration+"%的酒精")){
                                coverFlag = true;
                            }
                        }
                        if(coverFlag){
                            MyDialog_Delete myDialogDelete = new MyDialog_Delete(SpectrumActivity.this).setMessage("已存在同名文件,确定覆盖之前的数据吗？");
                            myDialogDelete.show();
                            myDialogDelete.setYesOnclickListener(new MyDialog_Delete.onYesOnclickListener() {
                                @Override
                                public void onYesOnclick() {
                                    myDialogDelete.dismiss();
                                    sharedPreferences = getSharedPreferences("浓度为"+concentration+"%的酒精",MODE_PRIVATE);
                                    arrPackage_R = new ArrayList<>();
                                    arrPackage_G = new ArrayList<>();
                                    arrPackage_B = new ArrayList<>();
                                    arrPackage_grey = new ArrayList<>();
                                    int len = mBitmap.getWidth()-1;
                                    String s = null;
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    SharedPreferences.Editor editor1 = sharedPreferences_con.edit();
                                    editor1.putString("浓度为"+concentration+"%的酒精","这里是什么DOU无所谓");
                                    for(int i = 0; i<4; i++)
                                        for(int j = 0; j<len; j++){
                                            s =Float.toString(mPoints[i][j]).trim();
                                            if(i == 0){
                                                arrPackage_R.add(s);
                                            }else if(i == 1){
                                                arrPackage_G.add(s);
                                            }else if(i ==2){
                                                arrPackage_B.add(s);
                                            }else if(i ==3){
                                                arrPackage_grey.add(s);
                                            }
                                            editor.putString("R",new Gson().toJson(arrPackage_R));
                                            editor.putString("G",new Gson().toJson(arrPackage_G));
                                            editor.putString("B",new Gson().toJson(arrPackage_B));
                                            editor.putString("grey",new Gson().toJson(arrPackage_grey));
                                            editor.commit();
                                            editor1.commit();
                                        }
                                        Toast.makeText(SpectrumActivity.this,"已新保存浓度为"+concentration+"%的酒精的数据",Toast.LENGTH_SHORT).show();
                                }
                            });
                            myDialogDelete.setNoOnclickListener(new MyDialog_Delete.onNoOnclickListener() {
                                @Override
                                public void onNoOnclick() {
                                    myDialogDelete.dismiss();
                                }
                            });
                        }else{
                            sharedPreferences = getSharedPreferences("浓度为"+concentration+"%的酒精",MODE_PRIVATE);
                            arrPackage_R = new ArrayList<>();
                            arrPackage_G = new ArrayList<>();
                            arrPackage_B = new ArrayList<>();
                            arrPackage_grey = new ArrayList<>();
                            int len = mBitmap.getWidth()-1;
                            String s = null;
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            SharedPreferences.Editor editor1 = sharedPreferences_con.edit();
                            editor1.putString("浓度为"+concentration+"%的酒精","这里是什么DOU无所谓");
                            for(int i = 0; i<4; i++)
                                for(int j = 0; j<len; j++){
                                    s =Float.toString(mPoints[i][j]).trim();
                                    if(i == 0){
                                        arrPackage_R.add(s);
                                    }else if(i == 1){
                                        arrPackage_G.add(s);
                                    }else if(i ==2){
                                        arrPackage_B.add(s);
                                    }else if(i ==3){
                                        arrPackage_grey.add(s);
                                    }
                                    editor.putString("R",new Gson().toJson(arrPackage_R));
                                    editor.putString("G",new Gson().toJson(arrPackage_G));
                                    editor.putString("B",new Gson().toJson(arrPackage_B));
                                    editor.putString("grey",new Gson().toJson(arrPackage_grey));
                                    editor.commit();
                                    editor1.commit();
                                }
                            Toast.makeText(SpectrumActivity.this,"已保存浓度为"+concentration+"%的酒精的数据",Toast.LENGTH_SHORT).show();
                        }
                        }
                });
                myDialogConcentration.setNoOnclickListener(new MyDialog_Concentration.onNoOnclickListener() {
                    @Override
                    public void onNoOnclick() {
                        myDialogConcentration.dismiss();
                    }
                });
            } }
        });

        //在SurfaceView上绘制
        drawBlank();
        SurfaceHolder surfaceHolder2 = mSpectrum.getHolder();
        surfaceHolder2.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {

        mBtnSpec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = "spec";
                //分析图片后，方可保存
                isAnalysed = true;
                //将图片处理的光谱结果显示在SurfaceView上
                mProgress=mSeekBar.getProgress();
                mPoints = magnitudePoints(mBitmap,mProgress);//获取RGB三个强度数组，元素数量为mBitmap.getWidth()
                //显示三个通道的最大强度
                mRGB_max.setText("R_max:"+getMaxPix(mPoints[0])+"\n"+"G_max:"+getMaxPix(mPoints[1])+"\n"+"B_max:"+getMaxPix(mPoints[2]));
                //必须在该方法中获取Canvas对象，才能保证SurfaceView可用
                Canvas canvas = holder.lockCanvas(); //获得canvas对象
                //使用Canvas绘图
                //画布使用白色填充
                canvas.drawColor(Color.LTGRAY);
                Paint paint = new Paint();
                //创建蓝色画笔，宽度为3，绘制文字路径和文字
                Path path = new Path();
                //屏幕左上角（100,100）到（1000,100）画一条直线
                path.moveTo(100, 100);
                path.lineTo(1000, 100);
                paint.setColor(Color.BLACK);
                paint.setTextSize(60);
                canvas.drawTextOnPath("高度为"+mSeekBar.getProgress()+"时的RGB强度", path, 0, 0, paint);
                //创建蓝色画笔，宽度为3，绘制相关点
                paint.setStrokeWidth(5);
                for(int i = 0; i < 3; i++)
                    for(int j = 0; j < mBitmap.getWidth()-1; j++){
                        if(i == 0){
                            paint.setColor(Color.RED);
                        }else if (i == 1){
                            paint.setColor(Color.GREEN);
                        }else{
                            paint.setColor(Color.BLUE);
                        }
                        canvas.drawPoint(j*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[i][j], paint);
                        canvas.drawLine(j*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[i][j],(j+1)*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[i][j+1],paint);
                    }
                holder.unlockCanvasAndPost(canvas); //释放canvas对象
            }
        });
        mBtnGrey.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mode = "grey";
                        //分析图片后，方可保存
                        isAnalysed = true;
                        //将图片处理的光谱结果显示在SurfaceView上
                        mProgress=mSeekBar.getProgress();
                        mPoints = magnitudePoints(mBitmap,mProgress);//获取RGB三个强度数组，元素数量为mBitmap.getWidth()
                        //显示三个通道的最大强度
                        mRGB_max.setText("R_max:"+getMaxPix(mPoints[0])+"\n"+"G_max:"+getMaxPix(mPoints[1])+"\n"+"B_max:"+getMaxPix(mPoints[2]));
                        //必须在该方法中获取Canvas对象，才能保证SurfaceView可用
                        Canvas canvas = holder.lockCanvas(); //获得canvas对象
                        //使用Canvas绘图
                        //画布使用白色填充
                        canvas.drawColor(Color.LTGRAY);
                        Paint paint = new Paint();
                        //创建蓝色画笔，宽度为3，绘制文字路径和文字
                        Path path = new Path();
                        //屏幕左上角（100,100）到（1000,100）画一条直线
                        path.moveTo(100, 100);
                        path.lineTo(1000, 100);
                        paint.setColor(Color.BLACK);
                        paint.setTextSize(60);
                        canvas.drawTextOnPath("高度为"+mSeekBar.getProgress()+"时的灰度强度", path, 0, 0, paint);
                        //创建蓝色画笔，宽度为3，绘制相关点
                        paint.setStrokeWidth(5);
                            for(int j = 0; j < mBitmap.getWidth()-1; j++){
                                paint.setColor(Color.GRAY);
                                canvas.drawPoint(j*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[3][j], paint);
                                canvas.drawLine(j*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[3][j],(j+1)*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[3][j+1],paint);
                            }
                        holder.unlockCanvasAndPost(canvas); //释放canvas对象
                    }
                });
        mBtnSmooth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //分析图片后，方可保存
                        isAnalysed = true;
                        //将图片处理的光谱结果显示在SurfaceView上
                        mProgress=mSeekBar.getProgress();
                        mPoints = magnitudePoints(mBitmap,mProgress);//获取RGB三个强度数组，元素数量为mBitmap.getWidth()
                        mPoints[0] = smoothPoints(mPoints[0]);
                        mPoints[1] = smoothPoints(mPoints[1]);
                        mPoints[2] = smoothPoints(mPoints[2]);
                        mPoints[3] = smoothPoints(mPoints[3]);
                        mRGB_max.setText("R_max:"+getMaxPix(mPoints[0])+"\n"+"G_max:"+getMaxPix(mPoints[1])+"\n"+"B_max:"+getMaxPix(mPoints[2]));
                        //必须在该方法中获取Canvas对象，才能保证SurfaceView可用
                        Canvas canvas = holder.lockCanvas(); //获得canvas对象
                        //使用Canvas绘图
                        //画布使用白色填充
                        canvas.drawColor(Color.LTGRAY);
                        Paint paint = new Paint();
                        //创建蓝色画笔，宽度为3，绘制文字路径和文字
                        Path path = new Path();
                        //屏幕左上角（100,100）到（1000,100）画一条直线
                        path.moveTo(100, 100);
                        path.lineTo(1000, 100);
                        paint.setColor(Color.BLACK);
                        paint.setTextSize(60);
                        //创建蓝色画笔，宽度为3，绘制相关点
                        paint.setStrokeWidth(5);
                        if(mode == "spec"){
                            canvas.drawTextOnPath("高度为"+mSeekBar.getProgress()+"时的RGB强度", path, 0, 0, paint);
                        for(int i = 0; i < 3; i++)
                            for(int j = 0; j < mBitmap.getWidth()-1; j++){
                                if(i == 0){
                                    paint.setColor(Color.RED);
                                }else if (i == 1){
                                    paint.setColor(Color.GREEN);
                                }else if(i == 2){
                                    paint.setColor(Color.BLUE);
                                }
                                canvas.drawPoint(j*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[i][j], paint);
                                canvas.drawLine(j*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[i][j],(j+1)*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[i][j+1],paint);
                            }}
                        if(mode == "grey"){
                            canvas.drawTextOnPath("高度为"+mSeekBar.getProgress()+"时的灰度强度", path, 0, 0, paint);
                            for(int j = 0; j < mBitmap.getWidth()-1; j++){
                                    paint.setColor(Color.GRAY);
                                    canvas.drawPoint(j*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[3][j], paint);
                                    canvas.drawLine(j*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[3][j],(j+1)*(sv_width/mBitmap.getWidth()) ,sv_height-2*mPoints[3][j+1],paint);
                                }}
                        holder.unlockCanvasAndPost(canvas); //释放canvas对象
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

    //将Byte[]转换成Bitmap
    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,  opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }

    //输入Bitmap，返回图片的RGB强度数组
    public static float[][] magnitudePoints(Bitmap bitmap, int Progress){

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float [][]rgb = new float[4][width];
        int argb;

        for (int i = 0; i < width; i++) {
            if(Progress != 0) {
                argb = bitmap.getPixel(i, height - ((height * Progress) / 100));
            }else {
                argb = bitmap.getPixel(i, height - 1);
            }
            rgb[0][i] = (argb & 0x00ff0000) >> 16; // 取高两位
            rgb[1][i] = (argb & 0x0000ff00) >> 8; // 取中两位
            rgb[2][i] = argb & 0x000000ff; // 取低两位
            rgb[3][i] = 2*(float) (rgb[0][i] * 0.3 +  rgb[1][i]  * 0.59 + rgb[2][i]  * 0.11);
        }
        return rgb;
    }

    public static float getMaxPix(float[] Channel){
        float max = 0;
        for(int i = 1;i<Channel.length;i++){
            if(Channel[i]>max){
                max = Channel[i];
            }
        }
        return max;
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
    //绘制底板
    private void drawBlank(){
        SurfaceHolder surfaceHolder1 = mSpectrum.getHolder();
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

}