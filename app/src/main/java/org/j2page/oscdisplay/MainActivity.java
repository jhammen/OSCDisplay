package org.j2page.oscdisplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import org.j2page.oscdisplay.model.Path;
import org.j2page.oscdisplay.model.Template;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getName();

    private OSCPortIn oscPort;
    private Template loadedTemplate;
    private DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
    private MessageInterface messageInterface = new MessageInterface();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // populate webview with initial template
        final WebView webview = (WebView) this.findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(messageInterface, "Message");
        webview.loadDataWithBaseURL("", getText(R.string.template_waiting).toString(), "text/html", "UTF-8", "");
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int port = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_PORT, getString(R.string.pref_default_port)));
        boolean multicast = sharedPref.getBoolean(SettingsActivity.KEY_PREF_MULTICAST_SWITCH, false);
        String multicastAddr = sharedPref.getString(SettingsActivity.KEY_PREF_MULTICAST_IP, getString(R.string.pref_default_multicast_ip));

        final WebView webview = (WebView) this.findViewById(R.id.webview);
        try {
            if (multicast) {
                InetAddress group = InetAddress.getByName(multicastAddr);
                MulticastSocket socket = new MulticastSocket(port);
                socket.joinGroup(group);
                oscPort = new OSCPortIn(socket);
            } else {
                oscPort = new OSCPortIn(port);
            }

            OscDisplayApplication app = (OscDisplayApplication) getApplication();
            List<Path> paths = app.getAllPaths();
            for (final Path path : paths) {
                final Template template = path.getTemplate();
                OSCListener listener = new OSCListener() {
                    public void acceptMessage(java.util.Date time, final OSCMessage message) {
                        webview.post(new Runnable() {
                            @Override
                            public void run() {
                                messageInterface.setMessage(message);
                                if (template.equals(loadedTemplate) && path.isUpdateTemplate()) {
                                    webview.loadUrl("javascript:update()");
                                } else {
                                    webview.loadDataWithBaseURL("", template.getBody(), "text/html", "UTF-8", "");
                                    loadedTemplate = template;
                                }
                            }
                        });
                    }

                };
                oscPort.addListener(path.getAddress(), listener);
            }
            oscPort.startListening();
            Log.d(TAG, "onStart, oscPort now listening on port " + port + ", multicast is " + multicast);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class MessageInterface {
        private OSCMessage message;

        public void setMessage(OSCMessage message) {
            this.message = message;
        }

        @JavascriptInterface
        public String address() {
            return message.getAddress();
        }

        @JavascriptInterface
        public int numArgs() {
            return message.getArguments().size();
        }

        @JavascriptInterface
        public String arg(int index) {
            return message.getArguments().get(index).toString();
        }

        @JavascriptInterface
        public String time() {
            return dateFormat.format(new Date());
        }
    }


    @Override
    protected void onStop() {
        oscPort.stopListening();
        oscPort.close();
        Log.d(TAG, "onStop, oscPort stopped listening");
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
