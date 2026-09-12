package com.nexvary.emergencymesh;

import org.junit.Test;

import static org.junit.Assert.*;

public class NavigationRegistryTest {
    @Test public void everyDeclaredLinkPointsToARealPage(){
        assertTrue(NavigationRegistry.integrityOk());
        assertEquals(10, NavigationRegistry.routes().size());
        for(String source: NavigationRegistry.routes()){
            for(String target: NavigationRegistry.links(source)){
                assertTrue("broken link "+source+" -> "+target, NavigationRegistry.isValid(target));
            }
        }
    }

    @Test public void homeExposesAllPrimaryDestinations(){
        assertTrue(NavigationRegistry.links(NavigationRegistry.HOME).contains(NavigationRegistry.MESSAGES));
        assertTrue(NavigationRegistry.links(NavigationRegistry.HOME).contains(NavigationRegistry.RADAR));
        assertTrue(NavigationRegistry.links(NavigationRegistry.HOME).contains(NavigationRegistry.MAP));
        assertTrue(NavigationRegistry.links(NavigationRegistry.HOME).contains(NavigationRegistry.DIAGNOSTICS));
        assertTrue(NavigationRegistry.links(NavigationRegistry.HOME).contains(NavigationRegistry.SETTINGS));
        assertTrue(NavigationRegistry.links(NavigationRegistry.HOME).contains(NavigationRegistry.ABOUT));
    }
}
