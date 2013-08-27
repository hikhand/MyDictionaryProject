package ir.khaled.mydictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {


    DatabaseHandler database;
    DatabaseLeitner databaseLeitner;
    SharedPreferences prefs;
    SharedPreferences mainPrefs;
    SharedPreferences.Editor editorMainPrefs;

    public String newWordEdit;
    public String newMeaningEdit;

    public EditText etNewWord;
    public EditText etNewMeaning;
    public EditText etSearch;
    public ListView items;

    ImageView imgAdd;
    public boolean isFromSearch;

    ArrayList<Custom> arrayItems;
    ArrayList<CustomShow> arrayItemsToShow;
    public Adapter adapterWords1;
    public AlertDialog dialogAddNew;
    public AlertDialog dialogMeaning;
    public AlertDialog dialogEdit;
    public AlertDialog dialogAskDelete;
    public AlertDialog dialogNewPost;
    public AlertDialog dialogExpire;
    public AlertDialog dialogSortBy;
    public AlertDialog dialogRate;

    boolean dialogAddNewIsOpen = false;
    boolean dialogMeaningIsOpen = false;
    int dialogMeaningWordPosition = 0;
    boolean dialogEditIsOpen = false;
    boolean dialogAskDeleteIsOpen = false;
    boolean dialogNewPostIsOpen = false;
    boolean dialogExpireIsOpen = false;
    boolean dialogSortByIsOpen = false;
    boolean dialogRateIsOpen = false;
    String searchMethod;
    boolean showItemNumber = true;
    boolean showItemMeaning = false;
    String isDistance;
    String isDistanceTempAdd;
    String isDistanceTempLast = "";
    String sortMethod;
    private boolean markSeveral = false;
    Parcelable listViewPosition = null;
    ArrayList<Integer> checkedPositionsInt;
    boolean isToMarkAll = true;
    private boolean doubleBackToExitPressedOnce = false;
    boolean isLongClick = false;//for check items long click

    String searchText = "";

    Names v = null;


    private TextToSpeech tts;

    String s;


    @Override
    public void onBackPressed() {
        if (markSeveral) {
            adapterWords1 = new Adapter(MainActivity.this, R.layout.row, arrayItemsToShow);
            markSeveral = false;
            setElementsId();
            listViewPosition = items.onSaveInstanceState();
            refreshListViewData(false);
            clearMarks();
        } else if (isFromSearch) {
            etSearch.setText("");
            isFromSearch = false;
            listViewPosition = items.onSaveInstanceState();
            refreshListViewData(false);

        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onSearchRequested() {
        etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        return false;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        s = File.separator;

        setElementsId();
        getPrefs();

        if (icicle != null) {
            listViewPosition = icicle.getParcelable("listViewPosition");
            searchText = icicle.getString("etSearchText");
        }

        if (etSearch == null || searchText.equals(null)) {
            etSearch = (EditText) findViewById(R.id.etSearch);
            etSearch.setText("");
        } else {
            etSearch.setText(searchText);
        }

        refreshListViewData(false);

        setImgAddVisibility();
        restore(icicle);

        listeners();

        checkSiteForPosts();

        try {
            checkSiteForVersionChange();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!mainPrefs.getBoolean("rated", false)) {
            if (arrayItems.size() < 50 && !mainPrefs.getBoolean("rate20Viewed", false) && arrayItems.size() > 20) {
                editorMainPrefs.putBoolean("rate20Viewed", true);
                showDialogRate();
            } else if (arrayItems.size() < 100 && !mainPrefs.getBoolean("rate50Viewed", false) && arrayItems.size() > 50) {
                editorMainPrefs.putBoolean("rate50Viewed", true);
                showDialogRate();
            } else if (arrayItems.size() < 150 && !mainPrefs.getBoolean("rate100Viewed", false) && arrayItems.size() > 100) {
                editorMainPrefs.putBoolean("rate100Viewed", true);
                showDialogRate();
            } else if (arrayItems.size() < 200 && !mainPrefs.getBoolean("rate150Viewed", false) && arrayItems.size() > 150) {
                editorMainPrefs.putBoolean("rate150Viewed", true);
                showDialogRate();
            } else if (arrayItems.size() < 250 && !mainPrefs.getBoolean("rate200Viewed", false) && arrayItems.size() > 200) {
                editorMainPrefs.putBoolean("rate200Viewed", true);
                showDialogRate();
            } else if (arrayItems.size() < 300 && !mainPrefs.getBoolean("rate250Viewed", false) && arrayItems.size() > 250) {
                editorMainPrefs.putBoolean("rate250Viewed", true);
                showDialogRate();
            } else if (arrayItems.size() < 350 && !mainPrefs.getBoolean("rate300Viewed", false) && arrayItems.size() > 300) {
                editorMainPrefs.putBoolean("rate300Viewed", true);
                showDialogRate();
            }
            editorMainPrefs.commit();
        }
        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
//                btnSpeak.setEnabled(true);
//                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }


    void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    void listeners() {
        items.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                int position = position1 - 1;
                //if keyboard was up puts it down !!
                if (!arrayItemsToShow.get(position).getWord().equals("   Nothing found") && !arrayItemsToShow.get(position).getMeaning().equals("My Dictionary")) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

                    if (isLongClick) {
                        isLongClick = false;
                        return;
                    }

                    if (markSeveral) {
                        if (arrayItemsToShow.get(position).isChChecked()) {
                            arrayItemsToShow.get(position).setChChecked(false);
                            adapterWords1.notifyDataSetChanged();
                            notifyCheckedPositionsInt();
                        } else {
                            arrayItemsToShow.get(position).setChChecked(true);
                            adapterWords1.notifyDataSetChanged();
                            notifyCheckedPositionsInt();
                        }
                    } else {
                        if (!dialogMeaning.isShowing())
                            dialogMeaning(position, getPosition(position));
                    }
                }
            }
        });

        items.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                int position = position1 - 1;
                if (!arrayItemsToShow.get(position).getWord().equals("   Nothing found") && !arrayItemsToShow.get(position).getMeaning().equals("My Dictionary")) {
                    isLongClick = true;
                    if (position == -1/*0*/) {

                    } else if (markSeveral) {
                        openOptionsMenu();
                    } else {
                        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        mVibrator.vibrate(30);
                        markSeveral = true;
                        int currentApi = android.os.Build.VERSION.SDK_INT;
                        if (currentApi >= Build.VERSION_CODES.HONEYCOMB) {
                            invalidateOptionsMenu();
                        }
                        setElementsId();
                        listViewPosition = items.onSaveInstanceState();
                        refreshListViewData(false);
                        if (isFromSearch) {
                            search(etSearch.getText().toString());
                        }
                        arrayItemsToShow.get(position).setChChecked(true);
                        adapterWords1.notifyDataSetChanged();
                        notifyCheckedPositionsInt();
                    }
                }
                return false;
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
                    refreshListViewData(false);
                } else {
                    search(etSearch.getText().toString());
                }
            }
        });

    }


    void notifyCheckedPositionsInt() {
        checkedPositionsInt.clear();
        for (int i = 0; i < arrayItemsToShow.size(); i++) {
            checkedPositionsInt.add(i, arrayItemsToShow.get(i).isChChecked() ? 0 : 1);
        }
    }

    private void getPrefs() {
        // Get the xml/preferences.xml preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        searchMethod = prefs.getString("searchMethod", "wordsAndMeanings");
        showItemNumber = prefs.getBoolean("showItemNumber", true);
        showItemMeaning = prefs.getBoolean("showItemMeaning", false);
        isDistance = prefs.getString("timeMethod", "distance");
        sortMethod = prefs.getString("sortMethod", "dateA");
    }

    public void setElementsId() {
        tts = new TextToSpeech(this, this);
        v = new Names();
        database = new DatabaseHandler(this);
        databaseLeitner = new DatabaseLeitner(this);
        mainPrefs = getSharedPreferences("main", MODE_PRIVATE);
        editorMainPrefs = mainPrefs.edit();

        if (mainPrefs.getBoolean("firstLogin", true)) {
            databaseLeitner.addItem(new Item("1", "1", "1", "1", "1"), "leitner");
            databaseLeitner.deleteItem(databaseLeitner.getItemId("1", "1"));
            editorMainPrefs.putBoolean("firstLogin", false);
            editorMainPrefs.commit();
            countMe();
        }



        items = (ListView) findViewById(R.id.listView);
        etSearch = (EditText) findViewById(R.id.etSearch);


        arrayItems = new ArrayList<Custom>();
        arrayItemsToShow = new ArrayList<CustomShow>();

        adapterWords1 = new Adapter(MainActivity.this, R.layout.row, arrayItemsToShow);

        dialogAddNew = new AlertDialog.Builder(this).create();
        dialogMeaning = new AlertDialog.Builder(this).create();
        dialogEdit = new AlertDialog.Builder(this).create();
        dialogAskDelete = new AlertDialog.Builder(this).create();
        dialogNewPost = new AlertDialog.Builder(this).create();
        dialogExpire = new AlertDialog.Builder(this).create();
        dialogSortBy = new AlertDialog.Builder(this).create();
        dialogRate = new AlertDialog.Builder(this).create();

        if (checkedPositionsInt == null) {
            checkedPositionsInt = new ArrayList<Integer>();
        }
        listViewPosition = items.onSaveInstanceState();

    }

    void dialogAddNew() {
        LayoutInflater inflater = this.getLayoutInflater();
        dialogAddNew = new AlertDialog.Builder(this)
                .setView(inflater.inflate(R.layout.dialog_addnew, null))
                .setPositiveButton(R.string.save,
                        new Dialog.OnClickListener() {
                            public void onClick(DialogInterface d, int which) {
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialogAddNew.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialogAddNew.show();
        TextView tvTotalCount = (TextView) dialogAddNew.findViewById(R.id.tvTotalCount);
        TextView tvHeader = (TextView) dialogAddNew.findViewById(R.id.tvHeader);
        tvTotalCount.setText(Integer.toString(arrayItems.size()));
        tvHeader.setText("Add An Item");

        Spinner chSpinner1 = (Spinner) dialogAddNew.findViewById(R.id.tag1);
        Spinner chSpinner2 = (Spinner) dialogAddNew.findViewById(R.id.tag2);
        Spinner chSpinner3 = (Spinner) dialogAddNew.findViewById(R.id.tag3);
        Spinner chSpinner4 = (Spinner) dialogAddNew.findViewById(R.id.tag4);
        Spinner chSpinner5 = (Spinner) dialogAddNew.findViewById(R.id.tag5);

        TextView tvRemove1 = (TextView) dialogAddNew.findViewById(R.id.tvRemove1);
        TextView tvRemove2 = (TextView) dialogAddNew.findViewById(R.id.tvRemove2);
        TextView tvRemove3 = (TextView) dialogAddNew.findViewById(R.id.tvRemove3);
        TextView tvRemove4 = (TextView) dialogAddNew.findViewById(R.id.tvRemove4);
        TextView tvRemove5 = (TextView) dialogAddNew.findViewById(R.id.tvRemove5);

        EditText etExample = (EditText) dialogAddNew.findViewById(R.id.etExample);

        chSpinner1.setVisibility(View.GONE);
        chSpinner2.setVisibility(View.GONE);
        chSpinner3.setVisibility(View.GONE);
        chSpinner4.setVisibility(View.GONE);
        chSpinner5.setVisibility(View.GONE);
        tvRemove1.setVisibility(View.GONE);
        tvRemove2.setVisibility(View.GONE);
        tvRemove3.setVisibility(View.GONE);
        tvRemove4.setVisibility(View.GONE);
        tvRemove5.setVisibility(View.GONE);
        etExample.setVisibility(View.GONE);

        boolean first = false;
        if (mainPrefs.getBoolean("firstLoginTag", true)) {
            first = true;
            editorMainPrefs.putBoolean("firstLoginTag", false);
            editorMainPrefs.commit();
        }

        ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
        ArrayList<String> tagsStr = database.getTags(first);
        for (String str : tagsStr) {
            tags.add(str);
        }
        tags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        chSpinner1.setAdapter(tags);
        chSpinner2.setAdapter(tags);
        chSpinner3.setAdapter(tags);
        chSpinner4.setAdapter(tags);
        chSpinner5.setAdapter(tags);


        tvRemove1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner1 = (Spinner) dialogAddNew.findViewById(R.id.tag1);
                TextView tvRemove1 = (TextView) dialogAddNew.findViewById(R.id.tvRemove1);
                chSpinner1.setVisibility(View.GONE);
                tvRemove1.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });
        tvRemove2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner2 = (Spinner) dialogAddNew.findViewById(R.id.tag2);
                TextView tvRemove2 = (TextView) dialogAddNew.findViewById(R.id.tvRemove2);
                chSpinner2.setVisibility(View.GONE);
                tvRemove2.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });
        tvRemove3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner3 = (Spinner) dialogAddNew.findViewById(R.id.tag3);
                TextView tvRemove3 = (TextView) dialogAddNew.findViewById(R.id.tvRemove3);
                chSpinner3.setVisibility(View.GONE);
                tvRemove3.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });
        tvRemove4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner4 = (Spinner) dialogAddNew.findViewById(R.id.tag4);
                TextView tvRemove4 = (TextView) dialogAddNew.findViewById(R.id.tvRemove4);
                chSpinner4.setVisibility(View.GONE);
                tvRemove4.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });
        tvRemove5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner5 = (Spinner) dialogAddNew.findViewById(R.id.tag5);
                TextView tvRemove5 = (TextView) dialogAddNew.findViewById(R.id.tvRemove5);
                chSpinner5.setVisibility(View.GONE);
                tvRemove5.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });




        etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
        etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.etMeaning);

        etNewWord.setFocusableInTouchMode(true);
        etNewMeaning.setFocusableInTouchMode(true);

        etNewWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(etNewWord, InputMethodManager.SHOW_FORCED);
                }
//                else
//                    Toast.makeText(getApplicationContext(), "lost the focus", 2000).show();
            }
        });

        etNewMeaning.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(etNewMeaning, InputMethodManager.SHOW_FORCED);
                }
//                else
//                    Toast.makeText(getApplicationContext(), "lost the focus", 2000).show();
            }
        });

        etNewWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = etNewWord.getText().toString();
                int length = s.length();
                String c = "";
                if (length > 1) {
                    c = s.substring(length - 1, length);
                    if (c.equals("@")) {
                        etNewMeaning.requestFocus();
                        etNewMeaning.setSelection(etNewMeaning.getText().toString().length());
                        etNewWord.setText(s.substring(0, length - 1));
                    }
                } else if (length == 1) {
                    c = s;
                    if (c.equals("@")) {
                        etNewMeaning.requestFocus();
                        etNewMeaning.setSelection(etNewMeaning.getText().toString().length());
                        etNewWord.setText("");
                    }
                }
            }
        });

        etNewMeaning.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = etNewMeaning.getText().toString();
                int length = s.length();
                String c = "";
                if (length > 1) {
                    c = s.substring(length - 1, length);
                    if (c.equals("@")) {
                        EditText etExample = (EditText) dialogAddNew.findViewById(R.id.etExample);
                        if (etExample.getVisibility() == View.VISIBLE) {
                            etExample.requestFocus();
                            etExample.setSelection(etExample.getText().toString().length());
                        } else {
                            etNewWord.requestFocus();
                            etNewWord.setSelection(etNewWord.getText().toString().length());
                        }
                        etNewMeaning.setText(s.substring(0, length - 1));
                    }
                } else if (length == 1) {
                    c = s;
                    if (c.equals("@")) {
                        EditText etExample = (EditText) dialogAddNew.findViewById(R.id.etExample);
                        if (etExample.getVisibility() == View.VISIBLE) {
                            etExample.requestFocus();
                            etExample.setSelection(etExample.getText().toString().length());

                        } else {
                            etNewWord.requestFocus();
                            etNewWord.setSelection(etNewWord.getText().toString().length());
                        }
                        etNewMeaning.setText("");
                    }
                }
            }
        });

        etExample.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                EditText etExample = (EditText) dialogAddNew.findViewById(R.id.etExample);
                String s = etExample.getText().toString();
                int length = s.length();
                String c = "";
                if (length > 1) {
                    c = s.substring(length - 1, length);
                    if (c.equals("@")) {
                        etNewWord.requestFocus();
                        etNewWord.setSelection(etNewWord.getText().toString().length());
                        etExample.setText(s.substring(0, length - 1));
                    }
                } else if (length == 1) {
                    c = s;
                    if (c.equals("@")) {
                        etNewWord.requestFocus();
                        etNewWord.setSelection(etNewWord.getText().toString().length());
                        etExample.setText("");
                    }
                }
            }
        });

        dialogAddNew.setCanceledOnTouchOutside(false);
        Button theButton = dialogAddNew.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListenerAddNew(dialogAddNew));
    }

    class CustomListenerAddNew implements View.OnClickListener {
        private final Dialog dialog;

        public CustomListenerAddNew(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            if (isReadyToAddNew()) {
                CheckBox chDontToLeitner = (CheckBox) dialog.findViewById(R.id.chDoOrDoNot);
                etNewWord = (EditText) dialog.findViewById(R.id.etWord);
                etNewMeaning = (EditText) dialog.findViewById(R.id.etMeaning);
                EditText etNewExample = (EditText) dialog.findViewById(R.id.etExample);
                String newWord = etNewWord.getText().toString();
                String newMeaning = etNewMeaning.getText().toString();
                String newExample = etNewExample.getText().toString();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                String currentDateAndTime = simpleDateFormat.format(new Date());

                Spinner spinner1 = (Spinner) dialogAddNew.findViewById(R.id.tag1);
                Spinner spinner2 = (Spinner) dialogAddNew.findViewById(R.id.tag2);
                Spinner spinner3 = (Spinner) dialogAddNew.findViewById(R.id.tag3);
                Spinner spinner4 = (Spinner) dialogAddNew.findViewById(R.id.tag4);
                Spinner spinner5 = (Spinner) dialogAddNew.findViewById(R.id.tag5);
                ArrayList<String> tagsArray = new ArrayList<String>();
                if (spinner1.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner1.getSelectedItem().toString());
                if (spinner2.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner2.getSelectedItem().toString());
                if (spinner3.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner3.getSelectedItem().toString());
                if (spinner4.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner4.getSelectedItem().toString());
                if (spinner5.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner5.getSelectedItem().toString());
                String tags = "";
                for (int i = 0; i < tagsArray.size(); i++) {
                    String str = tagsArray.get(i);
                    if (i == 0) {
                        tags = str;
                    } else {
                        tags += "," + str;
                    }
                }

                database.addItem(new Custom(newWord, newMeaning, newExample, tags, currentDateAndTime, currentDateAndTime, 0));

                if (!chDontToLeitner.isChecked()) {
                    databaseLeitner.addItem(new Item(newWord, newMeaning, newExample, tags, currentDateAndTime), MainActivity.this.v.TABLE_LEITNER);
                }

                setImgAddVisibility();
                markSeveral = false;
                setElementsId();
                listViewPosition = items.onSaveInstanceState();
                refreshListViewData(false);
                clearMarks();
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Successfully added.", Toast.LENGTH_SHORT).show();
                dialogAddNew.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        }
    }

//    int getTagsCount() {
//        int countTags = 1;
//        Spinner spinner1 = (Spinner) dialogAddNew.findViewById(R.id.tag1);
//        if (spinner1 != null) {
//            Spinner chSpinner2 = (Spinner) dialogAddNew.findViewById(v.tag2Id);
//            if (chSpinner2 != null) {
//                countTags++;
//                Spinner chSpinner3 = (Spinner) dialogAddNew.findViewById(v.tag3Id);
//                if (chSpinner3 != null) {
//                    countTags++;
//                    Spinner chSpinner4 = (Spinner) dialogAddNew.findViewById(v.tag4Id);
//                    if (chSpinner4 != null) {
//                        countTags++;
//                        Spinner chSpinner5 = (Spinner) dialogAddNew.findViewById(v.tag5Id);
//                        if (chSpinner5 != null) {
//                            countTags++;
//                        }
//                    }
//                }
//            }
//        }
//        return countTags;
//    }

int x = 0;

    int getTagsCountAfter() {
        int countTags = 0;
        AlertDialog dialog = dialogAddNew.isShowing() ? dialogAddNew : dialogEdit;
        Spinner spinner1 = (Spinner) dialog.findViewById(R.id.tag1);
        if (spinner1.getVisibility() == View.VISIBLE) {
            countTags++;
            Spinner chSpinner2 = (Spinner) dialog.findViewById(R.id.tag2);
            if (chSpinner2.getVisibility() == View.VISIBLE) {
                countTags++;
                Spinner chSpinner3 = (Spinner) dialog.findViewById(R.id.tag3);
                if (chSpinner3.getVisibility() == View.VISIBLE) {
                    countTags++;
                    Spinner chSpinner4 = (Spinner) dialog.findViewById(R.id.tag4);
                    if (chSpinner4.getVisibility() == View.VISIBLE) {
                        countTags++;
                        Spinner chSpinner5 = (Spinner) dialog.findViewById(R.id.tag5);
                        if (chSpinner5.getVisibility() == View.VISIBLE) {
                            countTags++;
                        }
                    }
                }
            }
        }
        return countTags;
    }

    int getTagsCount() {
        int countTags = 0;
        AlertDialog dialog = dialogAddNew.isShowing() ? dialogAddNew : dialogEdit;

        Spinner chSpinner1 = (Spinner) dialog.findViewById(R.id.tag1);
        Spinner chSpinner2 = (Spinner) dialog.findViewById(R.id.tag2);
        Spinner chSpinner3 = (Spinner) dialog.findViewById(R.id.tag3);
        Spinner chSpinner4 = (Spinner) dialog.findViewById(R.id.tag4);
        Spinner chSpinner5 = (Spinner) dialog.findViewById(R.id.tag5);

        if (chSpinner1.getVisibility() == View.VISIBLE) countTags++;
        if (chSpinner2.getVisibility() == View.VISIBLE) countTags++;
        if (chSpinner3.getVisibility() == View.VISIBLE) countTags++;
        if (chSpinner4.getVisibility() == View.VISIBLE) countTags++;
        if (chSpinner5.getVisibility() == View.VISIBLE) countTags++;
        return countTags;
    }

    public void addMoreTags_click(View view) {
        int countTags = getTagsCountAfter();
        AlertDialog dialog = dialogAddNew.isShowing() ? dialogAddNew : dialogEdit;
        Spinner chSpinner1 = (Spinner) dialog.findViewById(R.id.tag1);
        Spinner chSpinner2 = (Spinner) dialog.findViewById(R.id.tag2);
        Spinner chSpinner3 = (Spinner) dialog.findViewById(R.id.tag3);
        Spinner chSpinner4 = (Spinner) dialog.findViewById(R.id.tag4);
        Spinner chSpinner5 = (Spinner) dialog.findViewById(R.id.tag5);
        TextView tvRemove1 = (TextView) dialog.findViewById(R.id.tvRemove1);
        TextView tvRemove2 = (TextView) dialog.findViewById(R.id.tvRemove2);
        TextView tvRemove3 = (TextView) dialog.findViewById(R.id.tvRemove3);
        TextView tvRemove4 = (TextView) dialog.findViewById(R.id.tvRemove4);
        TextView tvRemove5 = (TextView) dialog.findViewById(R.id.tvRemove5);

        TextView tvMore = (TextView) dialog.findViewById(R.id.tvAddMoreTag);
        EditText etExample = (EditText) dialog.findViewById(R.id.etExample);

        if (dialogAddNew.isShowing() && (etExample.getVisibility() == View.GONE)) {
                etExample.setVisibility(View.VISIBLE);
                countTags = 0;
        } else if (etExample.getVisibility() == View.GONE){
                etExample.setVisibility(View.VISIBLE);
                countTags = 0;
        }

        switch (countTags) {
            case 0:
//                createTagSelector(R.id.tag1, v.img2Id, v.tag2Id);
                chSpinner1.setVisibility(View.VISIBLE);
                tvRemove1.setVisibility(View.VISIBLE);
                tvMore.setText("Add more tags");
                tvMore.setTextColor(Color.parseColor("#3183c2"));

                RelativeLayout.LayoutParams lprams = (RelativeLayout.LayoutParams) tvMore.getLayoutParams();
                lprams.setMargins(0, 2, 0, 0);
                tvMore.setLayoutParams(lprams);
                break;
            case 1:
//                createTagSelector(v.tag2Id, v.img3Id, v.tag3Id);
                chSpinner2.setVisibility(View.VISIBLE);
                tvRemove2.setVisibility(View.VISIBLE);
                tvMore.setText("Add more tags");
                tvMore.setTextColor(Color.parseColor("#3183c2"));
                break;
            case 2:
//                createTagSelector(v.tag3Id, v.img4Id, v.tag4Id);
                chSpinner3.setVisibility(View.VISIBLE);
                tvRemove3.setVisibility(View.VISIBLE);
                tvMore.setText("Add more tags");
                tvMore.setTextColor(Color.parseColor("#3183c2"));
                break;
            case 3:
//                createTagSelector(v.tag4Id, v.img5Id, v.tag5Id);
                chSpinner4.setVisibility(View.VISIBLE);
                tvRemove4.setVisibility(View.VISIBLE);
                tvMore.setText("Add more tags");
                tvMore.setTextColor(Color.parseColor("#3183c2"));
                break;
            case 4:
//                Toast.makeText(MainActivity.this, "No more!", Toast.LENGTH_SHORT).show();
                chSpinner5.setVisibility(View.VISIBLE);
                tvRemove5.setVisibility(View.VISIBLE);
                tvMore.setText("Add more tags");
                tvMore.setVisibility(View.GONE);
                break;
        }
//        countTags = 0;
//
//        RelativeLayout rLayout = (RelativeLayout) dialogAddNew.findViewById(R.id.dialog_addnew_contex);
//
//        RelativeLayout.LayoutParams lprams = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT,
//                RelativeLayout.LayoutParams.WRAP_CONTENT);
//        final RelativeLayout.LayoutParams lPrams = lprams;
//
//        if (countTags == 1) {
//            lprams = lPrams;
//            ImageView image = new ImageView(MainActivity.this);
//            image.setBackgroundResource(R.drawable.remove);
//            lprams.addRule(RelativeLayout.BELOW, R.id.tag1);
//            lprams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.etWord);
//            image.setId(v.img2Id);
//            image.setLayoutParams(lprams);
//            rLayout.addView(image);
//
//            lprams = lPrams;
//            Spinner spinner2 = new Spinner(MainActivity.this);
//            lprams.addRule(RelativeLayout.BELOW, R.id.tag1);
//            lprams.addRule(RelativeLayout.ALIGN_LEFT, R.id.etWord);
//            lprams.addRule(RelativeLayout.LEFT_OF, v.img2Id);
//            spinner2.setId(v.tag2Id);
//            spinner2.setLayoutParams(lprams);
//            rLayout.addView(spinner2);
//
//            ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
//            ArrayList<String> tagsStr = databaseMain.getTags();
//            for (String str : tagsStr) {
//                tags.add(str);
//            }
//            tags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner2.setAdapter(tags);
//
//            TextView tvMoreTag = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
//            lprams = (RelativeLayout.LayoutParams) tvMoreTag.getLayoutParams();
//            lprams.addRule(RelativeLayout.BELOW, spinner2.getId());
//            tvMoreTag.setLayoutParams(lprams);
//        }
//
//        if (countTags == 2) {
//            lprams = lPrams;
//            ImageView image = new ImageView(MainActivity.this);
//            image.setBackgroundResource(R.drawable.remove);
//            lprams.addRule(RelativeLayout.BELOW, v.tag2Id);
//            lprams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.etWord);
//            image.setId(v.img3Id);
//            image.setLayoutParams(lprams);
//            rLayout.addView(image);
//
//            lprams = lPrams;
//            Spinner spinner3 = new Spinner(MainActivity.this);
//            lprams.addRule(RelativeLayout.BELOW, v.tag2Id);
//            lprams.addRule(RelativeLayout.ALIGN_LEFT, R.id.etWord);
//            lprams.addRule(RelativeLayout.LEFT_OF, v.img3Id);
//            spinner3.setId(v.tag2Id);
//            spinner3.setLayoutParams(lprams);
//            rLayout.addView(spinner3);
//
//            ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
//            ArrayList<String> tagsStr = databaseMain.getTags();
//            for (String str : tagsStr) {
//                tags.add(str);
//            }
//            tags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner3.setAdapter(tags);
//
//            TextView tvMoreTag = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
//            lprams = (RelativeLayout.LayoutParams) tvMoreTag.getLayoutParams();
//            lprams.addRule(RelativeLayout.BELOW, spinner3.getId());
//            tvMoreTag.setLayoutParams(lprams);
//        }
//
//        if (countTags == 3) {
//            lprams = lPrams;
//            ImageView image = new ImageView(MainActivity.this);
//            image.setBackgroundResource(R.drawable.remove);
//            lprams.addRule(RelativeLayout.BELOW, R.id.tag1);
//            lprams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.etWord);
//            image.setId(v.img2Id);
//            image.setLayoutParams(lprams);
//            rLayout.addView(image);
//
//
//            lprams = lPrams;
//            Spinner spinner4 = new Spinner(MainActivity.this);
//            lprams.addRule(RelativeLayout.BELOW, v.tag3Id);
//            lprams.addRule(RelativeLayout.ALIGN_LEFT, R.id.etWord);
//            lprams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.etWord);
//            spinner4.setId(v.tag4Id);
//            spinner4.setLayoutParams(lprams);
//            rLayout.addView(spinner4);
//
//            TextView tvMoreTag = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
//            lprams = (RelativeLayout.LayoutParams) tvMoreTag.getLayoutParams();
//
//            lprams.addRule(RelativeLayout.BELOW, spinner4.getId());
//
//            tvMoreTag.setLayoutParams(lprams);
//
//            ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
//            ArrayList<String> tagsStr = databaseMain.getTags();
//            for (String str : tagsStr) {
//                tags.add(str);
//            }
//            tags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner4.setAdapter(tags);
//        }
//
//        if (countTags == 4) {
//            lprams = lPrams;
//            ImageView image = new ImageView(MainActivity.this);
//            image.setBackgroundResource(R.drawable.remove);
//            lprams.addRule(RelativeLayout.BELOW, R.id.tag1);
//            lprams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.etWord);
//            image.setId(v.img2Id);
//            image.setLayoutParams(lprams);
//            rLayout.addView(image);
//
//
//            lprams = lPrams;
//            Spinner spinner5 = new Spinner(MainActivity.this);
//            lprams.addRule(RelativeLayout.BELOW, v.tag4Id);
//            lprams.addRule(RelativeLayout.ALIGN_LEFT, R.id.etWord);
//            lprams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.etWord);
//            spinner5.setId(v.tag5Id);
//            spinner5.setLayoutParams(lprams);
//            rLayout.addView(spinner5);
//
//            TextView tvMoreTag = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
//            lprams = (RelativeLayout.LayoutParams) tvMoreTag.getLayoutParams();
//
//            lprams.addRule(RelativeLayout.BELOW, spinner5.getId());
//
//            tvMoreTag.setLayoutParams(lprams);
//
//            ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
//            ArrayList<String> tagsStr = databaseMain.getTags();
//            for (String str : tagsStr) {
//                tags.add(str);
//            }
//            tags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner5.setAdapter(tags);
//
//            tvMoreTag.setTextColor(Color.GRAY);
//        }
//
//        if (countTags == 5) {
//            Toast.makeText(MainActivity.this, "No more!", Toast.LENGTH_SHORT).show();
//        }
    }

//    void createTagSelector(int idLastSp, int idImg, int idSp) {
//        RelativeLayout rLayout = (RelativeLayout) dialogAddNew.findViewById(R.id.dialog_addnew_contex);
//
//        RelativeLayout.LayoutParams lpramsImg = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT,
//                RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//        ImageView image = new ImageView(MainActivity.this);
//        image.setImageResource(R.drawable.remove);
//        lpramsImg.addRule(RelativeLayout.BELOW, idLastSp);
//        lpramsImg.addRule(RelativeLayout.ALIGN_RIGHT, R.id.etWord);
//        image.setId(idImg);
//        image.setLayoutParams(lpramsImg);
//        rLayout.addView(image);
//
//        RelativeLayout.LayoutParams lpramsSp = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT,
//                RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//        Spinner spinner = new Spinner(MainActivity.this);
//        lpramsSp.addRule(RelativeLayout.BELOW, idLastSp);
//        lpramsSp.addRule(RelativeLayout.ALIGN_LEFT, R.id.etWord);
//        lpramsSp.addRule(RelativeLayout.LEFT_OF, idImg);
//        spinner.setId(idSp);
//        spinner.setLayoutParams(lpramsSp);
//        rLayout.addView(spinner);
//
//        ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
//        ArrayList<String> tagsStr = databaseMain.getTags();
//        for (String str : tagsStr) {
//            tags.add(str);
//        }
//        tags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(tags);
//
//        TextView tvMoreTag = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);
//        RelativeLayout.LayoutParams lpramsM = (RelativeLayout.LayoutParams) tvMoreTag.getLayoutParams();
//        lpramsM.addRule(RelativeLayout.BELOW, spinner.getId());
//        tvMoreTag.setLayoutParams(lpramsM);
//        imgRemoveOnClickListener();
//    }
//    void imgRemoveOnClickListener() {
//        ImageView img1 = (ImageView) dialogAddNew.findViewById(R.id.img1);
//        ImageView img2 = (ImageView) dialogAddNew.findViewById(v.img2Id);
//        ImageView img3 = (ImageView) dialogAddNew.findViewById(v.img3Id);
//        ImageView img4 = (ImageView) dialogAddNew.findViewById(v.img4Id);
//        ImageView img5 = (ImageView) dialogAddNew.findViewById(v.img5Id);
//
//        if (img1 != null) {
//            img1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(MainActivity.this, "1 clicked", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//        if (img2 != null) {
//            img2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(MainActivity.this, "2 clicked", Toast.LENGTH_SHORT).show();
//
//                }
//            });
//        }
//
//        if (img3 != null) {
//            img3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(MainActivity.this, "3 clicked", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//        if (img4 != null) {
//            img4.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(MainActivity.this, "4 clicked", Toast.LENGTH_SHORT).show();
//
//                }
//            });
//        }
//
//        if (img5 != null) {
//            img5.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
//        }
//    }

    public void search(String key) {
        char first[] = key.toCharArray();
        if (key.length() > 0 && first[0] == '.') {
            searchAdvanced(key);
        } else if ((key.length() > 0)) {
            int found = 0;
            if (arrayItems.size() > 0) {
                arrayItemsToShow.clear();
                for (int i = 0; i < arrayItems.size(); i++) {
                    key = key.toUpperCase();
                    String word = arrayItems.get(i).getWord().toUpperCase();
                    String meaning = arrayItems.get(i).getMeaning().toUpperCase();

                    if (searchMethod.equals("wordsAndMeanings") ? word.contains(key) || meaning.contains(key) :
                            searchMethod.equals("justWords") ? word.contains(key) :
                                    meaning.contains(key)) {

                        arrayItemsToShow.add(convertToShow(arrayItems.get(i)));
                        found++;
                    }
                }
                if (found > 0) {
                    adapterWords1.notifyDataSetChanged();
                    items.setAdapter(adapterWords1);
                    sort();
                } else {
                    arrayItemsToShow.add(convertToShow(new Custom("   Nothing found", "My Dictionary", "KHaledBLack73", false)));
                    adapterWords1.notifyDataSetChanged();
                }
            }

            isFromSearch = true;

            if (arrayItemsToShow.size() > 0) {
                for (int i = 0; i < arrayItemsToShow.size(); i++) {
                    if (!(arrayItemsToShow.get(i).getWord().equals("   Nothing found") &&
                            arrayItemsToShow.get(i).getMeaning().equals("My Dictionary") && arrayItemsToShow.get(i).getDate().equals("KHaledBLack73"))) {
                        arrayItemsToShow.get(i).setChVisible(markSeveral);
                        //whether show item's number or not
                        if (!(arrayItemsToShow.get(i).getWord().equals("   Nothing found") && arrayItemsToShow.get(i).getMeaning().equals("My Dictionary") && arrayItemsToShow.get(i).getDate().equals("KHaledBLack73")))
                            arrayItemsToShow.get(i).setWord(showItemNumber ? i + 1 + ". " + arrayItemsToShow.get(i).getWord() : arrayItemsToShow.get(i).getWord());
                        //whether show item's meaning or not
                        arrayItemsToShow.get(i).setMeaningVisible(showItemMeaning);
                    }
                }

                notifyCheckedPositionsInt();
            }
        }
    }

    void searchAdvanced(String key) {
        char first[] = key.toCharArray();
        if (key.length() > 1 && (first[1] == 't' || first[1] == 'T')) {
            searchByTag(key);
        } else {
//            int found = 0;
//            if (arrayItems.size() > 0) {
//                arrayItemsToShow.clear();
//                for (int i = 0; i < arrayItems.size(); i++) {
//                    key = key.toUpperCase();
//                    String word = arrayItems.get(i).getWord().toUpperCase();
//                    String meaning = arrayItems.get(i).getMeaning().toUpperCase();
//
//                    if (searchMethod.equals("wordsAndMeanings") ? word.contains(key) || meaning.contains(key) :
//                            searchMethod.equals("justWords") ? word.contains(key) :
//                                    meaning.contains(key)) {
//
//                        arrayItemsToShow.add(convertToShow(arrayItems.get(i)));
//                        found++;
//                    }
//                }
//                if (found > 0) {
//                    adapterWords1.notifyDataSetChanged();
//                    items.setAdapter(adapterWords1);
//                    sort();
//                } else {
//                    arrayItemsToShow.add(convertToShow(new Custom("   Nothing found", "My Dictionary", "KHaledBLack73", false)));
//                    adapterWords1.notifyDataSetChanged();
//                }
//            }
//
//            isFromSearch = true;
//
//            if (arrayItemsToShow.size() > 0) {
//                for (int i = 0; i < arrayItemsToShow.size(); i++) {
//                    if (!(arrayItemsToShow.get(i).getWord().equals("   Nothing found") &&
//                            arrayItemsToShow.get(i).getMeaning().equals("My Dictionary") && arrayItemsToShow.get(i).getDate().equals("KHaledBLack73"))) {
//                        arrayItemsToShow.get(i).setChVisible(markSeveral);
//                        //whether show item's number or not
//                        if (!(arrayItemsToShow.get(i).getWord().equals("   Nothing found") && arrayItemsToShow.get(i).getMeaning().equals("My Dictionary") && arrayItemsToShow.get(i).getDate().equals("KHaledBLack73")))
//                            arrayItemsToShow.get(i).setWord(showItemNumber ? i + 1 + ". " + arrayItemsToShow.get(i).getWord() : arrayItemsToShow.get(i).getWord());
//                        //whether show item's meaning or not
//                        arrayItemsToShow.get(i).setMeaningVisible(showItemMeaning);
//                    }
//                }
//
//                notifyCheckedPositionsInt();
//            }
        }
    }

    void searchByTag(String key) {
        int found = 0;
        if (arrayItems.size() > 0) {
            arrayItemsToShow.clear();
            key = key.toLowerCase();
            key = key.substring(2);
            for (Custom j : arrayItems) {
                String tags = j.getTags().toLowerCase();
                if (tags.contains(key)) {
                    found++;
                    arrayItemsToShow.add(convertToShow(j));
                }
            }

            if (found > 0) {
                adapterWords1.notifyDataSetChanged();
                items.setAdapter(adapterWords1);
                sort();
            } else {
                arrayItemsToShow.add(convertToShow(new Custom("   Nothing found", "My Dictionary", "KHaledBLack73", false)));
                adapterWords1.notifyDataSetChanged();
            }
        }

        isFromSearch = true;

        if (arrayItemsToShow.size() > 0) {
            for (int i = 0; i < arrayItemsToShow.size(); i++) {
                if (!(arrayItemsToShow.get(i).getWord().equals("   Nothing found") &&
                        arrayItemsToShow.get(i).getMeaning().equals("My Dictionary") && arrayItemsToShow.get(i).getDate().equals("KHaledBLack73"))) {
                    arrayItemsToShow.get(i).setChVisible(markSeveral);
                    //whether show item's number or not
                    if (!(arrayItemsToShow.get(i).getWord().equals("   Nothing found") && arrayItemsToShow.get(i).getMeaning().equals("My Dictionary") && arrayItemsToShow.get(i).getDate().equals("KHaledBLack73")))
                        arrayItemsToShow.get(i).setWord(showItemNumber ? i + 1 + ". " + arrayItemsToShow.get(i).getWord() : arrayItemsToShow.get(i).getWord());
                    //whether show item's meaning or not
                    arrayItemsToShow.get(i).setMeaningVisible(showItemMeaning);
                }
            }

            notifyCheckedPositionsInt();
        }
    }

    CustomShow convertToShow(Custom custom) {
        return new CustomShow(custom.getId(), custom.getWord(), custom.getMeaning(), custom.getExample(), custom.getTags(), custom.getDate(), custom.getLastDate(), custom.getCount());
    }
    Custom convertToCustom(CustomShow j) {
        return new Custom(j.getId(), j.getWord(), j.getMeaning(), j.getExample(), j.getTags(), j.getDate(), j.getLastDate(), j.getCount());
    }

    void dialogEdit(boolean fromSearch, int fakePosition, int realPosition) {
        final int fakPositionToSendToDialogDelete = fakePosition;
        final int realPositionToSendToDialogDelete = realPosition;
        LayoutInflater inflater = this.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_addnew, null);
        final AlertDialog.Builder d = new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton(R.string.save, new Dialog.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText dialogEditWord = (EditText) dialogEdit.findViewById(R.id.etWord);
                        EditText dialogEditMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
                        newWordEdit = dialogEditWord.getText().toString();
                        newMeaningEdit = dialogEditMeaning.getText().toString();
                        dialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        if (!dialogAskDelete.isShowing())
                            dialogAskDelete(fakPositionToSendToDialogDelete, true);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!dialogMeaning.isShowing())
                            dialogMeaning(fakPositionToSendToDialogDelete, realPositionToSendToDialogDelete);
                    }
                });

        dialogEdit = d.create();
        dialogEdit.show();

        dialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
        etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
        EditText etExample = (EditText) dialogEdit.findViewById(R.id.etExample);

        Custom j = arrayItems.get(realPosition);

        etNewWord.setText(j.getWord());
        etNewMeaning.setText(j.getMeaning());
        etExample.setText(j.getExample());


        Spinner chSpinner1 = (Spinner) dialogEdit.findViewById(R.id.tag1);
        Spinner chSpinner2 = (Spinner) dialogEdit.findViewById(R.id.tag2);
        Spinner chSpinner3 = (Spinner) dialogEdit.findViewById(R.id.tag3);
        Spinner chSpinner4 = (Spinner) dialogEdit.findViewById(R.id.tag4);
        Spinner chSpinner5 = (Spinner) dialogEdit.findViewById(R.id.tag5);

        TextView tvRemove1 = (TextView) dialogEdit.findViewById(R.id.tvRemove1);
        TextView tvRemove2 = (TextView) dialogEdit.findViewById(R.id.tvRemove2);
        TextView tvRemove3 = (TextView) dialogEdit.findViewById(R.id.tvRemove3);
        TextView tvRemove4 = (TextView) dialogEdit.findViewById(R.id.tvRemove4);
        TextView tvRemove5 = (TextView) dialogEdit.findViewById(R.id.tvRemove5);

        TextView tvMore = (TextView) dialogEdit.findViewById(R.id.tvAddMoreTag);

        chSpinner1.setVisibility(View.GONE);
        chSpinner2.setVisibility(View.GONE);
        chSpinner3.setVisibility(View.GONE);
        chSpinner4.setVisibility(View.GONE);
        chSpinner5.setVisibility(View.GONE);
        tvRemove1.setVisibility(View.GONE);
        tvRemove2.setVisibility(View.GONE);
        tvRemove3.setVisibility(View.GONE);
        tvRemove4.setVisibility(View.GONE);
        tvRemove5.setVisibility(View.GONE);
        etExample.setVisibility(View.GONE);

        ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
        ArrayList<String> tagsStr = database.getTags(false);
        for (String str : tagsStr) {
            tags.add(str);
        }
        tags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chSpinner1.setAdapter(tags);
        chSpinner2.setAdapter(tags);
        chSpinner3.setAdapter(tags);
        chSpinner4.setAdapter(tags);
        chSpinner5.setAdapter(tags);

        if (etExample.getText().toString().equals("")) {
            etExample.setVisibility(View.GONE);
        } else {
            etExample.setVisibility(View.VISIBLE);
        }

        String tag[] = j.getTags().split(",");
        int countTags = tag.length;
        if (j.getTags().equals("")) {
            countTags = 0;
        }

        if (countTags > 0) {
            chSpinner1.setVisibility(View.VISIBLE);
            tvRemove1.setVisibility(View.VISIBLE);
            chSpinner1.setSelection(tags.getPosition(tag[0]));
            tvMore.setVisibility(View.VISIBLE);
            if (etExample.getVisibility() == View.VISIBLE) {
                tvMore.setText("Add more tags");
            }
        }
        if (countTags > 1) {
            chSpinner2.setVisibility(View.VISIBLE);
            tvRemove2.setVisibility(View.VISIBLE);
            chSpinner2.setSelection(tags.getPosition(tag[1]));
        }
        if (countTags > 2) {
            chSpinner3.setVisibility(View.VISIBLE);
            tvRemove3.setVisibility(View.VISIBLE);
            chSpinner3.setSelection(tags.getPosition(tag[2]));
        }
        if (countTags > 3) {
            chSpinner4.setVisibility(View.VISIBLE);
            tvRemove4.setVisibility(View.VISIBLE);
            chSpinner4.setSelection(tags.getPosition(tag[3]));
        }
        if (countTags > 4) {
            chSpinner5.setVisibility(View.VISIBLE);
            tvRemove5.setVisibility(View.VISIBLE);
            chSpinner5.setSelection(tags.getPosition(tag[4]));
            if (etExample.getVisibility() == View.VISIBLE) {
                tvMore.setVisibility(View.GONE);
            }
        }

        tvRemove1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner1 = (Spinner) dialogEdit.findViewById(R.id.tag1);
                TextView tvRemove1 = (TextView) dialogEdit.findViewById(R.id.tvRemove1);
                chSpinner1.setVisibility(View.GONE);
                tvRemove1.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogEdit.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });
        tvRemove2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner2 = (Spinner) dialogEdit.findViewById(R.id.tag2);
                TextView tvRemove2 = (TextView) dialogEdit.findViewById(R.id.tvRemove2);
                chSpinner2.setVisibility(View.GONE);
                tvRemove2.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogEdit.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });
        tvRemove3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner3 = (Spinner) dialogEdit.findViewById(R.id.tag3);
                TextView tvRemove3 = (TextView) dialogEdit.findViewById(R.id.tvRemove3);
                chSpinner3.setVisibility(View.GONE);
                tvRemove3.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogEdit.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });
        tvRemove4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner4 = (Spinner) dialogEdit.findViewById(R.id.tag4);
                TextView tvRemove4 = (TextView) dialogEdit.findViewById(R.id.tvRemove4);
                chSpinner4.setVisibility(View.GONE);
                tvRemove4.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogEdit.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });
        tvRemove5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner chSpinner5 = (Spinner) dialogEdit.findViewById(R.id.tag5);
                TextView tvRemove5 = (TextView) dialogEdit.findViewById(R.id.tvRemove5);
                chSpinner5.setVisibility(View.GONE);
                tvRemove5.setVisibility(View.GONE);
                TextView tvMore = (TextView) dialogEdit.findViewById(R.id.tvAddMoreTag);
                if (getTagsCount() == 0) tvMore.setText("Add tag");
                if (getTagsCount() != 0) tvMore.setVisibility(View.VISIBLE);
            }
        });


        etNewWord.setFocusableInTouchMode(true);
        etNewMeaning.setFocusableInTouchMode(true);

        etNewWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(etNewWord, InputMethodManager.SHOW_FORCED);
                }

            }
        });

        etNewMeaning.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(etNewMeaning, InputMethodManager.SHOW_FORCED);
                }

            }
        });

        etNewWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = etNewWord.getText().toString();
                int length = s.length();
                String c = "";
                if (length > 1) {
                    c = s.substring(length - 1, length);
                    if (c.equals("@")) {
                        etNewMeaning.requestFocus();
                        etNewMeaning.setSelection(etNewMeaning.getText().toString().length());
                        etNewWord.setText(s.substring(0, length - 1));
                    }
                } else if (length == 1) {
                    c = s;
                    if (c.equals("@")) {
                        etNewMeaning.requestFocus();
                        etNewMeaning.setSelection(etNewMeaning.getText().toString().length());
                        etNewWord.setText("");
                    }
                }
            }
        });

        etNewMeaning.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = etNewMeaning.getText().toString();
                int length = s.length();
                String c = "";
                if (length > 1) {
                    c = s.substring(length - 1, length);
                    if (c.equals("@")) {
                        EditText etExample = (EditText) dialogEdit.findViewById(R.id.etExample);
                        if (etExample.getVisibility() == View.VISIBLE) {
                            etExample.requestFocus();
                            etExample.setSelection(etExample.getText().toString().length());
                        } else {
                            etNewWord.requestFocus();
                            etNewWord.setSelection(etNewWord.getText().toString().length());
                        }
                        etNewMeaning.setText(s.substring(0, length - 1));
                    }
                } else if (length == 1) {
                    c = s;
                    if (c.equals("@")) {
                        EditText etExample = (EditText) dialogEdit.findViewById(R.id.etExample);
                        if (etExample.getVisibility() == View.VISIBLE) {
                            etExample.requestFocus();
                            etExample.setSelection(etExample.getText().toString().length());

                        } else {
                            etNewWord.requestFocus();
                            etNewWord.setSelection(etNewWord.getText().toString().length());
                        }
                        etNewMeaning.setText("");
                    }
                }
            }
        });

        etExample.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                EditText etExample = (EditText) dialogEdit.findViewById(R.id.etExample);
                String s = etExample.getText().toString();
                int length = s.length();
                String c = "";
                if (length > 1) {
                    c = s.substring(length - 1, length);
                    if (c.equals("@")) {
                        etNewWord.requestFocus();
                        etNewWord.setSelection(etNewWord.getText().toString().length());
                        etExample.setText(s.substring(0, length - 1));
                    }
                } else if (length == 1) {
                    c = s;
                    if (c.equals("@")) {
                        etNewWord.requestFocus();
                        etNewWord.setSelection(etNewWord.getText().toString().length());
                        etExample.setText("");
                    }
                }
            }
        });


        CheckBox chDontToLeitner = (CheckBox) dialogEdit.findViewById(R.id.chDoOrDoNot);
        chDontToLeitner.setVisibility(View.GONE);

        TextView tvTotalCount = (TextView) dialogEdit.findViewById(R.id.tvTotalCount);
        TextView tvHeader = (TextView) dialogEdit.findViewById(R.id.tvHeader);
        tvTotalCount.setVisibility(View.INVISIBLE);
        tvHeader.setText("Edit An Item");


        dialogEdit.setCanceledOnTouchOutside(false);


        Button theButton = dialogEdit.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListenerEdit(dialogEdit, j.getWord(), j.getMeaning(), realPosition));
    }

    class CustomListenerEdit implements View.OnClickListener {
        private final Dialog dialog;
        private String word;
        private String meaning;
        private int realPosition;

        public CustomListenerEdit(Dialog dialog, String word, String meaning, int realPosition) {
            this.dialog = dialog;
            this.word = word;
            this.meaning = meaning;
            this.realPosition = realPosition;
        }

        @Override
        public void onClick(View v) {
            if (isReadyEdit(word)) {
                etNewWord = (EditText) dialog.findViewById(R.id.etWord);
                etNewMeaning = (EditText) dialog.findViewById(R.id.etMeaning);
                EditText etNewExample = (EditText) dialogEdit.findViewById(R.id.etExample);
                newWordEdit = etNewWord.getText().toString();
                newMeaningEdit = etNewMeaning.getText().toString();
                String newExample = etNewExample.getText().toString();

                Spinner spinner1 = (Spinner) dialogEdit.findViewById(R.id.tag1);
                Spinner spinner2 = (Spinner) dialogEdit.findViewById(R.id.tag2);
                Spinner spinner3 = (Spinner) dialogEdit.findViewById(R.id.tag3);
                Spinner spinner4 = (Spinner) dialogEdit.findViewById(R.id.tag4);
                Spinner spinner5 = (Spinner) dialogEdit.findViewById(R.id.tag5);
                ArrayList<String> tagsArray = new ArrayList<String>();
                if (spinner1.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner1.getSelectedItem().toString());
                if (spinner2.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner2.getSelectedItem().toString());
                if (spinner3.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner3.getSelectedItem().toString());
                if (spinner4.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner4.getSelectedItem().toString());
                if (spinner5.getVisibility() == View.VISIBLE)
                    tagsArray.add(spinner5.getSelectedItem().toString());
                String tags = "";
                for (int i = 0; i < tagsArray.size(); i++) {
                    String str = tagsArray.get(i);
                    if (i == 0) {
                        tags = str;
                    } else {
                        tags += "," + str;
                    }
                }

                Custom j = arrayItems.get(realPosition);
                database.updateItem(new Custom(j.getId(), newWordEdit, newMeaningEdit, newExample, tags, j.getDate(), j.getLastDate(), j.getCount()));
                int idLeitner = databaseLeitner.getItemId(word, meaning);
                if (idLeitner > 0) {
                    Item j1 = databaseLeitner.getItem(idLeitner);
                    databaseLeitner.updateItem(new Item(j1.getId(), newWordEdit, newMeaningEdit, j.getExample(), j.getTags(),
                            j1.getAddDate(), j1.getLastCheckDate(), j1.getLastCheckDay(),
                            j1.getDeck(), j1.getIndex(), j1.getCountCorrect(), j1.getCountInCorrect(), j1.getCount()));
                }

                listViewPosition = items.onSaveInstanceState();
                refreshListViewData(false);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Successfully edited.", Toast.LENGTH_SHORT).show();
                dialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        }
    }

    void dialogAskDelete(final int position, final boolean fromEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ask To Delete");
        builder.setMessage("Are you sure you want to delete this word ?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete(getPosition(position), position);


                Toast.makeText(MainActivity.this, "Successfully deleted.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (fromEdit) {
                    if (!dialogEdit.isShowing())
                        dialogEdit(isFromSearch, position, getPosition(position));
                    EditText dialogEditWord = (EditText) dialogEdit.findViewById(R.id.etWord);
                    EditText dialogEditMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
                    dialogEditWord.setText(newWordEdit);
                    dialogEditMeaning.setText(newMeaningEdit);
                } else {
                    dialogMeaning(position, getPosition(position));
                }
            }
        });
        dialogAskDelete = builder.create();
        dialogAskDelete.show();
        dialogAskDelete.setCanceledOnTouchOutside(false);

    }


    void refreshItemsCount(int position, int realPosition) {
        int count = arrayItems.get(realPosition).getCount();
        int id = database.getItemId(arrayItems.get(realPosition).getWord(), arrayItems.get(realPosition).getMeaning());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String currentDateAndTime = simpleDateFormat.format(new Date());

        Custom current = database.getItem(id);
        database.updateItem(new Custom(id, current.getWord(), current.getMeaning(), current.getExample(), current.getTags(), current.getDate(), currentDateAndTime, current.getCount() + 1));

        arrayItems.get(realPosition).setCount(count + 1);
        arrayItemsToShow.get(position).setCount(count + 1);
    }


    void delete(int realPosition, int showPosition) {
        database.deleteItem(database.getItemId(arrayItems.get(realPosition).getWord(), arrayItems.get(realPosition).getMeaning()));

        int idLeitner = databaseLeitner.getItemId(arrayItems.get(realPosition).getWord(), arrayItems.get(realPosition).getMeaning());
        if (idLeitner > 0) {
            Item j = databaseLeitner.getItem(idLeitner);
            databaseLeitner.deleteItem(idLeitner);
        }

        Log.i("void delete", Integer.toString(database.getItemId(arrayItems.get(realPosition).getWord(), arrayItems.get(realPosition).getMeaning())));
        arrayItems.remove(realPosition);
        arrayItemsToShow.remove(showPosition);

        if (!isFromSearch) {
            setImgAddVisibility();
        }

        listViewPosition = items.onSaveInstanceState();
        refreshListViewData(false);

        if (isFromSearch && arrayItemsToShow.size() == 0) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }


    void refreshListViewData(boolean isFromDeleteMark) {
        arrayItems.clear();
        arrayItemsToShow.clear();
        if (database.getItemsCount() > 0) {
            arrayItems.addAll(database.getAllItems());
            for (Custom custom : arrayItems)
                arrayItemsToShow.add(convertToShow(custom));

            sort();

            if (arrayItemsToShow.size() > 0) {
                for (int i = 0; i < arrayItemsToShow.size(); i++) {
                    arrayItemsToShow.get(i).setChVisible(markSeveral);
                    if (markSeveral && checkedPositionsInt.size() > 0)
                        arrayItemsToShow.get(i).setChChecked(checkedPositionsInt.get(i) == 0);

                    //whether show item's number or not
                    arrayItemsToShow.get(i).setWord(showItemNumber ? i + 1 + ". " + arrayItemsToShow.get(i).getWord() : arrayItemsToShow.get(i).getWord());
                    //whether show item's meaning or not
                    arrayItemsToShow.get(i).setMeaningVisible(showItemMeaning);
                }
            }
        }
        adapterWords1.notifyDataSetChanged();
        items.setAdapter(adapterWords1);

        if (listViewPosition != null)
            items.onRestoreInstanceState(listViewPosition);

        if (isFromSearch) {
            listViewPosition = items.onSaveInstanceState();
            search(etSearch.getText().toString());
            items.onRestoreInstanceState(listViewPosition);
        } else {
            setImgAddVisibility();
        }

//        if (arrayItemsToShow.size() > 0 )
//            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

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
        for (CustomShow item : arrayItemsToShow) {
            words.add(item.getWord());
        }
        Collections.sort(words);
        ArrayList<Custom> buff = new ArrayList<Custom>();
        for (CustomShow item: arrayItemsToShow) {
            buff.add(convertToCustom(item));
        }
        arrayItemsToShow.clear();
        for (int i = 0; i < buff.size(); i++) {
            for (Custom j : buff) {
                if (words.get(i).equals(j.getWord())) {
                    arrayItemsToShow.add(convertToShow(j));
                }
            }
        }
    }

    void sortNameD() {
        sortNameA();
        ArrayList<Custom> buff = new ArrayList<Custom>();
        for (CustomShow item: arrayItemsToShow) {
            buff.add(convertToCustom(item));
        }
        arrayItemsToShow.clear();
        for (int i = buff.size()-1; i >= 0; i--) {
            arrayItemsToShow.add(convertToShow(buff.get(i)));
        }
    }

    void sortDateD() {
        ArrayList<Custom> buff = new ArrayList<Custom>();
        for (CustomShow item: arrayItemsToShow) {
            buff.add(convertToCustom(item));
        }
        arrayItemsToShow.clear();
        for (int i = buff.size()-1; i >= 0; i--) {
            arrayItemsToShow.add(convertToShow(buff.get(i)));
        }
    }

    void sortCountA() {
        for (int i = 0; i < arrayItemsToShow.size()-1; i++) {
            for (int j = 0; j < arrayItemsToShow.size()-1; j++) {
                if (arrayItemsToShow.get(j).getCount() > arrayItemsToShow.get(j+1).getCount()) {
                    CustomShow temp = arrayItemsToShow.get(j);
                    CustomShow temp1 = arrayItemsToShow.get(j+1);
                    arrayItemsToShow.set(j, temp1);
                    arrayItemsToShow.set(j+1, temp);
                }
            }
        }
    }

    void sortCountD() {
        for (int i = 0; i < arrayItemsToShow.size()-1; i++) {
            for (int j = 0; j < arrayItemsToShow.size()-1; j++) {
                if (arrayItemsToShow.get(j).getCount() < arrayItemsToShow.get(j+1).getCount()) {
                    CustomShow temp = arrayItemsToShow.get(j);
                    CustomShow temp1 = arrayItemsToShow.get(j+1);
                    arrayItemsToShow.set(j, temp1);
                    arrayItemsToShow.set(j+1, temp);
                }
            }
        }
    }


    void showDialogSortBy() {
//
//        LayoutInflater inflater = this.getLayoutInflater();
//        dialogSortBy = new AlertDialog.Builder(this)
//                .setView(inflater.inflate(R.layout.dialog_sort, null))
//                .setTitle("Sort By: ")
//                .setPositiveButton(R.string.ok,
//                        new Dialog.OnClickListener() {
//                            public void onClick(DialogInterface d, int which) {
//                                sortBy();
//                            }
//                        })
//                .create();
//        dialogSortBy.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//        dialogSortBy.show();
    }

//    void sortBy() {
//        if (databaseMain.getItemsCount() > 0) {
//            listViewPosition = items.onSaveInstanceState();
//            RadioButton nameA = (RadioButton) dialogSortBy.findViewById(R.id.rbNameA);
//            RadioButton nameD = (RadioButton) dialogSortBy.findViewById(R.id.rbNameD);
//            RadioButton DateA = (RadioButton) dialogSortBy.findViewById(R.id.rbDateA);
//            RadioButton DateD = (RadioButton) dialogSortBy.findViewById(R.id.rbDateD);
//            RadioButton CountA = (RadioButton) dialogSortBy.findViewById(R.id.rbCountA);
//            RadioButton CountD = (RadioButton) dialogSortBy.findViewById(R.id.rbCountD);
//            if (nameA.isChecked()) {
//                sortNameA();
//            } else if (nameD.isChecked()) {
//                sortNameD();
//            } else if (DateA.isChecked()) {
//
//            } else if (DateD.isChecked()) {
//                sortDateD();
//            } else if (CountA.isChecked()) {
//                sortCountA();
//            } else if (CountD.isChecked()) {
//                sortCountD();
//            }
//        }
//        adapterWords1.notifyDataSetChanged();
//        items.setAdapter(adapterWords1);
//
//        if (listViewPosition != null)
//            items.onRestoreInstanceState(listViewPosition);
//
//        if (isFromSearch) {
//            listViewPosition = items.onSaveInstanceState();
//            search(etSearch.getText().toString());
//            items.onRestoreInstanceState(listViewPosition);
//        } else {
//            setImgAddVisibility();
//        }
//
//    }

    void setImgAddVisibility() {
        imgAdd = (ImageView) findViewById(R.id.imgAdd);
        imgAdd.setVisibility(View.GONE);
        if (database.getItemsCount() == 0) {
            imgAdd.setVisibility(View.VISIBLE);
        } else {
            imgAdd.setVisibility(View.GONE);
        }
    }

    int getPosition(int position) {
        int realPosition = 0;
        boolean found = false;
        for (int i = 0; i < arrayItems.size(); i++) {
            if (arrayItems.get(i).getWord().equals(arrayItemsToShow.get(position).getWord()) &&
                    arrayItems.get(i).getMeaning().equals(arrayItemsToShow.get(position).getMeaning())) {

                realPosition = i;
                break;
            }
            for (int j = 0; j < arrayItems.size(); j++) {
                if ((Integer.toString(j + 1) + ". " + arrayItems.get(i).getWord()).equals(arrayItemsToShow.get(position).getWord()) &&
                        arrayItems.get(i).getMeaning().equals(arrayItemsToShow.get(position).getMeaning())) {

                    realPosition = i;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        return realPosition;
    }

    int getPosition(String word, String meaning) {
        for (int i = 0; i < arrayItems.size(); i++) {
            if (arrayItems.get(i).getWord().toUpperCase().equals(word) &&
                    arrayItems.get(i).getMeaning().toUpperCase().equals(meaning)) {
                return i;
            }
        }
        return 0;
    }


    public boolean isReadyToAddNew() {
        etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
        etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
        String newWord = etNewWord.getText().toString();
        String newMeaning = etNewMeaning.getText().toString();

        if (isStringJustSpace(newWord)) {
            Toast.makeText(this, "The Card's Name is missing.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isStringJustSpace(newMeaning)) {
            Toast.makeText(this, "The Card's Meaning is missing.", Toast.LENGTH_SHORT).show();
            return false;
        }
        for (int i = 0; i < database.getItemsCount(); i++) {
//            if (newWord.equals(arrayItems.get(i).getWord()) && newMeaning.equals(arrayItems.get(i).getMeaning())) {
            if (newWord.toLowerCase().equals(arrayItems.get(i).getWord().toLowerCase())) {
                Toast.makeText(this, "The Word exists in the databaseMain", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


    public boolean isReadyEdit(String word) {
        etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
        etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
        EditText etNewExample = (EditText) dialogEdit.findViewById(R.id.etExample);
        String newWord = etNewWord.getText().toString();
        String newMeaning = etNewMeaning.getText().toString();
        String newExample = etNewExample.getText().toString();

        Spinner spinner1 = (Spinner) dialogEdit.findViewById(R.id.tag1);
        Spinner spinner2 = (Spinner) dialogEdit.findViewById(R.id.tag2);
        Spinner spinner3 = (Spinner) dialogEdit.findViewById(R.id.tag3);
        Spinner spinner4 = (Spinner) dialogEdit.findViewById(R.id.tag4);
        Spinner spinner5 = (Spinner) dialogEdit.findViewById(R.id.tag5);
        ArrayList<String> tagsArray = new ArrayList<String>();
        if (spinner1.getVisibility() == View.VISIBLE)
            tagsArray.add(spinner1.getSelectedItem().toString());
        if (spinner2.getVisibility() == View.VISIBLE)
            tagsArray.add(spinner2.getSelectedItem().toString());
        if (spinner3.getVisibility() == View.VISIBLE)
            tagsArray.add(spinner3.getSelectedItem().toString());
        if (spinner4.getVisibility() == View.VISIBLE)
            tagsArray.add(spinner4.getSelectedItem().toString());
        if (spinner5.getVisibility() == View.VISIBLE)
            tagsArray.add(spinner5.getSelectedItem().toString());
        String tags = "";
        for (int i = 0; i < tagsArray.size(); i++) {
            String str = tagsArray.get(i);
            if (i == 0) {
                tags = str;
            } else {
                tags += "," + str;
            }
        }

        if (isStringJustSpace(newWord)) {
            Toast.makeText(this, "The Card's Name is missing.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isStringJustSpace(newMeaning)) {
            Toast.makeText(this, "The Card's Meaning is missing.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (arrayItems.get(getPosition(dialogMeaningWordPosition)).getWord().toLowerCase().equals(newWord.toLowerCase()) &&
                arrayItems.get(getPosition(dialogMeaningWordPosition)).getMeaning().toLowerCase().equals(newMeaning.toLowerCase()) &&
                arrayItems.get(getPosition(dialogMeaningWordPosition)).getExample().toLowerCase().equals(newExample.toLowerCase()) &&
                arrayItems.get(getPosition(dialogMeaningWordPosition)).getTags().toLowerCase().equals(tags.toLowerCase())) {
            Toast.makeText(this, "Nothing has changed", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < arrayItems.size(); i++) {
            if (newWord.toLowerCase().equals(arrayItems.get(i).getWord().toLowerCase()) && !newWord.toLowerCase().equals(word.toLowerCase())) {
                Toast.makeText(this, "The Word exists in the databaseMain", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    boolean isStringJustSpace(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch != ' ') return false;
        }
        return true;
    }


    void restore(Bundle icicle) {
        if (icicle != null) {
            dialogAddNewIsOpen = icicle.getBoolean("dialogAddNewIsOpen");
            dialogMeaningIsOpen = icicle.getBoolean("dialogMeaningIsOpen");
            dialogEditIsOpen = icicle.getBoolean("dialogEditIsOpen");
            dialogAskDeleteIsOpen = icicle.getBoolean("dialogAskDeleteIsOpen");
            dialogNewPostIsOpen = icicle.getBoolean("dialogNewPostIsOpen");
            dialogExpireIsOpen = icicle.getBoolean("dialogExpireIsOpen");
            dialogSortByIsOpen = icicle.getBoolean("dialogSortByIsOpen");
            dialogRateIsOpen = icicle.getBoolean("dialogRateIsOpen");
            listViewPosition = icicle.getParcelable("listViewPosition");
            markSeveral = icicle.getBoolean("markSeveral");
            isFromSearch = icicle.getBoolean("isFromSearch");
        }
        if (dialogAddNewIsOpen) {
            if (!dialogAddNew.isShowing())
                dialogAddNew();
            EditText wordAddNew = (EditText) dialogAddNew.findViewById(R.id.etWord);
            EditText meaningAddNew = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
            EditText exampleAddNew = (EditText) dialogAddNew.findViewById(R.id.etExample);
            wordAddNew.setText(icicle.getString("addWord"));
            meaningAddNew.setText(icicle.getString("addMeaning"));
            exampleAddNew.setText(icicle.getString("addExample"));

            CheckBox chDoOrDoNot = (CheckBox) dialogAddNew.findViewById(R.id.chDoOrDoNot);
            chDoOrDoNot.setChecked(icicle.getBoolean("chDoOrDoNot"));

            Spinner chSpinner1 = (Spinner) dialogAddNew.findViewById(R.id.tag1);
            Spinner chSpinner2 = (Spinner) dialogAddNew.findViewById(R.id.tag2);
            Spinner chSpinner3 = (Spinner) dialogAddNew.findViewById(R.id.tag3);
            Spinner chSpinner4 = (Spinner) dialogAddNew.findViewById(R.id.tag4);
            Spinner chSpinner5 = (Spinner) dialogAddNew.findViewById(R.id.tag5);
            TextView tvRemove1 = (TextView) dialogAddNew.findViewById(R.id.tvRemove1);
            TextView tvRemove2 = (TextView) dialogAddNew.findViewById(R.id.tvRemove2);
            TextView tvRemove3 = (TextView) dialogAddNew.findViewById(R.id.tvRemove3);
            TextView tvRemove4 = (TextView) dialogAddNew.findViewById(R.id.tvRemove4);
            TextView tvRemove5 = (TextView) dialogAddNew.findViewById(R.id.tvRemove5);
            TextView tvMore = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);

            boolean visibles[] = icicle.getBooleanArray("visibles");

            chSpinner1.setVisibility(visibles[0] ? View.VISIBLE : View.GONE);
            chSpinner2.setVisibility(visibles[1] ? View.VISIBLE : View.GONE);
            chSpinner3.setVisibility(visibles[2] ? View.VISIBLE : View.GONE);
            chSpinner4.setVisibility(visibles[3] ? View.VISIBLE : View.GONE);
            chSpinner5.setVisibility(visibles[4] ? View.VISIBLE : View.GONE);
            tvRemove1.setVisibility(visibles[5] ? View.VISIBLE : View.GONE);
            tvRemove2.setVisibility(visibles[6] ? View.VISIBLE : View.GONE);
            tvRemove3.setVisibility(visibles[7] ? View.VISIBLE : View.GONE);
            tvRemove4.setVisibility(visibles[8] ? View.VISIBLE : View.GONE);
            tvRemove5.setVisibility(visibles[9] ? View.VISIBLE : View.GONE);
            tvMore.setVisibility(visibles[10] ? View.VISIBLE : View.GONE);
            exampleAddNew.setVisibility(visibles[11] ? View.VISIBLE : View.GONE);

            String selectTags[] = icicle.getStringArray("selectTags");

            ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
            ArrayList<String> tagsStr = database.getTags(false);
            for (String str : tagsStr) {
                tags.add(str);
            }
            chSpinner1.setSelection(tags.getPosition(selectTags[0]));
            chSpinner2.setSelection(tags.getPosition(selectTags[1]));
            chSpinner3.setSelection(tags.getPosition(selectTags[2]));
            chSpinner4.setSelection(tags.getPosition(selectTags[3]));
            chSpinner5.setSelection(tags.getPosition(selectTags[4]));
        }
        if (dialogMeaningIsOpen) {
            refreshListViewData(false);
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            if (!dialogMeaning.isShowing())
                dialogMeaning(dialogMeaningWordPosition, getPosition(dialogMeaningWordPosition));
        }
        if (dialogEditIsOpen) {
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            if (!dialogEdit.isShowing())
                dialogEdit(isFromSearch, dialogMeaningWordPosition, getPosition(dialogMeaningWordPosition));
            EditText wordAddNew = (EditText) dialogEdit.findViewById(R.id.etWord);
            EditText meaningAddNew = (EditText) dialogEdit.findViewById(R.id.etMeaning);
            EditText exampleAddNew = (EditText) dialogEdit.findViewById(R.id.etExample);
            wordAddNew.setText(icicle.getString("editWord"));
            meaningAddNew.setText(icicle.getString("editMeaning"));
            exampleAddNew.setText(icicle.getString("editExample"));

            Spinner chSpinner1 = (Spinner) dialogEdit.findViewById(R.id.tag1);
            Spinner chSpinner2 = (Spinner) dialogEdit.findViewById(R.id.tag2);
            Spinner chSpinner3 = (Spinner) dialogEdit.findViewById(R.id.tag3);
            Spinner chSpinner4 = (Spinner) dialogEdit.findViewById(R.id.tag4);
            Spinner chSpinner5 = (Spinner) dialogEdit.findViewById(R.id.tag5);
            TextView tvRemove1 = (TextView) dialogEdit.findViewById(R.id.tvRemove1);
            TextView tvRemove2 = (TextView) dialogEdit.findViewById(R.id.tvRemove2);
            TextView tvRemove3 = (TextView) dialogEdit.findViewById(R.id.tvRemove3);
            TextView tvRemove4 = (TextView) dialogEdit.findViewById(R.id.tvRemove4);
            TextView tvRemove5 = (TextView) dialogEdit.findViewById(R.id.tvRemove5);
            TextView tvMore = (TextView) dialogEdit.findViewById(R.id.tvAddMoreTag);

            boolean visibles[] = icicle.getBooleanArray("visibles");

            chSpinner1.setVisibility(visibles[0] ? View.VISIBLE : View.GONE);
            chSpinner2.setVisibility(visibles[1] ? View.VISIBLE : View.GONE);
            chSpinner3.setVisibility(visibles[2] ? View.VISIBLE : View.GONE);
            chSpinner4.setVisibility(visibles[3] ? View.VISIBLE : View.GONE);
            chSpinner5.setVisibility(visibles[4] ? View.VISIBLE : View.GONE);
            tvRemove1.setVisibility(visibles[5] ? View.VISIBLE : View.GONE);
            tvRemove2.setVisibility(visibles[6] ? View.VISIBLE : View.GONE);
            tvRemove3.setVisibility(visibles[7] ? View.VISIBLE : View.GONE);
            tvRemove4.setVisibility(visibles[8] ? View.VISIBLE : View.GONE);
            tvRemove5.setVisibility(visibles[9] ? View.VISIBLE : View.GONE);
            tvMore.setVisibility(visibles[10] ? View.VISIBLE : View.GONE);
            exampleAddNew.setVisibility(visibles[11] ? View.VISIBLE : View.GONE);

            String selectTags[] = icicle.getStringArray("selectTags");

            ArrayAdapter<String> tags = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
            ArrayList<String> tagsStr = database.getTags(false);
            for (String str : tagsStr) {
                tags.add(str);
            }
            chSpinner1.setSelection(tags.getPosition(selectTags[0]));
            chSpinner2.setSelection(tags.getPosition(selectTags[1]));
            chSpinner3.setSelection(tags.getPosition(selectTags[2]));
            chSpinner4.setSelection(tags.getPosition(selectTags[3]));
            chSpinner5.setSelection(tags.getPosition(selectTags[4]));
        }

        if (dialogAskDeleteIsOpen) {
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            if (!dialogAskDelete.isShowing()) {
//                dialogAskDelete(dialogMeaningWordPosition);
            }
            newWordEdit = icicle.getString("dialogEditWordText");
            newMeaningEdit = icicle.getString("dialogEditMeaningText");
        }
        if (dialogNewPostIsOpen) {
            showDialogNewPost();
        }

        if (dialogExpireIsOpen) {
            showDialogExpire();
        }

        if (dialogSortByIsOpen) {
            showDialogSortBy();
        }

        if (dialogRateIsOpen) {
            showDialogRate();
        }

        if (markSeveral) {
            checkedPositionsInt = icicle.getIntegerArrayList("checkedPositionsInt");
            refreshListViewData(false);
        }
    }


    void clearMarks() {
        for (int i = 0; i < arrayItemsToShow.size(); i++) {
            arrayItemsToShow.get(i).setChChecked(false);
            notifyCheckedPositionsInt();
        }
        adapterWords1.notifyDataSetChanged();
    }


    //btn add new word
    public void AddNew(View view) {
        if (!dialogAddNew.isShowing())
            dialogAddNew();
    }


    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);

        icicle.putParcelable("listViewPosition", items.onSaveInstanceState());
        icicle.putBoolean("isFromSearch", isFromSearch);

        if (!etSearch.getText().equals(null)) {
            icicle.putString("etSearchText", etSearch.getText().toString());
        } else {
            icicle.putString("etSearchText", "");
        }


        if (dialogAddNew.isShowing()) {
            EditText wordAddNew = (EditText) dialogAddNew.findViewById(R.id.etWord);
            EditText meaningAddNew = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
            EditText exampleAddNew = (EditText) dialogAddNew.findViewById(R.id.etExample);

            Spinner chSpinner1 = (Spinner) dialogAddNew.findViewById(R.id.tag1);
            Spinner chSpinner2 = (Spinner) dialogAddNew.findViewById(R.id.tag2);
            Spinner chSpinner3 = (Spinner) dialogAddNew.findViewById(R.id.tag3);
            Spinner chSpinner4 = (Spinner) dialogAddNew.findViewById(R.id.tag4);
            Spinner chSpinner5 = (Spinner) dialogAddNew.findViewById(R.id.tag5);
            TextView tvRemove1 = (TextView) dialogAddNew.findViewById(R.id.tvRemove1);
            TextView tvRemove2 = (TextView) dialogAddNew.findViewById(R.id.tvRemove2);
            TextView tvRemove3 = (TextView) dialogAddNew.findViewById(R.id.tvRemove3);
            TextView tvRemove4 = (TextView) dialogAddNew.findViewById(R.id.tvRemove4);
            TextView tvRemove5 = (TextView) dialogAddNew.findViewById(R.id.tvRemove5);
            TextView tvMore = (TextView) dialogAddNew.findViewById(R.id.tvAddMoreTag);

            boolean[] visibles = {
                    chSpinner1.getVisibility() == View.VISIBLE,
                    chSpinner2.getVisibility() == View.VISIBLE,
                    chSpinner3.getVisibility() == View.VISIBLE,
                    chSpinner4.getVisibility() == View.VISIBLE,
                    chSpinner5.getVisibility() == View.VISIBLE,
                    tvRemove1.getVisibility() == View.VISIBLE,
                    tvRemove2.getVisibility() == View.VISIBLE,
                    tvRemove3.getVisibility() == View.VISIBLE,
                    tvRemove4.getVisibility() == View.VISIBLE,
                    tvRemove5.getVisibility() == View.VISIBLE,
                    tvMore.getVisibility() == View.VISIBLE,
                    exampleAddNew.getVisibility() == View.VISIBLE
            };
            icicle.putBooleanArray("visibles", visibles);

            String selectTags[] = {
                    chSpinner1.getSelectedItem().toString(),
                    chSpinner2.getSelectedItem().toString(),
                    chSpinner3.getSelectedItem().toString(),
                    chSpinner4.getSelectedItem().toString(),
                    chSpinner5.getSelectedItem().toString(),
            };
           icicle.putStringArray("selectTags", selectTags);

            CheckBox chDoOrDoNot = (CheckBox) dialogAddNew.findViewById(R.id.chDoOrDoNot);
            icicle.putBoolean("chDoOrDoNot", chDoOrDoNot.isChecked());

            icicle.putBoolean("dialogAddNewIsOpen", dialogAddNew.isShowing());
            icicle.putString("addWord", wordAddNew.getText().toString());
            icicle.putString("addMeaning", meaningAddNew.getText().toString());
            icicle.putString("addExample", exampleAddNew.getText().toString());
        }

        if (dialogMeaning.isShowing()) {
            icicle.putBoolean("dialogMeaningIsOpen", dialogMeaning.isShowing());
            icicle.putInt("dialogMeaningWordPosition", dialogMeaningWordPosition);
            icicle.putBoolean("isFromSearch", isFromSearch);
        }

        if (dialogEdit.isShowing()) {
            icicle.putBoolean("dialogEditIsOpen", dialogEdit.isShowing());
            icicle.putInt("dialogMeaningWordPosition", dialogMeaningWordPosition);
            EditText wordAddNew = (EditText) dialogEdit.findViewById(R.id.etWord);
            EditText meaningAddNew = (EditText) dialogEdit.findViewById(R.id.etMeaning);
            EditText exampleAddNew = (EditText) dialogEdit.findViewById(R.id.etExample);

            icicle.putBoolean("dialogEditIsOpen", dialogEdit.isShowing());
            icicle.putString("editWord", wordAddNew.getText().toString());
            icicle.putString("editMeaning", meaningAddNew.getText().toString());
            icicle.putString("editExample", exampleAddNew.getText().toString());

            Spinner chSpinner1 = (Spinner) dialogEdit.findViewById(R.id.tag1);
            Spinner chSpinner2 = (Spinner) dialogEdit.findViewById(R.id.tag2);
            Spinner chSpinner3 = (Spinner) dialogEdit.findViewById(R.id.tag3);
            Spinner chSpinner4 = (Spinner) dialogEdit.findViewById(R.id.tag4);
            Spinner chSpinner5 = (Spinner) dialogEdit.findViewById(R.id.tag5);
            TextView tvRemove1 = (TextView) dialogEdit.findViewById(R.id.tvRemove1);
            TextView tvRemove2 = (TextView) dialogEdit.findViewById(R.id.tvRemove2);
            TextView tvRemove3 = (TextView) dialogEdit.findViewById(R.id.tvRemove3);
            TextView tvRemove4 = (TextView) dialogEdit.findViewById(R.id.tvRemove4);
            TextView tvRemove5 = (TextView) dialogEdit.findViewById(R.id.tvRemove5);
            TextView tvMore = (TextView) dialogEdit.findViewById(R.id.tvAddMoreTag);

            boolean[] visibles = {
                    chSpinner1.getVisibility() == View.VISIBLE,
                    chSpinner2.getVisibility() == View.VISIBLE,
                    chSpinner3.getVisibility() == View.VISIBLE,
                    chSpinner4.getVisibility() == View.VISIBLE,
                    chSpinner5.getVisibility() == View.VISIBLE,
                    tvRemove1.getVisibility() == View.VISIBLE,
                    tvRemove2.getVisibility() == View.VISIBLE,
                    tvRemove3.getVisibility() == View.VISIBLE,
                    tvRemove4.getVisibility() == View.VISIBLE,
                    tvRemove5.getVisibility() == View.VISIBLE,
                    tvMore.getVisibility() == View.VISIBLE,
                    exampleAddNew.getVisibility() == View.VISIBLE
            };
            icicle.putBooleanArray("visibles", visibles);

            String selectTags[] = {
                    chSpinner1.getSelectedItem().toString(),
                    chSpinner2.getSelectedItem().toString(),
                    chSpinner3.getSelectedItem().toString(),
                    chSpinner4.getSelectedItem().toString(),
                    chSpinner5.getSelectedItem().toString(),
            };
            icicle.putStringArray("selectTags", selectTags);
        }

        if (dialogAskDelete.isShowing()) {
            icicle.putInt("dialogMeaningWordPosition", dialogMeaningWordPosition);

            icicle.putBoolean("dialogAskDeleteIsOpen", dialogAskDelete.isShowing());

            icicle.putString("dialogEditWordText", newWordEdit);
            icicle.putString("dialogEditMeaningText", newMeaningEdit);
        }

        if (dialogNewPost.isShowing()) {
            icicle.putBoolean("dialogNewPostIsOpen", dialogNewPost.isShowing());
        }

        if (dialogExpire.isShowing()) {
            icicle.putBoolean("dialogExpireIsOpen", dialogExpire.isShowing());
        }

        if (dialogSortBy.isShowing()) {
            icicle.putBoolean("dialogSortByIsOpen", dialogSortBy.isShowing());
        }

        if (dialogRate.isShowing()) {
            icicle.putBoolean("dialogRateIsOpen", dialogRate.isShowing());
        }

        if (markSeveral) {
            icicle.putBoolean("markSeveral", markSeveral);

            icicle.putIntegerArrayList("checkedPositionsInt", checkedPositionsInt);
        }


    }


    void dialogAskDeleteByMark() {
        int countItems = 0;
        for (int i = 0; i < arrayItemsToShow.size(); i++)
            if (checkedPositionsInt.get(i) == 0)
                countItems++;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission To Delete");
        builder.setMessage("Are you sure you want to delete these '" + Integer.toString(countItems) + "' words ?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int i = 0;
                while (checkedPositionsInt.contains(0)) {
                    if (arrayItemsToShow.get(i).isChChecked()) {
                        int rPosition = getPosition(i);
                        arrayItemsToShow.get(i).setChChecked(false);
                        database.deleteItem(database.getItemId(arrayItems.get(rPosition).getWord(), arrayItems.get(rPosition).getMeaning()));
                        checkedPositionsInt.set(i, 1);
                        i = 0;
                        continue;
                    }
                    i++;
                }

                if (database.getItemsCount() < 1) {
                    markSeveral = false;
                }

                listViewPosition = items.onSaveInstanceState();
                refreshListViewData(true);
                notifyCheckedPositionsInt();
                if (isFromSearch && arrayItemsToShow.size() == 0) {
                    MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
                Toast.makeText(MainActivity.this, "Successfully deleted.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogAskDelete = builder.create();
        dialogAskDelete.show();
        dialogAskDelete.setCanceledOnTouchOutside(false);
    }


    void menu_Delete() {

        boolean arrayItemsCheckedIsEmpty = !checkedPositionsInt.contains(0);
        if (arrayItemsCheckedIsEmpty) {
            Toast.makeText(MainActivity.this, "You haven't selected any item.", Toast.LENGTH_SHORT).show();
        } else {
            dialogAskDeleteByMark();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        listViewPosition = items.onSaveInstanceState();
    }

    @Override
    public void onStop() {
        super.onStop();
        final View view = getLayoutInflater().inflate(R.layout.row_header, items, false);
        items.removeHeaderView(view);
    }


    @Override
    public void onResume() {
        super.onResume();
        getPrefs();
//        listViewPosition = items.onSaveInstanceState();
        refreshListViewData(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        dialogAddNew.dismiss();
        dialogEdit.dismiss();
        dialogMeaning.dismiss();
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
        if (markSeveral && database.getItemsCount() > 0) {
            getMenuInflater().inflate(R.menu.on_delete, menu);
            MenuItem itemMarkAll = menu.findItem(R.id.action_markAll);

            boolean isAllMarked = true;
            boolean isAllUnmark = true;

            notifyCheckedPositionsInt();
            for (int i = 0; i < arrayItemsToShow.size(); i++) {
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
            getMenuInflater().inflate(R.menu.main, menu);
        }

        MenuItem itemLeitner = menu.findItem(R.id.action_leitner);
        if (itemLeitner != null) {
            itemLeitner.setTitle("Leitner");
        }
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                MainActivity.this.startActivity(new Intent(MainActivity.this, Preferences.class));
                return true;
//            case R.id.action_about:
//                MainActivity.this.startActivity(new Intent(MainActivity.this, AboutActivity.class));
//                return true;

            case R.id.action_mark:
                if (arrayItemsToShow.size() > 0) {
                    markSeveral = true;
                    setElementsId();
                    listViewPosition = items.onSaveInstanceState();
                    refreshListViewData(false);

                } else {
                    Toast.makeText(MainActivity.this, "There is nothing to select!", Toast.LENGTH_SHORT).show();
                }
                return true;

//            case R.id.action_sort:
//                showDialogSortBy();

//                return true;

            case R.id.action_delete:
                menu_Delete();
                return true;


            case R.id.action_markAll:
                if (isToMarkAll) {
                    for (int i = 0; i < arrayItemsToShow.size(); i++) {
                        arrayItemsToShow.get(i).setChChecked(true);
                        notifyCheckedPositionsInt();
                    }
                    isToMarkAll = false;
                } else {
                    for (int i = 0; i < arrayItemsToShow.size(); i++) {
                        arrayItemsToShow.get(i).setChChecked(false);
                        notifyCheckedPositionsInt();
                    }
                    isToMarkAll = true;
                }
                adapterWords1.notifyDataSetChanged();
                return true;


            case R.id.action_cancel:
                markSeveral = false;
                clearMarks();
                setElementsId();
                listViewPosition = items.onSaveInstanceState();
                refreshListViewData(false);
                if (isFromSearch) {
                    search(etSearch.getText().toString());
                }
                return true;

            case R.id.action_leitner:
                MainActivity.this.startActivity(new Intent(MainActivity.this, LeitnerActivity.class));
                return true;

            case R.id.action_package:
                MainActivity.this.startActivity(new Intent(MainActivity.this, PackageActivity.class));
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    void dialogMeaning(final int position, final int realPosition) {
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflater.inflate(R.layout.dialog_meaning, null));
        builder.setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!dialogEdit.isShowing())
                    dialogEdit(isFromSearch, position, realPosition);
            }
        });
        builder.setNegativeButton(R.string.close, null);
        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!dialogAskDelete.isShowing())
                    dialogAskDelete(position, false);
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

        Custom j = arrayItems.get(realPosition);

        TextView tvDate = (TextView) dialogMeaning.findViewById(R.id.dmDate);
        TextView tvLastDate = (TextView) dialogMeaning.findViewById(R.id.dmLastDate);
        TextView tvWord = (TextView) dialogMeaning.findViewById(R.id.dmWord);
        TextView tvMeaning = (TextView) dialogMeaning.findViewById(R.id.dmMeaning);
        TextView tvExample = (TextView) dialogMeaning.findViewById(R.id.dmExample);
        TextView tvTags = (TextView) dialogMeaning.findViewById(R.id.dmTags);
        TextView tvCount = (TextView) dialogMeaning.findViewById(R.id.dmCount);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String currentDateAndTime = simpleDateFormat.format(new Date());

        dialogMeaningWordPosition = position;

        refreshItemsCount(position, realPosition);

        tvMeaning.setText(arrayItemsToShow.get(position).getMeaning());
        tvWord.setText(j.getWord());
        if (isStringJustSpace(j.getExample())) {
            tvExample.setText("-");
        } else {
            tvExample.setText(j.getExample());
        }
        if (isStringJustSpace(j.getTags())) {
            tvTags.setText("-");
        } else {
            String newTags = j.getTags().replace(",", "  #");
            newTags = "#" + newTags;
            tvTags.setText(newTags);
        }
        tvCount.setText(Integer.toString(arrayItemsToShow.get(position).getCount()));

        isDistanceTempAdd = isDistance;
        isDistanceTempLast = isDistance;
        if (isDistance.equals("distance")) {
            tvDate.setText(getDistance(j.getDate()));
            tvLastDate.setText(getDistance(j.getLastDate()));
        } else {
            tvDate.setText(arrayItemsToShow.get(position).getDate());
            tvLastDate.setText(j.getLastDate());
        }
        j.setLastDate(currentDateAndTime);

        final String word = tvWord.getText().toString();
        final String meaning = tvMeaning.getText().toString();
        final String example = j.getExample().toString();

        tvWord.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(word);
                return false;
            }
        });

        tvMeaning.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(meaning);
                return false;
            }
        });

        tvExample.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(example);
                return false;
            }
        });


        dialogMeaning.setCanceledOnTouchOutside(true);
    }

    public void tvDateOnClick(View view) {
        TextView tvAddDate = (TextView) dialogMeaning.findViewById(R.id.dmDate);

        if (isDistanceTempAdd.equals("date")) {
            isDistanceTempAdd = "distance";
            tvAddDate.setText(getDistance(arrayItems.get(getPosition(dialogMeaningWordPosition)).getDate()));
        } else {
            tvAddDate.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getDate());
            isDistanceTempAdd = "date";
        }
    }

    public void tvLastDateOnClick(View view) {
        TextView tvLastDate = (TextView) dialogMeaning.findViewById(R.id.dmLastDate);
        if (isDistanceTempLast.equals("date")) {
            isDistanceTempLast = "distance";
            tvLastDate.setText(getDistance(arrayItems.get(getPosition(dialogMeaningWordPosition)).getLastDate()));
        } else {
            tvLastDate.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getLastDate());
            isDistanceTempLast = "date";
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

    void backup() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                File currentDB = getDatabasePath(DatabaseHandler.DATABASE_NAME);
                String backupDBPath = "//My Dictionary//backups";
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
        }
    }


    void checkSiteForPosts() {
        final int last = mainPrefs.getInt("lastPost", 0);
        class FtpTask extends AsyncTask<Void, Integer, Void> {
            FTPClient con;
            boolean succeed = false;
            String error = "";
            String errorS = "";
            private Context context;

            String lastPostStr = "";
            int lastPostNum = 0;


            public FtpTask(Context context) { this.context = context; }

            protected void onPreExecute() {
                lastPostNum = last;
            }

            protected Void doInBackground(Void... args) {
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("mdftp@khaled.ir", "3k2oy8HRhs")) {
                        con.enterLocalPassiveMode(); // important!

                        InputStream inputStream;
                        BufferedReader r;

                        inputStream = con.retrieveFileStream(s + "lastpost" + s + "lastpost");
                        r = new BufferedReader(new InputStreamReader(inputStream));
                        lastPostStr = r.readLine();
                        inputStream.close();
                        r.close();
                        con.completePendingCommand();
                        if (Integer.parseInt(lastPostStr) > lastPostNum) {
                            lastPostNum = Integer.parseInt(lastPostStr);
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
                if (lastPostNum > last) {
                    showDialogNewPost();
                    editorMainPrefs.putInt("lastPost", lastPostNum);
                    editorMainPrefs.commit();
                }
            }

            protected void onProgressUpdate(Integer... args) {
            }
        }
        new FtpTask(MainActivity.this).execute();
    }

    void showDialogNewPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("site notification");
        builder.setMessage("There are new post in our blog about the application would you like to read them ?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uriUrl = Uri.parse("http://mydictionary.khaled.ir/");
//                            Uri uriUrl = Uri.parse("market://details?id=com.hister.mydictionary");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                editorMainPrefs.putInt("lastPost", lastPostNum);
            }
        });
        dialogNewPost = builder.create();
        if (!dialogNewPost.isShowing())
            dialogNewPost.show();
        dialogNewPost.setCanceledOnTouchOutside(false);
    }

    void checkSiteForVersionChange() throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        final String currentVersion = pInfo.versionName;
        class FtpTask extends AsyncTask<Void, Integer, Void> {
            FTPClient con;
            boolean succeed = false;
            String error = "";
            String errorS = "";
            private Context context;

            String newVersion = "";


            public FtpTask(Context context) { this.context = context; }

            protected void onPreExecute() {
                newVersion = currentVersion;
            }

            protected Void doInBackground(Void... args) {
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("mdftp@khaled.ir", "3k2oy8HRhs")) {
                        con.enterLocalPassiveMode(); // important!

                        InputStream inputStream;
                        BufferedReader r;

                        inputStream = con.retrieveFileStream(s + "versionPro" + s + "lastVersion");
                        r = new BufferedReader(new InputStreamReader(inputStream));
                        newVersion = r.readLine();
                        inputStream.close();
                        r.close();
                        con.completePendingCommand();
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
                if (!newVersion.equals(currentVersion)) {
                    showDialogExpire();
                    editorMainPrefs.putString("currentVersion", newVersion );
                    editorMainPrefs.commit();
                }
            }
            protected void onProgressUpdate(Integer... args) {
            }
        }
        new FtpTask(MainActivity.this).execute();

    }

    void showDialogExpire() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Version notification");
        builder.setMessage("a new version of app has been published this version would you like to see the details ?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Uri uriUrl = Uri.parse("http://mydictionary.khaled.ir/");
                Uri uriUrl = Uri.parse("market://details?id=ir.khaled.mydictionary");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });

        builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                editorMainPrefs.putInt("lastPost", lastPostNum);
            }
        });
        dialogExpire = builder.create();
        if (!dialogExpire.isShowing())
            dialogExpire.show();
        dialogExpire.setCanceledOnTouchOutside(false);

    }

    void countMe() {
        class FtpTask extends AsyncTask<Void, Integer, Void> {
            FTPClient con;
            double rand = 0;
            private Context context;

            public FtpTask(Context context) { this.context = context; }

            protected void onPreExecute()
            {
                rand = Math.random() * ((999999999) + 1);
            }

            protected Void doInBackground(Void... args) {
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("mdftp@khaled.ir", "3k2oy8HRhs")) {
                        con.enterLocalPassiveMode(); // important!
                        con.setFileType(FTP.BINARY_FILE_TYPE);
                        String userPath = s + "usersPro" + s + Double.toString(rand);


                        FileOutputStream outputStream;
                        outputStream = openFileOutput("userNumber", Context.MODE_PRIVATE);
                        outputStream.write(Double.toString(rand).getBytes());
                        outputStream.close();

                        con.storeFile(userPath, openFileInput("userNumber"));
                    }

                    con.logout();
                    con.disconnect();

                } catch (Exception e) {
                }
                return null;
            }

            protected void onPostExecute(Void result) {
            }

            protected void onProgressUpdate(Integer... args) {

            }

        }
        new FtpTask(this).execute();
    }

    void showDialogRate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Give us five stars");
        builder.setMessage("Are you satisfied with the application ?\nWe would be thankful if you rate us and let us know your opinion about our work");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uriUrl = Uri.parse("market://details?id=ir.khaled.mydictionary");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                editorMainPrefs.putBoolean("rated", true);
                editorMainPrefs.commit();
            }
        });

        builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogRate = builder.create();
        if (!dialogRate.isShowing())
            dialogRate.show();
    }

}