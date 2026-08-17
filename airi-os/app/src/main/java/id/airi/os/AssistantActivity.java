package id.airi.os;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class AssistantActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int REQ_AUDIO = 77;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private TextView status;
    private boolean ttsReady;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        tts = new TextToSpeech(this, this);
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(listener);
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(48), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(247,245,239));

        TextView name = text("Irzuqni", 36, true);
        root.addView(name);
        TextView sub = text("AIRI Voice Assistant", 13, false);
        sub.setTextColor(Color.DKGRAY);
        root.addView(sub, margin(-1,-2,0,2,0,28));

        status = text("Sentuh tombol dan bicara", 17, false);
        status.setGravity(Gravity.CENTER);
        root.addView(status, margin(-1,dp(92),0,0,0,22));

        TextView listen = button("🎙  Dengarkan saya");
        listen.setOnClickListener(v -> startListening());
        root.addView(listen, margin(-1,dp(56),0,0,0,12));

        TextView examples = text("Coba: “buka kamera”, “buka WhatsApp”, “buka Wi-Fi”, “cari laptop terbaik”, “buka pengaturan”, atau “jam berapa”.", 14, false);
        examples.setTextColor(Color.rgb(90,90,90));
        root.addView(examples, margin(-1,-2,0,12,0,0));
        setContentView(root);
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO);
            return;
        }
        if (recognizer == null) {
            say("Maaf, pengenalan suara tidak tersedia di perangkat ini.");
            return;
        }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID");
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        status.setText("Saya mendengarkan…");
        recognizer.startListening(i);
    }

    private final RecognitionListener listener = new RecognitionListener() {
        @Override public void onReadyForSpeech(Bundle params) { status.setText("Silakan bicara…"); }
        @Override public void onBeginningOfSpeech() {}
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() { status.setText("Memahami perintah…"); }
        @Override public void onError(int error) { status.setText("Saya belum mendengar jelas. Coba lagi."); }
        @Override public void onPartialResults(Bundle partialResults) {
            ArrayList<String> r = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (r != null && !r.isEmpty()) status.setText(r.get(0));
        }
        @Override public void onEvent(int eventType, Bundle params) {}
        @Override public void onResults(Bundle results) {
            ArrayList<String> r = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (r == null || r.isEmpty()) return;
            String cmd = r.get(0);
            status.setText("“" + cmd + "”");
            execute(cmd);
        }
    };

    private void execute(String raw) {
        String s = raw.toLowerCase(Locale.ROOT).trim();
        if (s.contains("kamera")) {
            say("Baik, saya buka kamera.");
            Intent camera = new Intent("android.media.action.IMAGE_CAPTURE");
            try { startActivity(camera); } catch (Exception e) { openAppContaining("camera"); }
            return;
        }
        if (s.contains("wifi") || s.contains("wi-fi")) {
            say("Membuka pengaturan Wi-Fi.");
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); return;
        }
        if (s.contains("bluetooth")) {
            say("Membuka Bluetooth.");
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)); return;
        }
        if (s.contains("pengaturan") || s.contains("settings")) {
            say("Membuka pengaturan.");
            startActivity(new Intent(Settings.ACTION_SETTINGS)); return;
        }
        if (s.contains("jam berapa") || s.equals("jam")) {
            String time = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new java.util.Date());
            say("Sekarang pukul " + time.replace(':', '.')); return;
        }
        if (s.startsWith("buka ")) {
            String target = s.substring(5).trim();
            if (openAppContaining(target)) say("Baik, saya buka " + target + ".");
            else say("Saya belum menemukan aplikasi " + target + ".");
            return;
        }
        if (s.startsWith("cari ") || s.startsWith("search ")) {
            String q = s.replaceFirst("^(cari|search)\\s+", "");
            Intent web = new Intent(Intent.ACTION_WEB_SEARCH);
            web.putExtra("query", q);
            try { startActivity(web); say("Saya carikan " + q + "."); }
            catch (Exception e) { say("Pencarian web tidak tersedia."); }
            return;
        }
        say("Saya mendengar " + raw + ". Untuk sekarang saya bisa membuka aplikasi, kamera, pengaturan, Wi-Fi, Bluetooth, dan pencarian.");
    }

    private boolean openAppContaining(String needle) {
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        for (android.content.pm.ResolveInfo info : getPackageManager().queryIntentActivities(main, 0)) {
            CharSequence label = info.loadLabel(getPackageManager());
            String l = label == null ? "" : label.toString().toLowerCase(Locale.ROOT);
            if (l.contains(needle.toLowerCase(Locale.ROOT))) {
                Intent launch = getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);
                if (launch != null) { startActivity(launch); return true; }
            }
        }
        return false;
    }

    private void say(String text) {
        status.setText(text);
        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "irzuqni");
    }

    @Override public void onInit(int statusCode) {
        if (statusCode == TextToSpeech.SUCCESS) {
            ttsReady = true;
            int r = tts.setLanguage(new Locale("id", "ID"));
            tts.setPitch(1.08f);
            tts.setSpeechRate(0.93f);
            say("Assalamualaikum. Saya Irzuqni. Ada yang bisa saya bantu?");
        }
    }

    @Override public void onRequestPermissionsResult(int req, String[] p, int[] g) {
        super.onRequestPermissionsResult(req,p,g);
        if (req == REQ_AUDIO && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) startListening();
    }

    @Override protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    private TextView text(String s, float size, boolean bold) {
        TextView v = new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(Color.rgb(28,32,35));
        v.setTypeface(android.graphics.Typeface.create("sans", bold ? 1 : 0)); return v;
    }
    private TextView button(String s) {
        TextView v = text(s,16,true); v.setTextColor(Color.WHITE); v.setGravity(Gravity.CENTER);
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(Color.rgb(48,86,117)); d.setCornerRadius(dp(28)); v.setBackground(d); return v;
    }
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b) {
        int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w);
        int hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p;
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
