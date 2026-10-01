package com.example.focuslock;
final class SessionRules {
 static long remaining(int savedBoot,int currentBoot,long elapsedEnd,long wallEnd,long elapsedNow,long wallNow){return Math.max(0,savedBoot==currentBoot?elapsedEnd-elapsedNow:wallEnd-wallNow);}
 static boolean canStart(long remaining,int selectedCount,long duration){return remaining==0&&selectedCount>0&&duration>0;}
 static boolean canStop(long remaining,boolean strict){return remaining==0||!strict;}
}
