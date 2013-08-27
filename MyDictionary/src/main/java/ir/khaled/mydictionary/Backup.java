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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

    String s = File.separator;

    Names v;

    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);

        v= new Names();

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
        tvLastDateServer.setText("Last Backup: " + UserInfo.getString("lastDateServer", ""));
        tvDistanceServer.setText("Distance: " + getDistance(UserInfo.getString("lastDateServer", "")));

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

                    if (con.login("ftpUsers@khaled.ir", "8I4KJ4UeRq")) {
                        publishProgress(0);
                        con.enterLocalPassiveMode(); // important!

                        InputStream inputStream = con.retrieveFileStream(s + userUsername + v.fileLastDateServer);
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


    String getDistance(String date) {
        if (!date.equals("")) {
            boolean thisHour = false;
            boolean today = false;
            boolean thisMonth = false;
            boolean thisYear = false;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
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
            final long diffDays = diffHours /  24;
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

        final File currentDBMain = new File(getDatabasePath("items.db"), "");
        final File currentDBLeitner = new File(getDatabasePath("leitner.db"), "");


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
                    if (con == null) con = new FTPClient();
                    if (!con.isConnected()) con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("ftpUsers@khaled.ir", "8I4KJ4UeRq") && currentDBMain.exists()) {
                        con.enterLocalPassiveMode(); // important!
                        con.setFileType(FTP.BINARY_FILE_TYPE);
//                        FileInputStream inMain;

                        publishProgress(0);

                        con.makeDirectory(s + userUsername + s + "mydictionary");
                        con.makeDirectory(s + userUsername + s + "mydictionary" + s + "backups");

                        con.storeFile(s + userUsername + v.rootBackups + "items " + currentDateAndTime + ".db", new FileInputStream(currentDBMain));
                        con.storeFile(s + userUsername + v.rootBackups + "leitner " + currentDateAndTime + ".db", new FileInputStream(currentDBLeitner));

                        FileOutputStream outputStream;

                        outputStream = openFileOutput("lastDateServer", Context.MODE_PRIVATE);
                        outputStream.write(currentDateAndTime.getBytes());
                        outputStream.close();

                        File databasePathMain = Backup.this.getDatabasePath("items.db");
                        outputStream = openFileOutput("pathMainServer", Context.MODE_PRIVATE);
                        outputStream.write(databasePathMain.getAbsolutePath().getBytes());
                        outputStream.close();

                        File databasePathLeitner = Backup.this.getDatabasePath("leitner.db");
                        outputStream = openFileOutput("pathLeitnerServer", Context.MODE_PRIVATE);
                        outputStream.write(databasePathLeitner.getAbsolutePath().getBytes());
                        outputStream.close();


                        con.storeFile(s + userUsername + v.rootBackups + "lastDateServer", openFileInput("lastDateServer"));

                        con.storeFile(s + userUsername + v.rootBackups + "pathMainServer", openFileInput("pathMainServer"));
                        con.storeFile(s + userUsername + v.rootBackups + "pathLeitnerServer", openFileInput("pathLeitnerServer"));

                        File dbPackage504 = getDatabasePath(v.DATABASE_PACKAGE504);
                        if (dbPackage504.exists()) {

                            File currentDbPackage504 = new File(getDatabasePath(v.DATABASE_PACKAGE504), "");
                            con.storeFile(s + userUsername + v.rootBackups + "package504 " + currentDateAndTime + ".db", new FileInputStream(currentDbPackage504));

                            File databasePathPackage504 = Backup.this.getDatabasePath(v.DATABASE_PACKAGE504);
                            outputStream = openFileOutput("pathPackage504Server", Context.MODE_PRIVATE);
                            outputStream.write(databasePathPackage504.getAbsolutePath().getBytes());
                            outputStream.close();

                            con.storeFile(s + userUsername + v.rootBackups + "pathPackage504Server", openFileInput("pathPackage504Server"));

                            EditorUserInfo.putString("pathPackage504Server", databasePathLeitner.getAbsolutePath());

                        }


                        EditorUserInfo.putString("pathMainServer", databasePathMain.getAbsolutePath());
                        EditorUserInfo.putString("pathLeitnerServer", databasePathLeitner.getAbsolutePath());
                        EditorUserInfo.putString("lastDateServer", currentDateAndTime);
                        EditorUserInfo.commit();

                        succeed = true;
                    } else if (!currentDBMain.exists()) {
                        errorS = "there is nothing in databaseMain to create backup";
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
                    Toast.makeText(Backup.this, "backup successfully created.", Toast.LENGTH_SHORT).show();

                    TextView tvLastDateServer = (TextView) findViewById(R.id.tvLastServer);
                    TextView tvDistanceServer = (TextView) findViewById(R.id.tvDistanceServer);
                    tvLastDateServer.setText("Last Backup: " +  UserInfo.getString("lastDateServer", ""));
                    tvDistanceServer.setText("Distance: " + getDistance(UserInfo.getString("lastDateServer", "")));
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

            String date = "";
            String pathMain = "";
            String pathLeitner = "";
            String pathPackage504 = "";

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

                    if (con.login("ftpUsers@khaled.ir", "8I4KJ4UeRq")) {
                        con.enterLocalPassiveMode(); // important!

                        publishProgress(0);

                        InputStream inputStream = con.retrieveFileStream(s + userUsername + v.rootBackups + "lastDateServer");
                        BufferedReader r;
                        if (inputStream != null) {
                            r = new BufferedReader(new InputStreamReader(inputStream));
                            date = r.readLine();
                            inputStream.close();
                            con.completePendingCommand();
                            r.close();

                            publishProgress(1);

                            File dbPackage504 = getDatabasePath(v.DATABASE_PACKAGE504);
                            if (dbPackage504.exists()) {
                                inputStream = con.retrieveFileStream(s + userUsername + v.rootBackups + "pathPackage504Server");
                                if (inputStream != null) {
                                    r = new BufferedReader(new InputStreamReader(inputStream));
                                    pathPackage504 = r.readLine();
                                    inputStream.close();
                                    r.close();
                                    con.completePendingCommand();
                                    pathPackage504 = pathPackage504.replace("com.hister.mydictionary", "ir.khaled.mydictionary");

                                    dbPackage504.delete();

                                    FileOutputStream outBackup = new FileOutputStream(this.pathPackage504);
                                    con.retrieveFile(s + userUsername + v.rootBackups + "package504 " + date + ".db", outBackup);
                                }
                            }


                            inputStream = con.retrieveFileStream(s + userUsername + v.rootBackups + "pathMainServer");
                            r = new BufferedReader(new InputStreamReader(inputStream));
                            pathMain = r.readLine();
                            inputStream.close();
                            r.close();
                            con.completePendingCommand();
                            pathMain = pathMain.replace("com.hister.mydictionary", "ir.khaled.mydictionary");

                            inputStream = con.retrieveFileStream(s + userUsername + v.rootBackups + "pathLeitnerServer");
                            r = new BufferedReader(new InputStreamReader(inputStream));
                            pathLeitner = r.readLine();
                            inputStream.close();
                            r.close();
                            con.completePendingCommand();
                            pathLeitner = pathLeitner.replace("com.hister.mydictionary", "ir.khaled.mydictionary");

                            File pathMain = getDatabasePath("items.db");
                            if (pathMain.exists()) {
                                File currentDB = new File(pathMain, "");
                                currentDB.delete();
                            }

                            File pathLeitner = getDatabasePath("leitner.db");
                            if (pathLeitner.exists()) {
                                File currentDB = new File(pathLeitner, "");
                                currentDB.delete();
                            }


                            FileOutputStream outBackup = new FileOutputStream(this.pathMain);
                            con.retrieveFile(s + userUsername + v.rootBackups + "items " + date + ".db", outBackup);

                             outBackup = new FileOutputStream(this.pathLeitner);
                            con.retrieveFile(s + userUsername + v.rootBackups + "leitner " + date + ".db", outBackup);

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
                    TextView tvLastDateServer = (TextView) findViewById(R.id.tvLastServer);
                    TextView tvDistanceServer = (TextView) findViewById(R.id.tvDistanceServer);
                    tvLastDateServer.setText("Last backup: " + UserInfo.getString("lastDateServer", ""));
                    tvDistanceServer.setText("Distance: " + getDistance(UserInfo.getString("lastDateServer", "")));
                    Toast.makeText(Backup.this, "successfully restored.", Toast.LENGTH_SHORT).show();
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
            File pathMain = getDatabasePath("items.db");
            File pathLeitner = getDatabasePath("leitner.db");
            String backupPath = Environment.getExternalStorageDirectory() + s + "My Dictionary" + s + "backups" + s;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            String currentDateAndTime = simpleDateFormat.format(new Date());

            File directory = new File(backupPath);
            directory.mkdirs();

            if (sd.canWrite() && new File(pathMain, "").exists() && new File(backupPath).exists()) {
                FileChannel src = new FileInputStream(pathMain).getChannel();
                FileChannel dst = new FileOutputStream(backupPath + "items " + currentDateAndTime + ".db").getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                src = new FileInputStream(pathLeitner).getChannel();
                dst = new FileOutputStream(backupPath + "leitner " + currentDateAndTime + ".db").getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                File dbPackage504 = getDatabasePath(v.DATABASE_PACKAGE504);
                if (dbPackage504.exists()) {
                    src = new FileInputStream(dbPackage504).getChannel();
                    dst = new FileOutputStream(backupPath + "package504 " + currentDateAndTime + ".db").getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    FileOutputStream outputStream;
                    outputStream = openFileOutput("pathPackage504Local", Context.MODE_PRIVATE);
                    outputStream.write(dbPackage504.getAbsolutePath().getBytes());
                    outputStream.close();


                    outputStream = new FileOutputStream(backupPath + "pathPackage504Local");
                    outputStream.write(dbPackage504.getAbsolutePath().getBytes());
                    outputStream.close();
                }

                FileOutputStream outputStream;
                outputStream = openFileOutput("lastDateLocal", Context.MODE_PRIVATE);
                outputStream.write(currentDateAndTime.getBytes());
                outputStream.close();

                outputStream = openFileOutput("pathMainLocal", Context.MODE_PRIVATE);
                outputStream.write(pathMain.getAbsolutePath().getBytes());
                outputStream.close();

                outputStream = openFileOutput("pathLeitnerLocal", Context.MODE_PRIVATE);
                outputStream.write(pathLeitner.getAbsolutePath().getBytes());
                outputStream.close();

                outputStream = new FileOutputStream(backupPath + "lastDateLocal");
                outputStream.write(currentDateAndTime.getBytes());
                outputStream.close();

                outputStream = new FileOutputStream(backupPath + "pathMainLocal");
                outputStream.write(pathMain.getAbsolutePath().getBytes());
                outputStream.close();

                outputStream = new FileOutputStream(backupPath + "pathLeitnerLocal");
                outputStream.write(pathLeitner.getAbsolutePath().getBytes());
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
                long delayInMillis = 1500;
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

        getLastBackupDateOnLocal();
    }

    public void btnRestoreLocalBackup_Click(View view) {
        String error = "";
        String errorS= "";
        String date = "";
        String pathMain = "";
        String pathLeitner = "";
        String pathPackage504 = "";
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

                File dbPackage504 = getDatabasePath(v.DATABASE_PACKAGE504);
                if (dbPackage504.exists()) {
                    inputStream = new FileInputStream(backupPath + "pathPackage504Local");
                    if (inputStream != null) {
                        r = new BufferedReader(new InputStreamReader(inputStream));
                        pathPackage504 = r.readLine();
                        inputStream.close();
                        r.close();
                        pathPackage504 = pathPackage504.replace("com.hister.mydictionary", "ir.khaled.mydictionary");


                        File currentDB = new File(dbPackage504, "");
                        currentDB.delete();


                        FileChannel src = new FileInputStream(backupPath + "package504 " + date + ".db").getChannel();
                        FileChannel dst = new FileOutputStream(pathPackage504).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }
                }

                inputStream = new FileInputStream(backupPath + "pathMainLocal");
                r = new BufferedReader(new InputStreamReader(inputStream));
                pathMain = r.readLine();
                inputStream.close();
                r.close();
                pathMain = pathMain.replace("com.hister.mydictionary", "ir.khaled.mydictionary");

                inputStream = new FileInputStream(backupPath + "pathLeitnerLocal");
                r = new BufferedReader(new InputStreamReader(inputStream));
                pathLeitner = r.readLine();
                inputStream.close();
                r.close();
                pathLeitner = pathLeitner.replace("com.hister.mydictionary", "ir.khaled.mydictionary");

                File main = getDatabasePath("items.db");
                if (main.exists()) {
                    File currentDB = new File(main, "");
                    currentDB.delete();
                }

                main = getDatabasePath("leitner.db");
                if (main.exists()) {
                    File currentDB = new File(main, "");
                    currentDB.delete();
                }

                FileChannel src = new FileInputStream(backupPath + "items " + date + ".db").getChannel();
                FileChannel dst = new FileOutputStream(pathMain).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                src = new FileInputStream(backupPath + "leitner " + date + ".db").getChannel();
                dst = new FileOutputStream(pathLeitner).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

            } else {
                errorS = "Can't access sd card";
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
                long delayInMillis = 1500;
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
