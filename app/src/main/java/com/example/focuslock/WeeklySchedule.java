package com.example.focuslock;
import java.util.*;
/** Local civil-time weekly windows; day bits are Monday=0 through Sunday=6. */
final class WeeklySchedule {
 final String id,name; final int days,startMinute,endMinute; final boolean strict,enabled; final Set<String> apps;
 WeeklySchedule(String id,String name,int days,int start,int end,boolean strict,boolean enabled,Set<String> apps){this.id=id;this.name=name;this.days=days;startMinute=start;endMinute=end;this.strict=strict;this.enabled=enabled;this.apps=new HashSet<>(apps);}
 List<long[]> windows(long from,long to,TimeZone zone){
  List<long[]> out=new ArrayList<>();if(!enabled||days==0||apps.isEmpty()||startMinute==endMinute||to<=from)return out;
  Calendar day=Calendar.getInstance(zone);day.setTimeInMillis(from);day.set(Calendar.HOUR_OF_DAY,0);day.set(Calendar.MINUTE,0);day.set(Calendar.SECOND,0);day.set(Calendar.MILLISECOND,0);day.add(Calendar.DATE,-1);
  while(day.getTimeInMillis()<to){int bit=(day.get(Calendar.DAY_OF_WEEK)+5)%7;
   if((days&(1<<bit))!=0){Calendar a=(Calendar)day.clone();a.set(Calendar.HOUR_OF_DAY,startMinute/60);a.set(Calendar.MINUTE,startMinute%60);Calendar b=(Calendar)day.clone();if(endMinute<startMinute)b.add(Calendar.DATE,1);b.set(Calendar.HOUR_OF_DAY,endMinute/60);b.set(Calendar.MINUTE,endMinute%60);long lo=Math.max(from,a.getTimeInMillis()),hi=Math.min(to,b.getTimeInMillis());if(hi>lo)out.add(new long[]{lo,hi});}
   day.add(Calendar.DATE,1);
  }return out;
 }
 boolean active(long now){return !windows(now,now+1,TimeZone.getDefault()).isEmpty();}
 long activeEnd(long now){List<long[]> w=windows(now,now+2*86400000L,TimeZone.getDefault());return !w.isEmpty()&&w.get(0)[0]==now?w.get(0)[1]:0;}
 static long covered(List<WeeklySchedule> schedules,long from,long to,TimeZone zone){List<long[]> spans=new ArrayList<>();for(WeeklySchedule s:schedules)spans.addAll(s.windows(from,to,zone));spans.sort(Comparator.comparingLong(x->x[0]));long total=0,lo=-1,hi=-1;for(long[] w:spans){if(lo<0){lo=w[0];hi=w[1];}else if(w[0]<=hi)hi=Math.max(hi,w[1]);else{total+=hi-lo;lo=w[0];hi=w[1];}}return total+(lo<0?0:hi-lo);}
}
