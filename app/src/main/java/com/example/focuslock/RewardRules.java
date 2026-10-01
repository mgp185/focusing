package com.example.focuslock;
final class RewardRules {
 static final long HOUR=3600000L;
 static long automaticCoins(long totalMillis){return Math.max(0,totalMillis)/HOUR;}
 // Fractional completed-session time is retained, abandoned-session time is excluded.
 static long focusCoins(long completedMillis){return Math.max(0,completedMillis)/ (HOUR/10);}
}
