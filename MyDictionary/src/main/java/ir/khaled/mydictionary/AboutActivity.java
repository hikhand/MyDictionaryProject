package ir.khaled.mydictionary;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by khaled on 6/17/13.
 */

public class AboutActivity extends Activity {
    TextView tvSiteUrl;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        tvSiteUrl = (TextView) findViewById(R.id.tvSiteUrl);
    }

    public void linkToSite(View view) {

    }
}