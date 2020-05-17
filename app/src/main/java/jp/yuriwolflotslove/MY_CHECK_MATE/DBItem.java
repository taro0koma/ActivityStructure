package jp.yuriwolflotslove.MY_CHECK_MATE;

import java.util.ArrayList;

public class DBItem {
    ArrayList<String> placeNames = new ArrayList<>();
    ArrayList<String> photoPaths = new ArrayList<>();
    private String memo;
    private String dateTime;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


}
