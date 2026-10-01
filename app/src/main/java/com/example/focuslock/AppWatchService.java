package com.example.focuslock;
import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.widget.*;
import java.util.*;
public class AppWatchService extends AccessibilityService {
 static volatile boolean connected;
 private LinearLayout overlay;private TextView countdown;private boolean focusOverlay,confirming;private String foreground="";
 private long lastWall,lastElapsed;private List<WeeklySchedule> previous=new ArrayList<>();private final Set<String> calls=new HashSet<>();
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){public void run(){
  long wall=System.currentTimeMillis(),elapsed=SystemClock.elapsedRealtime(),dt=elapsed-lastElapsed;
  // Credit the union of schedule windows only while this service is actually connected.
  if(dt>0&&lastWall>0&&Math.abs((wall-lastWall)-dt)<2000&&dt<120000){FocusState.creditAutomatic(AppWatchService.this,WeeklySchedule.covered(previous,lastWall,wall,TimeZone.getDefault()));}
  lastWall=wall;lastElapsed=elapsed;previous=FocusState.schedules(AppWatchService.this);FocusState.settle(AppWatchService.this);
  if(!FocusState.focusActive(AppWatchService.this))stopService(new Intent(AppWatchService.this,CalmMusicService.class));
  update();handler.postDelayed(this,1000);
 }};
 @Override protected void onServiceConnected(){super.onServiceConnected();connected=true;calls.add("com.android.phone");calls.add("com.android.incallui");calls.add("com.android.emergency");calls.add("com.samsung.android.incallui");calls.add("com.samsung.android.app.telephonyui");calls.add("com.android.server.telecom");android.telecom.TelecomManager tm=(android.telecom.TelecomManager)getSystemService(TELECOM_SERVICE);if(tm!=null&&tm.getDefaultDialerPackage()!=null)calls.add(tm.getDefaultDialerPackage());
  if(FocusState.p(this).getLong("focus_end",0)>0)FocusState.cancel(this,"접근성 서비스가 다시 연결되어 집중 보상이 취소됐어요.");
  lastWall=System.currentTimeMillis();lastElapsed=SystemClock.elapsedRealtime();previous=FocusState.schedules(this);handler.post(tick);
 }
 @Override public void onAccessibilityEvent(AccessibilityEvent event){if(event==null||event.getPackageName()==null)return;String pkg=event.getPackageName().toString();if(!pkg.equals(getPackageName())||String.valueOf(event.getClassName()).equals(MainActivity.class.getName())||String.valueOf(event.getClassName()).equals(ScheduleActivity.class.getName()))foreground=pkg;update();}
 private boolean exempt(String pkg){return pkg.isEmpty()||pkg.equals(getPackageName())||pkg.equals("com.android.systemui")||calls.contains(pkg);}
 private boolean blocked(){if(FocusState.focusActive(this))return !exempt(foreground);if(LockPrefs.active(this)&&LockPrefs.selected(this).contains(foreground))return true;for(WeeklySchedule s:FocusState.schedules(this))if(s.active(System.currentTimeMillis())&&s.apps.contains(foreground))return true;return false;}
 private long remaining(){if(FocusState.focusActive(this))return FocusState.remaining(this);long end=0,now=System.currentTimeMillis();if(LockPrefs.active(this)&&LockPrefs.selected(this).contains(foreground))end=now+LockPrefs.remaining(this);for(WeeklySchedule s:FocusState.schedules(this))if(s.apps.contains(foreground))end=Math.max(end,s.activeEnd(now));return Math.max(0,end-now);}
 private void update(){android.os.PowerManager power=(android.os.PowerManager)getSystemService(POWER_SERVICE);android.app.KeyguardManager keyguard=(android.app.KeyguardManager)getSystemService(KEYGUARD_SERVICE);if(!power.isInteractive()||keyguard.isKeyguardLocked()||!blocked()){remove();return;}boolean focus=FocusState.focusActive(this);if(overlay!=null&&focusOverlay!=focus)remove();show(focus);if(countdown!=null&&!confirming)countdown.setText(FocusState.format(remaining()));}
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
 private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.CENTER);t.setPadding(0,dp(12),0,dp(12));overlay.addView(t);return t;}
 private Button button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.rgb(10,35,30));android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(Color.parseColor("#75E5C2"));g.setCornerRadius(dp(16));b.setBackground(g);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(56));p.topMargin=dp(14);overlay.addView(b,p);b.setOnClickListener(v->action.run());return b;}
 private void show(boolean focus){if(overlay!=null)return;focusOverlay=focus;confirming=false;boolean dark=FocusState.p(this).getBoolean("dark",true);int ink=Color.parseColor(dark?"#F1F5FC":"#182235");overlay=new LinearLayout(this);overlay.setOrientation(LinearLayout.VERTICAL);overlay.setGravity(Gravity.CENTER);overlay.setPadding(dp(28),dp(28),dp(28),dp(28));overlay.setBackgroundColor(Color.parseColor(dark?"#0D1423":"#F4F7FA"));
  label("◉  FOCUSLOCK",16,Color.parseColor("#75E5C2"));label(focus?"나에게 집중하는 시간":"지금은 집중할 시간",28,ink);label(focus?"완료하면 시간당 10코인":"이 앱은 잠시 쉬어갈게요.",16,ink);countdown=label("",40,ink);countdown.setTypeface(android.graphics.Typeface.MONOSPACE,android.graphics.Typeface.BOLD);
  if(focus){button("집중 화면 · BGM 설정",()->{foreground=getPackageName();remove();Intent i=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);});button("이번 집중 포기하기",this::confirmQuit);}else button("홈으로 돌아가서 집중하기",()->{performGlobalAction(GLOBAL_ACTION_HOME);remove();});
  WindowManager.LayoutParams params=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,android.graphics.PixelFormat.OPAQUE);((WindowManager)getSystemService(WINDOW_SERVICE)).addView(overlay,params);
 }
 private void confirmQuit(){if(!FocusState.focusActive(this))return;confirming=true;overlay.removeAllViews();countdown=label(FocusState.quitMessage(this),23,Color.parseColor(FocusState.p(this).getBoolean("dark",true)?"#F1F5FC":"#182235"));button("예, 종료할게요",()->{FocusState.abandon(this);stopService(new Intent(this,CalmMusicService.class));remove();update();});button("아니오, 계속할게요",()->{remove();update();});}
 private void remove(){if(overlay!=null){((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(overlay);overlay=null;countdown=null;confirming=false;}}
 @Override public void onInterrupt(){FocusState.cancel(this,"접근성이 중단되어 집중 보상이 취소됐어요.");stopService(new Intent(this,CalmMusicService.class));remove();}
 @Override public void onDestroy(){connected=false;handler.removeCallbacks(tick);if(FocusState.p(this).getLong("focus_end",0)>0)FocusState.cancel(this,"접근성이 종료되어 집중 보상이 취소됐어요.");stopService(new Intent(this,CalmMusicService.class));remove();super.onDestroy();}
}
