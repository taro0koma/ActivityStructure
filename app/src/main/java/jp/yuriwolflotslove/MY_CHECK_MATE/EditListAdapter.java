package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class EditListAdapter extends BaseAdapter {
    private final static int RESULT_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;
    private int mLayoutID;
    private LayoutInflater mInflater;
    private ArrayList<ListItem> data = null;
    private Context mContext = null;
    private Activity mActivity = null;
    private int editItems = SecondActivity.editItems;
    private int[] buttonTags;
    private int[] textTags;

    public EditListAdapter(Activity activity, Context context, int itemLayoutId, ArrayList<ListItem> data) {
        super();
        mInflater = LayoutInflater.from(context);
        mLayoutID = itemLayoutId;
        this.data = data;
        mContext = context;
        mActivity = activity;

        buttonTags = new int[data.size()];
        textTags = new int[data.size()];
    }

    public int[] getButtonTags() {
        return buttonTags;
    }
    public int[] getTextTags() {
        return textTags;
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

    public String getEditText(int position){
        return data.get(position).getEditText().toString();
    }

    @Override
    public View getView(final int position, View view, final ViewGroup viewGroup) {
        final EditListView listView;
        final ListItem item = (ListItem)this.getItem(position);

        if (view == null) {
            view = mInflater.inflate(mLayoutID, null);
            listView = new EditListView();
            listView.listViewEdit = view.findViewById(R.id.editText1);
            listView.listViewBtn = view.findViewById(R.id.button1);
            listView.listViewLayout = view.findViewById(R.id.editLayout1);
            view.setTag(listView);
        } else {
            listView = (EditListView) view.getTag();
        }
        if (position == 0) {
            LayoutParams lp = listView.listViewLayout.getLayoutParams();
            MarginLayoutParams mlp = (MarginLayoutParams) lp;
            mlp.setMargins(40, 80, 40, 16);
            listView.listViewLayout.setLayoutParams(mlp);
        }
        if (position == editItems - 1) {
            LayoutParams lp = listView.listViewLayout.getLayoutParams();
            MarginLayoutParams mlp = (MarginLayoutParams) lp;
            mlp.setMargins(40, 0, 40, 160);
            listView.listViewLayout.setLayoutParams(mlp);
        }
        listView.listViewBtn.setImageResource(R.drawable.camera_merge);
        String str = "チェック場所　" + (position + 1);
        listView.listViewEdit.setHint(str);
        listView.listViewEdit.setText("");
        if(position==0){
            listView.listViewEdit.setFocusable(true);
        }
        listView.listViewEdit.setTextColor(mContext.getColor(R.color.colorPrimaryDark));
        listView.listViewEdit.getBackground().mutate().setColorFilter(mContext.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        listView.listViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) viewGroup).performItemClick(view, position, R.id.button1);
            }
        });

        listView.listViewEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if(!focus){
                    String editText = listView.listViewEdit.getText().toString();
                    item.setEditText(editText);
                    ((ListView) viewGroup).performItemClick(view, position, R.id.button1);

                }
            }
        });

//        listView.listViewEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                String editText = listView.listViewEdit.getText().toString();
//                item.setEditText(editText);
//                ((ListView) viewGroup).performItemClick(textView, position, R.id.button1);
//                return false;
//            }
//        });


        buttonTags[position]=listView.listViewBtn.getId();
        textTags[position]=listView.listViewEdit.getId();
        return view;
    }

    //ArrayListに格納するクラス　フィールドのみ
    static class EditListView {
        int buttonId;
        EditText listViewEdit;
        ImageView listViewBtn;
        TextView listViewAttention;
        ConstraintLayout listViewLayout;

        public int getButtonId() {
            return buttonId;
        }

        public void setButtonId(int buttonId) {
            this.buttonId = buttonId;
        }

        public EditText getListViewEdit() {
            return listViewEdit;
        }

        public void setListViewEdit(EditText listViewEdit) {
            this.listViewEdit = listViewEdit;
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
