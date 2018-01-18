package org.j2page.oscdisplay;

import android.app.Application;

import org.j2page.oscdisplay.model.Path;
import org.j2page.oscdisplay.model.Template;

import java.util.ArrayList;
import java.util.List;

/**
 * Application class.
 */

public class OscDisplayApplication extends Application {

    private List<Path> allPaths;

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: tie in with preferences
        allPaths = new ArrayList<>();

        // debug template
        Template debugTemplate = new Template();
        debugTemplate.setBody(getString(R.string.template_debug));
        allPaths.add(new Path("/debug", debugTemplate));
    }

    public List<Path> getAllPaths() {
        return allPaths;
    }
}
