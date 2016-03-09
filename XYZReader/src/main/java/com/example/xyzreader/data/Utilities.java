package com.example.xyzreader.data;

/**
 * Created by Spectre on 3/6/2016.
 */
public class Utilities {

    private static final String LOG_TAG = "Utilities";

    public static String generateTransitionName(int articleIndex) {
        return "transition_" + articleIndex;
    }

    public interface OnTransitionReadyListener {
        void onTransitionReady();
    }

}
