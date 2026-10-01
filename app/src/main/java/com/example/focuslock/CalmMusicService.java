package com.example.focuslock;
import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
/** Three original synthesized soundscapes. No network or external copyrighted recordings. */
public class CalmMusicService extends Service {
 static final String[] TITLES={"고요한 건반","느린 구름","비 오는 창가"};
 private AudioTrack audio;private AudioManager manager;private AudioFocusRequest request;private int current=-1,generation=0;private boolean ready,hasFocus;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){public void run(){if(!FocusState.focusActive(CalmMusicService.this)||!FocusState.p(CalmMusicService.this).getBoolean("bgm",true)){stopSelf();return;}int selected=FocusState.p(CalmMusicService.this).getInt("track",0);if(current!=selected)prepare(selected);if(audio!=null&&ready)audio.setVolume(FocusState.p(CalmMusicService.this).getInt("volume",35)/100f);handler.postDelayed(this,1000);}};
 static void sync(Context c){if(FocusState.focusActive(c)&&FocusState.p(c).getBoolean("bgm",true)){Intent i=new Intent(c,CalmMusicService.class);try{c.startForegroundService(i);}catch(IllegalStateException e){FocusState.p(c).edit().putString("music_error","BGM을 다시 켜 주세요.").apply();}}else c.stopService(new Intent(c,CalmMusicService.class));}
 @Override public void onCreate(){super.onCreate();NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);n.createNotificationChannel(new NotificationChannel("focus_music","집중 BGM",NotificationManager.IMPORTANCE_LOW));PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);Notification notification=new Notification.Builder(this,"focus_music").setSmallIcon(R.drawable.ic_focus).setContentTitle("FocusLock · 집중 BGM").setContentText("집중 화면에서 곡과 음량을 조절할 수 있어요").setContentIntent(open).setOngoing(true).build();startForeground(7,notification);
  manager=(AudioManager)getSystemService(AUDIO_SERVICE);request=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setWillPauseWhenDucked(true).setOnAudioFocusChangeListener(change->{hasFocus=change==AudioManager.AUDIOFOCUS_GAIN;if(audio!=null&&ready){if(hasFocus)audio.play();else audio.pause();}},handler).build();hasFocus=manager.requestAudioFocus(request)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
 }
 @Override public int onStartCommand(Intent i,int flags,int id){handler.removeCallbacks(tick);handler.post(tick);return START_NOT_STICKY;}
 private void prepare(int which){current=Math.max(0,Math.min(2,which));int token=++generation;ready=false;if(audio!=null){audio.release();audio=null;}final int selected=current;
  new Thread(()->{short[] pcm=render(selected);handler.post(()->{if(token!=generation||!FocusState.focusActive(this))return;try{audio=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setSampleRate(22050).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(pcm.length*2).build();audio.write(pcm,0,pcm.length);audio.setLoopPoints(0,pcm.length,-1);audio.setVolume(FocusState.p(this).getInt("volume",35)/100f);ready=true;if(hasFocus)audio.play();FocusState.p(this).edit().remove("music_error").apply();}catch(IllegalArgumentException|IllegalStateException e){FocusState.p(this).edit().putString("music_error","BGM 재생을 시작하지 못했어요.").apply();stopSelf();}});},"FocusLock-sound").start();
 }
 static short[] render(int kind){int rate=22050,length=rate*32;short[] pcm=new short[length];double[][] chords={{130.8128,164.8138,195.9977},{110,130.8128,164.8138},{87.3071,130.8128,174.6141},{97.9989,146.8324,195.9977}};java.util.Random random=new java.util.Random(70);double rain=0;for(int i=0;i<length;i++){double t=i/(double)rate,v=0;int bar=(int)(t/8)%4;double local=t%8;
   if(kind==0){for(int n=0;n<3;n++){double age=local-n*1.5;if(age>=0){double f=chords[bar][n]*2;v+=.22*Math.exp(-age*.85)*(1-Math.exp(-age*30))*(Math.sin(2*Math.PI*f*age)+.18*Math.sin(2*Math.PI*f*2*age));}}}
   else if(kind==1){for(int n=0;n<3;n++){double f=chords[bar][n];double env=Math.sin(Math.PI*local/8);v+=.12*env*env*Math.sin(2*Math.PI*f*t);}}
   else{rain=.96*rain+.04*(random.nextDouble()*2-1);v=rain*.6;double env=Math.sin(Math.PI*local/8);v+=.06*env*env*Math.sin(2*Math.PI*chords[bar][0]*t);}
   double fade=Math.min(1,Math.min(t/1.0,(32-t)/1.0));pcm[i]=(short)(Math.max(-.85,Math.min(.85,v*fade))*32767);
  }return pcm;}
 @Override public void onDestroy(){generation++;handler.removeCallbacks(tick);if(audio!=null){audio.release();audio=null;}if(manager!=null&&request!=null)manager.abandonAudioFocusRequest(request);stopForeground(true);super.onDestroy();}
 @Override public IBinder onBind(Intent i){return null;}
}
