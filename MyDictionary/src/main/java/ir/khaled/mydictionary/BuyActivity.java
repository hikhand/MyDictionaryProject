package ir.khaled.mydictionary;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;

public class BuyActivity extends Activity {
    IInAppBillingService mService;
    ServiceConnection mServiceConn = null;
    Names v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        v = new Names();

        connect();

        bindService(new
                Intent("ir.cafebazaar.pardakht.InAppBillingService.BIND"),
                mServiceConn, Context.BIND_AUTO_CREATE);



    }

    void connect() {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };
    }


    public void buy_click(View view) {
        Bundle buyIntentBundle = null;
        try {
            buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                    "test", "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(),
                    1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                    Integer.valueOf(0));
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001 && data != null) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (responseCode == v.BILLING_RESPONSE_RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Toast.makeText(BuyActivity.this, "You have bought the " + sku + ". Excellent choice, adventurer!", Toast.LENGTH_LONG).show();
                }
                catch (JSONException e) {
                    Toast.makeText(BuyActivity.this, "Failed to parse purchase data.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else if (responseCode == v.BILLING_RESPONSE_RESULT_USER_CANCELED) {
                Toast.makeText(BuyActivity.this, "you canceled the operation.", Toast.LENGTH_LONG).show();
            } else if (responseCode == v.BILLING_RESPONSE_RESULT_ERROR) {
                Toast.makeText(BuyActivity.this, "There was an error in payment operation", Toast.LENGTH_LONG).show();
            } else if (responseCode == v.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                Toast.makeText(BuyActivity.this, "you already bought 504 words databaseMain", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(BuyActivity.this, "error in operation", Toast.LENGTH_LONG).show();
        }
    }


    Bundle querySkus;
    Bundle skuDetails;
    String testPrice;
    String words504Price;
    public void click_check(View view) {
        ArrayList skuList = new ArrayList();
        skuList.add("test");
        skuList.add("504words");
        querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        class FtpTask extends AsyncTask<Void, Integer, Void> {
            protected void onPreExecute() {

            }

            protected Void doInBackground(Void... args) {
                try {
                    skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                int response = skuDetails.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> responseList
                            = skuDetails.getStringArrayList("DETAILS_LIST");

                    JSONObject object;
                    String sku = "";
                    String price = "";
                    for (String thisResponse : responseList) {
                        try {
                            object = new JSONObject(thisResponse);
                            sku = object.getString("productId");
                            price = object.getString("price");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (sku.equals("test")) {
                            testPrice = price;
                        } else if (sku.equals("504words")) {
                            words504Price = price;
                        }
                    }
                }
            }
        }
        new FtpTask().execute();






    }



















    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.buy, menu);
        return true;
    }
    
}
