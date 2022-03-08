package widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.demo1.R;

import java.util.HashMap;
import java.util.List;

public class MyAdapter extends ArrayAdapter<ListItem> {

    private int resourceId;
    private List<ListItem> list;
    private HashMap<Integer, Boolean> tempMap;

    public MyAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<ListItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        list = objects;
        tempMap = new HashMap<Integer,Boolean>();
        for(int i=0;i<list.size();i++) {//初始化数据,刚进来的话把每一个都置为false
            tempMap.put(i, false);		//如果不初始化会报空指针的,毕竟你还没存东西就要取了撒
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ListItem listItem = getItem(position);
        if(convertView==null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(getContext(), R.layout.layout_list, null);
            viewHolder.textView =  convertView.findViewById(R.id.tv_name);
            viewHolder.checkBox = convertView.findViewById(R.id.cb_checked);
            viewHolder.imageView  = convertView.findViewById(R.id.iv_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imageView.setImageResource(listItem.getMyResourceId());
        viewHolder.textView.setText(listItem.getName());
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listItem.getIsChecked()) {
                    listItem.setIsChecked(false);
                }else{
                    listItem.setIsChecked(true);
                }
            }
        });
        //为checkbox添加复选监听,把当前位置的checkbox的状态存进一个HashMap里面
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tempMap.put(position, isChecked);//把当前位置的状态给存储起来
            }
        });
        //从hashmap里面取出我们的状态值,然后赋值给listview对应位置的checkbox
        viewHolder.checkBox.setChecked(tempMap.get(position));
        return convertView;
    }
    private ViewHolder viewHolder;
    public class ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public CheckBox checkBox;
    }
}
