package com.example.focuslock;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class WeeklyScheduleTest {
 private final TimeZone zone=TimeZone.getTimeZone("Asia/Seoul");
 private long at(int day,int hour,int min){Calendar c=Calendar.getInstance(zone);c.clear();c.set(2026,Calendar.SEPTEMBER,day,hour,min);return c.getTimeInMillis();}
 private WeeklySchedule s(int days,int start,int end){return new WeeklySchedule("x","test",days,start,end,true,true,new HashSet<>(Arrays.asList("app")));}
 @Test public void weekdayWindow(){assertEquals(3600000,WeeklySchedule.covered(Arrays.asList(s(31,19*60,22*60)),at(28,20,0),at(28,21,0),zone));}
 @Test public void weekendExcluded(){assertEquals(0,WeeklySchedule.covered(Arrays.asList(s(31,19*60,22*60)),at(27,20,0),at(27,21,0),zone));}
 @Test public void overnightStartsOnSelectedDay(){WeeklySchedule sunday=s(64,23*60,7*60);assertEquals(8*3600000L,WeeklySchedule.covered(Arrays.asList(sunday),at(27,22,0),at(28,8,0),zone));}
 @Test public void overnightNotNextSelectedDay(){assertEquals(0,WeeklySchedule.covered(Arrays.asList(s(1,23*60,7*60)),at(28,1,0),at(28,2,0),zone));}
 @Test public void overlapNeverDoubleCounts(){assertEquals(3*3600000L,WeeklySchedule.covered(Arrays.asList(s(127,19*60,21*60),s(127,20*60,22*60)),at(28,18,0),at(28,23,0),zone));}
 @Test public void adjacentAndDisjoint(){assertEquals(3*3600000L,WeeklySchedule.covered(Arrays.asList(s(127,18*60,19*60),s(127,19*60,20*60),s(127,21*60,22*60)),at(28,17,0),at(28,23,0),zone));}
 @Test public void endIsExclusive(){assertEquals(0,WeeklySchedule.covered(Arrays.asList(s(127,19*60,22*60)),at(28,22,0),at(28,23,0),zone));}
 @Test public void disabledGetsNothing(){WeeklySchedule off=new WeeklySchedule("x","x",127,0,23*60,false,false,new HashSet<>(Arrays.asList("app")));assertEquals(0,WeeklySchedule.covered(Arrays.asList(off),at(28,10,0),at(28,11,0),zone));}
 @Test public void emptyTargetsGetNothing(){WeeklySchedule empty=new WeeklySchedule("x","x",127,0,23*60,false,true,new HashSet<>());assertEquals(0,WeeklySchedule.covered(Arrays.asList(empty),at(28,10,0),at(28,11,0),zone));}
 @Test public void identicalTimesRejectedAsEmpty(){assertEquals(0,WeeklySchedule.covered(Arrays.asList(s(127,600,600)),at(28,10,0),at(28,11,0),zone));}
 @Test public void rewardsAccumulateIndependently(){assertEquals(11,RewardRules.automaticCoins(3600000)+RewardRules.focusCoins(3600000));}
 @Test public void completedFractionsCarry(){assertEquals(10,RewardRules.focusCoins(30*60000L+30*60000L));assertEquals(0,RewardRules.automaticCoins(59*60000L));assertEquals(1,RewardRules.automaticCoins(60*60000L));}
 @Test public void dstCivilWindow(){TimeZone ny=TimeZone.getTimeZone("America/New_York");Calendar c=Calendar.getInstance(ny);c.clear();c.set(2026,Calendar.MARCH,8,0,0);long start=c.getTimeInMillis();c.set(Calendar.HOUR_OF_DAY,4);assertEquals(3*3600000L,WeeklySchedule.covered(Arrays.asList(s(64,0,240)),start,c.getTimeInMillis(),ny));}
}
