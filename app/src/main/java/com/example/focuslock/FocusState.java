package com.example.focuslock;
import android.content.*;
import android.os.SystemClock;
import org.json.*;
import java.util.*;
final class FocusState {
 static SharedPreferences p(Context c){return c.getSharedPreferences(LockPrefs.PREFS,0);}
 static List<WeeklySchedule> schedules(Context c){List<WeeklySchedule> all=new ArrayList<>();try{JSONArray a=new JSONArray(p(c).getString("schedules","[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);Set<String> apps=new HashSet<>();JSONArray ap=o.getJSONArray("apps");for(int j=0;j<ap.length();j++)apps.add(ap.getString(j));all.add(new WeeklySchedule(o.getString("id"),o.getString("name"),o.getInt("days"),o.getInt("start"),o.getInt("end"),o.getBoolean("strict"),o.getBoolean("enabled"),apps));}}catch(JSONException ignored){}return all;}
 static synchronized boolean save(Context c,WeeklySchedule edited){List<WeeklySchedule> all=schedules(c);for(WeeklySchedule s:all)if(s.id.equals(edited.id)&&s.strict&&s.active(System.currentTimeMillis()))return false;all.removeIf(s->s.id.equals(edited.id));all.add(edited);write(c,all);return true;}
 static synchronized boolean delete(Context c,String id){List<WeeklySchedule> all=schedules(c);for(WeeklySchedule s:all)if(s.id.equals(id)&&s.strict&&s.active(System.currentTimeMillis()))return false;all.removeIf(s->s.id.equals(id));write(c,all);return true;}
 private static void write(Context c,List<WeeklySchedule> all){JSONArray a=new JSONArray();try{for(WeeklySchedule s:all){JSONObject o=new JSONObject();o.put("id",s.id).put("name",s.name).put("days",s.days).put("start",s.startMinute).put("end",s.endMinute).put("strict",s.strict).put("enabled",s.enabled).put("apps",new JSONArray(new ArrayList<>(s.apps)));a.put(o);}}catch(JSONException e){throw new IllegalStateException(e);}p(c).edit().putString("schedules",a.toString()).commit();}
 static synchronized boolean start(Context c,long duration){settle(c);if(focusActive(c)||duration<60000||duration>86400000)return false;return p(c).edit().putLong("focus_duration",duration).putLong("focus_end",SystemClock.elapsedRealtime()+duration).putInt("focus_boot",LockPrefs.boot(c)).putString("focus_result","").commit();}
 static synchronized void settle(Context c){SharedPreferences p=p(c);long end=p.getLong("focus_end",0);if(end==0)return;if(p.getInt("focus_boot",-2)!=LockPrefs.boot(c)){cancel(c,"재부팅으로 집중이 종료되어 보상이 취소됐어요.");return;}if(SystemClock.elapsedRealtime()>=end){long total=p.getLong("focus_completed_ms",0)+p.getLong("focus_duration",0);long gained=RewardRules.focusCoins(total)-RewardRules.focusCoins(p.getLong("focus_completed_ms",0));p.edit().putLong("focus_completed_ms",total).putLong("focus_end",0).putString("focus_result","집중 완료! +"+gained+"코인").commit();}}
 static boolean focusActive(Context c){return p(c).getInt("focus_boot",-2)==LockPrefs.boot(c)&&p(c).getLong("focus_end",0)>SystemClock.elapsedRealtime();}
 static long remaining(Context c){return focusActive(c)?p(c).getLong("focus_end",0)-SystemClock.elapsedRealtime():0;}
 static synchronized void cancel(Context c,String reason){p(c).edit().putLong("focus_end",0).putString("focus_result",reason).commit();}
 static long coins(Context c){SharedPreferences p=p(c);return RewardRules.automaticCoins(p.getLong("auto_ms",0))+RewardRules.focusCoins(p.getLong("focus_completed_ms",0));}
 static synchronized void creditAutomatic(Context c,long ms){if(ms>0)p(c).edit().putLong("auto_ms",p(c).getLong("auto_ms",0)+ms).commit();}
 static String format(long ms){long s=(Math.max(ms,0)+999)/1000;return String.format(Locale.ROOT,"%02d:%02d:%02d",s/3600,s/60%60,s%60);}
 static String quitMessage(Context c){String[] lines={"종을 치시겠습니까?","지금 포기할 거야?","조금만 더 해볼까요?","시작할 때의 마음을 떠올려 보세요.","오늘의 나에게 조금 더 시간을 줄까요?"};int n=p(c).getInt("quit_index",0);p(c).edit().putInt("quit_index",n+1).apply();return lines[Math.floorMod(n,lines.length)]+"\n\n종료하면 이번 집중의 코인은 지급되지 않아요.";}
}
