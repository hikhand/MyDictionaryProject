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
            etUsername = (EditText) dialog.findViewById(R.id.etUsername);
            etPassword = (EditText) dialog.findViewById(R.id.etPassword);
            String strUsername = etUsername.getText().toString();
            String strPassword = etPassword.getText().toString();

            String canFind = canFindUser(strUsername);
            if (canFind.equals("successful")) {
                if (strPassword.equals(getPasswordFromServer(strUsername))) {
                    EditorUserInfo.putString("userUsername", strUsername);
                    EditorUserInfo.putString("userPassword", strPassword);
                    EditorUserInfo.commit();

                    dialog.dismiss();
                    Toast.makeText(Backup.this, "you successfully logged in.", Toast.LENGTH_SHORT).show();
                    loggedIn();
                } else {
                    Toast.makeText(Backup.this, "your password is wrong, try again", Toast.LENGTH_SHORT).show();
                }
            } else if (canFind.equals("no such user")) {
                Toast.makeText(Backup.this, "your username is wrong, try again", Toast.LENGTH_SHORT).show();

            } else if (canFind.equals("unsuccessful")) {
                Toast.makeText(Backup.this, "process ran into a problem.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    String canFindUser(String username) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String status = "unsuccessful";
        try {
            con = new FTPClient();
            con.connect(InetAddress.getByName("ftp.khaled.ir"));

            if (con.login("windowsp", "KHaledBLack73")) {
                con.enterLocalPassiveMode(); // important!

                boolean canAdd = con.makeDirectory(File.separator+"MyDictionary"+File.separator+"backups"+File.separator+username);
                if (canAdd) {
                    con.removeDirectory(File.separator+"MyDictionary"+File.separator+"backups"+File.separator+username);
                    status = "no such user";
                } else {
                    status = "successful";
                }

                con.logout();
                con.disconnect();
            } else {
                Toast.makeText(Backup.this, "couldn't connect to server", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return status;
    }

    String getPasswordFromServer(String username) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String password = "";
        try {
            con = new FTPClient();
            con.connect(InetAddress.getByName("ftp.khaled.ir"));

            if (con.login("windowsp", "KHaledBLack73")) {
                con.enterLocalPassiveMode(); // important!

                InputStream inputStream = con.retrieveFileStream(File.separator+"MyDictionary"+File.separator+"backups"+File.separator+username+File.separator+"userPassword");

                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                password = r.readLine();

                inputStream.close();
                r.close();
                con.logout();
                con.disconnect();
            } else {
                Toast.makeText(Backup.this, "couldn't connect to server", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return password;
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
            etEmail = (EditText) dialogSingUp.findViewById(R.id.etEmail);
            etUsername = (EditText) dialogSingUp.findViewById(R.id.etUsername);
            etPassword = (EditText) dialogSingUp.findViewById(R.id.etPassword);

            String strEmail = etEmail.getText().toString();
            String strUsername = etUsername.getText().toString();
            String strPassword = etPassword.getText().toString();

            if (isValidEmail(strEmail) && strUsername.length() >= 3 && strPassword.length() >= 5 && !isIllegal(strUsername)) {
                String canCreate = canCreateUser(strUsername);
                if (canCreate.equals("successful")) {
                    FileOutputStream outputStream;
                    try {
                        outputStream = openFileOutput("userUsername", Context.MODE_PRIVATE);
                        outputStream.write(strUsername.getBytes());
                        outputStream.close();

                        outputStream = openFileOutput("userPassword", Context.MODE_PRIVATE);
                        outputStream.write(strPassword.getBytes());
                        outputStream.close();

                        outputStream = openFileOutput("userEmail", Context.MODE_PRIVATE);
                        outputStream.write(strEmail.getBytes());
                        outputStream.close();

                        if (addPasswordToServer(strUsername) && addEmailToServer(strUsername)) {
                            EditorUserInfo.putString("userUsername", strUsername);
                            EditorUserInfo.putString("userPassword", strPassword);
                            EditorUserInfo.putString("userEmail", strEmail);
                            EditorUserInfo.commit();

                            loggedIn();
                            Toast.makeText(Backup.this, "your account successfully created.", Toast.LENGTH_SHORT).show();
                            dialogSingUp.dismiss();
//                        return "finish";
                        }
                    } catch (Exception e) {
                        Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
//                    return e.toString();
                    }
                } else if (canCreate.equals("taken userUsername")) {
                    Toast.makeText(Backup.this, "this userUsername is taken choose another", Toast.LENGTH_SHORT).show();
//                return "taken userUsername";
                } else if (canCreate.equals("unsuccessful")) {
                    Toast.makeText(Backup.this, "process ran into a problem.", Toast.LENGTH_SHORT).show();
//                return "unsuccessful";
                }
            } else {
                if (!isValidEmail(strEmail)) {
                    Toast.makeText(Backup.this, "please enter an valid email address.", Toast.LENGTH_SHORT).show();
//                return "invalid email";
                } else if (strUsername.length() < 3) {
                    Toast.makeText(Backup.this, "lowest length for username is 3", Toast.LENGTH_SHORT).show();
//                return "low username";
                } else if (strPassword.length() < 5) {
                    Toast.makeText(Backup.this, "lowest length for password is 5", Toast.LENGTH_SHORT).show();
//                return "low password";
                } else if (isIllegal(strUsername)) {
                    Toast.makeText(Backup.this, "username cant contain '/'", Toast.LENGTH_SHORT).show();
//                return "illegal username";
                }
            }
//        return "";
        }
    }

    String canCreateUser(String username) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String status = "unsuccessful";
        try {
            con = new FTPClient();
            con.connect(InetAddress.getByName("ftp.khaled.ir"));

            if (con.login("windowsp", "KHaledBLack73")) {
                con.enterLocalPassiveMode(); // important!

                boolean canAdd = con.makeDirectory(File.separator+"MyDictionary"+File.separator+"backups"+File.separator+username);

                status = canAdd ? "successful" : "taken userUsername";

                con.logout();
                con.disconnect();
            } else {
                Toast.makeText(Backup.this, "couldn't connect to server", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return status;
    }

    boolean addPasswordToServer(String username) {
        boolean success = false;
        try {
            con = new FTPClient();
            con.connect(InetAddress.getByName("ftp.khaled.ir"));

            if (con.login("windowsp", "KHaledBLack73")) {

                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                FileInputStream in = openFileInput("userPassword");
                String remote = File.separator+"MyDictionary"+File.separator+"backups"+File.separator+username;
                boolean result = con.storeFile(remote+File.separator+"userPassword", in);
                in.close();
                if (result) Log.v("upload result", "succeeded");
                con.logout();
                con.disconnect();
                success = true;
            }
        } catch (Exception e) {
            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return success;
    }

    boolean addEmailToServer(String username) {
        boolean success = false;
        try {
            con = new FTPClient();
            con.connect(InetAddress.getByName("ftp.khaled.ir"));

            if (con.login("windowsp", "KHaledBLack73")) {

                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                FileInputStream in = openFileInput("userEmail");
                //
                //
                //
                //1123123
                String remote = File.separator+"MyDictionary"+File.separator+"backups"+File.separator+username;
                boolean result = con.storeFile(remote+File.separator+"userEmail", in);
                in.close();
                if (result) Log.v("upload result", "succeeded");
                con.logout();
                con.disconnect();
                success = true;
            }
        } catch (Exception e) {
            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return success;
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

    String onCLickSignUp() {
        return "";
    }

    void loggedIn() {
        setElementsId();
    }


    void setElementsValue() {

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
                    con.connect(InetAddress.getByName("ftp.khaled.ir"));

                    if (con.login("windowsp", "KHaledBLack73")) {
                        publishProgress(0);

                        con.enterLocalPassiveMode(); // important!

                        InputStream inputStream = con.retrieveFileStream(File.separator+"MyDictionary"+File.separator+"backups"+File.separator+userUsername+File.separator+"lastDateServer");

                        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                        lastDate = r.readLine();

                        inputStream.close();
                        r.close();
                        con.logout();
                        con.disconnect();
                        succeed = true;
                    } else {
                        Toast.makeText(Backup.this, "couldn't connect to server", Toast.LENGTH_SHORT).show();
                    }

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
        final Long diffMonth = diff / (60 * 60 * 1000 * 24 * 30);
        final Long diffYear = diff / (60 * 60 * 1000 * 24 * 30 * 365);


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
            Long difDay = diffDays;
            Long difHour = diffHours;
            String strDistance;

            if (diffHours > 24) {
                difHour = diffHours - 24;
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
            Long difDay = diffDays;
            Long difMonth = diffMonth;
            Long difYear = diffYear;
            String strDistance = "";

            if (difDay > 30) {
                difDay = difDay - 30;
            } {
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



    public void btnCheckLocal_Click(View view) {
        TextView tvLast = (TextView) findViewById(R.id.tvLastLocal);
        TextView tvDistance = (TextView) findViewById(R.id.tvDistanceLocal);
        String strLastDate = getLastBackupOnLocal();
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

    String getLastBackupOnLocal() {
        String lastDate = "no backup!";

        File sd = Environment.getExternalStorageDirectory();
        File dir = new File(sd, "/My Dictionary/backups/last");


        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(dir);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            lastDate = r.readLine();
            r.close();
            inputStream.close();



        } catch (FileNotFoundException e) {
//            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
//            Toast.makeText(Backup.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return lastDate;
    }


    public void btnCreateBackupOnServer_Click(View view) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
        final String currentDateAndTime = simpleDateFormat.format(new Date());

        File data = getDatabasePath("items.db");
//        File data = Environment.getDataDirectory();
        String currentDBPath = "items.db";
        final File currentDB = new File(data, "");


        class FtpTask extends AsyncTask<Void, Integer, Void> {
            boolean succeed = false;
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
                    con.connect(InetAddress.getByName("ftp.khaled.ir"));

                    if (con.login("windowsp", "KHaledBLack73")) {
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

                        in = openFileInput("lastDateServer");
                        remote = File.separator+"MyDictionary"+File.separator+"backups"+File.separator+userUsername;
                        con.storeFile(remote+File.separator+"lastDateServer", in);

                        in.close();
                        if (result) Log.v("upload result", "succeeded");
                        con.logout();
                        con.disconnect();
                        succeed = true;
                    }
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



    void backupOnSdCard() {
        try {
            File sd = Environment.getExternalStorageDirectory();
//            File data = Environment.getDataDirectory();
            File data = getDatabasePath("items.db");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            String currentDateAndTime = simpleDateFormat.format(new Date());

            if (sd.canWrite()) {
                String currentDBPath = "items.db";
                String backupDBPath = "//My Dictionary//backups//items " + currentDateAndTime + ".db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "My Dictionary" + File.separator + "backups");
                directory.mkdirs();

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    public void upload() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.toString());

            con = new FTPClient();
            con.connect(InetAddress.getByName("ftp.khaled.ir"));

            if (con.login("windowsp", "KHaledBLack73")) {

                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);
                String data = "/sdcard/vivekm4a.m4a";

                FileInputStream in = new FileInputStream(new File(data));
                boolean result = con.storeFile("/vivekm4a.m4a", in);
                in.close();
                if (result) Log.v("upload result", "succeeded");
                con.logout();
                con.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        TelephonyManager tm = (TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//        final String DeviceId, SerialNum, androidId;
//        DeviceId = tm.getDeviceId();
//        SerialNum = tm.getSimSerialNumber();
//        androidId = Secure.getString(getContentResolver(),Secure.ANDROID_ID);
//
//        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)DeviceId.hashCode() << 32) | SerialNum.hashCode());
//        String mydeviceId = deviceUuid.toString();
//        Log.v("My Id", "Android DeviceId is: " +DeviceId);
//        Log.v("My Id", "Android SerialNum is: " +SerialNum);
//        Log.v("My Id", "Android androidId is: " +androidId);
//        Log.v("My Id", "Android androidId is: " +mydeviceId);
    }


    void progress() {
//        progressBar = new ProgressDialog(this);
//        progressBar.setCancelable(true);
//        progressBar.setMessage("Connecting to server ...");
//        progressBar.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.backup, menu);
        return true;
    }

}
