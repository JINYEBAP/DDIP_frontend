package com.example.jin.myapplication;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class IntroActivity extends AppCompatActivity {

    //위치 허가 관련 상수들
    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    /** 로딩 화면이 떠있는 시간(밀리초단위)  **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //---------------------------------------------------------------------------------------------------------------// 위치 권한 받기
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(IntroActivity.this, Manifest.permission.WRITE_CALENDAR);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(IntroActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(IntroActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(IntroActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        //---------------------------------------------------------------------------------------------------------------//
        else{
            /* SPLASH_DISPLAY_LENGTH 뒤에 메뉴 액티비티를 실행시키고 종료한다.*/
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    /* 메뉴액티비티를 실행하고 로딩화면을 죽인다.*/
                    Intent mainIntent = new Intent(IntroActivity.this,MainActivity.class);
                    IntroActivity.this.startActivity(mainIntent);
                    IntroActivity.this.finish();
                }
            }, SPLASH_DISPLAY_LENGTH);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허가
                    // 해당 권한을 사용해서 작업을 진행할 수 있습니다
                    Intent mainIntent = new Intent(IntroActivity.this,MainActivity.class);
                    IntroActivity.this.startActivity(mainIntent);
                    IntroActivity.this.finish();
                } else {
                    // 권한 거부
                    // 사용자가 해당권한을 거부했을때 해주어야 할 동작을 수행합니다
                    AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
                    localBuilder.setTitle("권한 설정")
                            .setMessage("권한 거절로 인해 일부기능이 제한됩니다.")
                            .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt){
                                    try {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                .setData(Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        e.printStackTrace();
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                        startActivity(intent);
                                    }
                                }})
                            .setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {

                                }})
                            .create()
                            .show();
                }
                return;
        }
    }

}