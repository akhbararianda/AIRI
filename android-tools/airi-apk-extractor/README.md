# AIRI APK Extractor

Aplikasi Android kecil untuk mengambil paket WhatsApp yang terpasang di perangkat dan mengekspor `base.apk` beserta seluruh split APK menjadi satu ZIP.

## Fitur
- Mendeteksi `com.whatsapp`
- Mengambil `base.apk`
- Mengambil semua `splitSourceDirs`
- Membuat `AIRI-WhatsApp-<versi>-APKs.zip`
- Tombol berbagi ZIP melalui Android Sharesheet
- Tidak membutuhkan root
- Tidak membutuhkan izin penyimpanan penuh

## Build lokal
Buka folder ini di Android Studio lalu jalankan **Build > Build APK(s)**.

APK debug berada di:
`app/build/outputs/apk/debug/app-debug.apk`

## Build otomatis
Workflow GitHub Actions `Build AIRI APK Extractor` akan menghasilkan artifact bernama `AIRI-APK-Extractor` yang berisi APK siap dipasang untuk pengujian.
