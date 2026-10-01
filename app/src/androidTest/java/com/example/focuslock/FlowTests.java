package com.example.focuslock;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
public class FlowTests extends Instrumentation {
 private int passed=0; private String mode="";
 private void check(boolean v,String name){if(!v)throw new AssertionError(name);passed++;}
 @Override public void onCreate(Bundle b){super.onCreate(b);mode=b==null?"":b.getString("mode","");start();}
 @Override public void onStart(){Bundle result=new Bundle();try{
  Context c=getTargetContext();c.getSharedPreferences(LockPrefs.PREFS,0).edit().clear().commit();
  if(mode.equals("block")){LockPrefs.start(c,new HashSet<>(Arrays.asList("com.example.focusfixture")),30000,true);result.putString("stream","BLOCK_SETUP\n");finish(Activity.RESULT_OK,result);return;}
  check(!LockPrefs.active(c),"initial unlocked");
  LockPrefs.start(c,new HashSet<>(),60000,true);check(!LockPrefs.active(c),"empty selection rejected");
  Set<String> apps=new HashSet<>();apps.add("com.android.chrome");
  LockPrefs.start(c,apps,60000,false);check(LockPrefs.active(c)&&!LockPrefs.strict(c),"normal start");
  LockPrefs.stop(c);check(!LockPrefs.active(c),"normal early stop");
  LockPrefs.start(c,apps,60000,true);check(LockPrefs.strict(c),"strict start");
  long before=LockPrefs.remaining(c);LockPrefs.stop(c);check(LockPrefs.active(c),"strict early stop rejected");
  LockPrefs.start(c,new HashSet<>(Arrays.asList("other")),1000,false);check(LockPrefs.selected(c).equals(apps)&&LockPrefs.strict(c),"active reconfiguration rejected");
  c.getSharedPreferences(LockPrefs.PREFS,0).edit().putLong(LockPrefs.KEY_END,1).commit();check(LockPrefs.remaining(c)>before-10000,"wall clock does not shorten current boot");
  c.getSharedPreferences(LockPrefs.PREFS,0).edit().putLong("elapsed_end",SystemClock.elapsedRealtime()-1).commit();check(!LockPrefs.active(c)&&!LockPrefs.strict(c),"automatic expiry");
  LockPrefs.stop(c);
  Calendar now=Calendar.getInstance();int minute=now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE);Set<String> fixture=new HashSet<>(Arrays.asList("com.example.focusfixture"));
  WeeklySchedule strictSchedule=new WeeklySchedule("strict","매일 엄격",127,(minute+1439)%1440,(minute+2)%1440,true,true,fixture);
  check(FocusState.save(c,strictSchedule),"save new strict schedule");
  check(FocusState.schedules(c).size()==1&&FocusState.schedules(c).get(0).active(System.currentTimeMillis()),"persisted active weekly schedule");
  check(!FocusState.delete(c,"strict"),"running strict deletion rejected");
  check(!FocusState.save(c,new WeeklySchedule("strict","changed",127,minute,(minute+2)%1440,false,false,fixture)),"running strict edit rejected");
  WeeklySchedule normal=new WeeklySchedule("normal","일반 예약",127,(minute+1439)%1440,(minute+2)%1440,false,true,fixture);check(FocusState.save(c,normal)&&FocusState.schedules(c).size()==2,"multiple independent schedules");check(FocusState.delete(c,"normal"),"normal schedule deletable while active");
  c.getSharedPreferences(LockPrefs.PREFS,0).edit().putString("schedules","[]").commit();
  FocusState.creditAutomatic(c,3600000);check(FocusState.coins(c)==1,"automatic hourly reward");
  check(FocusState.start(c,3600000),"focus start");FocusState.cancel(c,"cancelled");check(FocusState.coins(c)==1&&!FocusState.focusActive(c),"abandon gives no reward");
  FocusState.start(c,3600000);c.getSharedPreferences(LockPrefs.PREFS,0).edit().putLong("focus_end",SystemClock.elapsedRealtime()-1).commit();FocusState.settle(c);check(FocusState.coins(c)==11,"focus completion adds ten alongside automatic");FocusState.settle(c);check(FocusState.coins(c)==11,"completion idempotent");
  FocusState.start(c,3600000);c.getSharedPreferences(LockPrefs.PREFS,0).edit().putInt("focus_boot",-99).commit();FocusState.settle(c);check(FocusState.coins(c)==11&&!FocusState.focusActive(c),"reboot cancels focus reward");
  c.getSharedPreferences(LockPrefs.PREFS,0).edit().clear().commit();
  Intent i=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);Activity a=startActivitySync(i);waitForIdleSync();
  runOnMainSync(()->{View root=a.findViewById(android.R.id.content);check(find(root,"집중 시작")!=null,"start flow visible");check(find(root,"엄격 모드")!=null,"strict choice visible");find(root,"예약").performClick();check(find(root,"＋ 예약 추가")!=null,"schedule UI visible");find(root,"집중").performClick();check(find(root,"집중모드 시작")!=null&&find(root,"잔잔한 BGM")!=null,"focus and music controls visible");find(root,"상점").performClick();check(find(root,"상점 준비 중")!=null,"shop UI visible");find(root,"잠금").performClick();});
  result.putString("stream","PASS "+passed+" checks\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable t){result.putString("stream","FAIL: "+t);finish(Activity.RESULT_CANCELED,result);}}
 private View find(View v,String text){if(v instanceof TextView&&((TextView)v).getText().toString().equals(text))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int n=0;n<g.getChildCount();n++){View f=find(g.getChildAt(n),text);if(f!=null)return f;}}return null;}
}
