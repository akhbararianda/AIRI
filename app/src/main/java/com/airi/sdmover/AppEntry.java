package com.airi.sdmover;
import android.graphics.drawable.Drawable;
public final class AppEntry {
 public final String label, packageName, sourceDir; public final Drawable icon;
 public AppEntry(String l,String p,String s,Drawable i){label=l;packageName=p;sourceDir=s;icon=i;}
 public boolean onAdoptedStorage(){return sourceDir!=null&&sourceDir.contains("/mnt/expand/");}
}
