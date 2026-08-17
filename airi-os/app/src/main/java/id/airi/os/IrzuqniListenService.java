package id.airi.os;

import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import java.util.*;

public class IrzuqniListenService extends Service implements TextToSpeech.OnInitListener {
    public static final String STOP="id.airi.os.STOP_LISTEN";
    private SpeechRecognizer sr; private TextToSpeech tts; private boolean running=true, ready=false;
    @Override public void onCreate(){super.onCreate();createChannel();startForeground(2202,notification());tts=new TextToSpeech(this,this);if(SpeechRecognizer.isRecognitionAvailable(this)){sr=SpeechRecognizer.createSpeechRecognizer(this);sr.setRecognitionListener(listener);}startLoop();}
    @Override public int onStartCommand(Intent i,int f,int id){if(i!=null&&STOP.equals(i.getAction())){stopSelf();return START_NOT_STICKY;}startLoop();return START_STICKY;}
    private Notification notification(){Intent stop=new Intent(this,IrzuqniListenService.class);stop.setAction(STOP);PendingIntent pi=PendingIntent.getService(this,1,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);return new Notification.Builder(this,"irzuqni_listen").setContentTitle("Irzuqni sedang mendengarkan").setContentText("Ucapkan perintah AIRI. Sentuh OFF untuk berhenti.").setSmallIcon(android.R.drawable.ic_btn_speak_now).addAction(new Notification.Action.Builder(null,"OFF",pi).build()).setOngoing(true).build();}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationManager n=getSystemService(NotificationManager.class);n.createNotificationChannel(new NotificationChannel("irzuqni_listen","Irzuqni Assistant",NotificationManager.IMPORTANCE_LOW));}}
    private void startLoop(){if(sr==null||!running)return;new Handler(Looper.getMainLooper()).postDelayed(()->{try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"id-ID");i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);sr.startListening(i);}catch(Exception e){restart();}},500);}
    private void restart(){if(!running)return;new Handler(Looper.getMainLooper()).postDelayed(this::startLoop,800);}
    private final RecognitionListener listener=new RecognitionListener(){public void onReadyForSpeech(Bundle b){}public void onBeginningOfSpeech(){}public void onRmsChanged(float f){}public void onBufferReceived(byte[] b){}public void onEndOfSpeech(){}public void onError(int e){restart();}public void onPartialResults(Bundle b){}public void onEvent(int e,Bundle b){}public void onResults(Bundle b){ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(r!=null&&!r.isEmpty())handle(r.get(0));restart();}};
    private void handle(String raw){String s=raw.toLowerCase(Locale.ROOT);if(!(s.contains("irzuqni")||s.contains("airi")))return;String cmd=s.replace("irzuqni","").replace("airi","").trim();if(cmd.contains("kamera")){speak("Baik, membuka kamera.");Intent i=new Intent("android.media.action.IMAGE_CAPTURE");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);try{startActivity(i);}catch(Exception ignored){}}else if(cmd.contains("wifi")||cmd.contains("wi-fi")){speak("Membuka Wi-Fi.");Intent i=new Intent(Settings.ACTION_WIFI_SETTINGS);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}else if(cmd.contains("pengaturan")){Intent i=new Intent(Settings.ACTION_SETTINGS);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}else if(cmd.startsWith("buka ")){openApp(cmd.substring(5).trim());}else speak("Saya mendengar perintah Anda.");}
    private void openApp(String n){Intent m=new Intent(Intent.ACTION_MAIN,null);m.addCategory(Intent.CATEGORY_LAUNCHER);for(android.content.pm.ResolveInfo r:getPackageManager().queryIntentActivities(m,0)){CharSequence l=r.loadLabel(getPackageManager());if(l!=null&&l.toString().toLowerCase(Locale.ROOT).contains(n)){Intent x=getPackageManager().getLaunchIntentForPackage(r.activityInfo.packageName);if(x!=null){x.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(x);speak("Membuka "+n);return;}}}speak("Aplikasi "+n+" tidak ditemukan.");}
    private void speak(String x){if(ready)tts.speak(x,TextToSpeech.QUEUE_FLUSH,null,"irzuqni_bg");}
    public void onInit(int s){if(s==TextToSpeech.SUCCESS){ready=true;tts.setLanguage(new Locale("id","ID"));tts.setPitch(1.08f);tts.setSpeechRate(.93f);}}
    @Override public void onDestroy(){running=false;if(sr!=null)sr.destroy();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
