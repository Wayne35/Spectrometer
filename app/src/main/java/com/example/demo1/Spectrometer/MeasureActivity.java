package com.example.demo1.Spectrometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.DataPoint;
import utils.RegressionLine;
import widget.ListItem;
import widget.MyAdapter;
import widget.MyDialog_Delete;

public class MeasureActivity extends AppCompatActivity {

    private float a,b;
    private float concentration;
    private ImageView mAilab;
    private ListView listView;
    private float sumData[][];
    private Button mBtnConcentration,mBtnSave,mBtnDelete;
    private List<ListItem> checkedListItemList = new ArrayList<ListItem>();
    private List<ListItem> listItemList = new ArrayList<ListItem>();
    private List<ListItem> known_listItemList = new ArrayList<ListItem>();
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
       initAlcohol();
        RegressionLine line = new RegressionLine();
            for(int j = 0 ; j < sumData[0].length; j++){
                line.addDataPoint(new DataPoint(sumData[j][0], sumData[j][1]));
        }
        a = line.getA1();
        b = line.getA0();
        Log.d("a和b","a:"+a+",b:"+b);
        //定义控件
        MyAdapter myAdapter = new MyAdapter(MeasureActivity.this, R.layout.layout_list, listItemList);
        listView = findViewById(R.id.lv_compare);
        listView.setAdapter(myAdapter);

        mAilab = findViewById(R.id.aiLab);
        mBtnConcentration = findViewById(R.id.btn_compare);
        mBtnSave = findViewById(R.id.btn_bright);
        mBtnDelete = findViewById(R.id.btn_delete);

        //获取手机屏幕的像素，并模拟光谱显示区域的高宽像素值
        Display display = getWindowManager().getDefaultDisplay();
        float sv_width = (float) (display.getWidth() * 0.9);
        float sv_height = (float) (display.getHeight() * 0.28);//模拟显示光谱区域的高宽像素值

        //网页跳转
        mAilab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.ailabcqu.com");    //设置跳转的网站
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog_Delete myDialogDelete = new MyDialog_Delete(MeasureActivity.this).setMessage("确认删除该组数据么？");
                myDialogDelete.show();
                myDialogDelete.setYesOnclickListener(new MyDialog_Delete.onYesOnclickListener() {
                    @Override
                    public void onYesOnclick() {
                        deleteData();
                        myDialogDelete.dismiss();
                        Toast.makeText(MeasureActivity.this, "成功删除数据！", Toast.LENGTH_SHORT).show();
                    }
                });
                myDialogDelete.setNoOnclickListener(new MyDialog_Delete.onNoOnclickListener() {
                    @Override
                    public void onNoOnclick() {
                        myDialogDelete.dismiss();
                        Toast.makeText(MeasureActivity.this, "取消删除数据！", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        mBtnConcentration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedListItemList.clear();
                findSelected(listItemList);
                if(checkedListItemList.size() == 1){
                    //int dev = (int) sumAll(checkedListItemList);
                    float sum = sumAll(checkedListItemList);
                    concentration = (sum - b)/a;
                    if(concentration > 100){
                        concentration = 100.f;
                    }else if(concentration < 0) {
                        concentration = 0.f;
                    }else{
                        concentration=(float)(Math.round(concentration*100)/100);
                    }

                    Toast.makeText(MeasureActivity.this, "a:"+a+"b:"+b, Toast.LENGTH_SHORT).show();
                    tipDialog();
                }else if(checkedListItemList.size() > 1){
                    Toast.makeText(MeasureActivity.this, "只能选择一组待测数据", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MeasureActivity.this, "请选择一组待测数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //初始化酒精数据
    private void initAlcohol() {
        SharedPreferences sharedPreferences = getSharedPreferences("unknown_name", MODE_PRIVATE);
        SharedPreferences sharedPreferences2 = getSharedPreferences("list_name", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        Map<String, ?> allEntries2 = sharedPreferences2.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            ListItem listItem = new ListItem(entry.getKey(), R.drawable.glass, false);
            listItemList.add(listItem);
        }
        for (Map.Entry<String, ?> entry : allEntries2.entrySet()) {
            ListItem listItem = new ListItem(entry.getKey(), R.drawable.glass, false);
            known_listItemList.add(listItem);
        }
        sumData = sumAll2(known_listItemList);
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

    private float sumAll(List<ListItem> checkedListItemList){
        float sum = 0;
        SharedPreferences sharedPreferences = getSharedPreferences(checkedListItemList.get(0).getName(), MODE_PRIVATE);
            String str = sharedPreferences.getString("crop", "");
            Gson gson = new Gson();
            String[] strings = gson.fromJson(str, String[].class);
//            for (int i = 0; i < strings.length; i++) {
//                try {
//                    sum += Float.parseFloat(strings[i]);
//                } catch (Exception e) {
//
//                }
//            }
        sum = (Float.parseFloat(strings[6])+Float.parseFloat(strings[5])+Float.parseFloat(strings[7]))-
                (Float.parseFloat(strings[67])+Float.parseFloat(strings[66])+Float.parseFloat(strings[68])) ;
      //  Toast.makeText(MeasureActivity.this,"差值:"+ sum,Toast.LENGTH_SHORT).show();

        return sum;
    }

    //获取选定的数据的选定通道的数据   返回数组维数：选取数据组数X横坐标点数
    private float[][] sumAll2(List<ListItem> ListItemList) {
        float sum[][] = new float[ListItemList.size()][2];
        for (int i = 0; i < ListItemList.size(); i++) {
            SharedPreferences sharedPreferences = getSharedPreferences(ListItemList.get(i).getName(), MODE_PRIVATE);
            sum[i][0] = Float.parseFloat(extractNum(ListItemList.get(i).getName()));
            String str = sharedPreferences.getString("crop", "");
            Gson gson = new Gson();
            String[] strings = gson.fromJson(str, String[].class);
//            for (int j = 0; j < strings.length; j++) {
//                try {
//                   sum[i][1] += Float.parseFloat(strings[j]);
//                } catch (Exception e) {
//                    //  Toast.makeText(CompareActivity.this,"数据转化出错！",Toast.LENGTH_SHORT).show();
//                }
//            }
            sum[i][1] = (Float.parseFloat(strings[6])+Float.parseFloat(strings[5])+Float.parseFloat(strings[7]))-
                    (Float.parseFloat(strings[67])+Float.parseFloat(strings[66])+Float.parseFloat(strings[68])) ;
            // 新建键值对（key=总和，value=浓度）
          //  Log.d(ListItemList.get(i).getName(),""+sum[i][0]+","+sum[i][1]);
        }
        return sum;
    }
    private String extractNum(String str) {
        str = str.trim();
        String str2 = "";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if ((str.charAt(i) >= 48 && str.charAt(i) <= 57)||(str.charAt(i) == 46)) {
                    str2 += str.charAt(i);
                }
            }
        }
        return str2;
    }

    private void deleteData() {
        findSelected(listItemList);
        SharedPreferences sharedPreferences = getSharedPreferences("unknown_name", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int i = 0; i < checkedListItemList.size(); i++) {
            editor.remove(checkedListItemList.get(i).getName());
        }
        editor.commit();
        listItemList.clear();
        initAlcohol();
        listView.setAdapter(new MyAdapter(MeasureActivity.this, R.layout.layout_list, listItemList));
    }
    /**
     * 提示对话框
     */
    public void tipDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeasureActivity.this);
        builder.setTitle("提示");
        builder.setMessage("测出浓度:"+concentration);
        builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失

        //设置正面按钮
        builder.setPositiveButton("确定并更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences("浓度为"+concentration+"%的酒精", MODE_PRIVATE);
                SharedPreferences sharedPreferences_con = getSharedPreferences("list_name", MODE_PRIVATE);
                SharedPreferences sharedPreferences2 = getSharedPreferences(checkedListItemList.get(0).getName(), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                SharedPreferences.Editor editor1 = sharedPreferences_con.edit();
                editor1.putString("浓度为"+concentration+"%的酒精","这里是什么DOU无所谓");
                editor.putString("R", sharedPreferences2.getString("R",null));
                editor.putString("G", sharedPreferences2.getString("G",null));
                editor.putString("B", sharedPreferences2.getString("B",null));
                editor.putString("grey", sharedPreferences2.getString("grey",null));
                editor.putString("crop", sharedPreferences2.getString("crop",null));
                editor.apply();
                editor1.apply();
                deleteData();
                dialog.dismiss();
            }
        });

        //设置中立按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();      //创建AlertDialog对象
        dialog.show();                              //显示对话框
    }
}