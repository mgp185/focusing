package com.example.focuslock;
import org.junit.Test;
import static org.junit.Assert.*;
public class SessionRulesTest {
 @Test public void sameBootUsesMonotonicTime(){assertEquals(60000,SessionRules.remaining(2,2,70000,90000,10000,999999));}
 @Test public void clockRollbackDoesNotExtendSameBoot(){assertEquals(60000,SessionRules.remaining(2,2,70000,90000,10000,0));}
 @Test public void rebootRestoresWallDeadline(){assertEquals(20000,SessionRules.remaining(2,3,70000,90000,10000,70000));}
 @Test public void expiredSessionClampsToZero(){assertEquals(0,SessionRules.remaining(2,2,10000,90000,20000,0));}
 @Test public void requiresTargetsAndPositiveDuration(){assertFalse(SessionRules.canStart(0,0,60000));assertFalse(SessionRules.canStart(0,1,0));assertTrue(SessionRules.canStart(0,1,60000));}
 @Test public void activeSessionCannotBeReconfigured(){assertFalse(SessionRules.canStart(1,1,60000));}
 @Test public void strictModeDeniesEarlyExit(){assertFalse(SessionRules.canStop(60000,true));}
 @Test public void ordinaryModeAllowsEarlyExit(){assertTrue(SessionRules.canStop(60000,false));}
 @Test public void expiredStrictSessionAllowsExit(){assertTrue(SessionRules.canStop(0,true));}
}
