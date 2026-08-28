package id.airi.apkextractor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {

    private static final String WA = "com.whatsapp";
    private TextView status;
    private Button exportButton;
    private Button shareButton;
    private File lastZip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(28), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(247, 241, 232));

        TextView badge = new TextView(this);
        badge.setText("AIRI");
        badge.setTextColor(Color.rgb(37, 99, 235));
        badge.setTextSize(16);
        badge.setTypeface(null, 1);
        root.addView(badge);

        TextView title = new TextView(this);
        title.setText("WhatsApp APK Extractor");
        title.setTextColor(Color.rgb(15, 23, 42));
        title.setTextSize(28);
        title.setTypeface(null, 1);
        title.setPadding(0, dp(6), 0, dp(10));
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Mengambil base.apk dan semua split APK WhatsApp yang terpasang di HP, lalu menggabungkannya menjadi satu ZIP.");
        desc.setTextColor(Color.rgb(71, 85, 105));
        desc.setTextSize(15);
        desc.setPadding(0, 0, 0, dp(22));
        root.addView(desc);

        exportButton = makeButton("Ambil WhatsApp");
        root.addView(exportButton);

        shareButton = makeButton("Bagikan ZIP");
        shareButton.setEnabled(false);
        shareButton.setAlpha(.5f);
        LinearLayout.LayoutParams shareLp = new LinearLayout.LayoutParams(-1, dp(54));
        shareLp.topMargin = dp(10);
        root.addView(shareButton, shareLp);

        status = new TextView(this);
        status.setText("Siap. Tekan “Ambil WhatsApp”.");
        status.setTextColor(Color.rgb(51, 65, 85));
        status.setTextSize(14);
        status.setPadding(0, dp(22), 0, 0);
        status.setMovementMethod(new ScrollingMovementMethod());
        root.addView(status, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView foot = new TextView(this);
        foot.setText("AIRI APK Extractor • Tidak membutuhkan root");
        foot.setGravity(Gravity.CENTER);
        foot.setTextColor(Color.rgb(100, 116, 139));
        foot.setTextSize(12);
        root.addView(foot);

        setContentView(root);

        exportButton.setOnClickListener(v -> exportWhatsApp());
        shareButton.setOnClickListener(v -> shareLastZip());
        detect();
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(37, 99, 235));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        b.setLayoutParams(lp);
        return b;
    }

    private void detect() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(WA, 0);
            ApplicationInfo ai = pi.applicationInfo;
            int splits = ai.splitSourceDirs == null ? 0 : ai.splitSourceDirs.length;
            status.setText("WhatsApp terdeteksi.\nVersi: " + pi.versionName +
                    "\nBase APK: 1\nSplit APK: " + splits +
                    "\n\nTekan “Ambil WhatsApp” untuk membuat ZIP.");
        } catch (Exception e) {
            exportButton.setEnabled(false);
            exportButton.setAlpha(.5f);
            status.setText("WhatsApp tidak terdeteksi di perangkat ini.\n\n" + e.getClass().getSimpleName());
        }
    }

    private void exportWhatsApp() {
        exportButton.setEnabled(false);
        status.setText("Membaca paket WhatsApp…");

        new Thread(() -> {
            try {
                PackageInfo pi = getPackageManager().getPackageInfo(WA, 0);
                ApplicationInfo ai = pi.applicationInfo;

                List<File> apks = new ArrayList<>();
                apks.add(new File(ai.sourceDir));
                if (ai.splitSourceDirs != null) {
                    for (String p : ai.splitSourceDirs) apks.add(new File(p));
                }

                File outDir = new File(getExternalFilesDir(null), "exports");
                if (!outDir.exists() && !outDir.mkdirs()) {
                    throw new Exception("Tidak bisa membuat folder export.");
                }

                String safeVersion = pi.versionName == null ? "unknown" : pi.versionName.replaceAll("[^0-9A-Za-z._-]", "_");
                File out = new File(outDir, "AIRI-WhatsApp-" + safeVersion + "-APKs.zip");

                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
                byte[] buffer = new byte[1024 * 128];

                StringBuilder report = new StringBuilder();
                report.append("Package: ").append(WA).append("\n");
                report.append("Version: ").append(pi.versionName).append("\n");
                report.append("Files: ").append(apks.size()).append("\n\n");

                for (int i = 0; i < apks.size(); i++) {
                    File f = apks.get(i);
                    String name;
                    if (i == 0) {
                        name = "base.apk";
                    } else {
                        String original = f.getName();
                        name = original.toLowerCase().endsWith(".apk")
                                ? original
                                : "split_" + i + ".apk";
                    }

                    report.append(name).append("  ").append(f.length()).append(" bytes\n");
                    zos.putNextEntry(new ZipEntry(name));
                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
                    int n;
                    while ((n = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, n);
                    }
                    in.close();
                    zos.closeEntry();
                }

                zos.putNextEntry(new ZipEntry("AIRI-export-info.txt"));
                byte[] info = report.toString().getBytes("UTF-8");
                zos.write(info);
                zos.closeEntry();
                zos.close();

                lastZip = out;
                runOnUiThread(() -> {
                    status.setText("Berhasil.\n\n" + report +
                            "\nZIP:\n" + out.getAbsolutePath() +
                            "\n\nTekan “Bagikan ZIP” lalu kirim ke ChatGPT.");
                    shareButton.setEnabled(true);
                    shareButton.setAlpha(1f);
                    exportButton.setEnabled(true);
                });

            } catch (final Exception e) {
                runOnUiThread(() -> {
                    status.setText("Gagal mengambil APK.\n\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
                    exportButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void shareLastZip() {
        if (lastZip == null || !lastZip.exists()) {
            status.setText("Belum ada ZIP untuk dibagikan.");
            return;
        }

        Uri uri = Uri.parse("content://id.airi.apkextractor.files/" + Uri.encode(lastZip.getName()));
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("application/zip");
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(send, "Bagikan paket WhatsApp"));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
