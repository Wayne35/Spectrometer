package widget;

public class Alcohol {

    private  String name;
    private  Boolean isChecked;

    public Alcohol(String name, Boolean isChecked){
        this.name = name;
        this.isChecked = isChecked;
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
