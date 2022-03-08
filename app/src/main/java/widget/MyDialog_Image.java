package widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.demo1.R;
import com.example.demo1.Spectrometer.AnalysisActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MyDialog_Image extends Dialog {

    public boolean isSelected = false;
    private String message;
    private Button mBtnConfirm, mBtnCancel;
    private TextView mTextView;
    private ListView listView;
    public List<ListItem> listItemList = new ArrayList<ListItem>();
    private List<ListItem> checkedListItemList = new ArrayList<ListItem>();
    private onYesOnclickListener yesOnclickListener;
    private onNoOnclickListener noOnclickListener;

    public MyDialog_Image(@NonNull Context context) {
        super(context);
    }

    public void setYesOnclickListener(onYesOnclickListener yesOnclickListener) {
        this.yesOnclickListener = yesOnclickListener;
    }
    public void setNoOnclickListener(onNoOnclickListener noOnclickListener) {
        this.noOnclickListener = noOnclickListener;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_dialog_image);
        //空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.));
        //一定要在setContentView之后调用，否则无效
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1200);
        mTextView = findViewById(R.id.tv);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnCancel = findViewById(R.id.btn_cancel);

        mTextView.setText(message);
        try {
            initPicture();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        MyAdapter myAdapter = new MyAdapter(getContext(),R.layout.layout_list, listItemList);
        listView = findViewById(R.id.lv_analysis);
        listView.setAdapter(myAdapter);

        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onYesOnclick();
                }
            }
        });
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (noOnclickListener != null) {
                    noOnclickListener.onNoOnclick();
                }
            }
        });

    }

    public MyDialog_Image setMessage(String message){
        this.message = message;
        return this;
    }

    public void initPicture() throws NoSuchFieldException, IllegalAccessException {
        //获取drawable文件名列表，不包含扩展名
        Field[] fields = R.drawable.class.getDeclaredFields();
        for(Field field:fields){
        	/*获取文件名对应的系统生成的id
        	需指定包路径 getClass().getPackage().getName()
        	指定资源类型drawable*/
            int resID = R.drawable.class.getDeclaredField(field.getName()).getInt(null);
//            System.out.println("fileName = " + field.getName()
//                    + "    resId = " + resID);
            listItemList.add(new ListItem(field.getName(),resID,false));
        }
    }
    public interface onYesOnclickListener {
        public void onYesOnclick();
    }
    public interface onNoOnclickListener {
        public void onNoOnclick();
    }

    //找出被选中的
    public ListItem findSelected(List<ListItem> listItemList){
        checkedListItemList.clear();
        for(int i = 0; i < listItemList.size(); i++){
            if(listItemList.get(i).getIsChecked()){
                checkedListItemList.add(listItemList.get(i));
            }
        }
        if(checkedListItemList.size() == 0){
            Toast.makeText(getContext(),"请选择一组数据",Toast.LENGTH_SHORT).show();
        }else if(checkedListItemList.size() == 1){
            isSelected = true;
        }else{
            Toast.makeText(getContext(),"选择了"+checkedListItemList.size()+"组数据",Toast.LENGTH_SHORT).show();
        }
        return isSelected?checkedListItemList.get(0):null;
    }
}
