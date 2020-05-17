package jp.yuriwolflotslove.MY_CHECK_MATE;

public class ListItem {
    private int id;
    private String editText = "";
    private String photoPath = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEditText() {
        return editText;
    }

    public void setEditText(String editText) {
        this.editText = editText;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
