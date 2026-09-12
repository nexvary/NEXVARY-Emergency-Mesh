package com.nexvary.emergencymesh;

import android.content.Intent;

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
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class NavigationIntegrityTest {
    @Test public void everyHomeTabOpensCorrectPageAndBackReturnsHome(){
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
            for(String route: NavigationRegistry.links(NavigationRegistry.HOME)){
                onView(withTagValue(is((Object)("nav:"+route)))).perform(scrollTo(),click());
                onView(withTagValue(is((Object)("page:"+route)))).check(matches(isDisplayed()));
                onView(withTagValue(is((Object)"back"))).perform(click());
                onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));
            }
        }
    }

    @Test public void everyInternalLinkNavigatesToDeclaredTarget(){
        for(String source: NavigationRegistry.routes()){
            if(NavigationRegistry.HOME.equals(source)) continue;
            for(String target: NavigationRegistry.links(source)){
                Intent i=new Intent(ApplicationProvider.getApplicationContext(),PageActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra(PageActivity.EXTRA_ROUTE,source);
                try(ActivityScenario<PageActivity> scenario=ActivityScenario.launch(i)){
                    onView(withTagValue(is((Object)("page:"+source)))).check(matches(isDisplayed()));
                    onView(withTagValue(is((Object)("nav:"+target)))).perform(scrollTo(),click());
                    onView(withTagValue(is((Object)("page:"+target)))).check(matches(isDisplayed()));
                }
            }
        }
    }

    @Test public void systemBackReturnsFromChildAndHomeBackShowsExitDialog(){
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
            onView(withTagValue(is((Object)"nav:messages"))).perform(scrollTo(),click());
            onView(withTagValue(is((Object)"page:messages"))).check(matches(isDisplayed()));
            Espresso.pressBack();
            onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));
            Espresso.pressBack();
            onView(withId(android.R.id.button2)).check(matches(isDisplayed())).perform(click());
            onView(withTagValue(is((Object)"page:home"))).check(matches(isDisplayed()));
            Espresso.pressBack();
            onView(withId(android.R.id.button1)).check(matches(isDisplayed())).perform(click());
        }
    }
}
