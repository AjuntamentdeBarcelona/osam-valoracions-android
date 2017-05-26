package cat.bcn.ratememaybeapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import cat.bcn.ratememaybe.RateMeMaybe;

public class MainActivity extends AppCompatActivity implements RateMeMaybe.OnRMMUserChoiceListener {

    private static final String SERVICE_URL = "http://www.bcn.cat/mobil/apps/rateme/ratememaybe_params.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForRating();
    }

    private void askForRating() {
        //RateMeMaybe.resetData(this);
        RateMeMaybe rmm = new RateMeMaybe(this);
        rmm.setButtonsTextColor(Color.YELLOW);
        rmm.setTitleColor(Color.YELLOW);
        rmm.setMessageColor(Color.LTGRAY);
        rmm.setBackgroundColor(Color.BLUE);
        rmm.setIcon(R.drawable.hellokitty_64);
        rmm.setAdditionalListener(this);
        //rmm.setHandleCancelAsNeutral(false);
        //rmm.setRunWithoutPlayStore(true);

        /* we can set popup params directly from code */
        rmm.setTmin(0);
        rmm.setNumApert(2);
        rmm.setText(getString(R.string.ratememaybeapp_message));
        /* or specifying an URL to obtain them in JSON format */
//        rmm.setServiceUrl(SERVICE_URL);

        //rmm.forceShow();
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
