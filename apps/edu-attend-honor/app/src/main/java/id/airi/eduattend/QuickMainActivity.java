package id.airi.eduattend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

import java.text.NumberFormat;
import java.util.Locale;

public class QuickMainActivity extends Activity {
    private DatabaseHelper db;
    private LinearLayout body;
    private long userId=-1;
    private String displayName="", role="", teacherCode=null;
    private final int GREEN=Color.rgb(11,107,75), RED=Color.rgb(184,45,45), AMBER=Color.rgb(176,112,0), INK=Color.rgb(25,35,31);

    @Override protected void onCreate(Bundle b){super.onCreate(b);db=new DatabaseHelper(this);showLogin();}

    private void showLogin(){
        LinearLayout root=column();root.setPadding(dp(28),dp(54),dp(28),dp(28));root.setBackgroundColor(Color.rgb(250,252,251));
        root.addView(text("AIRI",14,GREEN,true));root.addView(text("EduAttend",34,INK,true));root.addView(text("v2.1 • Quick Attendance",13,Color.GRAY,false));
        root.addView(text("Absensi guru & santri dibuat sesingkat mungkin.",14,Color.DKGRAY,false));space(root,28);
        EditText u=input("Username"),p=input("PIN");p.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);root.addView(u);root.addView(p);
        Button login=primary("MASUK");login.setOnClickListener(v->{Cursor c=db.login(u.getText().toString().trim(),p.getText().toString());if(c.moveToFirst()){userId=c.getLong(0);displayName=c.getString(2);role=c.getString(3);teacherCode=c.isNull(4)?null:c.getString(4);c.close();db.audit(userId,"LOGIN_QUICK","role="+role);showDashboard();}else{c.close();toast("Username atau PIN salah");}});root.addView(login);
        root.addView(text("Akun demo: admin/1234 • piket/1111 • bendahara/2222 • gr001/0000",11,Color.GRAY,false));
        setContentView(root);
    }

    private void shell(String title){
        LinearLayout root=column();root.setBackgroundColor(Color.rgb(250,252,251));LinearLayout head=column();head.setPadding(dp(18),dp(18),dp(18),dp(10));
        head.addView(text(db.getSetting("institution_name","Sekolah")+" • "+role,12,GREEN,true));head.addView(text(title,25,INK,true));head.addView(text(displayName,12,Color.GRAY,false));root.addView(head);
        ScrollView sv=new ScrollView(this);body=column();body.setPadding(dp(13),dp(4),dp(13),dp(30));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    }

    private void showDashboard(){
        shell("Dashboard Cepat");
        if("GURU".equals(role))showTeacherQuick(); else showOperationalQuick();
        Button full=secondary("BUKA SISTEM LENGKAP");full.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class)));body.addView(full);
        Button refresh=secondary("REFRESH");refresh.setOnClickListener(v->showDashboard());body.addView(refresh);
        Button out=secondary("KELUAR");out.setOnClickListener(v->{db.audit(userId,"LOGOUT_QUICK","");userId=-1;role="";teacherCode=null;showLogin();});body.addView(out);
    }

    private void showTeacherQuick(){
        Cursor c=db.getTodaySchedules("GURU",teacherCode);int now=db.currentMinutes(),tol=db.getIntSetting("late_tolerance_minutes",15);long active=-1;String cls=null,subject=null,start=null,end=null,status=null;int nextAt=99999;String nCls=null,nSubject=null,nStart=null,nEnd=null;
        while(c.moveToNext()){int st=db.toMinutes(c.getString(c.getColumnIndexOrThrow("start_time"))),en=db.toMinutes(c.getString(c.getColumnIndexOrThrow("end_time")));String s=c.getString(c.getColumnIndexOrThrow("attendance_status"));if(active<0&&now>=st-30&&now<=en+tol){active=c.getLong(c.getColumnIndexOrThrow("id"));cls=c.getString(c.getColumnIndexOrThrow("class_code"));subject=c.getString(c.getColumnIndexOrThrow("subject_name"));start=c.getString(c.getColumnIndexOrThrow("start_time"));end=c.getString(c.getColumnIndexOrThrow("end_time"));status=s;}else if(st>now&&st<nextAt){nextAt=st;nCls=c.getString(c.getColumnIndexOrThrow("class_code"));nSubject=c.getString(c.getColumnIndexOrThrow("subject_name"));nStart=c.getString(c.getColumnIndexOrThrow("start_time"));nEnd=c.getString(c.getColumnIndexOrThrow("end_time"));}}c.close();
        LinearLayout hero=card();hero.addView(text("ABSENSI MENGAJAR",12,GREEN,true));
        if(active>0){hero.addView(text(cls+" • "+subject,22,INK,true));hero.addView(text(start+"–"+end,13,Color.DKGRAY,false));hero.addView(text("Status: "+status,13,"HADIR".equals(status)?GREEN:("BELUM ABSEN".equals(status)?AMBER:RED),true));final long sid=active;final String fCls=cls;
            if("BELUM ABSEN".equals(status)){Button absen=primary("ABSEN SEKARANG");absen.setOnClickListener(v->confirmTeacherPresent(sid,fCls));hero.addView(absen);}else if("HADIR".equals(status)){Button santri=primary("ABSENSI SANTRI "+fCls);santri.setOnClickListener(v->showStudents(fCls));hero.addView(santri);}else hero.addView(text("Status sudah dicatat. Perubahan dapat dilakukan dari Sistem Lengkap.",12,Color.GRAY,false));
        }else if(nCls!=null){hero.addView(text("Jadwal berikutnya",12,Color.GRAY,false));hero.addView(text(nCls+" • "+nSubject,20,INK,true));hero.addView(text(nStart+"–"+nEnd,13,Color.DKGRAY,false));hero.addView(text("Tombol ABSEN aktif 30 menit sebelum jadwal.",12,Color.GRAY,false));}
        else hero.addView(text("Tidak ada sesi aktif atau jadwal berikutnya hari ini.",13,Color.GRAY,false));body.addView(hero);

        LinearLayout stats=new LinearLayout(this);stats.addView(stat("Jadwal",db.getScheduledCount("GURU",teacherCode),GREEN),weight());stats.addView(stat("Hadir",db.getPresentTeacherCount("GURU",teacherCode),GREEN),weight());body.addView(stats);
        long honor=0;Cursor h=db.getHonorRecap("GURU",teacherCode);while(h.moveToNext())honor+=db.honorForMinutes(h.getInt(3));h.close();LinearLayout hc=card();hc.addView(text("Honor bulan "+db.currentMonth(),12,Color.GRAY,false));hc.addView(text(rupiah(honor),25,GREEN,true));body.addView(hc);
        body.addView(text("Kelas saya",18,INK,true));Cursor cc=db.getReadableDatabase().rawQuery("SELECT DISTINCT c.code,c.name FROM classes c JOIN schedules s ON s.class_code=c.code WHERE s.teacher_code=? AND s.active=1 ORDER BY c.code",new String[]{teacherCode});while(cc.moveToNext()){String code=cc.getString(0),name=cc.getString(1);int[] sm=summary(code);LinearLayout r=card();r.addView(text(code+" • "+name,17,INK,true));r.addView(text("Hadir "+sm[1]+" • Izin "+sm[2]+" • Sakit "+sm[3]+" • Alpha "+sm[4]+" • Belum "+sm[5],12,Color.DKGRAY,false));r.setOnClickListener(v->showStudents(code));body.addView(r);}cc.close();
    }

    private void confirmTeacherPresent(long scheduleId,String cls){
        new AlertDialog.Builder(this).setTitle("Konfirmasi Kehadiran").setMessage("Anda sedang berada di kelas dan siap mengajar?").setPositiveButton("YA, ABSEN",(d,w)->{db.markTeacherAttendance(userId,scheduleId,"HADIR","Quick Attendance v2.1");new AlertDialog.Builder(this).setTitle("Berhasil").setMessage("Kehadiran guru sudah tercatat.").setPositiveButton("ISI ABSENSI SANTRI",(a,b)->showStudents(cls)).setNegativeButton("SELESAI",(a,b)->showDashboard()).show();}).setNegativeButton("BATAL",null).show();
    }

    private void showOperationalQuick(){
        int[] live=liveStats();LinearLayout a=new LinearLayout(this);a.addView(stat("Kelas Kosong",live[0],RED),weight());a.addView(stat("Guru Tidak Masuk",live[1],RED),weight());body.addView(a);body.addView(stat("Santri Tidak Hadir",db.getAbsentStudentCount(),AMBER));
        body.addView(text("Monitoring kelas saat ini",18,INK,true));int tol=db.getIntSetting("late_tolerance_minutes",15),now=db.currentMinutes(),shown=0;Cursor c=db.getTodaySchedules("ADMIN",null);while(c.moveToNext()){String st=c.getString(c.getColumnIndexOrThrow("start_time")),en=c.getString(c.getColumnIndexOrThrow("end_time")),s=c.getString(c.getColumnIndexOrThrow("attendance_status"));if(now>db.toMinutes(st)+tol&&now<=db.toMinutes(en)&&!"HADIR".equals(s)){shown++;long sid=c.getLong(c.getColumnIndexOrThrow("id"));LinearLayout r=card();r.addView(text("KELAS KOSONG",12,RED,true));r.addView(text(c.getString(c.getColumnIndexOrThrow("class_code"))+" • "+c.getString(c.getColumnIndexOrThrow("subject_name")),18,INK,true));r.addView(text(c.getString(c.getColumnIndexOrThrow("teacher_name"))+" • "+st+"–"+en,13,Color.DKGRAY,false));if(!"BENDAHARA".equals(role)){Button b=secondary("CATAT STATUS GURU");b.setOnClickListener(v->chooseTeacherStatus(sid));r.addView(b);}body.addView(r);}}c.close();if(shown==0)body.addView(info("Tidak ada kelas kosong yang sedang berlangsung."));
    }

    private int[] liveStats(){int empty=0,absent=0,tol=db.getIntSetting("late_tolerance_minutes",15),now=db.currentMinutes();Cursor c=db.getTodaySchedules("ADMIN",null);while(c.moveToNext()){int st=db.toMinutes(c.getString(c.getColumnIndexOrThrow("start_time"))),en=db.toMinutes(c.getString(c.getColumnIndexOrThrow("end_time")));String s=c.getString(c.getColumnIndexOrThrow("attendance_status"));if(now>st+tol&&now<=en&&!"HADIR".equals(s))empty++;if((now>en&&!"HADIR".equals(s))||"TIDAK HADIR".equals(s)||"IZIN".equals(s)||"SAKIT".equals(s))absent++;}c.close();return new int[]{empty,absent};}

    private void chooseTeacherStatus(long sid){String[] st={"HADIR","IZIN","SAKIT","TIDAK HADIR"};new AlertDialog.Builder(this).setTitle("Status Guru").setItems(st,(d,w)->{db.markTeacherAttendance(userId,sid,st[w],"Dicatat dari dashboard cepat");showDashboard();}).show();}

    private void showStudents(String cls){
        shell("Absensi Santri • "+cls);int[] sm=summary(cls);LinearLayout sum=card();sum.addView(text("RINGKASAN",12,GREEN,true));sum.addView(text("Total "+sm[0]+" • Hadir "+sm[1]+" • Izin "+sm[2]+" • Sakit "+sm[3]+" • Alpha "+sm[4]+" • Belum "+sm[5],13,Color.DKGRAY,false));if(sm[5]>0){Button all=primary("HADIRKAN SEMUA YANG BELUM ("+sm[5]+")");all.setOnClickListener(v->bulkPresent(cls));sum.addView(all);}body.addView(sum);
        Cursor c=db.getStudents(cls);while(c.moveToNext()){long id=c.getLong(0);String name=c.getString(2),status=c.getString(3);LinearLayout r=card();r.addView(text(name,17,INK,true));r.addView(text("NIS "+c.getString(1)+" • "+status,12,"HADIR".equals(status)?GREEN:("BELUM".equals(status)?Color.GRAY:RED),true));r.setOnClickListener(v->chooseStudent(id,cls,name));body.addView(r);}c.close();Button back=secondary("KEMBALI KE DASHBOARD");back.setOnClickListener(v->showDashboard());body.addView(back);
    }

    private int[] summary(String cls){int[] x=new int[6];Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(st.id),SUM(CASE WHEN sa.status='HADIR' THEN 1 ELSE 0 END),SUM(CASE WHEN sa.status='IZIN' THEN 1 ELSE 0 END),SUM(CASE WHEN sa.status='SAKIT' THEN 1 ELSE 0 END),SUM(CASE WHEN sa.status='ALPHA' THEN 1 ELSE 0 END),SUM(CASE WHEN sa.status IS NULL THEN 1 ELSE 0 END) FROM students st LEFT JOIN student_attendance sa ON sa.student_id=st.id AND sa.date=? WHERE st.class_code=? AND st.status='AKTIF'",new String[]{db.today(),cls});if(c.moveToFirst())for(int i=0;i<6;i++)x[i]=c.isNull(i)?0:c.getInt(i);c.close();return x;}

    private void bulkPresent(String cls){new AlertDialog.Builder(this).setTitle("Hadirkan Massal").setMessage("Semua santri berstatus BELUM akan ditandai HADIR. Izin/Sakit/Alpha yang sudah dicatat tidak diubah.").setPositiveButton("YA",(d,w)->{SQLiteDatabase sql=db.getWritableDatabase();sql.execSQL("INSERT OR IGNORE INTO student_attendance(student_id,date,status,note,recorded_by) SELECT id,?,'HADIR','Hadir massal v2.1',? FROM students WHERE class_code=? AND status='AKTIF'",new Object[]{db.today(),userId,cls});Cursor c=sql.rawQuery("SELECT changes()",null);int n=c.moveToFirst()?c.getInt(0):0;c.close();db.audit(userId,"STUDENT_BULK_PRESENT","class="+cls+", count="+n);toast(n+" santri ditandai hadir");showStudents(cls);}).setNegativeButton("BATAL",null).show();}

    private void chooseStudent(long id,String cls,String name){String[] st={"HADIR","IZIN","SAKIT","ALPHA"};new AlertDialog.Builder(this).setTitle(name).setItems(st,(d,w)->{db.markStudentAttendance(userId,id,st[w],"Quick Attendance v2.1");showStudents(cls);}).show();}

    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout card(){LinearLayout l=column();l.setPadding(dp(15),dp(13),dp(15),dp(13));l.setBackgroundColor(Color.WHITE);l.setElevation(dp(1));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(4),dp(4),dp(4),dp(4));l.setLayoutParams(p);return l;}
    private LinearLayout stat(String label,int value,int color){LinearLayout l=card();l.addView(text(label,12,Color.DKGRAY,false));l.addView(text(String.valueOf(value),28,color,true));return l;}
    private TextView text(String s,int sp,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(color);v.setPadding(0,dp(2),0,dp(2));if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private TextView info(String s){TextView v=text(s,13,Color.DKGRAY,false);v.setBackgroundColor(Color.rgb(235,246,240));v.setPadding(dp(13),dp(11),dp(13),dp(11));return v;}
    private EditText input(String hint){EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);e.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(56)));return e;}
    private Button primary(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundColor(GREEN);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));p.setMargins(dp(4),dp(7),dp(4),dp(4));b.setLayoutParams(p);return b;}
    private Button secondary(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(50));p.setMargins(dp(4),dp(4),dp(4),dp(4));b.setLayoutParams(p);return b;}
    private LinearLayout.LayoutParams weight(){return new LinearLayout.LayoutParams(0,-2,1);}
    private void space(LinearLayout l,int h){Space s=new Space(this);l.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}
    private String rupiah(long n){NumberFormat nf=NumberFormat.getCurrencyInstance(new Locale("id","ID"));nf.setMaximumFractionDigits(0);return nf.format(n);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
}
