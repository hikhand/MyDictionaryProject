package ir.khaled.mydictionary;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

        TextView tvVersion = (TextView) findViewById(R.id.aboutVersion);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("version " + pInfo.versionName + " pro");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void linkToSite(View view) {

    }
}