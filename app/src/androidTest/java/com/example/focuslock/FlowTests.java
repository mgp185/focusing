package com.example.focuslock;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
public class FlowTests extends Instrumentation {
 private int passed=0;
 private void check(boolean v,String name){if(!v)throw new AssertionError(name);passed++;}
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 @Override public void onStart(){Bundle result=new Bundle();try{
  Context c=getTargetContext();c.getSharedPreferences(LockPrefs.PREFS,0).edit().clear().commit();
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
  Intent i=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);Activity a=startActivitySync(i);waitForIdleSync();
  runOnMainSync(()->{View root=a.findViewById(android.R.id.content);check(find(root,"집중 시작")!=null,"start flow visible");check(find(root,"엄격 모드")!=null,"strict choice visible");});
  result.putString("stream","PASS "+passed+" checks\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable t){result.putString("stream","FAIL: "+t);finish(Activity.RESULT_CANCELED,result);}}
 private View find(View v,String text){if(v instanceof TextView&&((TextView)v).getText().toString().equals(text))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int n=0;n<g.getChildCount();n++){View f=find(g.getChildAt(n),text);if(f!=null)return f;}}return null;}
}
