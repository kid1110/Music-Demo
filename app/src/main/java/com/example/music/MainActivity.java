package com.example.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Button play, pause,con,pre,next;
    private static TextView tv_progress,tv_total;
    private MediaPlayer mediaPlayer=new MediaPlayer();
    private boolean hasPermissions = false;
    private File file;

    private static Context mContext;
    private static ContentResolver mContentResolver;
    private static Object mLock = new Object();
    private RxPermissions rxPermissions;
    private TextView songName;
    private String[] songList;
    private int songMark = 0;
    private List<File> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println(Environment.getExternalStorageDirectory());
        songName = findViewById(R.id.song_name);
        pre = findViewById(R.id.btn_pre);
        next = findViewById(R.id.btn_next);
        play = findViewById(R.id.btn_play);
        con = findViewById(R.id.btn_continue_play);
        pause = findViewById(R.id.btn_pause);

        permisson();
        init();



        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                PauseInit();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                ConInit();
            }
        });
        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                PauseInit();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songMark <songList.length-1){
                    songMark++;
                    try {
                      ChangeSong(fileList.get(songMark));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    songMark = 0;
                    try {
                        ChangeSong(fileList.get(songMark));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
         pre.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(songMark >0){
                     songMark--;
                     try {
                         ChangeSong(fileList.get(songMark));
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }else{
                     songMark = songList.length-1;
                     try {
                         ChangeSong(fileList.get(songMark));
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         });


    }
    private void ChangeSong(File file) throws IOException {
        mediaPlayer.stop();
       mediaPlayer.reset();
        mediaPlayer.setDataSource(file.getPath());
        mediaPlayer.prepare();
        songName.setText(file.getName());
        mediaPlayer.start();

    }
    private void init(){
        play.setEnabled(true);
        pause.setEnabled(false);
        con.setEnabled(false);
        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.INVISIBLE);
        con.setVisibility(View.INVISIBLE);
    }
    private void PauseInit(){
        play.setVisibility(View.INVISIBLE);
        pause.setVisibility(View.VISIBLE);
        con.setVisibility(View.INVISIBLE);
        play.setEnabled(false);
        pause.setEnabled(true);
        con.setEnabled(false);
    }
    private void ConInit(){
        play.setVisibility(View.INVISIBLE);
        pause.setVisibility(View.INVISIBLE);
        con.setVisibility(View.VISIBLE);
        play.setEnabled(false);
        pause.setEnabled(false);
        con.setEnabled(true);

    }
    public void permisson(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            initMediaPlay();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initMediaPlay();
                }else {
                    Toast.makeText(this,"请打开访问存储空间的权限",Toast.LENGTH_SHORT).show();
                }

                break;

            default:
                break;
        }
    }


    private void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    private void checkVersion() {
        //Android6.0及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果你是在Fragment中，则把this换成getActivity()
            rxPermissions = new RxPermissions(this);
            //权限请求
            rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted) {//申请成功
                            showMsg("已获取权限");
                            hasPermissions = true;
                        } else {//申请失败
                            showMsg("权限未开启");
                            hasPermissions = false;
                        }
                    });
        } else {
            //Android6.0以下
            showMsg("无需请求动态权限");
        }
    }
    class DirFilter implements FilenameFilter {//文件过滤器
        private Pattern p;
        public DirFilter(String regex) {
            p = Pattern.compile(regex);
        }
        @Override
        public boolean accept(File dir, String name) {
            // TODO Auto-generated method stub
            return p.matcher(name).matches();
        }
    }



    private void initMediaPlay(){
        try{
            file = new File("/storage/emulated/0/Music");
            songList = file.list(new DirFilter("[\\S\\s]*mp3"));
           for(int i = 0; i <songList.length;i++){
               String data = songList[i];
               System.out.println(file.getPath()+"/"+data);
               File temp = new File(file.getPath()+"/"+data);
               fileList.add(temp);
               System.out.println(data);
           }
            System.out.println(fileList);
            mediaPlayer.setDataSource(fileList.get(0).getPath());
            mediaPlayer.prepare();
            songName.setText(file.getName());
            songName.setTextSize(70f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}