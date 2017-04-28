package org.literacyapp.handwriting;

import android.app.Application;

/**
 * Created by sladomic on 28.04.17.
 */

public class HandwritingApplication extends Application {
    private int completionCounter;

    @Override
    public void onCreate() {
        super.onCreate();

        completionCounter = 0;
    }

    public int getCompletionCounter() {
        return completionCounter;
    }

    public void setCompletionCounter(int completionCounter) {
        this.completionCounter = completionCounter;
    }
}
