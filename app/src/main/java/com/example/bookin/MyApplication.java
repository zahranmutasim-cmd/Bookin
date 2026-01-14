// In app/src/main/java/com/example/bookin/MyApplication.java
package com.example.bookin;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Create a configuration map for Cloudinary.
        // For unsigned uploads, only the cloud_name is needed.
        // NEVER include your api_secret in client-side code.
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dfnbdboas");

        // Initialize the MediaManager with the configuration
        MediaManager.init(this, config);
    }
}
