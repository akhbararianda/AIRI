package id.airi.eduattend;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class BackupUtil {
    private BackupUtil() {}
    public static void exportDb(Context context, Uri uri) throws Exception {
        File source = context.getDatabasePath(DatabaseHelper.DB_NAME);
        try (InputStream in = new FileInputStream(source); OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            copy(in,out);
        }
    }
    public static void importDb(Context context, Uri uri) throws Exception {
        File target = context.getDatabasePath(DatabaseHelper.DB_NAME);
        File temp = new File(target.getParentFile(), DatabaseHelper.DB_NAME + ".restore");
        try (InputStream in = context.getContentResolver().openInputStream(uri); OutputStream out = new FileOutputStream(temp)) { copy(in,out); }
        if (!temp.exists() || temp.length() < 1024) throw new IllegalStateException("File backup tidak valid");
        if (target.exists() && !target.delete()) throw new IllegalStateException("Database lama tidak dapat diganti");
        if (!temp.renameTo(target)) throw new IllegalStateException("Restore database gagal");
    }
    private static void copy(InputStream in, OutputStream out) throws Exception { byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);out.flush(); }
}
