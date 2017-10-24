package cat.bcn.ratememaybeapp;

import com.crashlytics.android.Crashlytics;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import cat.bcn.ratememaybe.RateMeMaybe;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements RateMeMaybe.OnRMMUserChoiceListener {

    private static final String SERVICE_URL = "http://www.bcn.cat/mobil/apps/rateme/ratememaybe_params.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        askForRating();
    }

    private void askForRating() {
        //RateMeMaybe.resetData(this);
        RateMeMaybe rmm = new RateMeMaybe(this);
//        rmm.setIcon(android.R.drawable.sym_def_app_icon);
//        rmm.setBackgroundColor(Color.BLUE);
//        rmm.setAdditionalListener(this);
        //rmm.setHandleCancelAsNeutral(false);
        //rmm.setRunWithoutPlayStore(true);

        /* we can set popup params directly from code */
        Map<String, String> messages = new HashMap<String, String>();
        messages.put("ca", "Missatge");
        messages.put("es", "Mensaje");
        messages.put("en", "Message");
        rmm.setTmin(1);
        rmm.setNumApert(3);
        rmm.setMessages(messages);
        /* or specifying an URL to obtain them in JSON format */
//        rmm.setServiceUrl(SERVICE_URL);
//        rmm.forceShow();
        rmm.run();
    }

    @Override
    public void handlePositive() {
        Toast.makeText(this, "Positive button clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleNeutral() {
        Toast.makeText(this, "Neutral button clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleNegative() {
        Toast.makeText(this, "Negative button clicked", Toast.LENGTH_SHORT).show();
    }
}
