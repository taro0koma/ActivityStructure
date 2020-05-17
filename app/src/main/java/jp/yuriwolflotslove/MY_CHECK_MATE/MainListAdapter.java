package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class MainListAdapter extends BaseAdapter {
    private int mLayoutID;
    private LayoutInflater mInflater;
    private ArrayList<ListItem> data = null;
    private Context mContext = null;
    private Activity mActivity = null;
    private int editItems;
    private Uri uri;

    public MainListAdapter(Activity activity, Context context, int itemLayoutId, ArrayList<ListItem> data) {
        super();
        mInflater = LayoutInflater.from(context);
        mLayoutID = itemLayoutId;
        this.data = data;
        mContext = context;
        mActivity = activity;
        editItems = data.size();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(final int position, View view, final ViewGroup viewGroup) {
        final MainListView listView;

        if (view == null) {
            view = mInflater.inflate(mLayoutID, null);
            listView = new MainListView();
            listView.mainText = view.findViewById(R.id.main_text1);
            listView.listViewBtn = view.findViewById(R.id.main_button1);
            listView.listViewLayout = view.findViewById(R.id.main_layout1);
            view.setTag(listView);
        } else {
            listView = (MainListView) view.getTag();
        }

        if (position == 0) {
            LayoutParams lp = listView.listViewLayout.getLayoutParams();
            MarginLayoutParams mlp = (MarginLayoutParams) lp;
            mlp.setMargins(40, 80, 40, 4);
            listView.listViewLayout.setLayoutParams(mlp);
        }
        if (position == editItems - 1) {
            LayoutParams lp = listView.listViewLayout.getLayoutParams();
            MarginLayoutParams mlp = (MarginLayoutParams) lp;
            mlp.setMargins(40, 0, 40, 160);
            listView.listViewLayout.setLayoutParams(mlp);
        }
        if (editItems == 1) {
            LayoutParams lp = listView.listViewLayout.getLayoutParams();
            MarginLayoutParams mlp = (MarginLayoutParams) lp;
            mlp.setMargins(40, 80, 40, 160);
            listView.listViewLayout.setLayoutParams(mlp);
        }
        listView.listViewBtn.setImageResource(R.drawable.ic_camera);
        String str = data.get(position).getEditText();
        listView.mainText.setText(str);
        listView.mainText.setTextColor(mContext.getColor(R.color.darkblue));
        if(data.get(position).getPhotoPath().length()>0) {
            uri = Uri.parse(data.get(position).getPhotoPath());
            listView.listViewBtn.setImageURI(uri);
        }

        return view;
    }

    //ArrayListに格納するクラス　フィールドのみ
    static class MainListView {
        int buttonId;
        TextView mainText;
        ImageView listViewBtn;
        ConstraintLayout listViewLayout;

        public int getButtonId() {
            return buttonId;
        }

        public void setButtonId(int buttonId) {
            this.buttonId = buttonId;
        }

        public TextView getListViewEdit() {
            return mainText;
        }

        public void setListViewEdit(EditText listViewEdit) {
            this.mainText = listViewEdit;
        }

        public ImageView getListViewBtn() {
            return listViewBtn;
        }

        public void setListViewBtn(ImageView listViewBtn) {
            this.listViewBtn = listViewBtn;
        }

        public ConstraintLayout getListViewLayout() {
            return listViewLayout;
        }

        public void setListViewLayout(ConstraintLayout listViewLayout) {
            this.listViewLayout = listViewLayout;
        }
    }
}
