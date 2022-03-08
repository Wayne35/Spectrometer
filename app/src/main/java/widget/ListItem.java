package widget;

import android.media.Image;
import android.widget.ImageView;

import com.example.demo1.R;

public class ListItem {

    private  String name;
    private int resourceId;
    private  Boolean isChecked;

    public ListItem(String name, int resourceId, Boolean isChecked){
        this.name = name;
        this.isChecked = isChecked;
        this.resourceId = resourceId;
    }

    public ListItem(String name,  Boolean isChecked){
        this.name = name;
        this.isChecked = isChecked;
    }

    public int getMyResourceId() {
        return  this.resourceId;
    }

    public String getName() {
        return name;
    }
    public Boolean getIsChecked(){
        return isChecked;
    }
    public void setIsChecked(Boolean isChecked){
        this.isChecked = isChecked;
    }
}
