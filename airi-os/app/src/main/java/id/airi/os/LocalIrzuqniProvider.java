package id.airi.os;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalIrzuqniProvider implements AiProvider {
    private final Context context;
    public LocalIrzuqniProvider(Context c){context=c.getApplicationContext();}
    @Override public String name(){return "Irzuqni Local Bridge";}
    @Override public void query(String prompt, Callback cb){
        try{
            String p=prompt==null?"":prompt.trim().toLowerCase(Locale.ROOT);
            if(p.isEmpty()){cb.onResult("Ketik perintah atau pertanyaan untuk Irzuqni.");return;}
            if(p.contains("jam")||p.contains("waktu")){cb.onResult("Sekarang pukul "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date())+".");return;}
            if(p.contains("kamera")){Intent i=new Intent("android.media.action.IMAGE_CAPTURE");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);cb.onResult("Kamera dibuka.");return;}
            if(p.contains("wifi")||p.contains("internet")){Intent i=new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);cb.onResult("Panel internet dibuka.");return;}
            if(p.contains("bluetooth")){Intent i=new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);cb.onResult("Pengaturan Bluetooth dibuka.");return;}
            if(p.contains("setting")||p.contains("pengaturan")){Intent i=new Intent(context,AiriSettingsActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);cb.onResult("AIRI Settings dibuka.");return;}
            cb.onResult("Saya memahami: “"+prompt.trim()+"”. Mode lokal aktif. Untuk jawaban generatif penuh, AIRI dapat dihubungkan ke penyedia LLM melalui AiProvider tanpa mengubah launcher.");
        }catch(Exception e){cb.onError("Perintah belum dapat dijalankan: "+e.getClass().getSimpleName());}
    }
}
