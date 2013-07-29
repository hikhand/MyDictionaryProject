package ir.khaled.mydictionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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

public class MainActivity extends FragmentActivity {


    DatabaseHandler database;
    DatabaseHandlerLeitner databaseLeitner;
    SharedPreferences prefs;
    SharedPreferences mainPrefs;
    SharedPreferences.Editor editorMainPrefs;

    public String newWordEdit;
    public String newMeaningEdit;

    public EditText etNewWord;
    public EditText etNewMeaning;
    public EditText etSearch;
    public ListView items;
    public Spinner sortBy;
    public Spinner sortWay;
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

    boolean dialogAddNewIsOpen = false;
    boolean dialogMeaningIsOpen = false;
    int dialogMeaningWordPosition = 0;
    boolean dialogEditIsOpen = false;
    boolean dialogAskDeleteIsOpen = false;
    boolean dialogNewPostIsOpen = false;
    boolean dialogExpireIsOpen = false;
    String searchMethod;
    boolean showItemNumber = true;
    boolean showItemMeaning = false;
    String isDistance;
    String isDistanceTemp;
    String sortMethod;
    private boolean markSeveral = false;
    Parcelable listViewPosition = null;
    ArrayList<Integer> checkedPositionsInt;
    boolean isToMarkAll = true;
    private boolean doubleBackToExitPressedOnce = false;
    boolean isLongClick = false;//for check items long click

    String searchText = "";

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
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);


        setElementsId();
        getPrefs();

        header();
        if (icicle != null) {
            listViewPosition = icicle.getParcelable("listViewPosition");
            searchText = icicle.getString("etSearchText");
        }
    etSearch.setText(searchText);

        refreshListViewData(false);
        sortByName();

        setImgAddVisibility();
        restore(icicle);

        listeners();

        checkSiteForPosts();
        checkSiteForVersionChange();
    }

//    void setElementsValue() {
//        if (mainPrefs.getBoolean("expireStart", false)) {
//            daysLeftExpire = mainPrefs.getInt("daysLeftExpire", 0);
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
//            todayDate = simpleDateFormat.format(new Date());
//            lastDate = mainPrefs.getString("lastDate", "lastDate");
//
//            if (lastDate.equals(todayDate)) {
//                todayNum = mainPrefs.getInt("lastDay", 0);
//            } else {
//                todayNum = mainPrefs.getInt("lastDay", 0) + 1;
//                editorMainPrefs.putInt("lastDay", todayNum);
//                editorMainPrefs.putString("lastDate", todayDate);
//                editorMainPrefs.putInt("daysLeftExpire", daysLeftExpire)
//                editorMainPrefs.commit();
//            }
//
//        }
//    }

    void header() {
//        final View view = getLayoutInflater().inflate(R.layout.row_header, items, false);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.sortBy, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sortBy = (Spinner) view.findViewById(R.id.sortBy);
//        sortBy.setAdapter(adapter);
//        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
//                R.array.sortWay, android.R.layout.simple_spinner_item);
//        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sortWay = (Spinner) view.findViewById(R.id.sortWay);
//        sortWay.setAdapter(adapter1);
//        items.addHeaderView(view, null, true);
//
//        sortBySelectedItem();
    }

    void sortBySelectedItem() {
//        sortBy.setSelection(sortMethod.equals("name") ? 0 : sortMethod.equals("date") ? 1 : 2);
    }

    void sortByName() {
//        if (sortBy.getSelectedItem().toString().equals("name")) {
//            ArrayList<String> words = new ArrayList<String>();
//            for (int i = 0; i < arrayItemsToShow.size(); i++) {
//                words.add(arrayItems.get(getPosition(i)).getWord());
//            }
//            Collections.sort(words);
//
//            for (int i = 0; i < arrayItemsToShow.size(); i++) {
//                arrayItemsToShow.get(i).setWord(words.get(i));
//                arrayItemsToShow.get(i).setChVisible(markSeveral);
//                arrayItemsToShow.get(i).setWord(showItemNumber ? i + 1 + ". " + arrayItemsToShow.get(i).getWord() : arrayItemsToShow.get(i).getWord());
//                arrayItemsToShow.get(i).setMeaningVisible(showItemMeaning);
//            }
//
//            adapterWords1.notifyDataSetChanged();
//            items.setAdapter(adapterWords1);
//        }
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

//        sortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this, "Wowww", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                Toast.makeText(MainActivity.this, "noWOW", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        sortWay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
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
        sortMethod = prefs.getString("sortMethod", "date");
    }

    public void setElementsId() {

        database = new DatabaseHandler(this);
        databaseLeitner = new DatabaseHandlerLeitner(this);
        mainPrefs = getSharedPreferences("main", MODE_PRIVATE);
        editorMainPrefs = mainPrefs.edit();

        if (mainPrefs.getBoolean("firstLogin", true)) {
            databaseLeitner.addItem(new Item("1", "1", "1"), "leitner");
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
                etNewWord = (EditText) dialog.findViewById(R.id.etWord);
                etNewMeaning = (EditText) dialog.findViewById(R.id.etMeaning);
                String newWord = etNewWord.getText().toString();
                String newMeaning = etNewMeaning.getText().toString();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                String currentDateAndTime = simpleDateFormat.format(new Date());

                database.addItem(new Custom(newWord, newMeaning, currentDateAndTime, 0));

                setImgAddVisibility();
                listViewPosition = items.onSaveInstanceState();
                refreshListViewData(false);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Successfully added.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void search(String key) {
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
        return new CustomShow(custom.getId(), custom.getWord(), custom.getMeaning(), custom.getDate(), custom.getCount());
    }
    Custom convertToCustom(CustomShow j) {
        return new Custom(j.getId(), j.getWord(), j.getMeaning(), j.getDate(), j.getCount());
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
                            dialogAskDelete(fakPositionToSendToDialogDelete);
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
        if (fromSearch) {
            etNewMeaning.setText(arrayItemsToShow.get(fakePosition).getMeaning());
            etNewWord.setText(arrayItems.get(realPosition).getWord());

        } else {
            etNewWord.setText(arrayItems.get(realPosition).getWord());
            etNewMeaning.setText(arrayItemsToShow.get(fakePosition).getMeaning());
        }

        TextView tvTotalCount = (TextView) dialogEdit.findViewById(R.id.tvTotalCount);
        TextView tvHeader = (TextView) dialogEdit.findViewById(R.id.tvHeader);
        tvTotalCount.setVisibility(View.INVISIBLE);
        tvHeader.setText("Edit An Item");


        dialogEdit.setCanceledOnTouchOutside(false);


        Button theButton = dialogEdit.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListenerEdit(dialogEdit, arrayItems.get(realPosition).getWord(), arrayItemsToShow.get(fakePosition).getMeaning()));
    }

    class CustomListenerEdit implements View.OnClickListener {
        private final Dialog dialog;
        private String word;
        private String meaning;


        public CustomListenerEdit(Dialog dialog, String word, String meaning) {
            this.dialog = dialog;
            this.word = word;
            this.meaning = meaning;
        }

        @Override
        public void onClick(View v) {
            if (isReadyEdit(word)) {
                etNewWord = (EditText) dialog.findViewById(R.id.etWord);
                etNewMeaning = (EditText) dialog.findViewById(R.id.etMeaning);
                newWordEdit = etNewWord.getText().toString();
                newMeaningEdit = etNewMeaning.getText().toString();

                Custom current = database.getItem(database.getItemId(word, meaning));
//                Log.i("current item", current.getWord() + "  " + current.getMeaning() + " " + current.getDate() + " " + current.getCount() + " " + current.getId());
                database.updateItem(new Custom(database.getItemId(word, meaning), newWordEdit, newMeaningEdit, current.getDate(), current.getCount()));
                int idLeitner = databaseLeitner.getItemId(word, meaning);
                if (idLeitner > 0) {
                    Item j = databaseLeitner.getItem(idLeitner);
                    databaseLeitner.updateItem(new Item(j.getId(), newWordEdit, newMeaningEdit,
                            j.getAddDate(), j.getLastCheckDate(), j.getLastCheckDay(),
                            j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount()));
                }
//                Log.i("after edit", x + " rows were effected");
//                Log.i("after edit", "word for edit: " + word + "  meaning: " + meaning);
                listViewPosition = items.onSaveInstanceState();
                refreshListViewData(false);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Successfully edited.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void dialogAskDelete(final int position) {
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
                if (!dialogEdit.isShowing())
                    dialogEdit(isFromSearch, position, getPosition(position));
                EditText dialogEditWord = (EditText) dialogEdit.findViewById(R.id.etWord);
                EditText dialogEditMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
                dialogEditWord.setText(newWordEdit);
                dialogEditMeaning.setText(newMeaningEdit);
            }
        });
        dialogAskDelete = builder.create();
        dialogAskDelete.show();
        dialogAskDelete.setCanceledOnTouchOutside(false);

    }


    void refreshItemsCount(int position, int realPosition) {
        int count = arrayItems.get(realPosition).getCount();
        int id = database.getItemId(arrayItems.get(realPosition).getWord(), arrayItems.get(realPosition).getMeaning());
        Custom current = database.getItem(id);
        database.updateItem(new Custom(id, current.getWord(), current.getMeaning(), current.getDate(), current.getCount() + 1));

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
            for (Custom custom : database.getAllItems())
                arrayItemsToShow.add(convertToShow(custom));
//            arrayItemsToShow.addAll(database.getAllItems());

            sortAlphabetical();

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

        if (arrayItemsToShow.size() > 0 )
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }


    void sortAlphabetical() {
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


    void setImgAddVisibility() {
        imgAdd = (ImageView) findViewById(R.id.imgAdd);
        imgAdd.setVisibility(View.GONE);
        if (database.getItemsCount() == 0) {
            imgAdd.setVisibility(View.VISIBLE);
        } else {
            imgAdd.setVisibility(View.GONE);
        }
    }


    //Get An Item's Real Position
    //
    //

//    int getPosition(final int position) {
//        for (int i = 0; i < database.getItemsCount(); i++) {
//            if (arrayItems.get(i).getId() == arrayItemsToShow.get(position).getId()) {
//                return i;
//            }
//        }
//        return 0;
//    }

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
            Toast.makeText(this, "The Word's Name is missing.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isStringJustSpace(newMeaning)) {
            Toast.makeText(this, "The Word's Meaning is missing.", Toast.LENGTH_SHORT).show();
            return false;
        }
        for (int i = 0; i < database.getItemsCount(); i++) {
//            if (newWord.equals(arrayItems.get(i).getWord()) && newMeaning.equals(arrayItems.get(i).getMeaning())) {
            if (newWord.toLowerCase().equals(arrayItems.get(i).getWord().toLowerCase())) {
                Toast.makeText(this, "The Word exists in the database", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


    public boolean isReadyEdit(String word) {
        etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
        etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
        String newWord = etNewWord.getText().toString();
        String newMeaning = etNewMeaning.getText().toString();


        if (isStringJustSpace(newWord)) {
            Toast.makeText(this, "The Word's Name is missing.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isStringJustSpace(newMeaning)) {
            Toast.makeText(this, "The Word's Meaning is missing.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (arrayItems.get(getPosition(dialogMeaningWordPosition)).getWord().toLowerCase().equals(newWord.toLowerCase()) && arrayItems.get(getPosition(dialogMeaningWordPosition)).getMeaning().toLowerCase().equals(newMeaning.toLowerCase())) {
            Toast.makeText(this, "Nothing has changed", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < database.getItemsCount(); i++) {
            if (newWord.toLowerCase().equals(arrayItems.get(i).getWord().toLowerCase()) && !newWord.toLowerCase().equals(word.toLowerCase())) {
                Toast.makeText(this, "The Word exists in the database", Toast.LENGTH_SHORT).show();
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
            listViewPosition = icicle.getParcelable("listViewPosition");
            markSeveral = icicle.getBoolean("markSeveral");
            isFromSearch = icicle.getBoolean("isFromSearch");
        }
        if (dialogAddNewIsOpen) {
            if (!dialogAddNew.isShowing())
                dialogAddNew();
            EditText wordAddNew = (EditText) dialogAddNew.findViewById(R.id.etWord);
            EditText meaningAddNew = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
            wordAddNew.setText(icicle.getString("editTextWordAddNew"));
            meaningAddNew.setText(icicle.getString("editTextMeaningAddNew"));
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
            wordAddNew.setText(icicle.getString("dialogEditWordText"));
            meaningAddNew.setText(icicle.getString("dialogEditMeaningText"));
        }
        if (dialogAskDeleteIsOpen) {
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            if (!dialogAskDelete.isShowing())
                dialogAskDelete(dialogMeaningWordPosition);
            newWordEdit = icicle.getString("dialogEditWordText");
            newMeaningEdit = icicle.getString("dialogEditMeaningText");
        }
        if (dialogNewPostIsOpen) {
            showDialogNewPost();
        }

        if (dialogExpireIsOpen) {
            showDialogExpire();
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

        EditText wordAddNew = (EditText) dialogAddNew.findViewById(R.id.etWord);
        EditText meaningAddNew = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
        icicle.putParcelable("listViewPosition", items.onSaveInstanceState());
        icicle.putBoolean("isFromSearch", isFromSearch);

        if (!etSearch.getText().equals(null)) {
            icicle.putString("etSearchText", etSearch.getText().toString());
        } else {
            icicle.putString("etSearchText", "");
        }


        if (dialogAddNew.isShowing()) {
            icicle.putBoolean("dialogAddNewIsOpen", dialogAddNew.isShowing());
            icicle.putString("editTextWordAddNew", wordAddNew.getText().toString());
            icicle.putString("editTextMeaningAddNew", meaningAddNew.getText().toString());
        }

        if (dialogMeaning.isShowing()) {
            icicle.putBoolean("dialogMeaningIsOpen", dialogMeaning.isShowing());
            icicle.putInt("dialogMeaningWordPosition", dialogMeaningWordPosition);
            icicle.putBoolean("isFromSearch", isFromSearch);
        }

        if (dialogEdit.isShowing()) {
            EditText dialogEditWord = (EditText) dialogEdit.findViewById(R.id.etWord);
            EditText dialogEditMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);

            icicle.putBoolean("dialogEditIsOpen", dialogEdit.isShowing());
            icicle.putInt("dialogMeaningWordPosition", dialogMeaningWordPosition);
            icicle.putString("dialogEditWordText", dialogEditWord.getText().toString());
            icicle.putString("dialogEditMeaningText", dialogEditMeaning.getText().toString());
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
            case R.id.action_about:
                MainActivity.this.startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;


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
        int currentApi = android.os.Build.VERSION.SDK_INT;
//        if (currentApi >= Build.VERSION_CODES.HONEYCOMB){
//            builder.setIconAttribute(android.R.drawable.ic_dialog_info);
//        }else {
        builder.setIcon(android.R.drawable.ic_dialog_info);
//        }
        dialogMeaning = builder.create();
        dialogMeaning.show();

        TextView tvDate = (TextView) dialogMeaning.findViewById(R.id.dmDate);
        TextView tvWord = (TextView) dialogMeaning.findViewById(R.id.dmWord);
        TextView tvMeaning = (TextView) dialogMeaning.findViewById(R.id.dmMeaning);
        TextView tvCount = (TextView) dialogMeaning.findViewById(R.id.dmCount);

        dialogMeaningWordPosition = position;

        refreshItemsCount(position, realPosition);

        tvMeaning.setText(arrayItemsToShow.get(position).getMeaning());
        tvWord.setText(arrayItems.get(realPosition).getWord());
        tvCount.setText(Integer.toString(arrayItemsToShow.get(position).getCount()));

        isDistanceTemp = isDistance;
        if (isDistance.equals("distance")) {
            changeDateToDistance();
        } else {
            tvDate.setText(arrayItemsToShow.get(position).getDate());
        }

        dialogMeaning.setCanceledOnTouchOutside(true);
    }




    public void tvDateOnClick(View view) {
        changeDateToDistanceOnClick();
    }

    void changeDateToDistance() {
        TextView etDate = (TextView) dialogMeaning.findViewById(R.id.dmDate);
        boolean thisHour = false;
        boolean today = false;
        boolean thisMonth = false;
        boolean thisYear = false;
        String originalDate = arrayItems.get(getPosition(dialogMeaningWordPosition)).getDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String currentDateAndTime = simpleDateFormat.format(new Date());

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = simpleDateFormat.parse(originalDate);
            d2 = simpleDateFormat.parse(currentDateAndTime);
        } catch (ParseException e) {
            e.printStackTrace();
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
            etDate.setText(diffMinutes == 0 ? "just now" : diffMinutes < 2 ? Long.toString(diffMinutes) + " minute ago" : Long.toString(diffMinutes) + " minutes ago");

        } else if (today) {
            etDate.setText(diffHours < 2 ? Long.toString(diffHours) + " hour ago" : Long.toString(diffHours) + " hours ago");

        } else if (thisMonth) {
            Long difDay = diffDays;
            Long difHour = diffHours;
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
            etDate.setText(strDistance);

        } else if (thisYear) {
            long difDay = diffDays;
            long difMonth = diffMonth;
            long difYear = diffYear;
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
            etDate.setText(strDistance);
        }
    }

    void changeDateToDistanceOnClick() {
        TextView etDate = (TextView) dialogMeaning.findViewById(R.id.dmDate);
        if (isDistanceTemp.equals("date")) {
            boolean thisHour = false;
            boolean today = false;
            boolean thisMonth = false;
            boolean thisYear = false;
            String originalDate = arrayItems.get(getPosition(dialogMeaningWordPosition)).getDate();
//            String originalDate = "2000/02/24 20:25";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String currentDateAndTime = simpleDateFormat.format(new Date());

            Date d1 = null;
            Date d2 = null;
            try {
                d1 = simpleDateFormat.parse(originalDate);
                d2 = simpleDateFormat.parse(currentDateAndTime);
            } catch (ParseException e) {
                e.printStackTrace();
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
                etDate.setText(diffMinutes == 0 ? "just now" : diffMinutes < 2 ? Long.toString(diffMinutes) + " minute ago" : Long.toString(diffMinutes) + " minutes ago");

            } else if (today) {
                etDate.setText(diffHours < 2 ? Long.toString(diffHours) + " hour ago" : Long.toString(diffHours) + " hours ago");

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
                etDate.setText(strDistance);

            } else if (thisYear) {
                long difDay = diffDays;
                long difMonth = diffMonth;
                long difYear = diffYear;
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
                etDate.setText(strDistance);
            }

                isDistanceTemp = "distance";
        } else {
            etDate.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getDate());
                isDistanceTemp = "date";
        }
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

    public void upload() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FTPClient con;

        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.toString());

            con = new FTPClient();
            con.connect(InetAddress.getByName("ftp.khaled.ir"));

            if (con.login("windowsp", "KHaledBLack73")) {
//                try {
//                    File sd = Environment.getExternalStorageDirectory();
//                    File data = Environment.getDataDirectory();
//
//                    if (sd.canWrite()) {
//                        String currentDBPath = "//data//ir.khaled.mydictionary//shared_prefs//Words.xml";
//                        String backupDBPath = "{database name}";
//                        File currentDB = new File(data, currentDBPath);
//                        File backupDB = new File(sd, backupDBPath);
//
//                        if (currentDB.exists()) {
//                            FileChannel src = new FileInputStream(currentDB).getChannel();
//                            FileChannel dst = new FileOutputStream(backupDB).getChannel();
//                            dst.transferFrom(src, 0, src.size());
//                            src.close();
//                            dst.close();
//                        }
//                    }
//                } catch (Exception e) {
//                }

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


    void checkSiteForPosts() {
        final int last = mainPrefs.getInt("lastPost", 0);
        class FtpTask extends AsyncTask<Void, Integer, Void> {
            FTPClient con;
            boolean succeed = false;
            String error = "";
            String errorS = "";
            private Context context;
            String s = File.separator;

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

                    if (con.login("windowsp", "KHaledBLack73")) {
                        con.enterLocalPassiveMode(); // important!

                        InputStream inputStream;
                        BufferedReader r;

                        inputStream = con.retrieveFileStream(s + "MyDictionary" + s + "lastpost" + s + "lastpost");
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

    void checkSiteForVersionChange() {
        final String currentVersion = mainPrefs.getString("currentVersion", "2.0.2");
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
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("windowsp", "KHaledBLack73")) {
                        con.enterLocalPassiveMode(); // important!

                        InputStream inputStream;
                        BufferedReader r;

                        inputStream = con.retrieveFileStream(s + "MyDictionary" + s + "versionPro" + s + "lastVersion");
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
            int rand = 0;
            private Context context;

            public FtpTask(Context context) { this.context = context; }

            protected void onPreExecute()
            {
                rand = (int) (Math.random() * ((999999999) + 1));
            }

            protected Void doInBackground(Void... args) {
                try {
                    con = new FTPClient();
                    con.connect(InetAddress.getByName("5.9.0.183"));

                    if (con.login("windowsp", "KHaledBLack73")) {
                        con.enterLocalPassiveMode(); // important!
                        con.setFileType(FTP.BINARY_FILE_TYPE);
//                        FileInputStream inMain;
                        String userPath = File.separator + "MyDictionary" + File.separator + "usersPro" + File.separator + Integer.toString(rand);


                        FileOutputStream outputStream;
                        outputStream = openFileOutput("userNumber", Context.MODE_PRIVATE);
                        outputStream.write(Integer.toString(rand).getBytes());
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

}
