package ir.khaled.mydictionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import javax.crypto.IllegalBlockSizeException;

public class PackageActivity extends Activity implements TextToSpeech.OnInitListener {

    DatabasePackage databasePackage;

    SharedPreferences prefs;
    SharedPreferences UserInfo;
    SharedPreferences.Editor EditorUserInfo;

    SharedPreferences mainPrefs;
    SharedPreferences.Editor editorMainPrefs;

    public AlertDialog dialogMeaning;
    //    public AlertDialog dialogEdit;
    public AlertDialog dialogSummery;
    AlertDialog dialogAskLogin;
    AlertDialog dialogAskBuy;
    AlertDialog dialogLogin;
    AlertDialog dialogSingUp;
    ProgressDialog progressBar;

    EditText etSearch;

    ArrayList<Custom> arrayItemsInMD;
    ArrayList<ItemPackage> arrayItemsDontAdd;
    ArrayList<ItemPackage> arrayItems;
    ArrayList<ItemPackageShow> itemsToShow;
    ArrayList<String> arrayItemsToday;
    ArrayList<Integer> checkedPositionsInt;
    ArrayList<Integer> arrayIndexesLastDay;
    ArrayList<String> arrayIndexesLastDayDate;

    ListView items;
    AdapterPackage adapter;

    boolean markSeveral = false;
    boolean showItemNumber = true;
    boolean isFromSearch = false;
    boolean isFromSearchDot = false;
    boolean isToMarkAll = true;
    boolean dialogMeaningIsOpen = false;
    boolean dialogSummeryIsOpen = false;
    boolean dialogLoginIsOpen;
    boolean dialogSingUpIsOpen;
    boolean dialogAskLoginIsOpen;
    boolean dialogAskBuyIsOpen;
    boolean answerViewed = false;
    boolean isNewDay = false;
    boolean isPaused = false;

    int dialogMeaningWordPosition = 0;
    int todayNum = 0;
    int addPerDay = 10;

    String isDistanceTempAdd = "";
    String isDistanceTempLast = "";
    String isDistance = "";
    String searchMethod;
    String todayDate = "";
    String lastDate = "";
    String sortMethod = "";
    String searchText = "";
    String userUsername = "";
    String userPassword = "";
    String s = File.separator;
    String packageL = "";

    Parcelable listViewPosition = null;

    IInAppBillingService mService;
    ServiceConnection mServiceConn = null;

    private TextToSpeech tts;

    FTPClient con;


    Names v;

    @Override
    public boolean onSearchRequested() {
        etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        return false;
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getPrefs();

//        get();

        setElementsId();

        if (userUsername.equals("")) {
            dialogAskLogin();
        } else {
            has504();
        }

        restore(savedInstanceState);

        if (savedInstanceState != null) {
            listViewPosition = savedInstanceState.getParcelable("listViewPosition");
            searchText = savedInstanceState.getString("etSearchText");
        }
        etSearch.setText(searchText);

        buyConnect();
        bindService(new
                Intent("ir.cafebazaar.pardakht.InAppBillingService.BIND"),
                mServiceConn, Context.BIND_AUTO_CREATE);

    }

    void setElementsId() {
        v = new Names();
        tts = new TextToSpeech(this, this);

        databasePackage = new DatabasePackage(this, "package504.db");

        UserInfo = getSharedPreferences("userInfo", 0);
        EditorUserInfo = UserInfo.edit();
        mainPrefs = getSharedPreferences("main", 0);
        editorMainPrefs = mainPrefs.edit();

        userUsername = UserInfo.getString("userUsername", "");
        userPassword = UserInfo.getString("userPassword", "");

        dialogMeaning = new AlertDialog.Builder(this).create();
        dialogSummery = new AlertDialog.Builder(this).create();
        dialogAskLogin = new AlertDialog.Builder(this).create();
        dialogAskBuy = new AlertDialog.Builder(this).create();
        dialogLogin = new AlertDialog.Builder(this).create();
        dialogSingUp = new AlertDialog.Builder(this).create();
        progressBar = new ProgressDialog(PackageActivity.this);

        etSearch = (EditText) findViewById(R.id.packageSearchET);

        arrayItemsInMD = new ArrayList<Custom>();
        arrayItemsDontAdd = new ArrayList<ItemPackage>();
        arrayItems = new ArrayList<ItemPackage>();
        itemsToShow = new ArrayList<ItemPackageShow>();
        arrayItemsToday = new ArrayList<String>();
        checkedPositionsInt = new ArrayList<Integer>();

        items = (ListView) findViewById(R.id.packageListView);
        adapter = new AdapterPackage(PackageActivity.this, R.layout.row, itemsToShow);

        arrayIndexesLastDay = new ArrayList<Integer>();
        arrayIndexesLastDayDate = new ArrayList<String>();
    }


    boolean returnTrue = false;


    void checkVersion504() throws PackageManager.NameNotFoundException {
        final String currentVersion = mainPrefs.getString("package504Version", "1");
        class FtpTask extends AsyncTask<Void, Integer, Void> {
            FTPClient con;
            boolean succeed = false;
            String error = "";
            String errorS = "";
            private Context context;
            String s = File.separator;

            String newVersion = "";


            public FtpTask(Context context) { this.context = context; }

            protected void onPreExecute() {
                newVersion = currentVersion;
            }

            protected Void doInBackground(Void... args) {
                try {
                    if (con == null) con = new FTPClient();
                    if (!con.isConnected()) con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("mdftp@khaled.ir", "3k2oy8HRhs")) {
                        con.enterLocalPassiveMode(); // important!

                        InputStream inputStream;
                        BufferedReader r;

                        inputStream = con.retrieveFileStream(s + "databases" + s + "package504" + s + "version");
                        r = new BufferedReader(new InputStreamReader(inputStream));
                        newVersion = r.readLine();
                        inputStream.close();
                        r.close();
                        con.completePendingCommand();

                        if (!newVersion.equals(currentVersion)) {
                            publishProgress(0);
                            editorMainPrefs.putString("package504Version", newVersion);
                            editorMainPrefs.commit();

                            File pathMain = getDatabasePath("package504update.db");
                            if (pathMain.exists()) {
                                File currentDB = new File(pathMain, "");
                                currentDB.delete();
                            }

                            FileOutputStream outBackup = new FileOutputStream("/data/data/ir.khaled.mydictionary/databases/package504Update.db");
                            con.retrieveFile("databases" + s + "package504" + s + "504.zip", outBackup);

                            DatabasePackage dbUpdate = new DatabasePackage(PackageActivity.this, "package504Update.db");
                            ArrayList<ItemPackage> items = dbUpdate.getAllItems(v.TABLE_LEITNER);

                            for (int i = 0; i < arrayItems.size(); i++) {
                                ItemPackage itemUp = items.get(i);

                                arrayItems.get(i).setMeaningFa(itemUp.getMeaningFa());
                                arrayItems.get(i).setMeaningEn(itemUp.getMeaningEn());
                                arrayItems.get(i).setExamplesEn(itemUp.getExamplesEn());
                                arrayItems.get(i).setExamplesFa(itemUp.getExamplesFa());

                                databasePackage.updateItem(arrayItems.get(i), v.TABLE_LEITNER);
                            }

                            if (pathMain.exists()) {
                                File currentDB = new File(pathMain, "");
                                currentDB.delete();
                            }
                            succeed = true;
                        }
                    }
                    con.logout();
                    con.disconnect();

                } catch (Exception e) {
                    error = e.toString();
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                if (succeed) {
                    Toast.makeText(PackageActivity.this, "Successfully updated.", Toast.LENGTH_SHORT).show();
                    if (progressBar.isShowing()) progressBar.dismiss();
                }
                if (mScreenOrientationLocked) {
                    unlockScreenOrientation();
                }
            }

            protected void onProgressUpdate(Integer... args) {
                if (args[0] == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar = new ProgressDialog(context);
                            progressBar.setCancelable(false);
                            progressBar.setTitle("updating your package");
                            progressBar.setMessage("package 504 has been updated on server\nplease wait till we update your package ...");
                            progressBar.show();
                        }
                    });
                    lockScreenOrientation();
                } else if (args[0] == 1) {
                    progressBar.setMessage("downloading package 504 for you ...");
                } else if (args[0] == 2) {
                    progressBar.setMessage("loading data ...");
                }
            }

        }
        new FtpTask(PackageActivity.this).execute();
    }

    boolean mScreenOrientationLocked = false;

    private void lockScreenOrientation() {
        if (!mScreenOrientationLocked) {
            final int orientation = getResources().getConfiguration().orientation;
            final int rotation = getWindowManager().getDefaultDisplay().getOrientation();

            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
            }

            mScreenOrientationLocked = true;
        }
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == android.view.Surface.ROTATION_90 || rotation == android.view.Surface.ROTATION_180) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;

            case Configuration.ORIENTATION_LANDSCAPE:
                 int rotation1 = getWindowManager().getDefaultDisplay().getRotation();
                if (rotation1 == android.view.Surface.ROTATION_0 || rotation1 == android.view.Surface.ROTATION_90) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
                break;
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mScreenOrientationLocked = false;
    }

    boolean has504() {
        returnTrue = false;

        class FtpTask extends AsyncTask<Void, Integer, Void> {

            boolean has504Buy = false;

            String error = "";
            ProgressDialog progressBar;
            private Context context;
            String s = File.separator;

            public FtpTask(Context context) {
                this.context = context;
            }

            protected void onPreExecute() {
                progressBar = new ProgressDialog(context);
                progressBar.setCancelable(false);
                progressBar.setMessage("Connecting to server ...");
                progressBar.show();
                lockScreenOrientation();
            }

            protected Void doInBackground(Void... args) {
                String userDbPath = s + userUsername + s + "mydictionary" + s + "databases" + s;
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("ftpUsers@khaled.ir", "8I4KJ4UeRq")) {
                        con.enterLocalPassiveMode(); // important!

                        publishProgress(0);

                        InputStream inputStream = con.retrieveFileStream(userDbPath + "has504");
                        if (inputStream != null) {
                            con.completePendingCommand();
                            has504Buy = true;

                            con = new FTPClient();
                            con.connect(InetAddress.getByName("5.9.0.183"));
                            if (con.login("mdftp@khaled.ir", "3k2oy8HRhs")) {
                                has504Buy = true;
                                publishProgress(1);

                                File pathMain = getDatabasePath("package504.db");
                                if (pathMain.exists()) {
                                    File currentDB = new File(pathMain, "");
                                    currentDB.delete();
                                }

                                FileOutputStream outBackup = new FileOutputStream("/data/data/ir.khaled.mydictionary/databases/package504.db");
                                con.retrieveFile(s + "databases" + s + "package504" + s + "504.zip", outBackup);

                                EditorUserInfo.putString("has504Buy", v.TRUE_HAS_BUY);///true
                                EditorUserInfo.putString("has504In", v.TRUE_HAS_IN);//true
                                EditorUserInfo.commit();
                            }
                        } else {
                            has504Buy = false;
                        }
                        inputStream.close();
                    }
                    con.logout();
                    con.disconnect();

                } catch (Exception e) {
                    error = e.toString();
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                if (has504Buy) {
                    returnTrue = true;

                    publishProgress(2);

                    setElementsValue();
                    refreshListViewData();
                    listeners();
                    try {
                        checkVersion504();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (!has504Buy) {
                    Toast.makeText(PackageActivity.this, "you don't have any package in your account.", Toast.LENGTH_SHORT).show();
                    dialogAskBuy();
                } else if (!error.equals("")) {
                    Toast.makeText(PackageActivity.this, error, Toast.LENGTH_SHORT).show();
                }
                progressBar.dismiss();
                unlockScreenOrientation();
            }

            protected void onProgressUpdate(Integer... args) {
                if (args[0] == 0) {
                    progressBar.setMessage("Checking your packages ...");
                } else if (args[0] == 1) {
                    progressBar.setMessage("downloading package 504 for you ...");
                } else if (args[0] == 2) {
                    progressBar.setMessage("loading data ...");
                }
            }
        }

        File pathMain = getDatabasePath("package504.db");

        if (pathMain.exists()) {
            EditorUserInfo.putString("has504In", v.TRUE_HAS_IN);//hasIn
            EditorUserInfo.commit();
        }

        if (UserInfo.getString("has504Buy", "hasBuy").equals(v.TRUE_HAS_BUY)) {
            if (UserInfo.getString("has504In", "hasIn").equals(v.TRUE_HAS_IN)) {
                setElementsValue();
                refreshListViewData();
                listeners();
                try {
                    checkVersion504();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                new FtpTask(PackageActivity.this).execute();
            }
        } else {
            new FtpTask(PackageActivity.this).execute();
        }
        return true;
    }


    void dialogAskBuy() {
        LayoutInflater inflater = this.getLayoutInflater();
        dialogAskBuy = new AlertDialog.Builder(this)
                .setTitle("You don't have any database, click on a package to buy it.")
                .setView(inflater.inflate(R.layout.dialog_package_list_buy, null))
                .setPositiveButton("More information", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uriUrl = Uri.parse("http://mydictionary.khaled.ir/");
//                            Uri uriUrl = Uri.parse("market://details?id=com.hister.mydictionary");
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
//                        PackageActivity.super.onBackPressed();
                        dialogAskBuy();
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PackageActivity.super.onBackPressed();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK &&
                                event.getAction() == KeyEvent.ACTION_UP &&
                                !event.isCanceled()) {
                            PackageActivity.super.onBackPressed();
                            return true;
                        }
                        return false;
                    }
                })
                .create();
        dialogAskBuy.show();
        dialogAskBuy.setCanceledOnTouchOutside(false);


        ArrayList<String> itemsBuy = new ArrayList<String>();
        itemsBuy.add("504 essential words");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PackageActivity.this, android.R.layout.simple_list_item_1, itemsBuy);

        ListView listBuy = (ListView) dialogAskBuy.findViewById(R.id.packageListBuy);
        listBuy.setAdapter(adapter);

        listBuy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                buy(i);
                dialogAskBuy.dismiss();
            }
        });

    }

    void buy(int position) {
        String sku = "";
        if (position == 0) {
            sku = "504words";
//            sku = "test";
        }

        Bundle buyIntentBundle = null;
        try {
            buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                    sku, "inapp", v.DEVELOPER_PAY_LOAD);
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

    void buyConnect() {
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

    void buySuccessful(String sku) {//sku = package name(id)
        EditorUserInfo.putString("has504Buy", v.TRUE_HAS_BUY);
        EditorUserInfo.commit();
        get504();
        if (dialogAskBuy.isShowing()) {
            dialogAskBuy.dismiss();
        }
    }

    void buyUnSuccessful() {
        dialogAskBuy();
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
                    String text = "";
                    if (sku.equals("504words")) {
                        text = "504 essential words package";
                    }
                    Toast.makeText(PackageActivity.this, "You have bought the " + text + ". enjoy it!", Toast.LENGTH_LONG).show();
                    buySuccessful(sku);
                } catch (JSONException e) {
                    Toast.makeText(PackageActivity.this, "Failed to parse purchase data.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    buyUnSuccessful();
                }
            } else if (responseCode == v.BILLING_RESPONSE_RESULT_USER_CANCELED) {
                Toast.makeText(PackageActivity.this, "you canceled the operation.", Toast.LENGTH_LONG).show();
                buyUnSuccessful();
            } else if (responseCode == v.BILLING_RESPONSE_RESULT_ERROR) {
                Toast.makeText(PackageActivity.this, "There was an error in payment operation", Toast.LENGTH_LONG).show();
                buyUnSuccessful();
            } else if (responseCode == v.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                Toast.makeText(PackageActivity.this, "you have already bought 504 essential words package", Toast.LENGTH_LONG).show();
                buyUnSuccessful();
            } else {
                buyUnSuccessful();
            }
        } else {
            Toast.makeText(PackageActivity.this, "the operation was unsuccessful", Toast.LENGTH_LONG).show();
            buyUnSuccessful();
        }
    }


    void setElementsValue() {
        arrayItems.addAll(databasePackage.getAllItems(v.TABLE_LEITNER));
        arrayIndexesLastDay.addAll(databasePackage.getAllItemsLastDay());
        arrayIndexesLastDayDate.addAll(databasePackage.getAllItemsLastDayDate());
    }


    void get504() {
        class FtpTask extends AsyncTask<Void, Integer, Void> {
            private Context context;

            public FtpTask(Context context) {
                this.context = context;
            }

            protected void onPreExecute() {
                progressBar = new ProgressDialog(context);
                progressBar.setCancelable(false);
                progressBar.setMessage("Connecting to server ...");
                progressBar.show();
                lockScreenOrientation();
            }

            protected Void doInBackground(Void... args) {
                String userDbPath = s + userUsername + s + "mydictionary" + s + "databases" + s;
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("ftpUsers@khaled.ir", "8I4KJ4UeRq")) {
                        con.enterLocalPassiveMode(); // important!

                        publishProgress(0);

                        FileOutputStream outputStream;
                        outputStream = openFileOutput("has504", Context.MODE_PRIVATE);
                        outputStream.write("yes".getBytes());
                        outputStream.close();

                        con.makeDirectory(userDbPath);
                        con.storeFile(userDbPath + "has504", openFileInput("has504"));

                        con = new FTPClient();
                        con.connect(InetAddress.getByName("5.9.0.183"));
                        if (con.login("mdftp@khaled.ir", "3k2oy8HRhs")) {
                            File pathMain = getDatabasePath("package504.db");
                            if (pathMain.exists()) {
                                File currentDB = new File(pathMain, "");
                                currentDB.delete();
                            }

                            FileOutputStream outBackup = new FileOutputStream("/data/data/ir.khaled.mydictionary/databases/package504.db");
                            con.retrieveFile(s + "databases" + s + "package504" + s + "504.zip", outBackup);

                            EditorUserInfo.putString("has504In", v.TRUE_HAS_IN);
                            EditorUserInfo.commit();
                        }
                    }
                    con.logout();
                    con.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                progressBar.dismiss();
                unlockScreenOrientation();
                setElementsValue();
                refreshListViewData();
                listeners();
                try {
                    checkVersion504();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }


            protected void onProgressUpdate(Integer... args) {
                if (args[0] == 0) {
                    progressBar.setMessage("downloading 504 words database ...");
                } else if (args[0] == 1) {
                    progressBar.setMessage("Restoring backup...");
                }
            }
        }
        new FtpTask(PackageActivity.this).execute();
    }

    void refreshListViewData() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        todayDate = simpleDateFormat.format(new Date());
        lastDate = databasePackage.getLastDate();

        if (lastDate.equals(todayDate)) {
            isNewDay = false;
            todayNum = databasePackage.getLastDay();
        } else {
            isNewDay = true;
            todayNum = databasePackage.getLastDay() + 1;
            if (todayNum < 31 && todayNum > 1) {
                todayNum--;
                updateIndexesLastDayLessThan30();
                todayNum++;
            } else if (todayNum > 1) {
                todayNum--;
                updateIndexLastDayMoreThan30();
                todayNum++;
            }
            databasePackage.updateLastDate(todayDate);
            databasePackage.updateLastDay(todayNum);
            lastDate = todayDate;
        }

        arrayItems.clear();
        itemsToShow.clear();
        if (databasePackage.getItemsCount(v.TABLE_LEITNER) > 0) {
            arrayItems.addAll(databasePackage.getAllItems(v.TABLE_LEITNER));

            if (mainPrefs.getBoolean("firstLoginPackage", true)) {
                editorMainPrefs.putBoolean("firstLoginPackage", false);
                editorMainPrefs.putInt("lastPerDay", 10);
                editorMainPrefs.commit();
            }

            if (isNewDay) {
                int added = 0;
                for (ItemPackage arrayItem : arrayItems) {
                    if (arrayItem.getCount() == 0 && added < addPerDay) {
                        added++;
                        arrayItemsToday.add(arrayItem.getName());
                        databasePackage.updateItemPerDay(added, arrayItem.getName());
                    }
                }
                for (int i = addPerDay; i <arrayItemsToday.size(); i++) {
                    arrayItemsToday.set(i, "NO");
                    databasePackage.updateItemPerDay(i+1, "NO");
                }
            } else {
                arrayItemsToday =  databasePackage.getItemsPerDay();
            }

            if (addPerDay != mainPrefs.getInt("lastPerDay", 10)) {
                int lastPerDay = mainPrefs.getInt("lastPerDay", 10);
                editorMainPrefs.putInt("lastPerDay", addPerDay);
                editorMainPrefs.commit();

                if (addPerDay > lastPerDay) {
                    int added = 0;
                    for (int i = 0; i < arrayItems.size(); i++) {
                        if (arrayItems.get(i).getCount() == 0 && added <= addPerDay - lastPerDay) {
                            boolean addThis = true;
                            for (int i1 = 0; i1 < lastPerDay - 1; i1++) {
                                if (arrayItemsToday.get(i1).equals(arrayItems.get(i).getName())) {
                                    addThis = false;
                                    break;
                                }
                            }
                            if (addThis) {
                                arrayItemsToday.set((lastPerDay + added), arrayItems.get(i).getName());
                                databasePackage.updateItemPerDay((lastPerDay + added)+1, arrayItems.get(i).getName());
                                added++;
                            }
                        }
                    }
                } else {
                    for (int i = addPerDay; i < arrayItemsToday.size(); i++) {
                        arrayItemsToday.set(i, "NO");
                        databasePackage.updateItemPerDay(i+1, "NO");
                    }
                }
            }

            refreshShow();
            sort();

            if (itemsToShow.size() > 0) {
                int k = 1;
                for (int i = 0; i < itemsToShow.size(); i++) {
                    itemsToShow.get(i).setChVisible(markSeveral);
                    if (markSeveral && checkedPositionsInt.size() > 0)
                        itemsToShow.get(i).setChChecked(checkedPositionsInt.get(i) == 0);
                    itemsToShow.get(i).setName(showItemNumber ? k + ". " + itemsToShow.get(i).getName() : itemsToShow.get(i).getName());
                    k++;
                }
            } else {
                itemsToShow.add(new ItemPackageShow("   Nothing found", "My Dictionary"));
            }
        }

        adapter.notifyDataSetChanged();
        items.setAdapter(adapter);

        if (listViewPosition != null)
            items.onRestoreInstanceState(listViewPosition);

        if (isFromSearch) {
            listViewPosition = items.onSaveInstanceState();
            search(etSearch.getText().toString());
            items.onRestoreInstanceState(listViewPosition);
        }

        TextView tvSummery = (TextView) findViewById(R.id.packageSummeryTV);
        if (itemsToShow.size() > 0) {
            if (!itemsToShow.get(0).getName().equals("   Nothing found") && !itemsToShow.get(0).getMeaningFa().equals("My Dictionary"))
                tvSummery.setText("'" + Integer.toString(itemsToShow.size()) + "'");
            else tvSummery.setText("'" + Integer.toString(itemsToShow.size() - 1) + "'");
        } else {
            tvSummery.setText("'0'");
        }
    }

    void sort() {
        if (sortMethod.equals("nameA")) {
            sortNameA();
        } else if (sortMethod.equals("nameD")) {
            sortNameD();
        } else if (sortMethod.equals("dateA")) {

        } else if (sortMethod.equals("dateD")) {
            sortDateD();
        } else if (sortMethod.equals("countA")) {
            sortCountA();
        } else if (sortMethod.equals("countD")) {
            sortCountD();
        }
    }

    void sortNameA() {
        ArrayList<String> words = new ArrayList<String>();
        for (ItemPackageShow item : itemsToShow) {
            words.add(item.getName());
        }
        Collections.sort(words);
        ArrayList<ItemPackage> buff = new ArrayList<ItemPackage>();
        for (ItemPackageShow item : itemsToShow) {
            buff.add(convertToItem(item));
        }
        itemsToShow.clear();
        for (int i = 0; i < buff.size(); i++) {
            for (ItemPackage j : buff) {
                if (words.get(i).equals(j.getName())) {
                    itemsToShow.add(convertToItemPackageShow(j));
                }
            }
        }
    }

    void sortNameD() {
        sortNameA();
        ArrayList<ItemPackage> buff = new ArrayList<ItemPackage>();
        for (ItemPackageShow item : itemsToShow) {
            buff.add(convertToItem(item));
        }
        itemsToShow.clear();
        for (int i = buff.size() - 1; i >= 0; i--) {
            itemsToShow.add(convertToItemPackageShow(buff.get(i)));
        }
    }

    void sortDateD() {
        ArrayList<ItemPackage> buff = new ArrayList<ItemPackage>();
        for (ItemPackageShow item : itemsToShow) {
            buff.add(convertToItem(item));
        }
        itemsToShow.clear();
        for (int i = buff.size() - 1; i >= 0; i--) {
            itemsToShow.add(convertToItemPackageShow(buff.get(i)));
        }
    }

    void sortCountA() {
        for (int i = 0; i < itemsToShow.size() - 1; i++) {
            for (int j = 0; j < itemsToShow.size() - 1; j++) {
                if (itemsToShow.get(j).getCount() > itemsToShow.get(j + 1).getCount()) {
                    ItemPackageShow temp = itemsToShow.get(j);
                    ItemPackageShow temp1 = itemsToShow.get(j + 1);
                    itemsToShow.set(j, temp1);
                    itemsToShow.set(j + 1, temp);
                }
            }
        }
    }

    void sortCountD() {
        for (int i = 0; i < itemsToShow.size() - 1; i++) {
            for (int j = 0; j < itemsToShow.size() - 1; j++) {
                if (itemsToShow.get(j).getCount() < itemsToShow.get(j + 1).getCount()) {
                    ItemPackageShow temp = itemsToShow.get(j);
                    ItemPackageShow temp1 = itemsToShow.get(j + 1);
                    itemsToShow.set(j, temp1);
                    itemsToShow.set(j + 1, temp);
                }
            }
        }
    }


    public void summery_OnClick(View view) {
        if (!dialogSummery.isShowing())
            dialogSummery();
    }

    void dialogSummery() {
        int[] deck = {0, 0, 0, 0, 0};

        LayoutInflater inflater = this.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_summery, null);
        final AlertDialog.Builder d = new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton(R.string.ok, null);

        dialogSummery = d.create();
        dialogSummery.show();

        TextView[] deckTv = {(TextView) dialogSummery.findViewById(R.id.d1),
                (TextView) dialogSummery.findViewById(R.id.d2),
                (TextView) dialogSummery.findViewById(R.id.d3),
                (TextView) dialogSummery.findViewById(R.id.d4),
                (TextView) dialogSummery.findViewById(R.id.d5)};

        for (ItemPackageShow item : itemsToShow) {
            if (!item.getName().equals("   Nothing found") && !item.getMeaningFa().equals("My Dictionary")) {
                if (item.getDeck() == 1) {
                    deck[0]++;
                    deckTv[0].setTextColor(Color.GREEN);
                } else if (item.getDeck() == 2) {
                    deck[1]++;
                    deckTv[1].setTextColor(Color.GREEN);
                } else if (item.getDeck() == 3) {
                    deck[2]++;
                    deckTv[2].setTextColor(Color.GREEN);
                } else if (item.getDeck() == 4) {
                    deck[3]++;
                    deckTv[3].setTextColor(Color.GREEN);
                } else if (item.getDeck() == 5) {
                    deck[4]++;
                    deckTv[4].setTextColor(Color.GREEN);
                }
            }
        }

        boolean d1Ready = deck[0] == 0;
        boolean d2Ready = deck[1] == 0;
        boolean d3Ready = deck[2] == 0;
        boolean d4Ready = deck[3] == 0;
        boolean d5Ready = deck[4] == 0;

        for (ItemPackage item : arrayItems) {
            if (item.getDeck() == 1 && d1Ready) {
                deck[0]++;
                deckTv[0].setTextColor(Color.RED);
            } else if (item.getDeck() == 2 && d2Ready) {
                deck[1]++;
                deckTv[1].setTextColor(Color.RED);
            } else if (item.getDeck() == 3 && d3Ready) {
                deck[2]++;
                deckTv[2].setTextColor(Color.RED);
            } else if (item.getDeck() == 4 && d4Ready) {
                deck[3]++;
                deckTv[3].setTextColor(Color.RED);
            } else if (item.getDeck() == 5 && d5Ready) {
                deck[4]++;
                deckTv[4].setTextColor(Color.RED);
            }
        }
        int totalCards = 0;
        for (int i = 0; i < 5; i++) {
            deckTv[i].setText(Integer.toString(deck[i]));
            totalCards+= deck[i];
        }

        TextView tAnswers = (TextView) dialogSummery.findViewById(R.id.tAnswers);
        TextView tCorrects = (TextView) dialogSummery.findViewById(R.id.tCorrects);
        TextView tIncorrects = (TextView) dialogSummery.findViewById(R.id.tIncorrects);
        TextView tCards = (TextView) dialogSummery.findViewById(R.id.tCards);
        TextView tDays = (TextView) dialogSummery.findViewById(R.id.tDays);

        int totalAnswers = 0, totalCorrects = 0, totalIncorrects = 0, totalDays = todayNum;
        for (ItemPackage item : arrayItems) {
            totalAnswers += item.getCount();
            totalCorrects += item.getCountCorrect();
            totalIncorrects += item.getCountInCorrect();
        }

        tAnswers.setText("Total Answers: " + totalAnswers);
        tCorrects.setText("Total Corrects: " + totalCorrects);
        tIncorrects.setText("Total Incorrects: " + totalIncorrects);
        tCards.setText("Total Cards: " + totalCards);
        tDays.setText("Total Days: " + totalDays);
    }

    void refreshShow() {
        if (todayNum < 31) {
            refreshLessThan30();
        } else {
            refreshMoreThan30();
        }
    }

    void refreshLessThan30() {
        switch (todayNum) {
            case 1: {
                for (ItemPackage item : arrayItems) {
                    if (item.getIndex() == 0 && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 2: {
                for (ItemPackage item : arrayItems) {
                    if (item.getIndex() == 0 && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 3: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 4: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 5: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 6: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 7: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 8: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 9: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 10: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 11: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 12: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 13: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 14: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 15: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 7 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 16: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 8 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 17: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 9 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 18: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 10 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 19: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 11 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 20: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 12 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 21: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 13 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 22: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 14 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 23: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 7 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 24: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 8 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 25: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 9 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 26: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 10 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 27: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 11 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 28: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 12 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 29: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 13 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 30: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 14 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    void refreshMoreThan30() {
        int lastIndexDeck5 = nextIndexMore30(16);
        switch (lastIndexDeck5) {
            case 15: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 15 || item.getIndex() == 7 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 16: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 16 || item.getIndex() == 8 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 17: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 17 || item.getIndex() == 9 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 18: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 18 || item.getIndex() == 10 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 19: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 19 || item.getIndex() == 11 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 20: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 20 || item.getIndex() == 12 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 21: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 21 || item.getIndex() == 13 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 22: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 22 || item.getIndex() == 14 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 23: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 23 || item.getIndex() == 7 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 24: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 24 || item.getIndex() == 8 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 25: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 25 || item.getIndex() == 9 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 26: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 26 || item.getIndex() == 10 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 27: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 27 || item.getIndex() == 11 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 28: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 28 || item.getIndex() == 12 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 29: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 29 || item.getIndex() == 13 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            case 30: {
                for (ItemPackage item : arrayItems) {
                    if ((item.getIndex() == 30 || item.getIndex() == 14 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        if (item.getCount() == 0 && arrayItemsToday.contains(item.getName())) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        } else if (item.getCount() > 0) {
                            itemsToShow.add(convertToItemPackageShow(item));
                        }
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    ItemPackageShow convertToItemPackageShow(ItemPackage j) {
        return new ItemPackageShow(j.getId(), j.getName(), j.getMeaningEn(), j.getMeaningFa(), j.getExamplesEn(), j.getExamplesFa(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount());
    }

    ItemPackage convertToItem(ItemPackageShow j) {
        return new ItemPackage(j.getId(), j.getName(), j.getMeaningEn(), j.getMeaningFa(), j.getExamplesEn(), j.getExamplesFa(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount());
    }

    void listeners() {
        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!itemsToShow.get(position).getName().equals("   Nothing found") && !itemsToShow.get(position).getMeaningFa().equals("My Dictionary")) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

                    if (markSeveral) {
                        if (itemsToShow.get(position).isChChecked()) {
                            itemsToShow.get(position).setChChecked(false);
                            adapter.notifyDataSetChanged();
                            notifyCheckedPositionsInt();
                        } else {
                            itemsToShow.get(position).setChChecked(true);
                            adapter.notifyDataSetChanged();
                            notifyCheckedPositionsInt();
                        }
                    } else {
                        if (!dialogMeaning.isShowing())
                            dialogMeaning(position);
                    }
//                    } else if (!(itemsToShow.get(position).getName().equals("   Nothing found") &&
//                            itemsToShow.get(position).getMeaning().equals("My Dictionary") &&
//                            itemsToShow.get(position).getAddDate().equals("8I4KJ4UeRq")) /*&& position1 != 0*/) {
//                        dialogMeaning(position, getPosition(position));
//                    }
                }
            }
        });


        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(etSearch.getText().toString());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etSearch.getText().length() == 0) {
                    isFromSearch = false;
                    listViewPosition = items.onSaveInstanceState();
                    refreshListViewData();
                } else {
                    search(etSearch.getText().toString());
                }
            }
        });

    }

    void notifyCheckedPositionsInt() {
        checkedPositionsInt.clear();
        for (int i = 0; i < itemsToShow.size(); i++) {
            checkedPositionsInt.add(i, itemsToShow.get(i).isChChecked() ? 0 : 1);
        }
    }

    String getDistance(String date) {
        if (!date.equals("")) {
            boolean thisHour = false;
            boolean today = false;
            boolean thisMonth = false;
            boolean thisYear = false;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String currentDateAndTime = simpleDateFormat.format(new Date());

            Date d1 = null;
            Date d2 = null;
            try {
                d1 = simpleDateFormat.parse(date);
                d2 = simpleDateFormat.parse(currentDateAndTime);
            } catch (ParseException e) {
                e.printStackTrace();
                return "wrong date";
            }

            final long diff = d2.getTime() - d1.getTime();
            final long diffSeconds = diff / 1000;
            final long diffMinutes = diffSeconds / 60;
            final long diffHours = diffMinutes / 60;
            final long diffDays = diffHours / 24;
            final long diffMonth = diffDays / 30;
            final long diffYear = diffMonth / 12;


            if (diffYear == 0 && diffMonth == 0 && diffDays == 0 && diffHours == 0) {
                thisHour = true;
            } else if (diffYear == 0 && diffMonth == 0 && diffDays == 0) {
                today = true;
            } else if (diffYear == 0 && diffMonth == 0) {
                thisMonth = true;
            } else if (diffYear == 0) {
                thisYear = true;
            }


            if (thisHour) {
                return diffMinutes == 0 ? "just now" : diffMinutes < 2 ? Long.toString(diffMinutes) + " minute ago" : Long.toString(diffMinutes) + " minutes ago";
            } else if (today) {
                return diffHours < 2 ? Long.toString(diffHours) + " hour ago" : Long.toString(diffHours) + " hours ago";

            } else if (thisMonth) {
                long difDay = diffDays;
                long difHour = diffHours;
                String strDistance;

                if (diffHours > 24) {
                    difHour = diffHours % 24;
                } else {
                    difDay--;
                    difHour = (difHour + 24) - difHour;
                }

                strDistance = difDay < 2 ? Long.toString(difDay) + " day" : Long.toString(difDay) + " days";
                strDistance += (difHour == 0 ? " ago"
                        : difHour < 2 ? " and " + Long.toString(difHour) + " hour ago"
                        : " and " + Long.toString(difHour) + " hours ago");

                if (difHour == 24) {
                    strDistance = "1 day and 0 hour ago";
                }
                return strDistance;

            } else {
                long difDay = diffDays;
                long difMonth = diffMonth;
                long difYear = diffYear;
                String strDistance = "";

                if (difDay > 30) {
                    difDay = difDay % 30;
                } else {
                    difMonth--;
                    difDay = (difDay + 30) - difDay;
                }
                if (difMonth > 12) {
                    difMonth = difMonth % 12;
                } else {
                    difYear--;
                    difMonth = (difMonth + 12) - difMonth;
                }

                if (diffYear == 0) {
                    if (difMonth > 0) {
                        strDistance = difMonth < 2 ? Long.toString(difMonth) + " month" : Long.toString(difMonth) + " months";
                        if (difDay == 0) {
                            strDistance += " and " + Long.toString(difDay) + " day ago";
                        }
                    }
                    strDistance += difDay < 2 ? " and " + Long.toString(difDay) + " day ago"
                            : " and " + Long.toString(difDay) + " days ago";
                } else {
                    strDistance = difYear < 2 ? Long.toString(difYear) + " year" : Long.toString(difYear) + " years";
                    strDistance += (difMonth == 0 ? " ago"
                            : difMonth < 2 ? " and " + Long.toString(difMonth) + " month ago"
                            : " and " + Long.toString(difMonth) + " months ago");
                }
                return strDistance;
            }
        }
        return "nothing";
    }

    int getPosition(int position) {
        int realPosition = 0;
        boolean found = false;
        for (int i = 0; i < arrayItems.size(); i++) {
            if (arrayItems.get(i).getName().equals(itemsToShow.get(position).getName())) {
                realPosition = i;
                break;
            }
            for (int j = 0; j < arrayItems.size(); j++) {
                if ((Integer.toString(j + 1) + ". " + arrayItems.get(i).getName()).equals(itemsToShow.get(position).getName())) {
                    realPosition = i;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        return realPosition;
    }

    int getPosition(String word) {
        for (int i = 0; i < arrayItems.size(); i++) {
            if (arrayItems.get(i).getName().toUpperCase().equals(word)) {
                return i;
            }
        }
        return 0;
    }

    void search(String key) {
        char first[] = key.toCharArray();
        if (key.length() > 0 && first[0] == '.') {
            int found = 0;
            itemsToShow.clear();

            if (arrayItems.size() > 0) {
                key = key.toUpperCase();
                key = key.substring(1);
                for (ItemPackage arrayItem : arrayItems) {
                    String word = arrayItem.getName().toUpperCase();
                    String meaning = arrayItem.getMeaningFa().toUpperCase();
                    if (searchMethod.equals("wordsAndMeanings") ? word.contains(key) || meaning.contains(key) :
                            searchMethod.equals("justWords") ? word.contains(key) :
                                    meaning.contains(key)) {
                        found++;
                        itemsToShow.add(convertToItemPackageShow(arrayItem));
                    }
                }
                if (found > 0) {
                    items.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    sort();
                }
            }
            isFromSearchDot = true;
            isFromSearch = true;

            if (itemsToShow.size() > 0) {
                for (int i = 0; i < itemsToShow.size(); i++) {
                    if (!(itemsToShow.get(i).getName().equals("   Nothing found") &&
                            itemsToShow.get(i).getMeaningFa().equals("My Dictionary"))) {
                        itemsToShow.get(i).setChVisible(markSeveral);
                        //whether show item's number or not
                        if (!(itemsToShow.get(i).getName().equals("   Nothing found") && itemsToShow.get(i).getMeaningFa().equals("My Dictionary")))
                            itemsToShow.get(i).setName(showItemNumber ? i + 1 + ". " + itemsToShow.get(i).getName() : itemsToShow.get(i).getName());
                    }
                }
                notifyCheckedPositionsInt();
                TextView tvSummery = (TextView) findViewById(R.id.packageSummeryTV);
                if (!itemsToShow.get(0).getName().equals("   Nothing found") && !itemsToShow.get(0).getMeaningFa().equals("My Dictionary")) {
                    tvSummery.setText("'" + Integer.toString(itemsToShow.size()) + "'");
                } else tvSummery.setText("'" + Integer.toString(itemsToShow.size() - 1) + "'");
            } else {
                TextView tvSummery = (TextView) findViewById(R.id.packageSummeryTV);
                tvSummery.setText("'0'");
                itemsToShow.add(new ItemPackageShow("   Nothing found", "My Dictionary"));
            }
        } else if (key.length() > 0) {
            int found = 0;
            itemsToShow.clear();
            refreshShow();

            adapter.notifyDataSetChanged();

            if (itemsToShow.size() > 0) {
                int i = 0;
                key = key.toUpperCase();
                while (i < itemsToShow.size()) {
                    String word = itemsToShow.get(i).getName().toUpperCase();
                    String meaning = itemsToShow.get(i).getMeaningFa().toUpperCase();

                    if (searchMethod.equals("wordsAndMeanings") ? !word.contains(key) && !meaning.contains(key) :
                            searchMethod.equals("justWords") ? !word.contains(key) :
                                    !meaning.contains(key)) {
                        itemsToShow.remove(i);
                        i = 0;
                    } else {
                        found++;
                        i++;
                    }
                }
                if (found > 0) {
                    items.setAdapter(adapter);
                }
                adapter.notifyDataSetChanged();
            }
            isFromSearch = true;

            if (itemsToShow.size() > 0) {
                for (int i = 0; i < itemsToShow.size(); i++) {
                    if (!(itemsToShow.get(i).getName().equals("   Nothing found") &&
                            itemsToShow.get(i).getMeaningFa().equals("My Dictionary"))) {
                        itemsToShow.get(i).setChVisible(markSeveral);
                        //whether show item's number or not
                        if (!(itemsToShow.get(i).getName().equals("   Nothing found") && itemsToShow.get(i).getMeaningFa().equals("My Dictionary")))
                            itemsToShow.get(i).setName(showItemNumber ? i + 1 + ". " + itemsToShow.get(i).getName() : itemsToShow.get(i).getName());
                    }
                }
                notifyCheckedPositionsInt();
            } else {
                itemsToShow.add(new ItemPackageShow("   Nothing found", "My Dictionary"));
            }
        }

    }

    void getPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        searchMethod = prefs.getString("searchMethod", "wordsAndMeanings");
        showItemNumber = prefs.getBoolean("showItemNumber", true);
        isDistance = prefs.getString("timeMethod", "distance");
        sortMethod = prefs.getString("sortMethod", "dateA");
        String addPerDayStr = prefs.getString("addPerDay", "s10");
        addPerDay = addPerDayStr.equals("s5") ? 5 : addPerDayStr.equals("s10") ? 10 : addPerDayStr.equals("s15") ? 15 : 20;
//        packageL = prefs.getString("")
    }


    void clearMarks() {
        for (int i = 0; i < itemsToShow.size(); i++) {
            itemsToShow.get(i).setChChecked(false);
            notifyCheckedPositionsInt();
        }
        adapter.notifyDataSetChanged();
    }


    public void tvLastDateOnClick(View view) {
        TextView tvLastDate = (TextView) dialogMeaning.findViewById(R.id.packageLastDate);
        if (isDistanceTempLast.equals("date")) {
            isDistanceTempLast = "distance";
            tvLastDate.setText(getDistance(arrayItems.get(getPosition(dialogMeaningWordPosition)).getLastCheckDate()));
        } else {
            tvLastDate.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getLastCheckDate());
            isDistanceTempLast = "date";
        }
    }


    void dialogMeaning(final int position) {
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflater.inflate(R.layout.dialog_meaning_package, null));
        builder.setPositiveButton(R.string.correct, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(R.string.Incorrect, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNeutralButton("Flip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        int currentApi = android.os.Build.VERSION.SDK_INT;
//        if (currentApi >= Build.VERSION_CODES.HONEYCOMB){
//            builder.setIconAttribute(android.R.drawable.ic_dialog_info);
//        }else {
        builder.setIcon(android.R.drawable.ic_dialog_info);
//        }
        dialogMeaning = builder.create();
        dialogMeaning.show();
        dialogMeaningWordPosition = position;
        ItemPackage j = arrayItems.get(getPosition(position));

        TextView tvName = (TextView) dialogMeaning.findViewById(R.id.packageName);
        TextView tvName1 = (TextView) dialogMeaning.findViewById(R.id.packageName1);
        TextView tvMeaningFa = (TextView) dialogMeaning.findViewById(R.id.packageMeaningFa);
        TextView tvMeaningEn = (TextView) dialogMeaning.findViewById(R.id.packageMeaningEn);
        TextView tvExamplesEn = (TextView) dialogMeaning.findViewById(R.id.packageExamplesEn);
        TextView tvExamplesFa = (TextView) dialogMeaning.findViewById(R.id.packageExamplesFa);

        TextView tvMFa = (TextView) dialogMeaning.findViewById(R.id.pMeaningFa);
        TextView tvMEn = (TextView) dialogMeaning.findViewById(R.id.pMeaningEn);
        TextView tvEEn = (TextView) dialogMeaning.findViewById(R.id.pExamplesEn);
        TextView tvEFa = (TextView) dialogMeaning.findViewById(R.id.pExamplesFa);

        TextView tvLastDate = (TextView) dialogMeaning.findViewById(R.id.packageLastDate);
        TextView tvPosition = (TextView) dialogMeaning.findViewById(R.id.packagePosition);
        TextView tvCountCorrect = (TextView) dialogMeaning.findViewById(R.id.packageCountCorrect);
        TextView tvCount = (TextView) dialogMeaning.findViewById(R.id.packageCount);
        TextView tvCountInCorrect = (TextView) dialogMeaning.findViewById(R.id.packageCountInCorrect);

        answerViewed = false;

        int index = indexDeck(j.getIndex());
        tvPosition.setText("at deck '" + Integer.toString(j.getDeck()) + "', index '" + Integer.toString(index) + "'");
        tvCountCorrect.setText(Integer.toString(j.getCountCorrect()));
        tvCount.setText(Integer.toString(j.getCount()));
        tvCountInCorrect.setText(Integer.toString(j.getCountInCorrect()));

        isDistanceTempAdd = isDistance;
        isDistanceTempLast = isDistance;
        if (isDistance.equals("distance")) {
            tvLastDate.setText(getDistance(j.getLastCheckDate()));
        } else {
            tvLastDate.setText(j.getLastCheckDate());
        }

        tvName.setText(j.getName());
        tvName1.setText(j.getName());
        tvMeaningFa.setText(j.getMeaningFa());
        tvMeaningEn.setText(j.getMeaningEn());
        tvExamplesEn.setText(j.getExamplesEn());
        tvExamplesFa.setText(j.getExamplesFa());


        TextView tvPos = (TextView) dialogMeaning.findViewById(R.id.packagePos);
        tvPos.setText(Integer.toString(position + 1) + " of " + Integer.toString(itemsToShow.size()));
        dialogMeaning.setCanceledOnTouchOutside(true);

        final String textTvName = tvName.getText().toString();
        tvName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(textTvName);
                return true;
            }
        });

        final String textTvName1 = tvName1.getText().toString();
        tvName1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(textTvName1);
                return true;
            }
        });


        final String textTvMeaningEn = tvMeaningEn.getText().toString();
        tvMeaningEn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(textTvMeaningEn);
                return true;
            }
        });

        final String textTvExamplesEn = tvExamplesEn.getText().toString();
        tvExamplesEn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(textTvExamplesEn);
                return true;
            }
        });


        tvName.setVisibility(View.VISIBLE);

        tvName1.setVisibility(View.GONE);
        tvMeaningFa.setVisibility(View.GONE);
        tvMeaningEn.setVisibility(View.GONE);
        tvExamplesEn.setVisibility(View.GONE);
        tvExamplesFa.setVisibility(View.GONE);

        tvMFa.setVisibility(View.GONE);
        tvMEn.setVisibility(View.GONE);
        tvEEn.setVisibility(View.GONE);
        tvEFa.setVisibility(View.GONE);


        Button btnPositive = dialogMeaning.getButton(DialogInterface.BUTTON_POSITIVE);
        Button btnNegative = dialogMeaning.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button btnNeutral = dialogMeaning.getButton(DialogInterface.BUTTON_NEUTRAL);
        btnPositive.setOnClickListener(new CustomListenerMeaning(dialogMeaning, position, 0));
        btnNegative.setOnClickListener(new CustomListenerMeaning(dialogMeaning, position, 1));
        btnNeutral.setOnClickListener(new CustomListenerMeaning(dialogMeaning, position, 2));
    }

    class CustomListenerMeaning implements View.OnClickListener {
        private final Dialog dialog;
        private final int position;
        private final int whatToDo;

        public CustomListenerMeaning(Dialog dialog, int position, int whatToDo) {
            this.dialog = dialog;
            this.position = position;
            this.whatToDo = whatToDo;
        }

        @Override
        public void onClick(View v) {
            if (!isFromSearch) {
                isFromSearchDot = false;
            }
            if (whatToDo == 2) {
                name_Click();
            } else if (answerViewed && !isFromSearchDot) {
                if (whatToDo == 0) {
                    move_Next_Correct(position);
                    update_Info_After_Answer(position, true);
                } else {
                    int realPosition = getPosition(position);
                    arrayItems.get(realPosition).setDeck(1);
                    arrayItems.get(realPosition).setIndex(0);

                    update_Info_After_Answer(position, false);
                }
                dialog.dismiss();
            } else if (isFromSearchDot) {
                Toast.makeText(PackageActivity.this, "you can't answer on review mode.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PackageActivity.this, "First check the answer by clicking on the word then answer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void name_Click() {
        answerViewed = true;

        TextView tvName1 = (TextView) dialogMeaning.findViewById(R.id.packageName1);
        TextView tvName = (TextView) dialogMeaning.findViewById(R.id.packageName);
        TextView tvMeaningFa = (TextView) dialogMeaning.findViewById(R.id.packageMeaningFa);
        TextView tvMeaningEn = (TextView) dialogMeaning.findViewById(R.id.packageMeaningEn);
        TextView tvExamplesEn = (TextView) dialogMeaning.findViewById(R.id.packageExamplesEn);
        TextView tvExamplesFa = (TextView) dialogMeaning.findViewById(R.id.packageExamplesFa);

        TextView tvMFa = (TextView) dialogMeaning.findViewById(R.id.pMeaningFa);
        TextView tvMEn = (TextView) dialogMeaning.findViewById(R.id.pMeaningEn);
        TextView tvEEn = (TextView) dialogMeaning.findViewById(R.id.pExamplesEn);
        TextView tvEFa = (TextView) dialogMeaning.findViewById(R.id.pExamplesFa);


        if (tvName.getVisibility() == View.VISIBLE) {
            tvName.setVisibility(View.GONE);

            tvName1.setVisibility(View.VISIBLE);
            tvMeaningFa.setVisibility(View.VISIBLE);

            tvMFa.setVisibility(View.VISIBLE);
            tvMEn.setVisibility(View.VISIBLE);
            tvEEn.setVisibility(View.VISIBLE);
            tvEFa.setVisibility(View.VISIBLE);
        } else {
            tvName.setVisibility(View.VISIBLE);

            tvName1.setVisibility(View.GONE);
            tvMeaningFa.setVisibility(View.GONE);
            tvMeaningEn.setVisibility(View.GONE);
            tvExamplesEn.setVisibility(View.GONE);
            tvExamplesFa.setVisibility(View.GONE);

            tvMFa.setVisibility(View.GONE);
            tvMEn.setVisibility(View.GONE);
            tvEEn.setVisibility(View.GONE);
            tvEFa.setVisibility(View.GONE);
        }
    }

    public void meaningFa_click(View view) {
        TextView tvMeaningFa = (TextView) dialogMeaning.findViewById(R.id.packageMeaningFa);
        if (tvMeaningFa.getVisibility() == View.VISIBLE) {
            tvMeaningFa.setVisibility(View.GONE);
        } else {
            tvMeaningFa.setVisibility(View.VISIBLE);
        }
    }

    public void meaningEn_click(View view) {
        TextView tvMeaningEn = (TextView) dialogMeaning.findViewById(R.id.packageMeaningEn);
        if (tvMeaningEn.getVisibility() == View.VISIBLE) {
            tvMeaningEn.setVisibility(View.GONE);
        } else {
            tvMeaningEn.setVisibility(View.VISIBLE);
        }
    }

    public void examplesEn_click(View view) {
        TextView tvExamplesEn = (TextView) dialogMeaning.findViewById(R.id.packageExamplesEn);
        if (tvExamplesEn.getVisibility() == View.VISIBLE) {
            tvExamplesEn.setVisibility(View.GONE);
        } else {
            tvExamplesEn.setVisibility(View.VISIBLE);
        }
    }

    public void examplesFa_click(View view) {
        TextView tvExamplesFa = (TextView) dialogMeaning.findViewById(R.id.packageExamplesFa);
        if (tvExamplesFa.getVisibility() == View.VISIBLE) {
            tvExamplesFa.setVisibility(View.GONE);
        } else {
            tvExamplesFa.setVisibility(View.VISIBLE);
        }
    }


    int indexDeck(int index) {
        switch (index) {
            case 0:
                return 1;

            case 1:
                return 1;
            case 2:
                return 2;

            case 3:
                return 1;
            case 4:
                return 2;
            case 5:
                return 3;
            case 6:
                return 4;

            case 7:
                return 1;
            case 8:
                return 2;
            case 9:
                return 3;
            case 10:
                return 4;
            case 11:
                return 5;
            case 12:
                return 6;
            case 13:
                return 7;
            case 14:
                return 8;

            case 15:
                return 1;
            case 16:
                return 2;
            case 17:
                return 3;
            case 18:
                return 4;
            case 19:
                return 5;
            case 20:
                return 6;
            case 21:
                return 7;
            case 22:
                return 8;
            case 23:
                return 9;
            case 24:
                return 10;
            case 25:
                return 11;
            case 26:
                return 12;
            case 27:
                return 13;
            case 28:
                return 14;
            case 29:
                return 15;
            case 30:
                return 16;
        }
        return 1;
    }

    void move_Next_Correct(int position) {
        int realPosition = getPosition(position);
        int currentDeck = arrayItems.get(realPosition).getDeck();
        int nextIndex = 0;
        switch (currentDeck) {
            case 0: //to deck 1 / /////// / / not going to happen
                arrayItems.get(realPosition).setDeck(1);
                arrayItems.get(realPosition).setIndex(0);
                databasePackage.updatePosition(arrayItems.get(realPosition).getId(), 1, 0);
                break;

            case 1:  //to deck 2
                nextIndex = whichIndexTurnDeck(2);

                databasePackage.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databasePackage.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(2);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databasePackage.updatePosition(arrayItems.get(realPosition).getId(), 2, nextIndex);
                break;

            case 2: // to deck 3
                nextIndex = whichIndexTurnDeck(4);

                databasePackage.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databasePackage.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(3);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databasePackage.updatePosition(arrayItems.get(realPosition).getId(), 3, nextIndex);
                break;

            case 3: // to deck 4
                nextIndex = whichIndexTurnDeck(8);

                databasePackage.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databasePackage.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(4);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databasePackage.updatePosition(arrayItems.get(realPosition).getId(), 4, nextIndex);
                break;

            case 4: // to deck 5
                nextIndex = whichIndexTurnDeck(16);

                databasePackage.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databasePackage.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(5);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databasePackage.updatePosition(arrayItems.get(realPosition).getId(), 5, nextIndex);
                break;
            case 5: // to archive
                databasePackage.addItem(arrayItems.get(realPosition), v.TABLE_ARCHIVE);
                break;
        }

    }

    int whichIndexTurnDeck(int size) {
        int lastIndex = -1;
        int lastDay = -1;

        if (size == 2) {
            if (arrayIndexesLastDay.get(1) == -1) {
                return 1;
            }
            for (int i = 1; i < 3; i++) {
                if (arrayIndexesLastDay.get(i) > lastDay) {
                    lastDay = arrayIndexesLastDay.get(i);
                    lastIndex = i;
                }
            }
            if (todayDate.equals(arrayIndexesLastDayDate.get(lastIndex))) {
                return lastIndex;
            } else {
                if (lastIndex == 2) {
                    return 1;//next index
                } else if (lastIndex == 1) {
                    return 2;//next index
                }
            }

        } else if (size == 4) {
            if (arrayIndexesLastDay.get(3) == -1) {
                return 3;
            }
            for (int i = 3; i < 7; i++) {
                if (arrayIndexesLastDay.get(i) > lastDay) {
                    lastDay = arrayIndexesLastDay.get(i);
                    lastIndex = i;
                }
            }
            if (todayDate.equals(arrayIndexesLastDayDate.get(lastIndex))) {
                return lastIndex;
            } else {
                if (lastIndex == 6) {
                    return 3;
                } else {
                    return lastIndex + 1;
                }
            }

        } else if (size == 8) {
            if (arrayIndexesLastDay.get(7) == -1) {
                return 7;
            }
            for (int i = 7; i < 15; i++) {
                if (arrayIndexesLastDay.get(i) > lastDay) {
                    lastDay = arrayIndexesLastDay.get(i);
                    lastIndex = i;
                }
            }
            if (todayDate.equals(arrayIndexesLastDayDate.get(lastIndex))) {
                return lastIndex;
            } else {
                if (lastIndex == 14) {
                    return 7;
                } else {
                    return lastIndex + 1;
                }
            }

        } else if (size == 16) {
            if (arrayIndexesLastDay.get(15) == -1) {
                return 15;
            }
            for (int i = 15; i < 31; i++) {
                if (arrayIndexesLastDay.get(i) > lastDay) {
                    lastDay = arrayIndexesLastDay.get(i);
                    lastIndex = i;
                }
            }
            if (todayDate.equals(arrayIndexesLastDayDate.get(lastIndex))) {
                return lastIndex;
            } else {
                if (lastIndex == 30) {
                    return 15;
                } else {
                    return lastIndex + 1;
                }
            }
        }
        return -1;
    }

    int lastIndexMore30() {
        int lastIndex = -1;
        int lastDay = -1;

        for (int i = 15; i < 31; i++) {
            if (arrayIndexesLastDay.get(i) > lastDay) {
                lastDay = arrayIndexesLastDay.get(i);
                lastIndex = i;
            }
        }
        return lastIndex;
    }

    int nextIndexMore30(int size) {
        int lastIndex = -1;
        int lastDay = -1;

        if (size == 16) {
            for (int i = 15; i < 31; i++) {
                if (arrayIndexesLastDay.get(i) > lastDay) {
                    lastDay = arrayIndexesLastDay.get(i);
                    lastIndex = i;
                }
            }
            if (todayDate.equals(arrayIndexesLastDayDate.get(lastIndex))) {
                return lastIndex;
            } else {
                if (lastIndex == 30) {
                    return 15;
                } else {
                    return lastIndex + 1;
                }
            }
        }
        return -1;
    }

    void update_Info_After_Answer(int position, boolean correct) {
        int realPosition = getPosition(position);
        ItemPackage j = arrayItems.get(realPosition);

        databasePackage.updateLastDate(todayDate);
        databasePackage.updateLastDay(todayNum);
        lastDate = todayDate;

        int countCorrect = correct ? j.getCountCorrect() + 1 : j.getCountCorrect();
        int countIncorrect = !correct ? j.getCountInCorrect() + 1 : j.getCountInCorrect();
        int count = j.getCount() + 1;

        arrayItems.get(realPosition).setCountCorrect(countCorrect);
        arrayItems.get(realPosition).setCountInCorrect(countIncorrect);
        arrayItems.get(realPosition).setCount(count);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String currentDateAndTime = simpleDateFormat.format(new Date());

        arrayItems.get(realPosition).setLastCheckDate(currentDateAndTime);
        arrayItems.get(realPosition).setLastCheckDay(todayNum);

        j = arrayItems.get(realPosition);

        databasePackage.updateItem(new ItemPackage(j.getId(), j.getName(), j.getMeaningEn(), j.getMeaningFa(), j.getExamplesEn(), j.getExamplesFa(),
                currentDateAndTime, todayNum,
                j.getDeck(), j.getIndex(),
                countCorrect, countIncorrect, count), v.TABLE_LEITNER);

        listViewPosition = items.onSaveInstanceState();
        refreshListViewData();

        if (position != itemsToShow.size() && (!itemsToShow.get(position).getName().equals("   Nothing found"))) {
            dialogMeaning(position);
        }
    }


    void updateIndexesLastDayLessThan30() {
        switch (todayNum) {
            case 1: {
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 2: {
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 3: {
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 4: {
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 5: {
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 6: {
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 7: {
                databasePackage.updateItemLastDays(8, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(8, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(7, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(7, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 8: {
                databasePackage.updateItemLastDays(9, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(9, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(8, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(8, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 9: {
                databasePackage.updateItemLastDays(10, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(102, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(9, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(9, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 10: {
                databasePackage.updateItemLastDays(11, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(11, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(10, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(10, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 11: {
                databasePackage.updateItemLastDays(12, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(12, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(11, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(11, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 12: {
                databasePackage.updateItemLastDays(13, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(13, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(12, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(12, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 13: {
                databasePackage.updateItemLastDays(14, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(14, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(13, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(13, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 14: {
                databasePackage.updateItemLastDays(15, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(15, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(14, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(14, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 15: {
                databasePackage.updateItemLastDays(16, todayNum);
                databasePackage.updateItemLastDays(8, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(16, lastDate);
                databasePackage.updateItemLastDaysDate(8, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(15, todayNum);
                arrayIndexesLastDay.set(7, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(15, lastDate);
                arrayIndexesLastDayDate.set(7, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 16: {
                databasePackage.updateItemLastDays(17, todayNum);
                databasePackage.updateItemLastDays(9, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(17, lastDate);
                databasePackage.updateItemLastDaysDate(9, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(16, todayNum);
                arrayIndexesLastDay.set(8, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(16, lastDate);
                arrayIndexesLastDayDate.set(8, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 17: {
                databasePackage.updateItemLastDays(18, todayNum);
                databasePackage.updateItemLastDays(10, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(18, lastDate);
                databasePackage.updateItemLastDaysDate(10, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(17, todayNum);
                arrayIndexesLastDay.set(9, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(17, lastDate);
                arrayIndexesLastDayDate.set(9, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 18: {
                databasePackage.updateItemLastDays(19, todayNum);
                databasePackage.updateItemLastDays(11, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(19, lastDate);
                databasePackage.updateItemLastDaysDate(11, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(18, todayNum);
                arrayIndexesLastDay.set(10, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(18, lastDate);
                arrayIndexesLastDayDate.set(10, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 19: {
                databasePackage.updateItemLastDays(20, todayNum);
                databasePackage.updateItemLastDays(12, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(20, lastDate);
                databasePackage.updateItemLastDaysDate(12, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(19, todayNum);
                arrayIndexesLastDay.set(11, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(19, lastDate);
                arrayIndexesLastDayDate.set(11, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 20: {
                databasePackage.updateItemLastDays(21, todayNum);
                databasePackage.updateItemLastDays(13, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(21, lastDate);
                databasePackage.updateItemLastDaysDate(13, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(20, todayNum);
                arrayIndexesLastDay.set(12, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(20, lastDate);
                arrayIndexesLastDayDate.set(12, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 21: {
                databasePackage.updateItemLastDays(22, todayNum);
                databasePackage.updateItemLastDays(14, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(22, lastDate);
                databasePackage.updateItemLastDaysDate(14, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(21, todayNum);
                arrayIndexesLastDay.set(13, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(21, lastDate);
                arrayIndexesLastDayDate.set(13, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 22: {
                databasePackage.updateItemLastDays(23, todayNum);
                databasePackage.updateItemLastDays(15, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(23, lastDate);
                databasePackage.updateItemLastDaysDate(15, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(22, todayNum);
                arrayIndexesLastDay.set(14, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(22, lastDate);
                arrayIndexesLastDayDate.set(14, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 23: {
                databasePackage.updateItemLastDays(24, todayNum);
                databasePackage.updateItemLastDays(8, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(24, lastDate);
                databasePackage.updateItemLastDaysDate(8, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(23, todayNum);
                arrayIndexesLastDay.set(7, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(23, lastDate);
                arrayIndexesLastDayDate.set(7, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 24: {
                databasePackage.updateItemLastDays(25, todayNum);
                databasePackage.updateItemLastDays(9, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(25, lastDate);
                databasePackage.updateItemLastDaysDate(9, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(24, todayNum);
                arrayIndexesLastDay.set(8, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(24, lastDate);
                arrayIndexesLastDayDate.set(8, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 25: {
                databasePackage.updateItemLastDays(26, todayNum);
                databasePackage.updateItemLastDays(10, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(26, lastDate);
                databasePackage.updateItemLastDaysDate(10, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(25, todayNum);
                arrayIndexesLastDay.set(9, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(25, lastDate);
                arrayIndexesLastDayDate.set(9, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 26: {
                databasePackage.updateItemLastDays(27, todayNum);
                databasePackage.updateItemLastDays(11, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(27, lastDate);
                databasePackage.updateItemLastDaysDate(11, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(26, todayNum);
                arrayIndexesLastDay.set(10, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(26, lastDate);
                arrayIndexesLastDayDate.set(10, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 27: {
                databasePackage.updateItemLastDays(28, todayNum);
                databasePackage.updateItemLastDays(12, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(28, lastDate);
                databasePackage.updateItemLastDaysDate(12, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(27, todayNum);
                arrayIndexesLastDay.set(11, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(27, lastDate);
                arrayIndexesLastDayDate.set(11, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 28: {
                databasePackage.updateItemLastDays(29, todayNum);
                databasePackage.updateItemLastDays(13, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(29, lastDate);
                databasePackage.updateItemLastDaysDate(13, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(28, todayNum);
                arrayIndexesLastDay.set(12, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(28, lastDate);
                arrayIndexesLastDayDate.set(12, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 29: {
                databasePackage.updateItemLastDays(30, todayNum);
                databasePackage.updateItemLastDays(14, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(30, lastDate);
                databasePackage.updateItemLastDaysDate(14, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(29, todayNum);
                arrayIndexesLastDay.set(13, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(29, lastDate);
                arrayIndexesLastDayDate.set(13, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 30: {
                databasePackage.updateItemLastDays(31, todayNum);
                databasePackage.updateItemLastDays(15, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(31, lastDate);
                databasePackage.updateItemLastDaysDate(15, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(30, todayNum);
                arrayIndexesLastDay.set(14, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(30, lastDate);
                arrayIndexesLastDayDate.set(14, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            default: {
                break;
            }
        }
    }

    void updateIndexLastDayMoreThan30() {
        int nextIndexDeck5 = lastIndexMore30();
        switch (nextIndexDeck5) {
            case 15: {
                databasePackage.updateItemLastDays(16, todayNum);
                databasePackage.updateItemLastDays(8, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(16, lastDate);
                databasePackage.updateItemLastDaysDate(8, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(15, todayNum);
                arrayIndexesLastDay.set(7, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(15, lastDate);
                arrayIndexesLastDayDate.set(7, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 16: {
                databasePackage.updateItemLastDays(17, todayNum);
                databasePackage.updateItemLastDays(9, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(17, lastDate);
                databasePackage.updateItemLastDaysDate(9, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(16, todayNum);
                arrayIndexesLastDay.set(8, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(16, lastDate);
                arrayIndexesLastDayDate.set(8, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 17: {
                databasePackage.updateItemLastDays(18, todayNum);
                databasePackage.updateItemLastDays(10, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(18, lastDate);
                databasePackage.updateItemLastDaysDate(10, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(17, todayNum);
                arrayIndexesLastDay.set(9, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(17, lastDate);
                arrayIndexesLastDayDate.set(9, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 18: {
                databasePackage.updateItemLastDays(19, todayNum);
                databasePackage.updateItemLastDays(11, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(19, lastDate);
                databasePackage.updateItemLastDaysDate(11, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(18, todayNum);
                arrayIndexesLastDay.set(10, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(18, lastDate);
                arrayIndexesLastDayDate.set(10, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 19: {
                databasePackage.updateItemLastDays(20, todayNum);
                databasePackage.updateItemLastDays(12, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(20, lastDate);
                databasePackage.updateItemLastDaysDate(12, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(19, todayNum);
                arrayIndexesLastDay.set(11, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(19, lastDate);
                arrayIndexesLastDayDate.set(11, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 20: {
                databasePackage.updateItemLastDays(21, todayNum);
                databasePackage.updateItemLastDays(13, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(21, lastDate);
                databasePackage.updateItemLastDaysDate(13, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(20, todayNum);
                arrayIndexesLastDay.set(12, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(20, lastDate);
                arrayIndexesLastDayDate.set(12, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 21: {
                databasePackage.updateItemLastDays(22, todayNum);
                databasePackage.updateItemLastDays(14, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(22, lastDate);
                databasePackage.updateItemLastDaysDate(14, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(21, todayNum);
                arrayIndexesLastDay.set(13, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(21, lastDate);
                arrayIndexesLastDayDate.set(13, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 22: {
                databasePackage.updateItemLastDays(23, todayNum);
                databasePackage.updateItemLastDays(15, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(23, lastDate);
                databasePackage.updateItemLastDaysDate(15, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(22, todayNum);
                arrayIndexesLastDay.set(14, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(22, lastDate);
                arrayIndexesLastDayDate.set(14, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 23: {
                databasePackage.updateItemLastDays(24, todayNum);
                databasePackage.updateItemLastDays(8, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(24, lastDate);
                databasePackage.updateItemLastDaysDate(8, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(23, todayNum);
                arrayIndexesLastDay.set(7, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(23, lastDate);
                arrayIndexesLastDayDate.set(7, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 24: {
                databasePackage.updateItemLastDays(25, todayNum);
                databasePackage.updateItemLastDays(9, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(25, lastDate);
                databasePackage.updateItemLastDaysDate(9, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(24, todayNum);
                arrayIndexesLastDay.set(8, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(24, lastDate);
                arrayIndexesLastDayDate.set(8, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 25: {
                databasePackage.updateItemLastDays(26, todayNum);
                databasePackage.updateItemLastDays(10, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(26, lastDate);
                databasePackage.updateItemLastDaysDate(10, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(25, todayNum);
                arrayIndexesLastDay.set(9, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(25, lastDate);
                arrayIndexesLastDayDate.set(9, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 26: {
                databasePackage.updateItemLastDays(27, todayNum);
                databasePackage.updateItemLastDays(11, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(27, lastDate);
                databasePackage.updateItemLastDaysDate(11, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(26, todayNum);
                arrayIndexesLastDay.set(10, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(26, lastDate);
                arrayIndexesLastDayDate.set(10, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 27: {
                databasePackage.updateItemLastDays(28, todayNum);
                databasePackage.updateItemLastDays(12, todayNum);
                databasePackage.updateItemLastDays(4, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(28, lastDate);
                databasePackage.updateItemLastDaysDate(12, lastDate);
                databasePackage.updateItemLastDaysDate(4, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(27, todayNum);
                arrayIndexesLastDay.set(11, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(27, lastDate);
                arrayIndexesLastDayDate.set(11, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 28: {
                databasePackage.updateItemLastDays(29, todayNum);
                databasePackage.updateItemLastDays(13, todayNum);
                databasePackage.updateItemLastDays(5, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(29, lastDate);
                databasePackage.updateItemLastDaysDate(13, lastDate);
                databasePackage.updateItemLastDaysDate(5, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(28, todayNum);
                arrayIndexesLastDay.set(12, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(28, lastDate);
                arrayIndexesLastDayDate.set(12, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 29: {
                databasePackage.updateItemLastDays(30, todayNum);
                databasePackage.updateItemLastDays(14, todayNum);
                databasePackage.updateItemLastDays(6, todayNum);
                databasePackage.updateItemLastDays(2, todayNum);
                databasePackage.updateItemLastDaysDate(30, lastDate);
                databasePackage.updateItemLastDaysDate(14, lastDate);
                databasePackage.updateItemLastDaysDate(6, lastDate);
                databasePackage.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(29, todayNum);
                arrayIndexesLastDay.set(13, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(29, lastDate);
                arrayIndexesLastDayDate.set(13, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 30: {
                databasePackage.updateItemLastDays(31, todayNum);
                databasePackage.updateItemLastDays(15, todayNum);
                databasePackage.updateItemLastDays(7, todayNum);
                databasePackage.updateItemLastDays(3, todayNum);
                databasePackage.updateItemLastDaysDate(31, lastDate);
                databasePackage.updateItemLastDaysDate(15, lastDate);
                databasePackage.updateItemLastDaysDate(7, lastDate);
                databasePackage.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(30, todayNum);
                arrayIndexesLastDay.set(14, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(30, lastDate);
                arrayIndexesLastDayDate.set(14, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            default: {
                break;
            }
        }
    }


    void dialogAskLogin() {
        dialogAskLogin = new AlertDialog.Builder(this)
                .setMessage("To use leitner packages you need to login or create an account.")
                .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogLogin();
                    }
                })
                .setNegativeButton(R.string.signUp, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogSignUp();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK &&
                                event.getAction() == KeyEvent.ACTION_UP &&
                                !event.isCanceled()) {
                            PackageActivity.super.onBackPressed();
                            return true;
                        }
                        return false;
                    }
                })
                .create();
        dialogAskLogin.show();
        dialogAskLogin.setCanceledOnTouchOutside(false);
    }




    void dialogLogin() {
        LayoutInflater inflater = this.getLayoutInflater();
        dialogLogin = new AlertDialog.Builder(this)
                .setView(inflater.inflate(R.layout.dialog_signup, null))
                .setPositiveButton(R.string.login,
                        new Dialog.OnClickListener() {
                            public void onClick(DialogInterface d, int which) {
                            }
                        })
                .setNegativeButton(R.string.signUp, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogSignUp();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK &&
                                event.getAction() == KeyEvent.ACTION_UP &&
                                !event.isCanceled()) {
                            PackageActivity.super.onBackPressed();
                            return true;
                        }
                        return false;
                    }
                })
                .create();
        dialogLogin.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialogLogin.show();

        TextView tvHeader = (TextView) dialogLogin.findViewById(R.id.tvHeader);
        tvHeader.setText("Login");

        EditText etEmail = (EditText) dialogLogin.findViewById(R.id.etEmail);
        EditText etUsername = (EditText) dialogLogin.findViewById(R.id.etUsername);
        EditText etPassword = (EditText) dialogLogin.findViewById(R.id.etPassword);

        etEmail.setVisibility(View.GONE);
        etUsername.setHint("Enter Your Username");
        etPassword.setHint("Enter Your Password");

        dialogLogin.setCanceledOnTouchOutside(false);
        Button theButton = dialogLogin.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListenerLogin(dialogLogin));
    }

    class CustomListenerLogin implements View.OnClickListener {
        private final Dialog dialog;

        public CustomListenerLogin(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {

            class FtpTask extends AsyncTask<Void, Integer, Void> {
                EditText etUsernameL = (EditText) dialogLogin.findViewById(R.id.etUsername);
                EditText etPasswordL = (EditText) dialogLogin.findViewById(R.id.etPassword);
                String strUsername = etUsernameL.getText().toString();
                String strPassword = etPasswordL.getText().toString();
                String strEmail = "";

                boolean succeed = false;
                String error = "";
                ProgressDialog progressBar;
                private Context context;
                String canFind = "";
                String password = "";

                public FtpTask(Context context) {
                    this.context = context;
                }

                protected void onPreExecute() {
                    progressBar = new ProgressDialog(context);
                    progressBar.setCancelable(false);
                    progressBar.setMessage("Connecting to server ...");
                    progressBar.show();
                    lockScreenOrientation();
                }

                protected Void doInBackground(Void... args) {
                    try {
                        if (con == null) con = new FTPClient();
                        if (!con.isConnected()) con.connect(InetAddress.getByName("5.9.0.183"));

                        if (con.login("ftpUsers@khaled.ir", "8I4KJ4UeRq")) {
                            con.enterLocalPassiveMode(); // important!

                            publishProgress(0);
                            boolean canAdd = con.makeDirectory(s + strUsername);
                            if (canAdd) {
                                con.removeDirectory(s + strUsername);
                                canFind = "no such user";
                            } else {
                                canFind = "successful";

                                InputStream inputStream = con.retrieveFileStream(s + strUsername + s + "userPassword");
                                con.completePendingCommand();
                                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                                password = r.readLine();
                                inputStream.close();
                                r.close();

                                inputStream = con.retrieveFileStream(s + strUsername + s + "userEmail");
                                con.completePendingCommand();
                                r = new BufferedReader(new InputStreamReader(inputStream));
                                strEmail = r.readLine();
                                inputStream.close();
                                r.close();
                            }
                            succeed = true;
                        } else {
//                            Toast.makeText(Backup.this, "couldn't connect to server", Toast.LENGTH_SHORT).show();
                        }

                        con.logout();
                        con.disconnect();

                    } catch (Exception e) {
                        error = e.toString();
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(Void result) {
                    Log.v("FTPTask", "FTP connection complete");
                    if (canFind.equals("successful")) {
                        if (strPassword.equals(password)) {

                            EditorUserInfo.putString("userUsername", strUsername);
                            EditorUserInfo.putString("userPassword", strPassword);
                            EditorUserInfo.putString("userEmail", strEmail);
                            EditorUserInfo.commit();
                            userPassword = strPassword;
                            userUsername = strUsername;
                            try {
                                FileOutputStream outputStream;
                                outputStream = openFileOutput("userUsername", Context.MODE_PRIVATE);
                                outputStream.write(strUsername.getBytes());
                                outputStream.close();

                                outputStream = openFileOutput("userPassword", Context.MODE_PRIVATE);
                                outputStream.write(strPassword.getBytes());
                                outputStream.close();

                                outputStream = openFileOutput("userEmail", Context.MODE_PRIVATE);
                                outputStream.write(strEmail.getBytes());
                                outputStream.close();
                            } catch (IOException e) {
//                                Toast.makeText(PackageActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            dialogLogin.dismiss();
                            Toast.makeText(PackageActivity.this, "you successfully logged in.", Toast.LENGTH_SHORT).show();
                            has504();
                        } else {
                            Toast.makeText(PackageActivity.this, "password is wrong, try again", Toast.LENGTH_SHORT).show();
                        }
                    } else if (canFind.equals("no such user")) {
                        Toast.makeText(PackageActivity.this, "username is wrong, try again", Toast.LENGTH_SHORT).show();

                    } else if (canFind.equals("unsuccessful")) {
                        Toast.makeText(PackageActivity.this, "process ran into a problem.", Toast.LENGTH_SHORT).show();
                    } else if (!succeed) {
                        Toast.makeText(PackageActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                    progressBar.dismiss();
                    unlockScreenOrientation();
                }

                protected void onProgressUpdate(Integer... args) {
                    if (args[0] == 0)
                        progressBar.setMessage("Matching details ...");
                }
            }
            new FtpTask(PackageActivity.this).execute();
        }
    }


    void dialogSignUp() {
        LayoutInflater inflater = this.getLayoutInflater();
        dialogSingUp = new AlertDialog.Builder(this)
                .setView(inflater.inflate(R.layout.dialog_signup, null))
                .setPositiveButton(R.string.create,
                        new Dialog.OnClickListener() {
                            public void onClick(DialogInterface d, int which) {
                            }
                        })
                .setNegativeButton(R.string.login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogLogin();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK &&
                                event.getAction() == KeyEvent.ACTION_UP &&
                                !event.isCanceled()) {
                            PackageActivity.super.onBackPressed();
                            return true;
                        }
                        return false;
                    }
                })
                .create();
        dialogSingUp.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialogSingUp.show();

        TextView tvHeader = (TextView) dialogSingUp.findViewById(R.id.tvHeader);
        tvHeader.setText("Sign Up");

        EditText etEmail = (EditText) dialogSingUp.findViewById(R.id.etEmail);
        EditText etUsername = (EditText) dialogSingUp.findViewById(R.id.etUsername);
        EditText etPassword = (EditText) dialogSingUp.findViewById(R.id.etPassword);

        etEmail.setHint("Enter your email address");
        etUsername.setHint("Enter a Username");
        etPassword.setHint("Enter a Password");

        dialogSingUp.setCanceledOnTouchOutside(false);
        Button theButton = dialogSingUp.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListenerSignUp(dialogSingUp));
    }

    class CustomListenerSignUp implements View.OnClickListener {
        private final Dialog dialog;

        public CustomListenerSignUp(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {

            class FtpTask extends AsyncTask<Void, Integer, Void> {
                EditText etEmailS = (EditText) dialogSingUp.findViewById(R.id.etEmail);
                EditText etUsernameS = (EditText) dialogSingUp.findViewById(R.id.etUsername);
                EditText etPasswordS = (EditText) dialogSingUp.findViewById(R.id.etPassword);
                String strEmail = etEmailS.getText().toString();
                String strUsername = etUsernameS.getText().toString();
                String strPassword = etPasswordS.getText().toString();

                boolean succeed = false;
                String error = "";
                ProgressDialog progressBar;
                private Context context;
                String canCreate = "unsuccessful";

                public FtpTask(Context context) {
                    this.context = context;
                }

                protected void onPreExecute() {
                    progressBar = new ProgressDialog(context);
                    progressBar.setCancelable(false);
                    progressBar.setMessage("Connecting to server ...");
                    progressBar.show();
                    lockScreenOrientation();
                }

                protected Void doInBackground(Void... args) {
                    try {
                        if (con == null) con = new FTPClient();
                        if (!con.isConnected()) con.connect(InetAddress.getByName("5.9.0.183"));

                        if (con.login("ftpUsers@khaled.ir", "8I4KJ4UeRq")) {
                            con.enterLocalPassiveMode(); // important!
                            publishProgress(0);
                            if (isValidEmail(strEmail) && strUsername.length() >= 3 && strPassword.length() >= 5 && !isIllegal(strUsername)) {
                                boolean canAdd = con.makeDirectory(s + strUsername);
                                canCreate = canAdd ? "successful" : "taken userUsername";
                                if (canAdd) {
                                    FileOutputStream outputStream;
                                    outputStream = openFileOutput("userUsername", Context.MODE_PRIVATE);
                                    outputStream.write(strUsername.getBytes());
                                    outputStream.close();

                                    outputStream = openFileOutput("userPassword", Context.MODE_PRIVATE);
                                    outputStream.write(strPassword.getBytes());
                                    outputStream.close();

                                    outputStream = openFileOutput("userEmail", Context.MODE_PRIVATE);
                                    outputStream.write(strEmail.getBytes());
                                    outputStream.close();



                                    EditorUserInfo.putString("userUsername", strUsername);
                                    EditorUserInfo.putString("userPassword", strPassword);
                                    EditorUserInfo.putString("userEmail", strEmail);
                                    EditorUserInfo.commit();

                                    FileInputStream in = openFileInput("userPassword");
                                    con.storeFile(s + strUsername + s + "userPassword", in);
                                    in.close();

                                    in = openFileInput("userEmail");
                                    con.storeFile(s + strUsername + s + "userEmail", in);
                                    in.close();


                                    succeed = true;
                                }
                            }
                        } else {
//                            Toast.makeText(Backup.this, "couldn't connect to server", Toast.LENGTH_SHORT).show();
                        }

                        con.logout();
                        con.disconnect();

                    } catch (Exception e) {
                        error = e.toString();
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(Void result) {
                    if (isValidEmail(strEmail) && strUsername.length() >= 3 && strPassword.length() >= 5 && !isIllegal(strUsername)) {
                        if (canCreate.equals("successful")) {
                            dialogSingUp.dismiss();
                            has504();
                            Toast.makeText(PackageActivity.this, "your account successfully created.", Toast.LENGTH_SHORT).show();
                        } else if (canCreate.equals("taken userUsername")) {
                            Toast.makeText(PackageActivity.this, "this userUsername is taken choose another", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!isValidEmail(strEmail)) {
                            Toast.makeText(PackageActivity.this, "please enter an valid email address.", Toast.LENGTH_SHORT).show();
                        } else if (strUsername.length() < 3) {
                            Toast.makeText(PackageActivity.this, "lowest length for username is 3", Toast.LENGTH_SHORT).show();
                        } else if (strPassword.length() < 5) {
                            Toast.makeText(PackageActivity.this, "lowest length for password is 5", Toast.LENGTH_SHORT).show();
                        } else if (isIllegal(strUsername)) {
                            Toast.makeText(PackageActivity.this, "username cant contain '/'", Toast.LENGTH_SHORT).show();
                        } else if (!succeed) {
                            Toast.makeText(PackageActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.dismiss();
                    unlockScreenOrientation();
                }

                protected void onProgressUpdate(Integer... args) {
                    if (args[0] == 0) {
                        progressBar.setMessage("Creating your account ...");
                    }


                }
            }
            new FtpTask(PackageActivity.this).execute();
        }
    }

    boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    boolean isIllegal(String string) {
        for (char c : string.toCharArray()) {
            if (c == '/') {
                return true;
            }
        }
        return false;
    }

    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);

        if (!etSearch.getText().equals(null)) {
            icicle.putString("etSearchText", etSearch.getText().toString());
        } else {
            icicle.putString("etSearchText", "");
        }

        icicle.putParcelable("listViewPosition", items.onSaveInstanceState());
        icicle.putBoolean("isFromSearch", isFromSearch);

        if (dialogMeaning.isShowing()) {
            icicle.putBoolean("dialogMeaningIsOpen", dialogMeaning.isShowing());
            icicle.putInt("dialogMeaningWordPosition", dialogMeaningWordPosition);
            icicle.putBoolean("isFromSearch", isFromSearch);
        }

        if (dialogSummery.isShowing()) {
            icicle.putBoolean("dialogSummeryIsOpen", dialogSummery.isShowing());
        }

        if (dialogAskLogin.isShowing()) {
            icicle.putBoolean("dialogAskLoginIsOpen", dialogAskLogin.isShowing());
        }

        if (dialogAskBuy.isShowing()) {
            icicle.putBoolean("dialogAskBuyIsOpen", dialogAskBuy.isShowing());
        }

        if (dialogLogin.isShowing()) {
            icicle.putBoolean("dialogLoginIsOpen", dialogLogin.isShowing());
            EditText etUsername = (EditText) dialogLogin.findViewById(R.id.etUsername);
            EditText etPassword = (EditText) dialogLogin.findViewById(R.id.etPassword);
            icicle.putString("loginUsername", etUsername.getText().toString());
            icicle.putString("loginPassword", etPassword.getText().toString());
        }

        if (dialogSingUp.isShowing()) {
            icicle.putBoolean("dialogSingUpIsOpen", dialogSingUp.isShowing());
            EditText etEmail = (EditText) dialogSingUp.findViewById(R.id.etEmail);
            EditText etUsername = (EditText) dialogSingUp.findViewById(R.id.etUsername);
            EditText etPassword = (EditText) dialogSingUp.findViewById(R.id.etPassword);

            icicle.putString("signUpEmail", etEmail.getText().toString());
            icicle.putString("signUpUsername", etUsername.getText().toString());
            icicle.putString("signUpPassword", etPassword.getText().toString());
        }

        if (markSeveral) {
            icicle.putBoolean("markSeveral", markSeveral);

            icicle.putIntegerArrayList("checkedPositionsInt", checkedPositionsInt);
        }


    }

    void restore(Bundle icicle) {
        if (icicle != null) {
            dialogMeaningIsOpen = icicle.getBoolean("dialogMeaningIsOpen");
            dialogSummeryIsOpen = icicle.getBoolean("dialogSummeryIsOpen");
            dialogAskLoginIsOpen = icicle.getBoolean("dialogAskLoginIsOpen");
            dialogAskBuyIsOpen = icicle.getBoolean("dialogAskBuyIsOpen");
            dialogLoginIsOpen = icicle.getBoolean("dialogLoginIsOpen");
            dialogSingUpIsOpen = icicle.getBoolean("dialogSingUpIsOpen");
            listViewPosition = icicle.getParcelable("listViewPosition");
            markSeveral = icicle.getBoolean("markSeveral");
            isFromSearch = icicle.getBoolean("isFromSearch");


        }

        if (dialogMeaningIsOpen) {
            refreshListViewData();
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            if (!dialogMeaning.isShowing())
                dialogMeaning(dialogMeaningWordPosition);
        }

        if (dialogSummeryIsOpen) {
            if (!dialogSummery.isShowing())
                dialogSummery();
        }

        if (dialogAskLoginIsOpen) {
            if (!dialogAskLogin.isShowing())
                dialogAskLogin();
        }

        if (dialogAskBuyIsOpen) {
            if (!dialogAskBuy.isShowing())
                dialogAskBuy();
        }

        if (dialogLoginIsOpen) {
            if (!dialogLogin.isShowing()) {
                dialogLogin();
                EditText etUsername = (EditText) dialogLogin.findViewById(R.id.etUsername);
                EditText etPassword = (EditText) dialogLogin.findViewById(R.id.etPassword);
                etUsername.setText(icicle.getString("loginUsername", ""));
                etPassword.setText(icicle.getString("loginPassword", ""));
            }
        }

        if (dialogSingUpIsOpen) {
            if (!dialogSingUp.isShowing()) {
                dialogSignUp();
                EditText etEmail = (EditText) dialogSingUp.findViewById(R.id.etEmail);
                EditText etUsername = (EditText) dialogSingUp.findViewById(R.id.etUsername);
                EditText etPassword = (EditText) dialogSingUp.findViewById(R.id.etPassword);

                etEmail.setText(icicle.getString("signUpEmail", ""));
                etUsername.setText(icicle.getString("signUpUsername", ""));
                etPassword.setText(icicle.getString("signUpPassword", ""));
            }
        }

        if (markSeveral) {
            checkedPositionsInt = icicle.getIntegerArrayList("checkedPositionsInt");
            refreshListViewData();
        }
    }

    @Override
    public void onBackPressed() {
        if (markSeveral) {
            markSeveral = false;
//            setElementsId();
            listViewPosition = items.onSaveInstanceState();
            refreshListViewData();
            clearMarks();
        } else if (isFromSearch) {
            etSearch.setText("");
            isFromSearch = false;
            listViewPosition = items.onSaveInstanceState();
            refreshListViewData();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onStop() {
        super.onStop();
//        final View view = getLayoutInflater().inflate(R.layout.row_header, items, false);
//        items.removeHeaderView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPrefs();

        if (UserInfo.getString("has504Buy", "hasBuy").equals(v.TRUE_HAS_BUY) && UserInfo.getString("has504In", "hasIn").equals(v.TRUE_HAS_IN)) {
            refreshListViewData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }

        dialogMeaning.dismiss();
        dialogSummery.dismiss();
        dialogAskLogin.dismiss();
        dialogAskBuy.dismiss();
        dialogLogin.dismiss();
        dialogSingUp.dismiss();
        progressBar.dismiss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            menu.clear();
        }
        if (markSeveral && arrayItems.size() > 0) {
            getMenuInflater().inflate(R.menu.on_delete, menu);
            MenuItem itemMarkAll = menu.findItem(R.id.action_markAll);

            boolean isAllMarked = true;
            boolean isAllUnmark = true;

            notifyCheckedPositionsInt();
            for (int i = 0; i < itemsToShow.size(); i++) {
                if (checkedPositionsInt.get(i).equals(1)) {
                    isAllMarked = false;
                }
                if (checkedPositionsInt.get(i).equals(0)) {
                    isAllUnmark = false;
                }
            }

            if ((isToMarkAll && isAllMarked) || (!isToMarkAll && isAllMarked) || (!isToMarkAll && !isAllMarked && !isAllUnmark) || isAllMarked) {
                isToMarkAll = false;
            } else if ((isToMarkAll && !isAllMarked) || (!isToMarkAll && !isAllMarked && isAllUnmark) || isAllUnmark) {
                isToMarkAll = true;
            }

            if (isToMarkAll) {
                itemMarkAll.setTitle(R.string.action_markAll);
            } else {
                itemMarkAll.setTitle(R.string.action_unmarkAll);
            }
        } else {
            getMenuInflater().inflate(R.menu.package_a, menu);
        }

//        MenuItem itemLeitner = menu.findItem(R.id.action_leitner);
//        if (itemLeitner != null) {
//            itemLeitner.setTitle("My Dictionary");
//        }

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                PackageActivity.this.startActivity(new Intent(PackageActivity.this, Preferences.class));
                return true;

            case R.id.action_close:
                this.finish();
                return true;

//            case R.id.action_count_today:
//                databasePackage.updateLastDate(todayDate);
//                databasePackage.updateLastDay(todayNum);
//                lastDate = todayDate;
//                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


}
