package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaPermission extends AppCompatActivity {

    //intentで受け渡しする値の定数名を明確化する
    private final static int RESULT_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;
    //画像を表示するためのパーツ
    private ImageView imageView;
    private Uri cameraUri; //android.net.Uri
    private File cameraFile; //java.io.File

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("debug","onCreate()"); //android.util.Log
        setContentView(R.layout.activity_second);

        int[] ids = new int[] {R.id.button1};
        Button[] buttons = new Button[ids.length];

        for (int i = 0; i<ids.length; i++){
            buttons[i] = (Button)findViewById(ids[i]);
        }
        final Activity activity = this;
        View.OnClickListener event = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Android6 / API23以降は、要パーミッション確認！
                if(Build.VERSION.SDK_INT >= 23) { checkPermission(); }
                else { cameraIntent(); }
            }
        };
        //View(ボタン）とリスナーイベントのリンク付け
        findViewById(R.id.button1).setOnClickListener(event);


    }

    //API23以降に対してパーミッション処理を通過させる
    private void checkPermission(){
        if(ActivityCompat.checkSelfPermission( this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            //　↑(ユーザが）メディアストレージへのアクセス許可の記録がある
            cameraIntent();
        }
        else{
            //　↑(ユーザが）メディアストレージへのアクセス拒否の記録がある
            //　ダイアログで許可を再度求める
            requestPermission();
        }
    }

    private void requestPermission(){
        //if内にダイアログ（アクセスパーミッション）表示メソッド（:boolean）。
        // 許可ならtrue、拒否ならelse文へ
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }else{
            Toast.makeText(this,
                    "許可された際にカメラ撮影が可能になります。",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //requestPermissionメソッドの結果から処理を実行する
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("debug","onRequestPermissionsResult()");
        //パーミッションリクエストが仮に複数あったとして
        //EXTERNAL STORAGEのアクセスに関するリクエストの結果
        if(requestCode == REQUEST_PERMISSION){
            //リクエスト結果が複数あるとして(今回はひとつ)
            //1番目の結果が許可を示しているか
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //許可であればカメラ起動メソッドへ
                cameraIntent();
            } else {
                Toast.makeText(this,
                        "カメラの撮影を中止します。",Toast.LENGTH_SHORT).show();
            }
        }
    }
    //カメラ撮影＆記録（API22以前ならパーミッション処理を通過させずに到達）
    private void cameraIntent(){
        //保存先のフォルダー
        File folder = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        //日時をddHHmmssで取得
        String fileDate = new SimpleDateFormat(
                "ddHHmmss", Locale.US).format(new Date());
        //日時でユニークにしてあるファイル名を決定
        String fileName = String.format("CameraIntent_%s.jpg",fileDate);
        //保存ファイルの準備（画像情報待ち）
        cameraFile = new File(folder,fileName);
        //撮影画像に付与する正確なアドレスを確定する
        cameraUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() +
                        ".fileprovider",
                cameraFile);
        //カメラ起動のためのIntentを生成する
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //ファイルのアドレスに出力された画像情報を割り当てる
        intent.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);
        //カメラ起動開始
        startActivityForResult(intent,RESULT_CAMERA);

        Log.d("debug","cameraUri == null");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == RESULT_CAMERA){
            if(cameraUri != null){
                imageView.setImageURI(cameraUri);
                registerDatabase(cameraFile);
            }else{
                Log.d("debug","cameraUri == null");
            }
        }
    }

    //Androidのデータベースへ登録する
    private void  registerDatabase(File file){
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        contentValues.put("_data",file.getAbsolutePath());
        contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
    }

}
