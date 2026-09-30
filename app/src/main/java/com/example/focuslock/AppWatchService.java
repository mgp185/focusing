package com.example.focuslock;
import android.accessibilityservice.AccessibilityService;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.*;
public class AppWatchService extends AccessibilityService {
 private LinearLayout overlay;
 private TextView countdown;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){ public void run(){
  if(!LockPrefs.active(AppWatchService.this)){remove();return;}
  long s=(LockPrefs.remaining(AppWatchService.this)+999)/1000;
  if(countdown!=null) countdown.setText(String.format(java.util.Locale.ROOT,"%02d:%02d:%02d",s/3600,s/60%60,s%60));
  handler.postDelayed(this,500);
 }};
 @Override public void onAccessibilityEvent(AccessibilityEvent event){
  if(event==null||event.getPackageName()==null)return;
  if(!LockPrefs.active(this)){remove();return;}
  String pkg=event.getPackageName().toString();
  if(LockPrefs.selected(this).contains(pkg)) show();
  else if(!pkg.equals(getPackageName())&&!pkg.equals("com.android.systemui")) remove();
 }
 private void show(){
  if(overlay!=null)return;
  boolean dark=getSharedPreferences(LockPrefs.PREFS,0).getBoolean("dark",true);
  int ink=Color.parseColor(dark?"#F1F5FC":"#182235"),muted=Color.parseColor(dark?"#9CAFC7":"#53657E"),mint=Color.parseColor("#75E5C2");
  int padding=(int)(28*getResources().getDisplayMetrics().density);
  overlay=new LinearLayout(this);overlay.setOrientation(LinearLayout.VERTICAL);overlay.setGravity(Gravity.CENTER);overlay.setPadding(padding,padding,padding,padding);overlay.setBackgroundColor(Color.parseColor(dark?"#0D1423":"#F4F7FA"));
  TextView badge=new TextView(this);badge.setText("◉  FOCUSLOCK");badge.setTextColor(mint);badge.setTextSize(16);badge.setGravity(Gravity.CENTER);overlay.addView(badge);
  TextView title=new TextView(this);title.setText("지금은 집중할 시간");title.setTextColor(ink);title.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);title.setTextSize(28);title.setGravity(Gravity.CENTER);title.setPadding(0,padding,0,padding/2);overlay.addView(title);
  TextView subtitle=new TextView(this);subtitle.setText("이 앱은 잠시 쉬어갈게요.");subtitle.setTextColor(muted);subtitle.setTextSize(16);subtitle.setGravity(Gravity.CENTER);overlay.addView(subtitle);
  countdown=new TextView(this);countdown.setTextColor(ink);countdown.setTypeface(android.graphics.Typeface.MONOSPACE,android.graphics.Typeface.BOLD);countdown.setTextSize(42);countdown.setPadding(0,padding,0,padding);countdown.setGravity(Gravity.CENTER);overlay.addView(countdown);
  Button home=new Button(this);home.setText("홈으로 돌아가서 집중하기");home.setAllCaps(false);home.setTextColor(Color.rgb(10,35,30));android.graphics.drawable.GradientDrawable surface=new android.graphics.drawable.GradientDrawable();surface.setColor(mint);surface.setCornerRadius(padding/2f);home.setBackground(surface);home.setPadding(padding,padding/2,padding,padding/2);home.setOnClickListener(v->{performGlobalAction(GLOBAL_ACTION_HOME);remove();});overlay.addView(home);
  WindowManager.LayoutParams params=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,android.graphics.PixelFormat.OPAQUE);
  ((WindowManager)getSystemService(WINDOW_SERVICE)).addView(overlay,params);handler.post(tick);
 }
 private void remove(){handler.removeCallbacks(tick);if(overlay!=null){((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(overlay);overlay=null;countdown=null;}}
 @Override public void onInterrupt(){remove();}
 @Override public void onDestroy(){remove();super.onDestroy();}
}
