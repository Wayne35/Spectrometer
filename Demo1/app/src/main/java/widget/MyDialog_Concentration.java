package widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.demo1.R;

public class MyDialog_Concentration extends Dialog {

    private Context mContext;
    private int concentration = 50;
    private boolean isConfirmed = false;
    private TextView mTv;  //提示输入浓度
    private EditText mEt;     //输入浓度
    private Button mBtnConfirm, mBtnCancel, mBtnPlus, mBtnMinus;     //确定按钮
    private onYesOnclickListener yesOnclickListener;
    private onNoOnclickListener noOnclickListener;

    public MyDialog_Concentration(@NonNull Context context) {
        super(context);
        mContext = context;
    }


    public int getConcentration(){
        return concentration;
    }

    public boolean getIsConfirmed(){
        return isConfirmed;
    }

    public void setYesOnclickListener(onYesOnclickListener yesOnclickListener) {
        this.yesOnclickListener = yesOnclickListener;
    }
    public void setNoOnclickListener(onNoOnclickListener noOnclickListener) {
        this.noOnclickListener = noOnclickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_dialog_concentration);
        //空白处不能取消动画
        setCanceledOnTouchOutside(false);
        mTv = findViewById(R.id.tv);
        mEt = findViewById(R.id.et);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnPlus = findViewById(R.id.btn_plus);
        mBtnMinus = findViewById(R.id.btn_minus);


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

        mEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("ResourceType")
            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if("".equals(str)){str = "0";}
                try {
                    if(Integer.parseInt(str)>=0 && Integer.parseInt(str)<=100){
                        concentration = Integer.parseInt(str);
                    }else{
                        mEt.setText(String.valueOf(50));
                        concentration = 50;
                        Toast.makeText(mContext,"请输入一个0到100之间的数",Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    mEt.setText(String.valueOf(50));
                    concentration = 50;
                    Toast.makeText(mContext,"输入无效",Toast.LENGTH_SHORT).show();
                };
            }
        });

        mBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int con = Integer.parseInt(mEt.getText().toString().trim());
                    if(con >= 95){
                    con = 100;
                    } else {
                    con = con + 5;
                    }
                    mEt.setText(String.valueOf(con));
            }
        });
        mBtnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int con = Integer.parseInt(mEt.getText().toString().trim());
                    if (con <= 5) {
                        con = 0;
                    } else {
                        con = con - 5;
                    }
                    mEt.setText(String.valueOf(con));
            }
        });
    }
    public interface onYesOnclickListener {
        public void onYesOnclick();
     }
    public interface onNoOnclickListener {
        public void onNoOnclick();
    }
}
