package com.example.demo1.Spectrometer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.demo1.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import widget.ListItem;
import widget.MyAdapter;
import widget.MyDialog_Image;

import static com.example.demo1.Spectrometer.SpectrumActivity.getPicFromBytes;

public class AnalysisActivity extends AppCompatActivity implements LifecycleObserver {

    private static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private ImageButton mTakePhoto,mChoosePhoto,mProcess,mCompare;
    private Button mBtnChange;
    private ImageView mPicture,mAilab;
    private Uri imageUri;
    private String imagePath;
    private String fileName,filePath;
    private byte[] result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        //??????????????????????????????
        mTakePhoto = findViewById(R.id.take_photo);
        mChoosePhoto = findViewById(R.id.choose_photo);
        mPicture = findViewById(R.id.picture);
        mAilab = findViewById(R.id.aiLab);
        mProcess = findViewById(R.id.process);
        mCompare = findViewById(R.id.process_compare);
        mBtnChange = findViewById(R.id.btn_change);

        //????????????
        mAilab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.ailabcqu.com");    //?????????????????????
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        //???Bitmap?????????ImageView?????????
        mPicture.setBackgroundResource(R.drawable.nophoto);//????????????

        mBtnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog_Image myDialogImage = new MyDialog_Image(AnalysisActivity.this).setMessage("??????????????????");
                myDialogImage.show();
                myDialogImage.setYesOnclickListener(new MyDialog_Image.onYesOnclickListener() {
                    @Override
                    public void onYesOnclick() {
                        ListItem listItem = myDialogImage.findSelected(myDialogImage.listItemList);
                        if(myDialogImage.isSelected) {
                            mPicture.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                                    listItem.getMyResourceId()));//????????????
                            myDialogImage.dismiss();
                        }
                    }
                });
                myDialogImage.setNoOnclickListener(new MyDialog_Image.onNoOnclickListener() {
                    @Override
                    public void onNoOnclick() {
                        myDialogImage.dismiss();
                    }
                });
            }
        });

        //???????????????????????????
//        mTakePhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //??????Picture?????????
//                File fileDir = new File(Environment.getExternalStorageDirectory(),"Pictures");
//                if(!fileDir.exists()){
//                    fileDir.mkdirs();
//                }
//                //?????????????????? IMG_????????????.jpg
//                fileName = "IMG_"+System.currentTimeMillis()+".jpg";
//                //?????????????????????????????????????????????Picture???????????????
//                filePath = fileDir.getAbsolutePath()+"/"+ fileName;
//                ContentValues contentValues = new ContentValues();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Pictures");
//                }else {
//                    contentValues.put(MediaStore.Images.Media.DATA, filePath);
//                }
//                //contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG");
//                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                startActivityForResult(intent, TAKE_PHOTO);
//            }
//        });
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnalysisActivity.this,CameraActivity.class));
            }
        });
        mChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(AnalysisActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AnalysisActivity.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
            }
        });
        mProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AnalysisActivity.this, SpectrumActivity.class);
                if(imagePath != null) {
                    intent.putExtra("imagePath", imagePath);
                    imagePath = null;
                    startActivity(intent);
                }else{
                    try {
                        result = passBitmap(mPicture,100);
                        intent.putExtra("Bitmap", result);
                        startActivity(intent);
                        result = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AnalysisActivity.this, "?????????????????????", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        mCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnalysisActivity.this,CompareActivity.class));
            }
        });
    }
    private void openAlbum(){
        Intent intent = null;
        if(Build.VERSION.SDK_INT <= 19) {
            intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
        }else{
            intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent,CHOOSE_PHOTO); //????????????
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(this,"????????????????????????????????????", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @TargetApi(19)
    private String handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            /*?????????document?????????Uri????????????document id??????*/
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1]; //????????????????????????id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //?????????content?????????uri??????????????????????????????
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //?????????file?????????uri?????????????????????????????????
            imagePath = uri.getPath();
        }
        displayImage(imagePath); //??????????????????????????????
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
        return imagePath;
    }

    private String getImagePath(Uri uri,String selection){
        String path = null;
        //??????Uri???selection??????????????????????????????
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mPicture.setImageBitmap(bitmap);
            Toast.makeText(this,"width:"+bitmap.getWidth()+",height:"+bitmap.getHeight(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this,"??????????????????", Toast.LENGTH_SHORT).show();
        }else {
            //Toast.makeText(this,"??????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    public static void deleteImage(Context context, String filepath){
        String where=MediaStore.Audio.Media.DATA+"='"+ filepath +"'";
        int i =  context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,where,null);
    }

    public byte[] passBitmap(ImageView imageView,int sizeLimit) throws FileNotFoundException {
        int quality = 100;
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream output = new ByteArrayOutputStream();//????????????????????????
        //        // ???????????????????????????????????????????????????
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output);
        while(output.toByteArray().length / 1024 > sizeLimit) {
            // ??????
            output.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output);
            quality -= 5;
        }
        Log.d("size:  ",""+output.toByteArray().length / 1024);
        result = output.toByteArray();//???????????????  result????????????bit???????????????
        return result;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //??????????????????????????????
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imagePath = filePath;
                        mPicture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    deleteImage(this,filePath);
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    //???????????????????????????
                    if (Build.VERSION.SDK_INT >= 19){
                        //4.4?????????????????????????????????????????????
                        imagePath = handleImageOnKitKat(data);
                       // Toast.makeText(AnalysisActivity.this,"Build.VERSION.SDK_INT >= 19",Toast.LENGTH_SHORT).show();
                    }else{
                        imagePath = handleImageBeforeKitKat(data);
                    }
                }
            default:
                break;
        }
    }
}