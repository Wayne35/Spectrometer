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

public class MyDialog_Concentration2 extends Dialog {

    private Context mContext;
    private int concentration = 50;
    private boolean isConfirmed = false;
    private EditText mEt;     //输入浓度
    private Button mBtnConfirm, mBtnCancel;     //确定按钮
    private onYesOnclickListener yesOnclickListener;
    private onNoOnclickListener noOnclickListener;

    public MyDialog_Concentration2(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public String getText(){
        return mEt.getText().toString();
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
        setContentView(R.layout.my_dialog_concentration2);
        //空白处不能取消动画
        setCanceledOnTouchOutside(false);
        mEt = findViewById(R.id.et);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnCancel = findViewById(R.id.btn_cancel);


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
                mEt.removeTextChangedListener(this);
                try{
                    mEt.setText(s.toString());
                    mEt.setSelection(s.toString().length());
                }catch (Exception e){
                }
                mEt.addTextChangedListener(this);
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
