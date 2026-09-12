package com.nexvary.emergencymesh;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NavigationRegistry {
    public static final String HOME="home", MESSAGES="messages", RADAR="radar", MAP="map", DIAGNOSTICS="diagnostics", RADIO="radio", PROFILE="profile", SETTINGS="settings", ABOUT="about", CHANNELS="channels";
    private static final Map<String,List<String>> LINKS = new LinkedHashMap<>();
    static {
        LINKS.put(HOME, Arrays.asList(MESSAGES,RADAR,MAP,DIAGNOSTICS,RADIO,PROFILE,CHANNELS,SETTINGS,ABOUT));
        LINKS.put(MESSAGES, Arrays.asList(CHANNELS,PROFILE,RADIO));
        LINKS.put(RADAR, Arrays.asList(MAP,DIAGNOSTICS));
        LINKS.put(MAP, Arrays.asList(RADAR,DIAGNOSTICS));
        LINKS.put(DIAGNOSTICS, Arrays.asList(RADIO,SETTINGS));
        LINKS.put(RADIO, Arrays.asList(DIAGNOSTICS,SETTINGS));
        LINKS.put(PROFILE, Arrays.asList(SETTINGS,ABOUT));
        LINKS.put(CHANNELS, Arrays.asList(MESSAGES,RADIO));
        LINKS.put(SETTINGS, Arrays.asList(PROFILE,ABOUT,DIAGNOSTICS));
        LINKS.put(ABOUT, Arrays.asList(SETTINGS,DIAGNOSTICS));
    }
    private NavigationRegistry(){}
    public static Set<String> routes(){ return Collections.unmodifiableSet(LINKS.keySet()); }
    public static List<String> links(String route){ return LINKS.getOrDefault(route, Collections.emptyList()); }
    public static boolean isValid(String route){ return LINKS.containsKey(route); }
    public static boolean integrityOk(){
        for(Map.Entry<String,List<String>> e: LINKS.entrySet()) for(String target:e.getValue()) if(!LINKS.containsKey(target)) return false;
        return true;
    }
}
