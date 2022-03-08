package widget;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.demo1.R;

import java.util.List;

public class MyAdaptor extends ArrayAdapter<Alcohol> {

    private int resourceId;

    public MyAdaptor(@NonNull Context context, int textViewResourceId, @NonNull List<Alcohol> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Alcohol alcohol = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        ImageView imageView = view.findViewById(R.id.iv_alcohol);
        TextView textView = view.findViewById(R.id.tv_name);
        CheckBox checkBox = view.findViewById(R.id.cb_checked);
        imageView.setImageResource(R.drawable.glass);
        textView.setText(alcohol.getName());
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()) {
                    alcohol.setIsChecked(true);
                }else{
                    alcohol.setIsChecked(false);
                }
            }
        });
        return view;
    }
}
