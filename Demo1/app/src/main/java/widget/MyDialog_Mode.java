package widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.demo1.R;

public class MyDialog_Mode extends Dialog {

    private RadioGroup mRg;
    private Button mBtnConfirm, mBtnCancel;
    private TextView mTextView;
    private String message;
    private onYesOnclickListener yesOnclickListener;
    private onNoOnclickListener noOnclickListener;
    public String mode = "R";

    public MyDialog_Mode(@NonNull Context context) {
        super(context);
    }

    public void setYesOnclickListener(onYesOnclickListener yesOnclickListener) {
        this.yesOnclickListener = yesOnclickListener;
    }
    public void setNoOnclickListener(onNoOnclickListener noOnclickListener) {
        this.noOnclickListener = noOnclickListener;
    }

    public  MyDialog_Mode setMessage(String message){
        this.message = message;
        return  this;
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_dialog_mode);
        //空白处不能取消动画
        setCanceledOnTouchOutside(false);
        mRg = findViewById(R.id.rg);
        mTextView = findViewById(R.id.tv);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnCancel = findViewById(R.id.btn_cancel);
        mTextView.setText(message);
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
        mRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_r: mode = "R";
                        break;
                    case R.id.rb_g: mode = "G";
                        break;
                    case R.id.rb_b: mode = "B";
                        break;
                    case R.id.rb_grey: mode = "grey";
                        break;
                }
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
