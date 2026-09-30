package com.example.focuslock;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
 private LinearLayout shell,body,nav; private TextView timerText,modeText,appCount; private Ring ring;
 private int page=0; private long duration=25*60000L; private boolean strict=true,dark=true;
 private int bg,card,ink,muted,accent; private Set<String> picked=new HashSet<>();
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable ticker=new Runnable(){public void run(){refresh();handler.postDelayed(this,1000);}};
 private boolean wasActive;
 private android.content.SharedPreferences prefs(){return getSharedPreferences(LockPrefs.PREFS,0);}
 private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);dark=prefs().getBoolean("dark",true);duration=prefs().getLong("draft_duration",25*60000L);strict=prefs().getBoolean("draft_strict",true);picked=LockPrefs.selected(this);if(saved!=null)page=saved.getInt("page",0);wasActive=LockPrefs.active(this);draw();}
 @Override public void onSaveInstanceState(Bundle b){b.putInt("page",page);super.onSaveInstanceState(b);}
 @Override protected void onResume(){super.onResume();handler.removeCallbacks(ticker);handler.post(ticker);}
 @Override protected void onPause(){handler.removeCallbacks(ticker);super.onPause();}
 private GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
 private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("tnum");if(bold)t.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));return t;}
 private void add(LinearLayout l,View v,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);l.addView(v,p);}
 private Button button(String s,Runnable action,boolean primary){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(primary?Color.rgb(10,35,30):ink);b.setBackground(shape(primary?accent:card,16));b.setMinHeight(dp(54));b.setOnClickListener(v->action.run());return b;}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 private LinearLayout panel(){LinearLayout l=column();l.setPadding(dp(20),dp(20),dp(20),dp(20));l.setBackground(shape(card,24));return l;}
 private void label(String a,String b){add(body,text(a,27,ink,true),10);add(body,text(b,14,muted,false),8);}
 private void draw(){
  bg=Color.parseColor(dark?"#0D1423":"#F4F7FA");card=Color.parseColor(dark?"#182235":"#FFFFFF");ink=Color.parseColor(dark?"#F1F5FC":"#182235");muted=Color.parseColor(dark?"#9CAFC7":"#53657E");accent=Color.parseColor(dark?"#75E5C2":"#44C7A3");
  getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  shell=column();shell.setBackgroundColor(bg);setContentView(shell);
  shell.post(()->{if(Build.VERSION.SDK_INT>=30){WindowInsetsController c=getWindow().getInsetsController();if(c!=null){int mask=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;c.setSystemBarsAppearance(dark?0:mask,mask);}}});
  shell.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(0,i.getSystemWindowInsetTop(),0,i.getSystemWindowInsetBottom());return i;});
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(24),dp(16),dp(24),dp(8));
  TextView logo=text("◉  FocusLock",20,ink,true);header.addView(logo,new LinearLayout.LayoutParams(0,-2,1));TextView tag=text("LOCAL ONLY",10,accent,true);header.addView(tag);shell.addView(header);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);body=column();body.setPadding(dp(24),dp(8),dp(24),dp(24));scroll.addView(body);shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  timerText=null;modeText=null;ring=null;appCount=null;
  if(page==0)home();else if(page==1)apps();else settings();
  if(page==0){LinearLayout action=column();action.setPadding(dp(24),dp(8),dp(24),dp(8));boolean active=LockPrefs.active(this);action.addView(button(active?(LockPrefs.strict(this)?"엄격 모드 · 종료까지 유지":"이번 집중 종료"):"집중 시작",active?this::stop:this::start,!active||!LockPrefs.strict(this)));shell.addView(action);}
  nav=new LinearLayout(this);nav.setPadding(dp(16),dp(12),dp(16),dp(12));String[] names={"집중","차단 앱","설정"};for(int n=0;n<3;n++){final int tab=n;Button b=button(names[n],()->{page=tab;draw();},false);b.setTextColor(n==page?accent:muted);nav.addView(b,new LinearLayout.LayoutParams(0,dp(52),1));}shell.addView(nav);refresh();
 }
 private boolean enabled(){String s=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);String own=new ComponentName(this,AppWatchService.class).flattenToString();if(s!=null)for(String v:s.split(":"))if(own.equalsIgnoreCase(v))return true;return false;}
 private void home(){
  boolean active=LockPrefs.active(this);label(active?"지금, 집중하는 시간":"집중할 준비 됐나요?",active?"선택한 앱을 잠시 멀리 두세요.":"방해는 줄이고, 하고 싶은 일에 가까이.");
  LinearLayout hero=panel();hero.setGravity(Gravity.CENTER_HORIZONTAL);
  modeText=text("",12,accent,true);add(hero,modeText,0);ring=new Ring(this);int ringSize=Math.min(204,(int)(getResources().getDisplayMetrics().heightPixels/getResources().getDisplayMetrics().density*.22f));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(dp(ringSize),dp(ringSize));rp.topMargin=dp(18);hero.addView(ring,rp);
  timerText=text("",36,ink,true);timerText.setGravity(Gravity.CENTER);add(hero,timerText,8);add(hero,text(active?"남은 집중 시간":"이번 집중 시간",13,muted,false),6);
  appCount=text("",14,muted,false);add(hero,appCount,20);add(body,hero,24);
  if(!enabled()){add(body,button("접근성을 켜고 차단 준비하기",this::permission,false),16);add(body,text("접근성 권한이 꺼져 있어 앱 차단이 작동하지 않아요.",12,muted,false),8);}
  if(!active){
   add(body,text("집중 시간",17,ink,true),24);LinearLayout row=new LinearLayout(this);int[] times={1,25,50,90};for(int m:times){Button b=button(m+"분",()->chooseDuration(m),duration==m*60000L);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(50),1);p.setMargins(dp(2),0,dp(2),0);row.addView(b,p);}add(body,row,12);add(body,button("직접 설정 · 최대 24시간",this::customDuration,false),8);
   LinearLayout strictPanel=panel();Switch sw=new Switch(this);sw.setText("엄격 모드");sw.setTextColor(ink);sw.setTextSize(17);sw.setChecked(strict);sw.setOnCheckedChangeListener((v,on)->{strict=on;prefs().edit().putBoolean("draft_strict",on).apply();});add(strictPanel,sw,0);add(strictPanel,text("켜면 종료 전 앱 내부에서 해제하거나 설정을 바꿀 수 없어요.",13,muted,false),10);add(body,strictPanel,18);

  }else{add(body,text("홈 화면과 선택하지 않은 앱은 계속 사용할 수 있어요.",13,muted,false),12);}
 }
 private void chooseDuration(int m){if(LockPrefs.active(this))return;duration=m*60000L;prefs().edit().putLong("draft_duration",duration).apply();draw();}
 private void customDuration(){EditText input=new EditText(this);input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);input.setHint("분 단위 · 1~1440");new AlertDialog.Builder(this).setTitle("얼마나 집중할까요?").setView(input).setNegativeButton("취소",null).setPositiveButton("설정",(d,w)->{try{int n=Integer.parseInt(input.getText().toString());if(n<1||n>1440){toast("1~1440분을 입력하세요");return;}chooseDuration(n);}catch(NumberFormatException e){toast("숫자로 시간을 입력하세요");}}).show();}
 private void permission(){new AlertDialog.Builder(this).setTitle("앱 차단을 위한 접근성 권한").setMessage("열린 앱의 패키지 이름을 확인해 선택한 앱에 차단 화면을 표시합니다. 화면 내용·입력·비밀번호는 읽지 않으며 인터넷으로 전송하지 않습니다.\n\n제한된 설정으로 차단되면 설정 → 애플리케이션 → FocusLock의 앱 정보 화면 → ⋮ → 제한된 설정 허용을 확인하세요.\n\n권한은 시스템 설정에서 언제든 끌 수 있습니다.").setNegativeButton("취소",null).setPositiveButton("동의하고 설정",(d,w)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).show();}
 private void start(){if(LockPrefs.active(this))return;if(!enabled()){permission();return;}if(picked.isEmpty()){toast("먼저 차단할 앱을 선택하세요");page=1;draw();return;}
  new AlertDialog.Builder(this).setTitle(strict?"엄격 모드로 시작할까요?":"집중을 시작할까요?").setMessage("차단 앱 "+picked.size()+"개 · "+duration/60000+"분\n\n"+(strict?"시간이 끝나기 전 앱 내부 해제·목록·시간 변경이 불가능합니다.":"언제든 이번 집중을 종료할 수 있습니다.")+"\n\n시스템 권한 해제·앱 삭제 등의 우회까지 막지는 않습니다.").setNegativeButton("돌아가기",null).setPositiveButton("집중 시작",(d,w)->{if(LockPrefs.active(this))return;LockPrefs.start(this,picked,duration,strict);wasActive=true;draw();}).show();}
 private void stop(){if(LockPrefs.strict(this)){toast("엄격 모드는 시간이 끝날 때까지 유지돼요");return;}new AlertDialog.Builder(this).setTitle("이번 집중을 종료할까요?").setNegativeButton("계속 집중",null).setPositiveButton("종료",(d,w)->{LockPrefs.stop(this);wasActive=false;draw();}).show();}
 private void apps(){label("방해되는 앱만 골라요","설정은 기기에 저장돼요. 선택하지 않은 앱은 자유롭게.");
  boolean locked=LockPrefs.active(this);if(locked)add(body,text("집중 중에는 차단 앱을 변경할 수 없어요.",13,accent,true),16);
  TextView count=text(picked.size()+"개 선택됨",15,accent,true);add(body,count,20);
  EditText search=new EditText(this);search.setSingleLine(true);search.setTextColor(ink);search.setHintTextColor(muted);search.setHint("앱 이름 검색");search.setTextSize(16);search.setPadding(dp(16),dp(12),dp(16),dp(12));search.setBackground(shape(card,14));add(body,search,16);
  LinearLayout list=column();add(body,list,12);PackageManager pm=getPackageManager();Intent q=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);List<ResolveInfo> entries=pm.queryIntentActivities(q,0);entries.sort(Comparator.comparing(x->x.loadLabel(pm).toString(),String.CASE_INSENSITIVE_ORDER));Set<String> seen=new HashSet<>();List<View> rows=new ArrayList<>();
  ResolveInfo home=pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0);String homePkg=home==null?"":home.activityInfo.packageName;android.telecom.TelecomManager telecom=(android.telecom.TelecomManager)getSystemService(TELECOM_SERVICE);String dialer=telecom==null?"":telecom.getDefaultDialerPackage();
  for(ResolveInfo info:entries){String pkg=info.activityInfo.packageName;if(!seen.add(pkg)||pkg.equals(getPackageName())||pkg.equals("com.android.settings")||pkg.equals(homePkg)||pkg.equals(dialer))continue;
   LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(14),dp(12),dp(14),dp(12));row.setBackground(shape(card,16));ImageView icon=new ImageView(this);icon.setImageDrawable(info.loadIcon(pm));row.addView(icon,new LinearLayout.LayoutParams(dp(40),dp(40)));
   CheckBox cb=new CheckBox(this);cb.setText(info.loadLabel(pm));cb.setTextSize(15);cb.setTextColor(ink);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(accent));cb.setChecked(picked.contains(pkg));cb.setEnabled(!locked);cb.setPadding(dp(12),0,0,0);row.addView(cb,new LinearLayout.LayoutParams(0,-2,1));
   cb.setOnCheckedChangeListener((v,on)->{if(LockPrefs.active(this)){cb.setChecked(LockPrefs.selected(this).contains(pkg));return;}if(on)picked.add(pkg);else picked.remove(pkg);prefs().edit().putStringSet(LockPrefs.KEY_SELECTED,new HashSet<>(picked)).apply();count.setText(picked.size()+"개 선택됨");});
   row.setTag(info.loadLabel(pm).toString().toLowerCase(Locale.getDefault()));row.setOnClickListener(v->{if(cb.isEnabled())cb.performClick();});add(list,row,8);rows.add(row);
  }
  if(rows.isEmpty())add(list,text("선택 가능한 앱을 찾지 못했어요.",14,muted,false),16);
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){String f=s.toString().toLowerCase(Locale.getDefault());for(View r:rows)r.setVisibility(r.getTag().toString().contains(f)?View.VISIBLE:View.GONE);}public void afterTextChanged(Editable e){}});
 }
 private void settings(){label("내 방식대로 집중","필요한 설정만, 명확하게.");LinearLayout appearance=panel();add(appearance,text("화면 테마",17,ink,true),0);Switch theme=new Switch(this);theme.setText("다크 모드");theme.setTextColor(ink);theme.setChecked(dark);theme.setOnCheckedChangeListener((v,on)->{dark=on;prefs().edit().putBoolean("dark",on).apply();draw();});add(appearance,theme,12);add(body,appearance,24);
  add(body,button("접근성 권한 안내·설정",this::permission,false),18);
  LinearLayout privacy=panel();add(privacy,text("당신의 집중은 당신의 기기에",17,ink,true),0);add(privacy,text("인터넷 권한 · 광고 · 계정 · 분석 SDK 없음\n화면 내용과 비밀번호를 읽지 않습니다.\n선택 앱과 잠금 설정만 기기에 저장합니다.",14,muted,false),12);add(body,privacy,18);
  LinearLayout limits=panel();add(limits,text("엄격 모드의 범위",17,ink,true),0);add(limits,text("앱 내부 조기 해제와 설정 변경을 막습니다. 시스템 권한 해제, 삭제, 강제 종료, 안전 모드와 재부팅 후 시계 변경은 막지 않습니다.\n\n멀티윈도·PiP·알림 경로와 제조사 절전 정책은 실제 기기에서 시험해야 합니다.",14,muted,false),12);add(body,limits,16);
  add(body,text("FocusLock 2.0 · 개인 시험용\n처음에는 1분 잠금으로 동작을 확인하세요.",12,muted,false),24);
 }
 private void refresh(){boolean active=LockPrefs.active(this);if(active!=wasActive){wasActive=active;draw();return;}if(timerText!=null){long ms=active?LockPrefs.remaining(this):duration;long s=(ms+999)/1000;timerText.setText(String.format(Locale.ROOT,"%02d:%02d:%02d",s/3600,s/60%60,s%60));modeText.setText(active?(LockPrefs.strict(this)?"STRICT SESSION":"FOCUS SESSION"):"READY TO FOCUS");appCount.setText((active?LockPrefs.selected(this).size():picked.size())+"개 앱 · "+(enabled()?"차단 준비 완료":"접근성 설정 필요"));if(ring!=null){long total=prefs().getLong("session_duration",duration);ring.fraction=active?(float)Math.min(1,(double)ms/Math.max(1,total)):1;ring.invalidate();}}}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
 private class Ring extends View {float fraction=1;Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);Ring(Context c){super(c);setContentDescription("집중 시간 진행 표시");}protected void onDraw(Canvas canvas){super.onDraw(canvas);float pad=dp(10);RectF r=new RectF(pad,pad,getWidth()-pad,getHeight()-pad);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(8));paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(dark?Color.rgb(39,55,76):Color.rgb(225,235,240));canvas.drawOval(r,paint);paint.setColor(accent);canvas.drawArc(r,-90,360*fraction,false,paint);paint.setStyle(Paint.Style.FILL);paint.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(dp(25));paint.setColor(ink);canvas.drawText(LockPrefs.active(MainActivity.this)?"집중 중":"FOCUS",getWidth()/2f,getHeight()/2f+dp(8),paint);}}
}
