package cat.bcn.ratememaybeapp;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cat.bcn.ratememaybe.RateMeMaybe;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity{

    private static final String SERVICE_URL = "http://www.bcn.cat/mobil/apps/modulValoracions/sample/rateme_and.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        askForRating();
        Answers.getInstance().logCustom(new CustomEvent("probando Answers de crashlytics"));
        Crashlytics.logException(new Throwable("No se puede mostrar RateMeMaybe (log)"));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void askForRating() {
        //RateMeMaybe.resetData(this);
        RateMeMaybe rmm = new RateMeMaybe(this);
//        rmm.setIcon(android.R.drawable.sym_def_app_icon);
//        rmm.setBackgroundColor(Color.BLUE);
        rmm.setAdditionalListener(new RateMeMaybe.OnRMMUserChoiceListener() {
            @Override
            public void handlePositive() {
//                lauchSecondActivity();
            }

            @Override
            public void handleNeutral() {
//                lauchSecondActivity();
            }

            @Override
            public void handleNegative() {
//                lauchSecondActivity();
            }

            @Override
            public void handleError() {
                Answers.getInstance().logCustom(new CustomEvent("No se puede mostar RateMeMaybe (Answer)"));
                Crashlytics.logException(new Throwable("No se puede mostrar RateMeMaybe (log)"));
            }
        });
        //rmm.setHandleCancelAsNeutral(false);
        //rmm.setRunWithoutPlayStore(true);

        /* we can set popup params directly from code */
//        Map<String, String> messages = new HashMap<String, String>();
//        messages.put("ca", "Missatge en català");
//        messages.put("es", "Mensaje en español");
//        messages.put("en", "Message in English");
//        rmm.setTmin(0);
//        rmm.setNumApert(1);
//        rmm.setMessages(messages);
        /* or specifying an URL to obtain them in JSON format */
        rmm.setServiceUrl(SERVICE_URL);
//        rmm.forceShow();
        rmm.run();
    }

    private void lauchSecondActivity() {
        startActivity(new Intent(getBaseContext(), SecondActivity.class));
        finish();
    }
}
