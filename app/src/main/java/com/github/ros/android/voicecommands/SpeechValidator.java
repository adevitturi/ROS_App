package com.github.ros.android.voicecommands;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** A class to validate voice commands and get the corresponding location to its payload. */
public class SpeechValidator {
    private static final String[] MOVE_COMMANDS = new String[] {"move", "go", "walk", "run", "direct"};
    private static final String[] STOP_COMMANDS = new String[] {"wait", "stop", "cancel"};
    private static final String MOVE_CONNECTOR = "to";
    private SharedPreferences preferences;
    private Context context;

    public SpeechValidator(Context context){
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /** Validates a speech payload and returns it's target location. */
    public TargetLocation validateAndGetLocation(String payload) throws Exception {
        String[] words = payload.toLowerCase().split(" ");

        // Look for move commands.
        int commandIndex = getCommandIndex(words, MOVE_COMMANDS);
        if (!words[commandIndex + 1].equals(MOVE_CONNECTOR)) {
            throw new Exception("Move command connector not found.");
        }
        return getLocation(words, commandIndex + 2);
    }

    /** Looks for stop commands in the payload. */
    public boolean hasStopCommand(String payload) {
        String[] words = payload.toLowerCase().split(" ");
        try {
            getCommandIndex(words, STOP_COMMANDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int getCommandIndex(String[] words, String[] commands) throws Exception {
        for(int i=0 ; i<words.length ; i++){
            for(String command:commands) {
                if(words[i].equals(command)){
                    return i;
                }
            }
        }
        throw new Exception("Command not found.");
    }

    private int getWordIndex(String[] words, int startIndex, String target) throws Exception {
        for(int i=startIndex ; i<words.length ; i++){
            if(words[i].equals(target)){
                return i;
            }
        }
        throw new Exception("Matching word not found.");
    }

    private TargetLocation getLocation(String[] words, int startIndex) throws Exception {
        Set<String> keyset = preferences.getStringSet(context.getString(R.string.app_key_set),new LinkedHashSet<>());
        Set<TargetLocation> locations = new HashSet<>();
        for(String key:keyset) {
            String bundle = preferences.getString(key,"");
            locations.add(new TargetLocation(bundle));
        }

        for(TargetLocation location:locations) {
            String[] locationNameWords = location.getName().toLowerCase().split("\\W+");
            try{
                int firstWordIndex = getWordIndex(words, startIndex, locationNameWords[0]);
                for(int i=firstWordIndex ; i < words.length ; i++) {
                    validateWord(words[i] , locationNameWords[i-firstWordIndex]);
                }
                return location;
            } catch(Exception e){
                continue;
            }
        }
        throw new Exception("Command payload not found.");
    }

    private void validateWord(String pattern, String target) throws Exception{
        if(!pattern.equals(target)){
            throw new Exception("Word does not match target.");
        }
    }
}
