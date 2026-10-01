package com.example.focuslock;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import java.util.HashSet;
import java.util.Set;
final class LockPrefs {
 static final String PREFS="focus_lock", KEY_SELECTED="selected_apps", KEY_END="lock_end_at", KEY_STRICT="strict_mode";
 static Set<String> selected(Context c) { return new HashSet<>(c.getSharedPreferences(PREFS,0).getStringSet(KEY_SELECTED,new HashSet<>())); }
 static int boot(Context c) { return Settings.Global.getInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,-1); }
 static long remaining(Context c) {
  android.content.SharedPreferences p=c.getSharedPreferences(PREFS,0);
  return SessionRules.remaining(p.getInt("boot",-2),boot(c),p.getLong("elapsed_end",0),p.getLong(KEY_END,0),SystemClock.elapsedRealtime(),System.currentTimeMillis());
 }
 static long endAt(Context c) { return System.currentTimeMillis()+remaining(c); }
 static boolean active(Context c) { return remaining(c)>0; }
 static boolean strict(Context c) { return active(c)&&c.getSharedPreferences(PREFS,0).getBoolean(KEY_STRICT,false); }
 static void start(Context c,Set<String> apps,long duration,boolean strict) {
  if(!SessionRules.canStart(remaining(c),apps.size(),duration))return;
  c.getSharedPreferences(PREFS,0).edit().putStringSet(KEY_SELECTED,new HashSet<>(apps)).putBoolean(KEY_STRICT,strict).putLong("session_duration",duration).putLong(KEY_END,System.currentTimeMillis()+duration).putLong("elapsed_end",SystemClock.elapsedRealtime()+duration).putInt("boot",boot(c)).commit();
 }
 static void stop(Context c) { if(!SessionRules.canStop(remaining(c),strict(c))) return; c.getSharedPreferences(PREFS,0).edit().putLong(KEY_END,0).putLong("elapsed_end",0).putBoolean(KEY_STRICT,false).commit(); }
}
