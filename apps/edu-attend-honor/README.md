# AIRI EduAttend v2.0

Aplikasi Android offline-first untuk operasional absensi lembaga pendidikan.

## Fitur
- Login berbasis role: Admin, Piket, Bendahara, Guru.
- Jadwal mengajar sebagai sumber kebenaran operasional.
- Absensi guru: Hadir, Izin, Sakit, Tidak Hadir.
- Pencatatan menit keterlambatan.
- Deteksi kelas kosong otomatis setelah toleransi (default 15 menit).
- Absensi santri per kelas: Hadir, Izin, Sakit, Alpha.
- Rekap honor bulanan otomatis berdasarkan sesi Hadir dan durasi jadwal.
- Tarif awal Rp15.000/jam dan dapat diubah Admin.
- Master guru, kelas, mata pelajaran, santri, dan jadwal.
- Audit log perubahan operasional.
- Ekspor rekap honor ke CSV melalui Android Storage Access Framework.
- Backup/restore database lokal.
- Android 6+ (minSdk 23), target Android 15/API 35.

## Akun awal
| Username | PIN | Role |
|---|---:|---|
| admin | 1234 | Admin |
| piket | 1111 | Piket |
| bendahara | 2222 | Bendahara |
| gr001 | 0000 | Guru |
| gr002 | 0000 | Guru |
| gr003 | 0000 | Guru |

**Ubah PIN setelah instalasi.** PIN disimpan sebagai hash SHA-256 lokal, bukan teks biasa.

## Logika kelas kosong
Sebuah sesi ditandai sebagai kelas kosong apabila waktu sekarang telah melewati `jam_mulai + toleransi` dan status sesi belum `HADIR`.

## Logika honor
`honor = (durasi jadwal dalam menit / 60) × tarif_per_jam`, hanya untuk absensi berstatus `HADIR` pada bulan berjalan.

## Build
```bash
gradle :app:assembleDebug
```
APK debug berada di `app/build/outputs/apk/debug/`.

## Catatan produksi
Versi ini stabil untuk uji operasional offline satu perangkat. Untuk multi-perangkat realtime diperlukan backend/API terpusat, autentikasi server, sinkronisasi, dan kontrol akses server-side.
