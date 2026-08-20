package id.airi.eduattend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "airi_eduattend.db";
    private static final int DB_VERSION = 2;
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE teachers(code TEXT PRIMARY KEY,name TEXT NOT NULL,phone TEXT,status TEXT NOT NULL DEFAULT 'AKTIF')");
        db.execSQL("CREATE TABLE classes(code TEXT PRIMARY KEY,name TEXT NOT NULL,level TEXT)");
        db.execSQL("CREATE TABLE subjects(code TEXT PRIMARY KEY,name TEXT NOT NULL)");
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT UNIQUE NOT NULL,display_name TEXT NOT NULL,role TEXT NOT NULL,teacher_code TEXT,pin_hash TEXT NOT NULL,active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE schedules(id INTEGER PRIMARY KEY AUTOINCREMENT,day_of_week INTEGER NOT NULL,teacher_code TEXT NOT NULL,class_code TEXT NOT NULL,subject_code TEXT NOT NULL,start_time TEXT NOT NULL,end_time TEXT NOT NULL,active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE teacher_attendance(id INTEGER PRIMARY KEY AUTOINCREMENT,schedule_id INTEGER NOT NULL,date TEXT NOT NULL,status TEXT NOT NULL,check_in TEXT,late_minutes INTEGER NOT NULL DEFAULT 0,note TEXT,recorded_by INTEGER,UNIQUE(schedule_id,date))");
        db.execSQL("CREATE TABLE students(id INTEGER PRIMARY KEY AUTOINCREMENT,nis TEXT UNIQUE NOT NULL,name TEXT NOT NULL,class_code TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'AKTIF')");
        db.execSQL("CREATE TABLE student_attendance(id INTEGER PRIMARY KEY AUTOINCREMENT,student_id INTEGER NOT NULL,date TEXT NOT NULL,status TEXT NOT NULL,note TEXT,recorded_by INTEGER,UNIQUE(student_id,date))");
        db.execSQL("CREATE TABLE settings(key TEXT PRIMARY KEY,value TEXT NOT NULL)");
        db.execSQL("CREATE TABLE audit_logs(id INTEGER PRIMARY KEY AUTOINCREMENT,created_at TEXT NOT NULL,user_id INTEGER,action TEXT NOT NULL,detail TEXT)");
        seed(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS audit_logs");
        db.execSQL("DROP TABLE IF EXISTS student_attendance");
        db.execSQL("DROP TABLE IF EXISTS students");
        db.execSQL("DROP TABLE IF EXISTS teacher_attendance");
        db.execSQL("DROP TABLE IF EXISTS schedules");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS subjects");
        db.execSQL("DROP TABLE IF EXISTS classes");
        db.execSQL("DROP TABLE IF EXISTS teachers");
        db.execSQL("DROP TABLE IF EXISTS settings");
        onCreate(db);
    }

    private void seed(SQLiteDatabase db) {
        teacher(db,"GR001","Ust. Ahmad","-"); teacher(db,"GR002","Ust. Naufal","-"); teacher(db,"GR003","Ust. Fadli","-");
        cls(db,"10A","Kelas 10 A","ULYA"); cls(db,"10B","Kelas 10 B","ULYA"); cls(db,"11A","Kelas 11 A","ULYA");
        subject(db,"MTK","Matematika"); subject(db,"BAR","Bahasa Arab"); subject(db,"FIQ","Fiqih"); subject(db,"HAD","Hadits");
        user(db,"admin","Administrator","ADMIN",null,"1234");
        user(db,"piket","Petugas Piket","PIKET",null,"1111");
        user(db,"bendahara","Bendahara","BENDAHARA",null,"2222");
        user(db,"gr001","Ust. Ahmad","GURU","GR001","0000");
        user(db,"gr002","Ust. Naufal","GURU","GR002","0000");
        user(db,"gr003","Ust. Fadli","GURU","GR003","0000");
        schedule(db,2,"GR001","10A","MTK","07:30","09:00");
        schedule(db,2,"GR002","10B","BAR","09:00","10:30");
        schedule(db,2,"GR003","11A","FIQ","10:30","12:00");
        schedule(db,2,"GR001","10B","HAD","13:30","15:00");
        student(db,"S1001","Abdullah Akram","10A"); student(db,"S1002","Adli Al Ghafiri","10A"); student(db,"S1003","Alfathir Rahmatillah","10A");
        student(db,"S2001","Lathifah Khairani","10B"); student(db,"S2002","Luqiana Mawaddah","10B"); student(db,"S2003","Maryam Marzeia","10B");
        student(db,"S3001","Ahmad Zhafran","11A"); student(db,"S3002","Alif Prawira","11A");
        setting(db,"institution_name","MATAQU"); setting(db,"honor_per_hour","15000"); setting(db,"late_tolerance_minutes","15"); setting(db,"academic_year","2026/2027");
    }

    private void teacher(SQLiteDatabase db,String c,String n,String p){ ContentValues v=new ContentValues();v.put("code",c);v.put("name",n);v.put("phone",p);db.insert("teachers",null,v); }
    private void cls(SQLiteDatabase db,String c,String n,String l){ ContentValues v=new ContentValues();v.put("code",c);v.put("name",n);v.put("level",l);db.insert("classes",null,v); }
    private void subject(SQLiteDatabase db,String c,String n){ ContentValues v=new ContentValues();v.put("code",c);v.put("name",n);db.insert("subjects",null,v); }
    private void user(SQLiteDatabase db,String u,String d,String r,String t,String pin){ ContentValues v=new ContentValues();v.put("username",u);v.put("display_name",d);v.put("role",r);v.put("teacher_code",t);v.put("pin_hash",Security.sha256(pin));db.insert("users",null,v); }
    private void schedule(SQLiteDatabase db,int day,String t,String c,String s,String st,String en){ ContentValues v=new ContentValues();v.put("day_of_week",day);v.put("teacher_code",t);v.put("class_code",c);v.put("subject_code",s);v.put("start_time",st);v.put("end_time",en);db.insert("schedules",null,v); }
    private void student(SQLiteDatabase db,String nis,String n,String c){ ContentValues v=new ContentValues();v.put("nis",nis);v.put("name",n);v.put("class_code",c);db.insert("students",null,v); }
    private void setting(SQLiteDatabase db,String k,String val){ ContentValues v=new ContentValues();v.put("key",k);v.put("value",val);db.insert("settings",null,v); }

    public Cursor login(String username,String pin){ return getReadableDatabase().rawQuery("SELECT id,username,display_name,role,teacher_code FROM users WHERE lower(username)=lower(?) AND pin_hash=? AND active=1",new String[]{username,Security.sha256(pin)}); }
    public void changePin(long userId,String newPin){ ContentValues v=new ContentValues();v.put("pin_hash",Security.sha256(newPin));getWritableDatabase().update("users",v,"id=?",new String[]{String.valueOf(userId)}); audit(userId,"CHANGE_PIN","PIN akun diperbarui"); }
    public String today(){ return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date()); }
    public String currentMonth(){ return new SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(new Date()); }
    public int todayDayOfWeek(){ return Calendar.getInstance().get(Calendar.DAY_OF_WEEK); }
    public int currentMinutes(){ Calendar c=Calendar.getInstance(); return c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE); }
    public int toMinutes(String hhmm){ String[] p=hhmm.split(":");return Integer.parseInt(p[0])*60+Integer.parseInt(p[1]); }
    public String getSetting(String key,String fallback){ Cursor c=getReadableDatabase().rawQuery("SELECT value FROM settings WHERE key=?",new String[]{key});String v=fallback;if(c.moveToFirst())v=c.getString(0);c.close();return v; }
    public int getIntSetting(String key,int fallback){ try{return Integer.parseInt(getSetting(key,String.valueOf(fallback)));}catch(Exception e){return fallback;} }
    public void setSetting(long userId,String key,String value){ ContentValues v=new ContentValues();v.put("value",value);getWritableDatabase().update("settings",v,"key=?",new String[]{key});audit(userId,"SETTING",key+"="+value); }
    public Cursor getTodaySchedules(String role,String teacherCode){ String base="SELECT s.id,s.start_time,s.end_time,s.teacher_code,t.name teacher_name,s.class_code,s.subject_code,p.name subject_name,COALESCE(a.status,'BELUM ABSEN') attendance_status,a.check_in,COALESCE(a.late_minutes,0) late_minutes FROM schedules s JOIN teachers t ON t.code=s.teacher_code JOIN subjects p ON p.code=s.subject_code LEFT JOIN teacher_attendance a ON a.schedule_id=s.id AND a.date=? WHERE s.day_of_week=? AND s.active=1"; if("GURU".equals(role)) return getReadableDatabase().rawQuery(base+" AND s.teacher_code=? ORDER BY s.start_time",new String[]{today(),String.valueOf(todayDayOfWeek()),teacherCode}); return getReadableDatabase().rawQuery(base+" ORDER BY s.start_time",new String[]{today(),String.valueOf(todayDayOfWeek())}); }
    public void markTeacherAttendance(long userId,long scheduleId,String status,String note){ Cursor c=getReadableDatabase().rawQuery("SELECT start_time FROM schedules WHERE id=?",new String[]{String.valueOf(scheduleId)});String start="00:00";if(c.moveToFirst())start=c.getString(0);c.close();int late=Math.max(0,currentMinutes()-toMinutes(start));ContentValues v=new ContentValues();v.put("schedule_id",scheduleId);v.put("date",today());v.put("status",status);v.put("check_in",new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date()));v.put("late_minutes",late);v.put("note",note);v.put("recorded_by",userId);getWritableDatabase().insertWithOnConflict("teacher_attendance",null,v,SQLiteDatabase.CONFLICT_REPLACE);audit(userId,"TEACHER_ATTENDANCE","schedule="+scheduleId+", status="+status+", late="+late); }
    public int getScheduledCount(String role,String teacherCode){ Cursor c=getTodaySchedules(role,teacherCode);int n=c.getCount();c.close();return n; }
    public int getPresentTeacherCount(String role,String teacherCode){ Cursor c=getTodaySchedules(role,teacherCode);int n=0;while(c.moveToNext())if("HADIR".equals(c.getString(c.getColumnIndexOrThrow("attendance_status"))))n++;c.close();return n; }
    public int getEmptyClassCount(){ Cursor c=getTodaySchedules("ADMIN",null);int n=0,tol=getIntSetting("late_tolerance_minutes",15),now=currentMinutes();while(c.moveToNext()){String st=c.getString(c.getColumnIndexOrThrow("start_time"));String status=c.getString(c.getColumnIndexOrThrow("attendance_status"));if(now>toMinutes(st)+tol&&!"HADIR".equals(status))n++;}c.close();return n; }
    public Cursor getClasses(){ return getReadableDatabase().rawQuery("SELECT code,name,level FROM classes ORDER BY code",null); }
    public Cursor getTeachers(){ return getReadableDatabase().rawQuery("SELECT code,name,phone,status FROM teachers ORDER BY name",null); }
    public Cursor getSubjects(){ return getReadableDatabase().rawQuery("SELECT code,name FROM subjects ORDER BY name",null); }
    public Cursor getStudents(String classCode){ return getReadableDatabase().rawQuery("SELECT st.id,st.nis,st.name,COALESCE(sa.status,'BELUM') status,COALESCE(sa.note,'') note FROM students st LEFT JOIN student_attendance sa ON sa.student_id=st.id AND sa.date=? WHERE st.class_code=? AND st.status='AKTIF' ORDER BY st.name",new String[]{today(),classCode}); }
    public void markStudentAttendance(long userId,long studentId,String status,String note){ ContentValues v=new ContentValues();v.put("student_id",studentId);v.put("date",today());v.put("status",status);v.put("note",note);v.put("recorded_by",userId);getWritableDatabase().insertWithOnConflict("student_attendance",null,v,SQLiteDatabase.CONFLICT_REPLACE);audit(userId,"STUDENT_ATTENDANCE","student="+studentId+", status="+status); }
    public int getAbsentStudentCount(){ Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM student_attendance WHERE date=? AND status<>'HADIR'",new String[]{today()});int n=c.moveToFirst()?c.getInt(0):0;c.close();return n; }
    public Cursor getHonorRecap(String role,String teacherCode){ String sql="SELECT t.code,t.name,COUNT(CASE WHEN a.status='HADIR' AND substr(a.date,1,7)=? THEN 1 END) sessions,COALESCE(SUM(CASE WHEN a.status='HADIR' AND substr(a.date,1,7)=? THEN ((CAST(substr(s.end_time,1,2) AS INTEGER)*60+CAST(substr(s.end_time,4,2) AS INTEGER))-(CAST(substr(s.start_time,1,2) AS INTEGER)*60+CAST(substr(s.start_time,4,2) AS INTEGER))) ELSE 0 END),0) minutes FROM teachers t LEFT JOIN schedules s ON s.teacher_code=t.code LEFT JOIN teacher_attendance a ON a.schedule_id=s.id WHERE t.status='AKTIF'"; if("GURU".equals(role)) return getReadableDatabase().rawQuery(sql+" AND t.code=? GROUP BY t.code,t.name ORDER BY t.name",new String[]{currentMonth(),currentMonth(),teacherCode}); return getReadableDatabase().rawQuery(sql+" GROUP BY t.code,t.name ORDER BY t.name",new String[]{currentMonth(),currentMonth()}); }
    public long honorForMinutes(int minutes){ return Math.round((minutes/60.0)*getIntSetting("honor_per_hour",15000)); }
    public void addTeacher(long userId,String code,String name){ ContentValues v=new ContentValues();v.put("code",code.trim().toUpperCase(Locale.ROOT));v.put("name",name.trim());v.put("phone","-");getWritableDatabase().insertOrThrow("teachers",null,v);audit(userId,"MASTER_TEACHER_ADD",code+" "+name); }
    public void addClass(long userId,String code,String name){ ContentValues v=new ContentValues();v.put("code",code.trim().toUpperCase(Locale.ROOT));v.put("name",name.trim());v.put("level","-");getWritableDatabase().insertOrThrow("classes",null,v);audit(userId,"MASTER_CLASS_ADD",code+" "+name); }
    public void addSubject(long userId,String code,String name){ ContentValues v=new ContentValues();v.put("code",code.trim().toUpperCase(Locale.ROOT));v.put("name",name.trim());getWritableDatabase().insertOrThrow("subjects",null,v);audit(userId,"MASTER_SUBJECT_ADD",code+" "+name); }
    public void addStudent(long userId,String nis,String name,String cls){ ContentValues v=new ContentValues();v.put("nis",nis.trim());v.put("name",name.trim());v.put("class_code",cls);getWritableDatabase().insertOrThrow("students",null,v);audit(userId,"MASTER_STUDENT_ADD",nis+" "+name); }
    public void addSchedule(long userId,int day,String teacher,String cls,String subject,String start,String end){ ContentValues v=new ContentValues();v.put("day_of_week",day);v.put("teacher_code",teacher);v.put("class_code",cls);v.put("subject_code",subject);v.put("start_time",start);v.put("end_time",end);getWritableDatabase().insertOrThrow("schedules",null,v);audit(userId,"SCHEDULE_ADD",teacher+"/"+cls+"/"+subject+" "+start+"-"+end); }
    public Cursor getAllSchedules(){ return getReadableDatabase().rawQuery("SELECT s.id,s.day_of_week,s.start_time,s.end_time,t.name teacher_name,s.class_code,p.name subject_name FROM schedules s JOIN teachers t ON t.code=s.teacher_code JOIN subjects p ON p.code=s.subject_code WHERE s.active=1 ORDER BY s.day_of_week,s.start_time",null); }
    public Cursor getAudit(){ return getReadableDatabase().rawQuery("SELECT a.created_at,COALESCE(u.display_name,'Sistem') actor,a.action,COALESCE(a.detail,'') detail FROM audit_logs a LEFT JOIN users u ON u.id=a.user_id ORDER BY a.id DESC LIMIT 100",null); }
    public void audit(long userId,String action,String detail){ ContentValues v=new ContentValues();v.put("created_at",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(new Date()));if(userId>0)v.put("user_id",userId);v.put("action",action);v.put("detail",detail);getWritableDatabase().insert("audit_logs",null,v); }
    public String buildHonorCsv(String role,String teacherCode){ StringBuilder sb=new StringBuilder("Kode Guru,Nama Guru,Bulan,Sesi Hadir,Total Jam,Tarif per Jam,Honor\n");Cursor c=getHonorRecap(role,teacherCode);int rate=getIntSetting("honor_per_hour",15000);while(c.moveToNext()){int mins=c.getInt(3);sb.append(csv(c.getString(0))).append(',').append(csv(c.getString(1))).append(',').append(currentMonth()).append(',').append(c.getInt(2)).append(',').append(String.format(Locale.US,"%.2f",mins/60.0)).append(',').append(rate).append(',').append(honorForMinutes(mins)).append('\n');}c.close();return sb.toString(); }
    private String csv(String s){ return "\""+(s==null?"":s.replace("\"","\"\""))+"\""; }
}
