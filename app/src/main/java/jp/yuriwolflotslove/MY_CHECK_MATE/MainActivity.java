package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static DBAdapter dbAdapter;
    static MainListAdapter listAdapter;
    ListItem item;
    private NonScrollListView listView = null;
    private ArrayList<ListItem> data;
    private String dateTime;
    private String memo="未作成";
    private Activity activity;
    private boolean DBStatus = false;


    protected void loadList() {
        dbAdapter.open();
        Cursor c = dbAdapter.getAllNotes();
        String dateTimeCatch = "";
        c.moveToFirst();
        if (c.moveToFirst()) {
            do {
                item = new ListItem();
                item.setId(c.getInt(c.getColumnIndex(DBAdapter.COL_ID)));
                item.setEditText(c.getString(c.getColumnIndex(DBAdapter.COL_PLACE)));
                item.setPhotoPath(c.getString(c.getColumnIndex(DBAdapter.COL_URI)));
                data.add(item);
                dateTimeCatch = c.getString(c.getColumnIndex(DBAdapter.COL_LASTUPDATE));
                memo = c.getString(c.getColumnIndex(DBAdapter.COL_MEMO));
                DBStatus = true;
            } while (c.moveToNext());
        }
        if (dateTimeCatch != null) {
            dateTime = dateTimeCatch;
        }

        stopManagingCursor(c);
        dbAdapter.close();

//        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");


        data = new ArrayList<>();
        listView = findViewById(R.id.mainListView);
        activity = this;
        dbAdapter = new DBAdapter(this);
        loadList();

        //スクローラーが下がる現象に対処
        ScrollView sv = (ScrollView) findViewById(R.id.main_scrollView);
        sv.smoothScrollTo(0, 0);


        Button button = (Button) findViewById(R.id.btn_checking);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SecondActivity.class);
                activity.startActivity(intent);
            }
        });

        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
    }

    @Override
    protected void onStart() {
        //カードにDBデータを表示
        TextView textPrimary = (TextView) findViewById(R.id.text_primary);
        TextView textSecondary = (TextView) findViewById(R.id.text_secondary);
        TextView memoView = (TextView) findViewById(R.id.text_memo);
        TextView placeView = (TextView) findViewById(R.id.text_detail);
        TextView statusView = (TextView) findViewById(R.id.status_text);
        ImageView icon = (ImageView) findViewById(R.id.icon_finished);
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.card_content);
        String first = "0000年00月00日(0)";
        String second = "00:00";
        StringBuilder places = new StringBuilder();
        if (dateTime != null) {
            int space = dateTime.indexOf(" ");
            if(space!=-1) {
                first = dateTime.substring(0, space);
                second = dateTime.substring(space + 1);
            }
            for (int i = 0; i < data.size(); i++) {
                places.append(data.get(i).getEditText());
                if (i != data.size() - 1) {
                    places.append(" ／ ");
                }
            }
        }
        String status = "あなたのチェック記録は未作成です。";
        if (DBStatus) {
            status = "あなたのチェック記録";
        }else{
            places.append("未作成");
            icon.setImageDrawable(getDrawable(R.drawable.ic_yet));
            layout.setBackground(getDrawable(R.drawable.card_cover0));
        }

        if (data.size() == 1 && data.get(0).getEditText().length() == 0
                && data.get(0).getPhotoPath().length() == 0) {
            places.append("未作成");
            data.clear();
        }
        listAdapter = new MainListAdapter(activity, this.getApplicationContext(),
                R.layout.main_listview, data);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callPhotoPreview(position);
            }
        });

        textPrimary.setText(first);
        textSecondary.setText(second);
        memoView.setText(memo);
        placeView.setText(places);
        statusView.setText(status);
        super.onStart();
    }

    public void callPhotoPreview(int position) {
        int number = 0;

        switch (position) {
            case 0:
                number = 0;
                break;
            case 1:
                number = 1;
                break;
            case 2:
                number = 2;
                break;
            case 3:
                number = 3;
                break;
            case 4:
                number = 4;
                break;
        }
        try {
            String alertText = data.get(number).getEditText();
            String alertPhoto = data.get(number).getPhotoPath();

            final Dialog dialog = new Dialog(activity);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            dialog.setContentView(R.layout.dialog_custom);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((TextView) dialog.findViewById(R.id.dialog_title)).setText(alertText);
            ImageView imageView = (ImageView) dialog.findViewById(R.id.dialog_message);
            imageView.setImageURI(Uri.parse(alertPhoto));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setAdjustViewBounds(true);
            dialog.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.positive_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }catch (Exception e){
            String place = data.get(number).getEditText();
            Toast toast = Toast.makeText(activity, place + "には画像の記録がありません。", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:

                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
