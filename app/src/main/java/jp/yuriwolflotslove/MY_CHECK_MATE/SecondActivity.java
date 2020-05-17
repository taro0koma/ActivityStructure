package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SecondActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    public static final int editItems = 5;
    //camera撮影用
    private final static int RESULT_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;
    static DBAdapter sDBAdapter;
    private static int mHour;
    private static int mMinute;
    private Activity activity;
    private Uri[] photoPath;
    private boolean[] hasCaptured;
    private int tappedPosition = 0;
    private ArrayList<ListItem> data;
    private NonScrollListView listView = null;
    private EditListAdapter adapter;
    private int toastShowCount = 0;
    private int[] textId;
    private Map<Integer, Uri> photoList = new HashMap();
    private int buttonId;
    private Uri cameraUri;
    private File cameraFile;
    private ImageView imageButton;
    private EditText memo;
    private DBItem mDBItem = new DBItem();
    //ソフトキーボードを隠すための変数
    private InputMethodManager mInputMethodManager;
    private LinearLayout mMainLayout;
    //記録時間を取得するための変数
    private SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy 年 M 月 d 日 HH : mm");
    private SimpleDateFormat DBFormat = new SimpleDateFormat("yyyy年M月d日 HH:mm");
    private StringBuilder messageBuilder = null;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity = this;
//----------ツールバーの設定-------------------ここから//
        setTitle("記録画面");
        setTitleColor(R.color.darkblue);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left);
        toolbar.setNavigationContentDescription("基本画面に戻る");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "お出かけ前の記録を中止しました。", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });
//----------ツールバーの設定-------------------------ここまで//

        //記録データ入力用のリストビューを生成しAdapter経由で出力する
        listView = findViewById(R.id.editListView);
        //リストのヘッダー表示（こうしないとなぜか最上面が表示されない）
        View header = View.inflate(this, R.layout.header_edit_listview, null);
        listView.addHeaderView(header, null, false);
        //リストビューを指定個数で複製するためのリスト
        data = new ArrayList<>();
        ListItem item;
        for (int i = 0; i < editItems; i++) {
            item = new ListItem();
            item.setEditText("");
            item.setId(i);
            data.add(item);
        }
        //リストビューとアダプターの接続
        adapter = new EditListAdapter(activity, this.getApplicationContext(),
                R.layout.edit_listview, data);
        listView.setAdapter(adapter);
        //リストビュー内ビューのためのクリックリスナー → @Override onItemClickでひろう
        listView.setOnItemClickListener(this);
        //リストビュー複製分だけ配列を用意する
        hasCaptured = new boolean[adapter.getCount()];
        textId = new int[adapter.getCount()];
        photoPath = new Uri[adapter.getCount()];

        //ひとことメモ（EditText）を取得
        memo = (EditText) findViewById(R.id.edit_memo);

        //データベース記録時のプログレスバー
        progressBar= new ProgressBar(activity,null,android.R.attr.progressBarStyleLarge);
        CoordinatorLayout layout = findViewById(R.id.second_layout);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(100,100);
        params.gravity=Gravity.CENTER;
        layout.addView(progressBar,params);
        progressBar.setVisibility(View.GONE);


        //記録確定ボタンの設定
        Button btn_enter = (Button) findViewById(R.id.btn_setting);
        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mCalendar = Calendar.getInstance();
                callPresetDialog(mCalendar.getTime());
            }
        });

        //データベースを生成する
        sDBAdapter = new DBAdapter(this);

        //ソフトキーボードを隠すため親Viewと入力時（input）メソッドを取得しておく
        mMainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //Activity遷移時のトランジション効果
        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
    }
//--------------------------onCreate-----------------------------ここまで//


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final View itemView = view;
        tappedPosition = position;
        String item = new String("チェック場所の画像 " + (position + 1) + "番目");
        String[] alert_menu = {"もう一度、カメラで撮影する", "撮影した画像を削除する", "キャンセル"};
        String toastText = "「チェック場所名の入力」と「チェック場所の撮影」、どちらかを省略することができます。\n\n" +
                "( 場所例： ガスの元栓 ／ エアコン ／ ストーブ ／ 加湿器 ／ 台所の窓 ／ 物干し ／ 玄関の鍵 など )\n";

        switch (view.getId()) {
            case R.id.button1:
                if (!hasCaptured[position]) {
                    callCamera(itemView);
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle(item);
                    alert.setItems(alert_menu, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if (index == 0) {
                                callCamera(itemView);
                            } else if (index == 1) {
                                deleteCheck();
                            } else {
                                Log.d("debug", "cancel");
                            }
                        }
                    });
                    alert.show();
                }
                break;
            case R.id.editText1:
                if (toastShowCount < 1) {

                    Toast toast = Toast.makeText(activity, toastText,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 48);
                    toast.show();
                    toastShowCount++;
                }
                break;
            default:
                break;
        }

    }

    public void deleteCheck() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("「 画像の削除 」");
        alertDialogBuilder.setMessage("本当に削除しますか？");
        alertDialogBuilder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
            }
        });
        alertDialogBuilder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //キャンセル操作を可能にする
        alertDialogBuilder.setCancelable(true);
        //既に立ち上がっているダイアログを再形成する
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        imageButton.setImageResource(R.drawable.camera_merge);
        hasCaptured[tappedPosition] = false;
        photoPath[tappedPosition] = null;
        //リストビューの更新
        adapter.notifyDataSetChanged();
    }

    public void callCamera(View view) {
        imageButton = (ImageView) view;
        buttonId = imageButton.getId();
        if (Build.VERSION.SDK_INT >= 23) {
            this.checkPermissions();
        } else {
            cameraIntent();
        }
    }

    //-----------------------------パーミッション関連の記述　　ここから---------------------------------//
    private void checkPermissions() {
        //ストレージアクセスのパーミッションを既に行っているか
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            cameraIntent();
        } else {
            requestPermission();
        }
    }

    //requestPermissionsからonRequestPermissionResult（Override）の
    // 一連の流れ （１）
    private void requestPermission() {
        //位置情報取得のパーミッション
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            //↑のrequestPermissionsの通信をonRequestPermissionsResultで
            //受け取ることができる。
        } else {
            Toast.makeText(this,
                    "許可いただくとカメラが使用できます。",
                    Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    //配列要素末尾に「,」をつけて空要素を示し、2要素にする
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);
        }
    }

    //一連の流れ（２）
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //カメラ関連のrequestPermissionのResultであることをifで判別
        if (requestCode == REQUEST_PERMISSION) {
            //パーミッションが許可されていた
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraIntent();
            } else {
                Toast.makeText(this,
                        "カメラの起動を中止しました。",
                        Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //------------------------------パーミッション関連の記述　　ここまで---------------------------------//

    //カメラアプリの呼び出し
    private void cameraIntent() {
        //フォルダ定義
        File cFolder = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        //現在の日付取得（ユニークファイル名用）
        String fileData = new SimpleDateFormat("MMddHHmmss", Locale.JAPAN).format(new Date());
        //ファイル定義
        String fileName = String.format("MCMate_%s.jpg", fileData);

        cameraFile = new File(cFolder, fileName);

        cameraUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                cameraFile);
        photoList.put(buttonId, cameraUri);

        //intent（行き） カメラアプリの呼び出し
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        //開始
        startActivityForResult(intent, RESULT_CAMERA);//intentインスタンス名＝1001
    }

    //intent（戻り)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_CAMERA) {
            if (cameraUri != null) {

                imageButton.setImageURI(cameraUri);
                hasCaptured[tappedPosition] = true;
                photoPath[tappedPosition] = cameraUri;
            }
            //アプリ確保領域にも撮影ファイルを保存することで
            //ギャラリー表示の反映の遅延がない
            registerDatabase(cameraFile);
        }
    }

    //アプリ確保領域に撮影ファイルを保存
    private void registerDatabase(File file) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", file.getAbsolutePath());
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }


    //遷移時のフェードインフェードアウト
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //キーボードを隠す
        mInputMethodManager.hideSoftInputFromWindow(mMainLayout.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        mMainLayout.requestFocus();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //キーボードを隠す
        mInputMethodManager.hideSoftInputFromWindow(mMainLayout.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        mMainLayout.requestFocus();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void finish() {

        sDBAdapter.open();
//-------------------DBの書き込み処理----------------------------ここから//
        //以前の記録を削除できる
        try {
            boolean test;
            do{
                test=sDBAdapter.deleteAllNotes();
            }while (!test);
            if(mDBItem.placeNames.size()==0){
                sDBAdapter.saveItem("","",mDBItem.getMemo(),
                        mDBItem.getDateTime());
            }else {

                for (int i = 0; i < mDBItem.placeNames.size(); i++) {
                    sDBAdapter.saveItem(
                            mDBItem.placeNames.get(i),
                            mDBItem.photoPaths.get(i),
                            mDBItem.getMemo(),
                            mDBItem.getDateTime());

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            canNotDelete();
        }finally {
            sDBAdapter.close();
        }
        super.finish();
    }

    //------------------全記録内容の確定-----------------------------ここから//
    public void callPresetDialog(Date date) {
        String dateTime = simpleFormat.format(date);
        mDBItem.setDateTime(DBFormat.format(date));
        messageBuilder = new StringBuilder("\n□ 記録日時： " + dateTime + "\n\n");
        String place;
        String photoPathText;
        String defaultPlaceName;
        int count = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            //-----DB用----//
//            place = data.get(i).getEditText().toString();
            place = adapter.getEditText(i);

            photoPathText = "";
            if (photoPath[i] != null) {
                photoPathText = photoPath[i].toString();
            }
            mDBItem.photoPaths.add(photoPathText);
            //画像がある。または、チェック場所にテキストがある
            if (place.length()!=0) {
                messageBuilder.append(" ・ " + place);
                mDBItem.placeNames.add(place);
                //画像があり、かつ、チェック場所のテキストが未記入
            } else if (hasCaptured[i] && data.get(i).getEditText().equals("")) {
                defaultPlaceName = "チェック場所 - " + (i + 1);
                messageBuilder.append(" ・ " + defaultPlaceName);
                mDBItem.placeNames.add(defaultPlaceName);
                //画像、チェック場所テキストどちらもない（表示文なければ改行させないため）
            } else {
                count++;
            }
            //画像がある
            if (hasCaptured[i]) {
                messageBuilder.append("（画像あり）");
            }
            //リストビュー全アイテムで、画像・チェック場所テキストどちらもない
            if (count == adapter.getCount()) {
                messageBuilder.append("（ ※ チェック場所は記録されていません。)");
            }
            //チェック場所１行ごとに改行
            if (i != adapter.getCount() - 1 && count == 0) {
                messageBuilder.append("\n");
            }
        }
        String memoFinal = memo.getText().toString();
        if (memoFinal.equals("")) {
            memoFinal = "（ ※ 記録されていません。）";
        }
        mDBItem.setMemo(memoFinal);
        messageBuilder.append("\n\n□ ひとことメモ／ \n　" + memoFinal);
        messageBuilder.append("\n\n\n以上でよろしいですか？");

        final AlertDialog.Builder presetDialog = new AlertDialog.Builder(activity);
        presetDialog.setTitle("「 あなたのチェック記録 」");
        presetDialog.setMessage(messageBuilder.toString());
        presetDialog.setPositiveButton("ＯＫ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //同時にfinish側でDBにデータを保存する
                progressBar.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                }).start();
            }
        });

        presetDialog.setNegativeButton("入力に戻る", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        presetDialog.setNeutralButton("記録時間の変更", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callTimePicker();
            }
        });
        presetDialog.show();
    }

    public void callTimePicker() {
        TimePicker picker = new TimePicker(activity);
        Calendar now = Calendar.getInstance();
        mHour=now.get(Calendar.HOUR_OF_DAY);
        mMinute=now.get(Calendar.MINUTE);
        picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker picker, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("「 チェック記録時間の変更 」")
                .setPositiveButton("決定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
                        mCalendar.set(Calendar.MINUTE, mMinute);
                        callPresetDialog(mCalendar.getTime());
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).setView(picker).show();
    }

    public void canNotDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("「 チェック場所の記録に失敗しました。 」");
        builder.setMessage("大変申し訳ありません。\n" +
                "デバイスの処理が遅くなっているようです。\n" +
                "デバイスを安定させていただき、記録を再開下さいませ。");
        builder.setPositiveButton("再開", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("記録中止", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sDBAdapter.close();
                Intent intent = new Intent(activity, MainActivity.class);
            }
        });
        builder.show();
    }
}
