package com.airi.sdmover;
import java.io.*; import java.util.concurrent.TimeUnit;
public final class RootShell {
 public static final class Result { public final int code; public final String out,err; Result(int c,String o,String e){code=c;out=o;err=e;} public boolean ok(){return code==0;} public String combined(){return (out+(err.isEmpty()?"":"\n"+err)).trim();}}
 public static boolean hasRoot(){try{Result r=run("id",5);return r.ok()&&r.out.contains("uid=0");}catch(Exception e){return false;}}
 public static Result run(String c)throws Exception{return run(c,90);} public static Result run(String c,int t)throws Exception{Process p=new ProcessBuilder("su","-c",c).start();boolean d=p.waitFor(t,TimeUnit.SECONDS);if(!d){p.destroyForcibly();return new Result(124,"","Command timed out");}return new Result(p.exitValue(),read(p.getInputStream()),read(p.getErrorStream()));}
 private static String read(InputStream in)throws IOException{BufferedReader br=new BufferedReader(new InputStreamReader(in));StringBuilder sb=new StringBuilder();String l;while((l=br.readLine())!=null){if(sb.length()>0)sb.append('\n');sb.append(l);}return sb.toString();}
 public static String shQuote(String s){return "'"+s.replace("'","'\\''")+"'";}
}
