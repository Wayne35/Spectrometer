package widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.demo1.R;

public class MyDialog_Delete extends Dialog {

    private String message;
    private Button mBtnConfirm, mBtnCancel;
    private TextView mTextView;
    private onYesOnclickListener yesOnclickListener;
    private onNoOnclickListener noOnclickListener;

    public MyDialog_Delete(@NonNull Context context) {
        super(context);
    }

    public void setYesOnclickListener(onYesOnclickListener yesOnclickListener) {
        this.yesOnclickListener = yesOnclickListener;
    }
    public void setNoOnclickListener(onNoOnclickListener noOnclickListener) {
        this.noOnclickListener = noOnclickListener;
    }

    public MyDialog_Delete setMessage(String message){
       this.message = message;
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_dialog_delete);
        //空白处不能取消动画
        setCanceledOnTouchOutside(false);
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
    }


    public interface onYesOnclickListener {
        public void onYesOnclick();
    }
    public interface onNoOnclickListener {
        public void onNoOnclick();
    }
}
