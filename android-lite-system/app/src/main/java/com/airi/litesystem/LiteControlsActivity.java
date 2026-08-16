package com.airi.litesystem;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LiteControlsActivity extends Activity {
    private TextView status;
    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Ui.BG); getWindow().setNavigationBarColor(Ui.BG); setContentView(build()); }
    @Override protected void onResume(){super.onResume();update();}
    private ScrollView build(){ ScrollView s=new ScrollView(this); s.setBackgroundColor(Ui.BG); LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.VERTICAL); r.setPadding(Ui.dp(this,20),Ui.dp(this,18),Ui.dp(this,20),Ui.dp(this,28)); s.addView(r); r.addView(Ui.text(this,"Lite Controls",30,Ui.TEXT)); r.addView(Ui.text(this,"Optimasi yang bisa dibalik tanpa root",15,Ui.MUTED)); status=Ui.text(this,"Memeriksa izin…",14,Ui.MUTED); r.addView(status); Ui.setMargins(status,0,16,0,8,this); TextView h=Ui.button(this,"Animasi 0.5× (direkomendasikan)"); h.setOnClickListener(v->setAnim(.5f)); r.addView(h,Ui.lpMatch(52,this)); TextView off=Ui.button(this,"Matikan animasi"); off.setOnClickListener(v->setAnim(0f)); r.addView(off,Ui.lpMatch(52,this)); Ui.setMargins(off,0,10,0,0,this); TextView normal=Ui.button(this,"Kembalikan animasi 1×"); normal.setOnClickListener(v->setAnim(1f)); r.addView(normal,Ui.lpMatch(52,this)); Ui.setMargins(normal,0,10,0,0,this); TextView dev=Ui.button(this,"Buka Developer Options"); dev.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))); r.addView(dev,Ui.lpMatch(52,this)); Ui.setMargins(dev,0,18,0,0,this); TextView apps=Ui.button(this,"Kelola aplikasi Realme"); apps.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS))); r.addView(apps,Ui.lpMatch(52,this)); Ui.setMargins(apps,0,10,0,0,this); TextView note=Ui.text(this,"Untuk mengubah skala animasi dari AIRI, izin WRITE_SECURE_SETTINGS perlu diberikan sekali lewat ADB. Ini bukan root.",13,Ui.MUTED); r.addView(note); Ui.setMargins(note,0,18,0,0,this); return s; }
    private boolean secure(){return checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)==PackageManager.PERMISSION_GRANTED;}
    private void update(){if(status!=null)status.setText(secure()?"✓ Advanced Lite Control aktif":"Advanced Lite Control belum diaktifkan via ADB");}
    private void setAnim(float v){if(!secure()){Toast.makeText(this,"Aktifkan izin via ADB terlebih dahulu.",Toast.LENGTH_LONG).show();return;} try{Settings.Global.putFloat(getContentResolver(),Settings.Global.WINDOW_ANIMATION_SCALE,v);Settings.Global.putFloat(getContentResolver(),Settings.Global.TRANSITION_ANIMATION_SCALE,v);Settings.Global.putFloat(getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,v);Toast.makeText(this,"Skala animasi: "+v+"×",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Gagal: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
}
