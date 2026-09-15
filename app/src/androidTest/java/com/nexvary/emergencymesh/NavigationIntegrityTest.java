package com.nexvary.emergencymesh;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class NavigationIntegrityTest {
 @Test public void homeQuickActionsOpenRealPagesAndBackReturnsHome(){setLanguage("ar");try(ActivityScenario<MainActivity>s=ActivityScenario.launch(MainActivity.class)){String[] routes={NavigationRegistry.RADIO,NavigationRegistry.MESSAGES,NavigationRegistry.MAP};for(String route:routes){onView(withTagValue(is((Object)("nav:"+route)))).perform(scrollTo(),click());onView(withTagValue(is((Object)("page:"+route)))).check(matches(isDisplayed()));onView(withTagValue(is((Object)("fingerprint:"+route)))).check(matches(isDisplayed()));onView(withTagValue(is((Object)"back"))).perform(click());onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));}}}
 @Test public void menuButtonIsAlive(){setLanguage("ar");try(ActivityScenario<MainActivity>s=ActivityScenario.launch(MainActivity.class)){onView(withTagValue(is((Object)"menu"))).perform(click());onView(withText("الرئيسية")).check(matches(isDisplayed()));onView(withText("الإعدادات")).check(matches(isDisplayed()));onView(withText("عن التطبيق")).check(matches(isDisplayed()));Espresso.pressBack();onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));}}
 @Test public void everyDestinationHasUniqueFunctionalFingerprint(){setLanguage("en");for(String route:new String[]{NavigationRegistry.MESSAGES,NavigationRegistry.CHANNELS,NavigationRegistry.RADAR,NavigationRegistry.MAP,NavigationRegistry.DIAGNOSTICS,NavigationRegistry.RADIO,NavigationRegistry.PROFILE,NavigationRegistry.SETTINGS,NavigationRegistry.ABOUT}){try(ActivityScenario<PageActivity>s=ActivityScenario.launch(routeIntent(route))){onView(withTagValue(is((Object)("page:"+route)))).check(matches(isDisplayed()));onView(withTagValue(is((Object)("fingerprint:"+route)))).check(matches(isDisplayed()));}}}
 @Test public void eachRouteHasItsOwnRequiredFunctionalControl(){setLanguage("en");assertControl(NavigationRegistry.MESSAGES,"message:compose");assertControl(NavigationRegistry.CHANNELS,"toggle:channel_rescue");assertControl(NavigationRegistry.RADAR,"radar:scan");assertControl(NavigationRegistry.MAP,"map:add-marker");assertControl(NavigationRegistry.DIAGNOSTICS,"diagnostics:run");assertControl(NavigationRegistry.RADIO,"toggle:radio_discovery");assertControl(NavigationRegistry.PROFILE,"profile:save");assertControl(NavigationRegistry.SETTINGS,"settings:lang:en");assertControl(NavigationRegistry.ABOUT,"fingerprint:about");}
 @Test public void keyControlsAreActuallyClickableNotDead(){setLanguage("ar");clickControl(NavigationRegistry.MESSAGES,"message:rescue");clickControl(NavigationRegistry.RADAR,"radar:scan");clickControl(NavigationRegistry.MAP,"map:add-marker");clickControl(NavigationRegistry.DIAGNOSTICS,"diagnostics:run");clickControl(NavigationRegistry.PROFILE,"profile:save");}
 @Test public void systemBackReturnsFromChildAndHomeBackShowsExitDialog(){setLanguage("ar");try(ActivityScenario<MainActivity>s=ActivityScenario.launch(MainActivity.class)){onView(withTagValue(is((Object)"nav:messages"))).perform(scrollTo(),click());onView(withTagValue(is((Object)"page:messages"))).check(matches(isDisplayed()));Espresso.pressBack();onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));Espresso.pressBack();onView(withId(android.R.id.button2)).check(matches(isDisplayed())).perform(click());onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));}}
 @Test public void arabicPersianUrduAreRtlAndEnglishIsLtr(){assertRootDirection("ar",View.LAYOUT_DIRECTION_RTL);assertRootDirection("fa",View.LAYOUT_DIRECTION_RTL);assertRootDirection("ur",View.LAYOUT_DIRECTION_RTL);assertRootDirection("en",View.LAYOUT_DIRECTION_LTR);}
 @Test public void languageButtonCyclesWithoutBreakingHome(){setLanguage("ar");try(ActivityScenario<MainActivity>s=ActivityScenario.launch(MainActivity.class)){for(int i=0;i<10;i++){onView(withTagValue(is((Object)"language"))).perform(click());onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));}}}
 @Test public void sosControlIsAlive(){setLanguage("ar");try(ActivityScenario<MainActivity>s=ActivityScenario.launch(MainActivity.class)){onView(withTagValue(is((Object)"sos"))).perform(scrollTo(),click());onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));}}
 private void assertControl(String route,String tag){try(ActivityScenario<PageActivity>s=ActivityScenario.launch(routeIntent(route))){onView(withTagValue(is((Object)("fingerprint:"+route)))).check(matches(isDisplayed()));onView(withTagValue(is((Object)tag))).perform(scrollTo()).check(matches(isDisplayed()));}}
 private void clickControl(String route,String tag){try(ActivityScenario<PageActivity>s=ActivityScenario.launch(routeIntent(route))){onView(withTagValue(is((Object)tag))).perform(scrollTo(),click());s.onActivity(a->assertFalse("Control click unexpectedly closed page: "+tag,a.isFinishing()));if(NavigationRegistry.MESSAGES.equals(route))Espresso.pressBack();}}
 private Intent routeIntent(String route){Intent i=new Intent(ApplicationProvider.getApplicationContext(),PageActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);i.putExtra(PageActivity.EXTRA_ROUTE,route);return i;}
 private void assertRootDirection(String language,int expected){setLanguage(language);try(ActivityScenario<MainActivity>s=ActivityScenario.launch(MainActivity.class)){s.onActivity(a->{ViewGroup scroll=(ViewGroup)a.getWindow().getDecorView().findViewWithTag("page:home");View root=scroll.getChildAt(0);assertEquals(expected,root.getLayoutDirection());});}}
 private void setLanguage(String code){Context c=ApplicationProvider.getApplicationContext();c.getSharedPreferences("ui",Context.MODE_PRIVATE).edit().putString("language",code).commit();}
}