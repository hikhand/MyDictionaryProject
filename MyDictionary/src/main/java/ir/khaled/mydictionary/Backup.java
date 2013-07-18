package ir.khaled.mydictionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Backup extends Activity {
    SharedPreferences UserInfo;
    SharedPreferences.Editor EditorUserInfo;

    AlertDialog dialogLogin;
    AlertDialog dialogSingUp;
    AlertDialog dialogAskLogin;
    EditText etEmail;
    EditText etUsername;
    EditText etPassword;
    TextView tvUsername;

    FTPClient con;

    String userEmail = "";
    String userUsername = "";
    String userPassword = "";

    String result;


    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);

        setElementsId();

        if (userUsername.equals("")) {
            dialogAskLogin();
        } else {
            setElementsValue();
        }
    }

    void setElementsId() {
        dialogLogin = new AlertDialog.Builder(this).create();
        dialogSingUp = new AlertDialog.Builder(this).create();
        dialogAskLogin = new AlertDialog.Builder(this).create();
        UserInfo = getSharedPreferences("userInfo", 0);
        EditorUserInfo = UserInfo.edit();

        userUsername = UserInfo.getString("userUsername", "");
        userPassword = UserInfo.getString("userPassword", "");
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvUsername.setText("Logged in as: " + userUsername);
    }

    void setElementsValue() {
        TextView tvLastDateServer = (TextView) findViewById(R.id.tvLastServer);
        TextView tvDistanceServer = (TextView) findViewById(R.id.tvDistanceServer);

        tvLastDateServer.setText(tvLastDateServer.getText().toString() + UserInfo.getString("lastDateServer", ""));
        tvDistanceServer.setText(tvDistanceServer.getText().toString() + getDistance(UserInfo.getString("lastDateServer", "")));

        getLastBackupDateOnLocal();

    }

    void loggedIn() {
        setElementsId();
    }


    void dialogAskLogin() {
        dialogAskLogin = new AlertDialog.Builder(this)
                .setMessage("to create backup you need to login or create an account.")
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
                            Backup.super.onBackPressed();
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
                            Backup.super.onBackPressed();
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

        etEmail = (EditText) dialogLogin.findViewById(R.id.etEmail);
        etUsername = (EditText) dialogLogin.findViewById(R.id.etUsername);
        etPassword = (EditText) dialogLogin.findViewById(R.id.etPassword);

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

                public FtpTask(Context context) { this.context = context; }

                protected void onPreExecute()
                {
                    progressBar = new ProgressDialog(context);
                    progressBar.setCancelable(false);
                    progressBar.setMessage("Connecting to server ...");
                    progressBar.show();
                }

                protected Void doInBackground(Void... args) {
                    try {
                        con = new FTPClient();
                        con.connect(InetAddress.getByName("5.9.0.183"));

                        if (con.login("windowsp", "KHaledBLack73")) {
                            con.enterLocalPassiveMode(); // important!

                            publishProgress(0);
                            boolean canAdd = con.makeDirectory(File.separator+"MyDictionary"+File.separator+"backups"+File.separator+strUsername);
                            if (canAdd) {
                                con.removeDirectory(File.separator + "MyDictionary" + File.separator + "backups" + File.separator + strUsername);
                                canFind = "no such user";
                            } else {
                                canFind = "successful";

                                InputStream inputStream = con.retrieveFileStream(File.separator + "MyDictionary" + File.separator + "backups" + File.separator + strUsername + File.separator + "userPassword");
                                con.completePendingCommand();
                                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                                password = r.readLine();
                                inputStream.close();
                                r.close();

                                inputStream = con.retrieveFileStream(File.separator + "MyDictionary" + File.separator + "backups" + File.separator + strUsername + File.separator + "userEmail");
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
//                                Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            dialogLogin.dismiss();
                            Toast.makeText(Backup.this, "you successfully logged in.", Toast.LENGTH_SHORT).show();
                            loggedIn();
                        } else {
                            Toast.makeText(Backup.this, "password is wrong, try again", Toast.LENGTH_SHORT).show();
                        }
                    } else if (canFind.equals("no such user")) {
                        Toast.makeText(Backup.this, "username is wrong, try again", Toast.LENGTH_SHORT).show();

                    } else if (canFind.equals("unsuccessful")) {
                        Toast.makeText(Backup.this, "process ran into a problem.", Toast.LENGTH_SHORT).show();
                    } else if (!succeed) {
                        Toast.makeText(Backup.this, error, Toast.LENGTH_SHORT).show();
                    }
                    progressBar.dismiss();
                }

                protected void onProgressUpdate(Integer... args) {
                    if (args[0] == 0)
                        progressBar.setMessage("Matching details ...");
                }
            }
            new FtpTask(Backup.this).execute();
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
                            Backup.super.onBackPressed();
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

        etEmail = (EditText) dialogSingUp.findViewById(R.id.etEmail);
        etUsername = (EditText) dialogSingUp.findViewById(R.id.etUsername);
        etPassword = (EditText) dialogSingUp.findViewById(R.id.etPassword);

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

                public FtpTask(Context context) { this.context = context; }

                protected void onPreExecute()
                {
                    progressBar = new ProgressDialog(context);
                    progressBar.setCancelable(false);
                    progressBar.setMessage("Connecting to server ...");
                    progressBar.show();
                }

                protected Void doInBackground(Void... args) {
                    try {
                        con = new FTPClient();
                        con.connect(InetAddress.getByName("5.9.0.183"));
                        if (con.login("windowsp", "KHaledBLack73")) {
                            con.enterLocalPassiveMode(); // important!
                            publishProgress(0);
                            if (isValidEmail(strEmail) && strUsername.length() >= 3 && strPassword.length() >= 5 && !isIllegal(strUsername)) {
                                boolean canAdd = con.makeDirectory(File.separator + "MyDictionary" + File.separator + "backups" + File.separator + strUsername);
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
                                    String remote = File.separator + "MyDictionary" + File.separator + "backups" + File.separator + strUsername;
                                    con.storeFile(remote + File.separator + "userPassword", in);
                                    in.close();

                                    in = openFileInput("userEmail");
                                    remote = File.separator + "MyDictionary" + File.separator + "backups" + File.separator + strUsername;
                                    con.storeFile(remote + File.separator + "userEmail", in);
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
                            loggedIn();
                            Toast.makeText(Backup.this, "your account successfully created.", Toast.LENGTH_SHORT).show();
                        } else if (canCreate.equals("taken userUsername")) {
                            Toast.makeText(Backup.this, "this userUsername is taken choose another", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!isValidEmail(strEmail)) {
                            Toast.makeText(Backup.this, "please enter an valid email address.", Toast.LENGTH_SHORT).show();
                        } else if (strUsername.length() < 3) {
                            Toast.makeText(Backup.this, "lowest length for username is 3", Toast.LENGTH_SHORT).show();
                        } else if (strPassword.length() < 5) {
                            Toast.makeText(Backup.this, "lowest length for password is 5", Toast.LENGTH_SHORT).show();
                        } else if (isIllegal(strUsername)) {
                            Toast.makeText(Backup.this, "username cant contain '/'", Toast.LENGTH_SHORT).show();
                        } else if (!succeed) {
                            Toast.makeText(Backup.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.dismiss();
                }

                protected void onProgressUpdate(Integer... args) {
                    if (args[0] == 0) {
                        progressBar.setMessage("Creating your account ...");
                    }


                }
            }
            new FtpTask(Backup.this).execute();
        }
    }

    boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    boolean isIllegal (String string) {
        for (char c : string.toCharArray()) {
            if (c == '/') {
                return true;
            }
        }
        return false;
    }







    public void btnCheckServer_Click(View view) {
        class FtpTask extends AsyncTask<Void, Integer, Void> {
            boolean succeed = false;
            String error = "";
            ProgressDialog progressBar;
            private Context context;
            String lastDate = "no backup!";

            public FtpTask(Context context) { this.context = context; }

            protected void onPreExecute()
            {
                progressBar = new ProgressDialog(context);
                progressBar.setCancelable(false);
                progressBar.setMessage("Connecting to server ...");
                progressBar.show();
            }

            protected Void doInBackground(Void... args) {
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("windowsp", "KHaledBLack73")) {
                        publishProgress(0);
                        con.enterLocalPassiveMode(); // important!

                        InputStream inputStream = con.retrieveFileStream(File.separator+"MyDictionary"+File.separator+"backups"+File.separator+userUsername+File.separator+"lastDateServer");
                        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                        lastDate = r.readLine();
                        inputStream.close();
                        r.close();
                        succeed = true;
                    } else {
                        Toast.makeText(Backup.this, "couldn't connect to server", Toast.LENGTH_SHORT).show();
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
                Log.v("FTPTask","FTP connection complete");
                if (succeed) {
//                    Toast.makeText(Backup.this, "the operation was completed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Backup.this, error, Toast.LENGTH_SHORT).show();
                }
                progressBar.dismiss();

                TextView tvLast = (TextView) Backup.this.findViewById(R.id.tvLastServer);
                TextView tvDistance = (TextView) Backup.this.findViewById(R.id.tvDistanceServer);
                String strLastDate = lastDate;
                String distance;

                tvLast.setText("Last backup: ");
                tvDistance.setText("Distance: ");

                tvLast.setText(tvLast.getText().toString() + strLastDate);
                if (!(strLastDate.equals("no backup!"))) {
                    distance = getDistance(strLastDate);
                    tvDistance.setText(tvDistance.getText().toString() + distance);
                } else {
                    tvDistance.setText(tvDistance.getText().toString() + "-");
                }
            }

            protected void onProgressUpdate(Integer... args) {
                if (args[0] == 0)
                    progressBar.setMessage("getting data from server ...");
            }

        }
        new FtpTask(this).execute();
    }

    String getDistance(String lastDate) {
        if (lastDate != "") {
            boolean thisHour = false;
            boolean today = false;
            boolean thisMonth = false;
            boolean thisYear = false;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            String currentDateAndTime = simpleDateFormat.format(new Date());

            Date d1 = null;
            Date d2 = null;
            try {
                d1 = simpleDateFormat.parse(lastDate);
                d2 = simpleDateFormat.parse(currentDateAndTime);
            } catch (ParseException e) {
                e.printStackTrace();
                return "wrong date";
            }

            final long diff = d2.getTime() - d1.getTime();
            final long diffSeconds = diff / 1000;
            final long diffMinutes = diff / (60 * 1000);
            final long diffHours = diff / (60 * 60 * 1000);
            final long diffDays = diff / (60 * 60 * 1000 * 24);
            final long diffMonth = diff / (60 * 60 * 1000 * 24 * 30);
            final long diffYear = diff / (60 * 60 * 1000 * 24 * 30 * 365);


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

            } else if (thisYear) {
                long difDay = diffDays;
                long difMonth = diffMonth;
                long difYear = diffYear;
                String strDistance = "";

                if (difDay > 30) {
                    difDay = difDay - 30;
                }
                {
                    difMonth--;
                    difDay = (difDay + 30) - difDay;
                }
                if (difMonth > 12) {
                    difMonth = difMonth - 12;
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
            return "nothing";
        }
        return "";
    }







    public void btnCheckLocal_Click(View view) {
        getLastBackupDateOnLocal();
    }

    void getLastBackupDateOnLocal() {

        TextView tvLast = (TextView) findViewById(R.id.tvLastLocal);
        TextView tvDistance = (TextView) findViewById(R.id.tvDistanceLocal);
        String strLastDate = "-";
        String distance = "-";

        tvLast.setText("Last backup: ");
        tvDistance.setText("Distance: ");

        File sd = Environment.getExternalStorageDirectory();
        File dir = new File(sd, "/My Dictionary/backups/lastDateLocal");

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(dir);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            strLastDate = r.readLine();
            r.close();
            inputStream.close();


        } catch (FileNotFoundException e) {
//            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
//            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        tvLast.setText(tvLast.getText().toString() + strLastDate);
        if (!(strLastDate.equals("no backup!"))) {
            distance = getDistance(strLastDate);
            tvDistance.setText(tvDistance.getText().toString() + distance);
        } else {
            tvDistance.setText(tvDistance.getText().toString() + "-");
        }



    }





    public void btnCreateBackupOnServer_Click(View view) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
        final String currentDateAndTime = simpleDateFormat.format(new Date());

        File data = getDatabasePath("items.db");
        final File currentDB = new File(data, "");


        class FtpTask extends AsyncTask<Void, Integer, Void> {
            boolean succeed = false;
            String errorS= "";
            String error = "";
            ProgressDialog progressBar;
            private Context context;

            public FtpTask(Context context) { this.context = context; }

            protected void onPreExecute()
            {
                progressBar = new ProgressDialog(context);
                progressBar.setCancelable(false);
                progressBar.setMessage("Connecting to server ...");
                progressBar.show();
            }

            protected Void doInBackground(Void... args) {
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("windowsp", "KHaledBLack73") && currentDB.exists()) {
                        con.enterLocalPassiveMode(); // important!
                        con.setFileType(FTP.BINARY_FILE_TYPE);
                        FileInputStream in = new FileInputStream(currentDB);

//                        progressBar.setMessage("uploading to server ...");

                        publishProgress(0);

                        String remote = File.separator+"MyDictionary"+File.separator+"backups"+File.separator+userUsername;
                        boolean result = con.storeFile(remote+File.separator+"items " + currentDateAndTime + ".db", in);

                        FileOutputStream outputStream;
                        outputStream = openFileOutput("lastDateServer", Context.MODE_PRIVATE);
                        outputStream.write(currentDateAndTime.getBytes());
                        outputStream.close();

                        File databasePath = Backup.this.getDatabasePath("items.db");
                        String path = databasePath.getAbsolutePath();
                        outputStream = openFileOutput("databasePathServer", Context.MODE_PRIVATE);
                        outputStream.write(path.getBytes());
                        outputStream.close();


                        in = openFileInput("lastDateServer");
                        remote = File.separator+"MyDictionary"+File.separator+"backups"+File.separator+userUsername;
                        con.storeFile(remote+File.separator+"lastDateServer", in);
                        in.close();

                        in = openFileInput("databasePathServer");
                        remote = File.separator + "MyDictionary" + File.separator + "backups" + File.separator + userUsername;
                        con.storeFile(remote + File.separator + "databasePath", in);
                        in.close();


                        EditorUserInfo.putString("databasePathServer", path);
                        EditorUserInfo.putString("lastDateServer", currentDateAndTime);
                        EditorUserInfo.commit();

                        succeed = true;
                    } else if (!currentDB.exists()) {
                        errorS = "there is nothing in database to create backup";
                    }

                    con.logout();
                    con.disconnect();

                } catch (Exception e) {
                    error = e.toString();
//                    Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                Log.v("FTPTask","FTP connection complete");
                if (succeed) {
                    Toast.makeText(Backup.this, "the operation was completed successfully", Toast.LENGTH_SHORT).show();
                } else if (!errorS.equals("")) {
                    Toast.makeText(Backup.this, errorS, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Backup.this, error, Toast.LENGTH_SHORT).show();
                }
                progressBar.dismiss();
                //Where ftpClient is a instance variable in the main activity
            }

            protected void onProgressUpdate(Integer... args) {
                if (args[0] == 0)
                progressBar.setMessage("Uploading to server ...");
            }

        }
        new FtpTask(this).execute();
    }

    public void btnRestoreBackupFromServer_Click(View view) {
        class FtpTask extends AsyncTask<Void, Integer, Void> {
            boolean succeed = false;
            String error = "";
            String errorS = "";
            ProgressDialog progressBar;
            private Context context;
            String s = File.separator;

            String date = "";
            String databasePath = "";

            public FtpTask(Context context) { this.context = context; }

            protected void onPreExecute()
            {
                progressBar = new ProgressDialog(context);
                progressBar.setCancelable(false);
                progressBar.setMessage("Connecting to server ...");
                progressBar.show();
            }

            protected Void doInBackground(Void... args) {
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("windowsp", "KHaledBLack73")) {
                        con.enterLocalPassiveMode(); // important!

                        publishProgress(0);
                        InputStream inputStream = con.retrieveFileStream(s + "MyDictionary" + s + "backups" + s + userUsername + s + "lastDateServer");
                        BufferedReader r;
                        if (inputStream != null) {
                            r = new BufferedReader(new InputStreamReader(inputStream));
                            date = r.readLine();
                            inputStream.close();
                            con.completePendingCommand();
                            r.close();

                            publishProgress(1);

                            inputStream = con.retrieveFileStream(s + "MyDictionary" + s + "backups" + s + userUsername + s + "databasePath");
                            r = new BufferedReader(new InputStreamReader(inputStream));
                            databasePath = r.readLine();
                            inputStream.close();
                            r.close();
                            con.completePendingCommand();

                            File databasePath = getDatabasePath("items.db");
                            if (databasePath.exists()) {
                                File currentDB = new File(databasePath, "");
                                currentDB.delete();
                            }

                            FileOutputStream outBackup = new FileOutputStream(this.databasePath);
                            con.retrieveFile(s + "MyDictionary" + s + "backups" + s + userUsername + s + "items " + date + ".db", outBackup);

                            succeed = true;
                        } else {
                            succeed = false;
                            errorS = "there is no backup on the server!";
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
                Log.v("FTPTask", "FTP connection complete");
                if (succeed) {
                    EditorUserInfo.putString("lastDateServer", date);
                    EditorUserInfo.commit();
                } else if (!errorS.equals("")) {
                    Toast.makeText(Backup.this, errorS, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Backup.this, error, Toast.LENGTH_SHORT).show();
                }
                progressBar.dismiss();
            }

            protected void onProgressUpdate(Integer... args) {
                if (args[0] == 0) {
                    progressBar.setMessage("looking for backup ...");
                } else if (args[0] == 1) {
                    progressBar.setMessage("Restoring backup...");
                }
            }
        }
        new FtpTask(Backup.this).execute();
    }






    public void btnCreateLocalBackup_Click(View view) {
        String error = "";
        String errorS= "";
        try {
            File sd = Environment.getExternalStorageDirectory();
            File databasePath = getDatabasePath("items.db");
            String s = File.separator;
            String backupPath = Environment.getExternalStorageDirectory() + s + "My Dictionary" + s + "backups" + s;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            String currentDateAndTime = simpleDateFormat.format(new Date());

            File currentDB = new File(databasePath, "");
            if (sd.canWrite() && currentDB.exists() && new File(backupPath).exists()) {
                File directory = new File(backupPath);
                directory.mkdirs();

                FileChannel src = new FileInputStream(databasePath).getChannel();
                FileChannel dst = new FileOutputStream(backupPath + "items " + currentDateAndTime + ".db").getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                FileOutputStream outputStream;
                outputStream = openFileOutput("lastDateLocal", Context.MODE_PRIVATE);
                outputStream.write(currentDateAndTime.getBytes());
                outputStream.close();

                outputStream = openFileOutput("databasePathLocal", Context.MODE_PRIVATE);
                outputStream.write(databasePath.getAbsolutePath().getBytes());
                outputStream.close();


                outputStream = new FileOutputStream(backupPath + "lastDateLocal");
                outputStream.write(currentDateAndTime.getBytes());
                outputStream.close();

                outputStream = new FileOutputStream(backupPath + "databasePathLocal");
                outputStream.write(databasePath.getAbsolutePath().getBytes());
                outputStream.close();

            } else {
                errorS = "Can't access sd card";
            }
        } catch (Exception e) {
            error = e.toString();
//            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

        final String errorF = error;
        final String errorSF= errorS;
        final ProgressDialog progressDialog = new ProgressDialog(Backup.this);
        progressDialog.setMessage("Creating backup ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        class WaitTime extends AsyncTask<Void, Integer, Void> {
            protected Void doInBackground(Void... args) {
                long delayInMillis = 2500;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                    }
                }, delayInMillis);
                return null;
            }

            protected void onPostExecute(Void result) {
                if (errorSF.equals("")) {
                    Toast.makeText(getBaseContext(), "the operation was completed successfully", Toast.LENGTH_SHORT).show();
                }else if (!errorSF.equals("")) {
                    Toast.makeText(getBaseContext(), errorSF, Toast.LENGTH_SHORT).show();
                } else if (!errorF.equals("")) {
                    Toast.makeText(getBaseContext(), errorF, Toast.LENGTH_SHORT).show();
                }
            }

        }
        new WaitTime().execute();
    }

    public void btnRestoreLocalBackup_Click(View view) {
        String error = "";
        String errorS= "";
        String date = "";
        String dataPath = "";
        String s = File.separator;
        String backupPath = Environment.getExternalStorageDirectory() + s + "My Dictionary" + s + "backups" + s;

        try {
            InputStream inputStream;
            BufferedReader r;
            if (Environment.getExternalStorageDirectory().canWrite() && new File(backupPath).exists()) {
                inputStream = new FileInputStream(backupPath + "lastDateLocal");
                r = new BufferedReader(new InputStreamReader(inputStream));
                date = r.readLine();
                inputStream.close();
                r.close();

                inputStream = new FileInputStream(backupPath + "databasePathLocal");
                r = new BufferedReader(new InputStreamReader(inputStream));
                dataPath = r.readLine();
                inputStream.close();
                r.close();

                File databasePath = getDatabasePath("items.db");
                if (databasePath.exists()) {
                    File currentDB = new File(databasePath, "");
                    currentDB.delete();
                }

                FileChannel src = new FileInputStream(backupPath + "items " + date + ".db").getChannel();
                FileChannel dst = new FileOutputStream(dataPath).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            } else {
                errorS = "Can't access sd card";
//                Toast.makeText(Backup.this, "can't access sd card", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            error = e.toString();
        }

        final String errorF = error;
        final String errorSF= errorS;
        final ProgressDialog progressDialog = new ProgressDialog(Backup.this);
        progressDialog.setMessage("Restoring backup ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();


        class WaitTime extends AsyncTask<Void, Integer, Void> {
            protected Void doInBackground(Void... args) {
                long delayInMillis = 2500;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, delayInMillis);
                return null;
            }

            protected void onPostExecute(Void result) {
                if (errorSF.equals("")) {
                    Toast.makeText(getBaseContext(), "the operation was completed successfully", Toast.LENGTH_SHORT).show();
                }else if (!errorSF.equals("")) {
                    Toast.makeText(getBaseContext(), errorSF, Toast.LENGTH_SHORT).show();
                } else if (!errorF.equals("")) {
                    Toast.makeText(getBaseContext(), errorF, Toast.LENGTH_SHORT).show();
                }
            }

        }
        new WaitTime().execute();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.backup, menu);
//        return true;
//    }

}
