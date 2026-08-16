package com.airi.litesystem;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView clock,date;
    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Ui.BG); getWindow().setNavigationBarColor(Ui.BG); if(Build.VERSION.SDK_INT>=23)getWindow().getDecorView().setSystemUiVisibility(0x00002000); setContentView(build()); }
    @Override protected void onResume(){ super.onResume(); updateClock(); }
    private ScrollView build(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(Ui.BG);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(Ui.dp(this,22),Ui.dp(this,20),Ui.dp(this,22),Ui.dp(this,20)); scroll.addView(root,new ScrollView.LayoutParams(-1,-1));
        TextView brand=Ui.text(this,"AIRI",13,Ui.ACCENT); brand.setLetterSpacing(.18f); root.addView(brand);
        clock=Ui.text(this,"18:10",56,Ui.TEXT); root.addView(clock,Ui.lpMatch(-2,this)); date=Ui.text(this,"Minggu, 16 Agustus",16,Ui.MUTED); root.addView(date);
        TextView search=Ui.text(this,"  🔎  Cari aplikasi",16,Ui.MUTED); search.setGravity(Gravity.CENTER_VERTICAL); search.setBackground(Ui.bg(0xFFFFFFFF,28,this)); search.setPadding(Ui.dp(this,16),0,Ui.dp(this,16),0); search.setOnClickListener(v->open(AppDrawerActivity.class)); root.addView(search,new LinearLayout.LayoutParams(-1,Ui.dp(this,56))); Ui.setMargins(search,0,28,0,16,this);
        LinearLayout status=Ui.card(this); root.addView(status,Ui.lpMatch(-2,this)); status.addView(Ui.text(this,"Perangkat ringan",20,Ui.TEXT)); TextView sub=Ui.text(this,"Launcher minimal • tanpa iklan • tanpa proses latar berat",14,Ui.MUTED); status.addView(sub); Ui.setMargins(sub,0,6,0,0,this);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.VERTICAL); root.addView(actions,Ui.lpMatch(-2,this)); Ui.setMargins(actions,0,18,0,0,this);
        TextView apps=Ui.button(this,"Semua aplikasi"); apps.setOnClickListener(v->open(AppDrawerActivity.class)); actions.addView(apps,Ui.lpMatch(52,this));
        TextView storage=Ui.button(this,"Pindahkan data ke SD Card"); storage.setOnClickListener(v->open(StorageActivity.class)); actions.addView(storage,Ui.lpMatch(52,this)); Ui.setMargins(storage,0,10,0,0,this);
        TextView lite=Ui.button(this,"Lite Controls"); lite.setOnClickListener(v->open(LiteControlsActivity.class)); actions.addView(lite,Ui.lpMatch(52,this)); Ui.setMargins(lite,0,10,0,0,this);
        TextView home=Ui.text(this,"Jadikan AIRI sebagai Home default",15,Ui.ACCENT); home.setGravity(Gravity.CENTER); home.setPadding(0,Ui.dp(this,18),0,Ui.dp(this,18)); home.setOnClickListener(v->requestHomeRole()); root.addView(home); return scroll;
    }
    private void updateClock(){ Date now=new Date(); Locale id=new Locale("id","ID"); if(clock!=null)clock.setText(new SimpleDateFormat("HH:mm",id).format(now)); if(date!=null)date.setText(new SimpleDateFormat("EEEE, d MMMM",id).format(now)); }
    private void open(Class<?> c){ startActivity(new Intent(this,c)); }
    private void requestHomeRole(){ if(Build.VERSION.SDK_INT>=29){ RoleManager rm=(RoleManager)getSystemService(ROLE_SERVICE); if(rm!=null&&rm.isRoleAvailable(RoleManager.ROLE_HOME)&&!rm.isRoleHeld(RoleManager.ROLE_HOME)){ startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),301); return; } } startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
}
