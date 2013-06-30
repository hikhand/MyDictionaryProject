package ir.khaled.mydictionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {


    DatabaseHandler database;
    SharedPreferences prefs;


    public String newWordEdit;
    public String newMeaningEdit;

    public EditText etNewWord;
    public EditText etNewMeaning;
    public EditText etSearch;
    public ListView items;
    ImageView imgAdd;
    public boolean isFromSearch;

    ArrayList<Custom> arrayItems;
    ArrayList<Custom> arrayItemsToShow;


    public Adapter adapterWords1;


    public AlertDialog dialogAddNew;
    public AlertDialog dialogMeaning;
    public AlertDialog dialogEdit;
    public AlertDialog dialogAskDelete;

    boolean dialogAddNewIsOpen = false;
    boolean dialogMeaningIsOpen = false;
    int dialogMeaningWordPosition = 0;
    boolean dialogEditIsOpen = false;
    boolean dialogAskDeleteIsOpen = false;


    String searchMethod;
    boolean showItemNumber = true;
    boolean showItemMeaning = false;
    String isDistance;


    private boolean markSeveral = false;
    Parcelable listViewPosition = null;

    ArrayList<Integer> checkedPositionsInt;

    boolean isToMarkAll = true;



    private boolean doubleBackToExitPressedOnce = false;

    boolean isLongClick = false;//for check items long click

    @Override
    public void onBackPressed() {
        if (markSeveral) {
            adapterWords1 = new Adapter(MainActivity.this, R.layout.row, arrayItemsToShow);
            markSeveral = false;
            setElementsId();
            refreshListViewData(false);
            clearMarks();
        } else if (isFromSearch) {
            etSearch.setText("");
            isFromSearch = false;
            refreshListViewData(false);

        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
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
        refreshListViewData(false);
        getPrefs();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setImgAddVisibility();
        restore(icicle);

        listeners();

        if (database.getItemsCount() > 0)
            database.getItemId("Hello", "Salam");
    }


    //
    //
    //Listeners
    //
    //
    void listeners() {

        items.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if keyboard was up puts it down !!
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
                } else if (!(arrayItemsToShow.get(position).getWord().equals("   Nothing found") &&
                        arrayItemsToShow.get(position).getMeaning().equals("My Dictionary") && arrayItemsToShow.get(position).getDate().equals("KHaledBLack73"))) {
                    refreshItemsCount(position, getPosition(position));
                    dialogMeaning(position, getPosition(position));
                }
            }
        });

        items.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isLongClick = true;
                if (markSeveral) {
                    openOptionsMenu();
                } else {
                    markSeveral = true;
                    int currentApi = android.os.Build.VERSION.SDK_INT;
                    if (currentApi >= Build.VERSION_CODES.HONEYCOMB) {
                        invalidateOptionsMenu();
                    }
                    setElementsId();
                    refreshListViewData(false);
                    if (isFromSearch) {
                        search(etSearch.getText().toString());
                    }
                    arrayItemsToShow.get(position).setChChecked(true);
                    adapterWords1.notifyDataSetChanged();
                    notifyCheckedPositionsInt();
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

    //Get Preferences
    //
    //
    private void getPrefs() {
        // Get the xml/preferences.xml preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        searchMethod = prefs.getString("searchMethod", "wordsAndMeanings");
        showItemNumber = prefs.getBoolean("showItemNumber", true);
        showItemMeaning = prefs.getBoolean("showItemMeaning", false);
        isDistance = prefs.getString("timeMethod", "distance");
    }


    //Set Elements Id
    //
    //
    public void setElementsId() {

        database = new DatabaseHandler(this);

        items = (ListView) findViewById(R.id.listView);
        etSearch = (EditText) findViewById(R.id.etSearch);

        arrayItems = new ArrayList<Custom>();
        arrayItemsToShow = new ArrayList<Custom>();

        adapterWords1 = new Adapter(MainActivity.this, R.layout.row, arrayItemsToShow);

        dialogAddNew = new AlertDialog.Builder(this).create();
        dialogMeaning = new AlertDialog.Builder(this).create();
        dialogEdit = new AlertDialog.Builder(this).create();
        dialogAskDelete = new AlertDialog.Builder(this).create();

        if (checkedPositionsInt == null) {
            checkedPositionsInt = new ArrayList<Integer>();
        }

        listViewPosition = items.onSaveInstanceState();

    }


    //Dialogs Add New Word
    //
    //
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
                etNewWord = (EditText) dialog.findViewById(R.id.word);
                etNewMeaning = (EditText) dialog.findViewById(R.id.meaning);
                String newWord = etNewWord.getText().toString();
                String newMeaning = etNewMeaning.getText().toString();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                String currentDateAndTime = simpleDateFormat.format(new Date());

                database.addItem(new Custom(newWord, newMeaning, currentDateAndTime, 0));

                setImgAddVisibility();
                refreshListViewData(false);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Successfully added.", Toast.LENGTH_SHORT).show();
            }
        }
    }




    //Dialog Meaning
    //
    //


    //Search
    //
    //
    public void search(String key) {
        int found = 0;
        if (database.getItemsCount() > 0) {
            arrayItemsToShow.clear();
            for (int i = 0; i < database.getItemsCount(); i++) {
                key = key.toUpperCase();
                String word = arrayItems.get(i).getWord().toUpperCase();
                String meaning = arrayItems.get(i).getMeaning().toUpperCase();

                if (searchMethod.equals("wordsAndMeanings") ? word.contains(key) || meaning.contains(key) :
                        searchMethod.equals("justWords") ? word.contains(key) :
                                meaning.contains(key)) {

                    arrayItemsToShow.add(database.getItem(database.getItemId(arrayItems.get(i).getWord(), arrayItems.get(i).getMeaning())));
                    found++;
                }
            }
            if (found > 0) {
                adapterWords1.notifyDataSetChanged();
                items.setAdapter(adapterWords1);
            } else {
                arrayItemsToShow.add(new Custom("   Nothing found", "My Dictionary", "KHaledBLack73", false));
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


    //Dialog Edit
    //
    //
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
                        EditText dialogEditWord = (EditText) dialogEdit.findViewById(R.id.word);
                        EditText dialogEditMeaning = (EditText) dialogEdit.findViewById(R.id.meaning);
                        newWordEdit = dialogEditWord.getText().toString();
                        newMeaningEdit = dialogEditMeaning.getText().toString();
                        dialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                        dialogAskDelete(fakPositionToSendToDialogDelete);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogMeaning(fakPositionToSendToDialogDelete, realPositionToSendToDialogDelete);
                    }
                });

        dialogEdit = d.create();
        dialogEdit.show();

        dialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        etNewWord = (EditText) dialogEdit.findViewById(R.id.word);
        etNewMeaning = (EditText) dialogEdit.findViewById(R.id.meaning);
        if (fromSearch) {
            etNewMeaning.setText(arrayItemsToShow.get(fakePosition).getMeaning());
            etNewWord.setText(arrayItems.get(realPosition).getWord());

        } else {
            etNewWord.setText(arrayItems.get(realPosition).getWord());
            etNewMeaning.setText(arrayItemsToShow.get(fakePosition).getMeaning());
        }

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
            if (isReadyEdit()) {
                etNewWord = (EditText) dialog.findViewById(R.id.word);
                etNewMeaning = (EditText) dialog.findViewById(R.id.meaning);
                newWordEdit = etNewWord.getText().toString();
                newMeaningEdit = etNewMeaning.getText().toString();

                Custom current = database.getItem(database.getItemId(word, meaning));
                Log.i("current item", current.getWord() + "  " + current.getMeaning() + " " + current.getDate() + " " + current.getCount() + " " + current.getId());
                int x = database.updateItem(new Custom(database.getItemId(word, meaning), newWordEdit, newMeaningEdit, current.getDate(), current.getCount()));
                Log.i("after edit", x + " rows were effected");
                Log.i("after edit", "word for edit: " + word + "  meaning: " + meaning);
                refreshListViewData(false);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Successfully edited.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //Dialog Ask To Delete
    //
    //
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
                dialogEdit(isFromSearch, position, getPosition(position));
                EditText dialogEditWord = (EditText) dialogEdit.findViewById(R.id.word);
                EditText dialogEditMeaning = (EditText) dialogEdit.findViewById(R.id.meaning);
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


    //Delete
    //
    //
    void delete(int realPosition, int showPosition) {
        database.deleteItem(database.getItemId(arrayItems.get(realPosition).getWord(), arrayItems.get(realPosition).getMeaning()));
        Log.i("void delete", Integer.toString(database.getItemId(arrayItems.get(realPosition).getWord(), arrayItems.get(realPosition).getMeaning())));
        arrayItems.remove(realPosition);
        arrayItemsToShow.remove(showPosition);

        if (!isFromSearch) {
            setImgAddVisibility();
        }

        refreshListViewData(false);

        if (isFromSearch && arrayItemsToShow.size() == 0) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }


    //Refresh List View's Data
    //
    //
    void refreshListViewData(boolean isFromDeleteMark) {
        arrayItems.clear();
        arrayItemsToShow.clear();
        if (database.getItemsCount() > 0) {
            arrayItems.addAll(database.getAllItems());
            arrayItemsToShow.addAll(database.getAllItems());

            for (int i = 0; i < arrayItems.size(); i++) {

            }


            if (arrayItemsToShow.size() > 0) {
                for (int i = 0; i < arrayItemsToShow.size(); i++) {
                    arrayItemsToShow.get(i).setChVisible(markSeveral);
                    //whether show item's number or not
                    arrayItemsToShow.get(i).setWord(showItemNumber ? i + 1 + ". " + arrayItemsToShow.get(i).getWord() : arrayItemsToShow.get(i).getWord());
                    //whether show item's meaning or not
                    arrayItemsToShow.get(i).setMeaningVisible(showItemMeaning);
                }
            }
        }

        adapterWords1.notifyDataSetChanged();
        items.setAdapter(adapterWords1);
        items.onRestoreInstanceState(listViewPosition);

        if (isFromSearch) {
            search(etSearch.getText().toString());
        } else {
            setImgAddVisibility();
        }

    }


    //Set Image Add For First Time Visibility
    //
    //
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
        for (int i = 0; i < database.getItemsCount(); i++) {
            if (arrayItems.get(i).getWord().toUpperCase().equals(word) &&
                    arrayItems.get(i).getMeaning().toUpperCase().equals(meaning)) {
                return i;
            }
        }
        return 0;
    }


    //Check Is EveryThing's Ready To Add New Word
    //
    //
    public boolean isReadyToAddNew() {
        etNewWord = (EditText) dialogAddNew.findViewById(R.id.word);
        etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.meaning);
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
            if (newWord.equals(arrayItems.get(i).getWord()) && newMeaning.equals(arrayItems.get(i).getMeaning())) {
                Toast.makeText(this, "The Word exists in the database", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


    //Check if EveryThing's Ready To Edit A Word
    //
    //
    public boolean isReadyEdit() {
        etNewWord = (EditText) dialogEdit.findViewById(R.id.word);
        etNewMeaning = (EditText) dialogEdit.findViewById(R.id.meaning);
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

        if (arrayItems.get(getPosition(dialogMeaningWordPosition)).getWord().equals(newWord) && arrayItems.get(getPosition(dialogMeaningWordPosition)).getMeaning().equals(newMeaning)) {
            Toast.makeText(this, "every Thing's the same.", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < database.getItemsCount(); i++) {
            if (newWord.equals(arrayItems.get(i).getWord()) && newMeaning.equals(arrayItems.get(i).getMeaning())) {
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
            listViewPosition = icicle.getParcelable("listViewPosition");
            markSeveral = icicle.getBoolean("markSeveral");
            isFromSearch = icicle.getBoolean("isFromSearch");


        }
        if (dialogAddNewIsOpen) {
            dialogAddNew();
            EditText wordAddNew = (EditText) dialogAddNew.findViewById(R.id.word);
            EditText meaningAddNew = (EditText) dialogAddNew.findViewById(R.id.meaning);
            wordAddNew.setText(icicle.getString("editTextWordAddNew"));
            meaningAddNew.setText(icicle.getString("editTextMeaningAddNew"));
        }
        if (dialogMeaningIsOpen) {
            refreshListViewData(false);
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            dialogMeaning(dialogMeaningWordPosition, getPosition(dialogMeaningWordPosition));
        }
        if (dialogEditIsOpen) {
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            dialogEdit(isFromSearch, dialogMeaningWordPosition, getPosition(dialogMeaningWordPosition));
            EditText wordAddNew = (EditText) dialogEdit.findViewById(R.id.word);
            EditText meaningAddNew = (EditText) dialogEdit.findViewById(R.id.meaning);
            wordAddNew.setText(icicle.getString("dialogEditWordText"));
            meaningAddNew.setText(icicle.getString("dialogEditMeaningText"));
        }
        if (dialogAskDeleteIsOpen) {
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            dialogAskDelete(dialogMeaningWordPosition);
            newWordEdit = icicle.getString("dialogEditWordText");
            newMeaningEdit = icicle.getString("dialogEditMeaningText");

        }

        if (markSeveral) {
            listViewPosition = icicle.getParcelable("listViewPosition");
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
        dialogAddNew();
    }


    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);

        EditText wordAddNew = (EditText) dialogAddNew.findViewById(R.id.word);
        EditText meaningAddNew = (EditText) dialogAddNew.findViewById(R.id.meaning);


        icicle.putParcelable("listViewPosition", items.onSaveInstanceState());
        icicle.putBoolean("isFromSearch", isFromSearch);

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
            EditText dialogEditWord = (EditText) dialogEdit.findViewById(R.id.word);
            EditText dialogEditMeaning = (EditText) dialogEdit.findViewById(R.id.meaning);

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

    void doDeleteByMark() {

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
    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public void onResume() {
        super.onResume();
        getPrefs();
        refreshListViewData(false);
        items.onRestoreInstanceState(listViewPosition);

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
                refreshListViewData(false);
                if (isFromSearch) {
                    search(etSearch.getText().toString());
                }
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
//        ImageButton tvFavorite = (ImageButton) dialogMeaning.findViewById(R.id.dmFavorite);

        dialogMeaningWordPosition = position;


        tvMeaning.setText(arrayItemsToShow.get(position).getMeaning());
        tvWord.setText(arrayItems.get(realPosition).getWord());
        tvCount.setText(Integer.toString(arrayItemsToShow.get(position).getCount()));

        if (isDistance.equals("distance")) {
            changeDateToDistance();
        } else {
            tvDate.setText(arrayItemsToShow.get(position).getDate());
        }

        dialogMeaning.setCanceledOnTouchOutside(true);
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
            String completeDate[] = originalDate.split(" ");
            String justDate[] = completeDate[0].split("/");
            String justTime[] = completeDate[1].split(":");
            int year = Integer.parseInt(justDate[0]);
            int month = Integer.parseInt(justDate[1]);
            int day = Integer.parseInt(justDate[2]);
            int hour = Integer.parseInt(justTime[0]);
            int minute = Integer.parseInt(justTime[1]);


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String currentDateAndTime = simpleDateFormat.format(new Date());
            String cCompleteDate[] = currentDateAndTime.split(" ");
            String cJustDate[] = cCompleteDate[0].split("/");
            String cJustTime[] = cCompleteDate[1].split(":");
            int cYear = Integer.parseInt(cJustDate[0]);
            int cMonth = Integer.parseInt(cJustDate[1]);
            int cDay = Integer.parseInt(cJustDate[2]);
            int cHour = Integer.parseInt(cJustTime[0]);
            int cMinute = Integer.parseInt(cJustTime[1]);

            if (year == cYear && month == cMonth && day == cDay && hour == cHour) {
                thisHour = true;
            } else if (year == cYear && month == cMonth && day == cDay) {
                today = true;
            } else if (year == cYear && month == cMonth) {
                thisMonth = true;
            } else if (year == cYear) {
                thisYear = true;
            }

            if (thisHour) {
                int distanceMin = cMinute - minute;
                etDate.setText(distanceMin == 0 ? "just now" : distanceMin < 2 ? Integer.toString(distanceMin) + " minute ago" : Integer.toString(distanceMin) + " minutes ago");

            } else if (today) {
                int distanceHour = cHour - hour;
                etDate.setText(distanceHour < 2 ? Integer.toString(distanceHour) + " hour ago" : Integer.toString(distanceHour) + " hours ago");

            } else if (thisMonth) {
                int distanceDay = cDay - day;
                int distanceHour = cHour - hour;
                String strDistance;
                strDistance = distanceDay < 2 ? Integer.toString(distanceDay) + " day" : Integer.toString(distanceDay) + " days";
                strDistance += (distanceHour == 0 ? " ago"
                        : distanceHour < 2 ? " and " + Integer.toString(distanceHour) + " hour ago"
                        : " and " + Integer.toString(distanceHour) + " hours ago");

                etDate.setText(strDistance);

            } else if (thisYear) {
                int distanceYear = cYear - year;
                int distanceMonth = cMonth - month;
                int distanceDay = cDay - day;
                String strDistance;

                if (distanceYear == 0) {
                    strDistance = distanceMonth < 2 ? Integer.toString(distanceMonth) + " month" : Integer.toString(distanceMonth) + " months";
                    strDistance += (distanceDay == 0 ? " ago"
                            : distanceDay < 2 ? " and " + Integer.toString(distanceDay) + " day ago"
                            : " and " + Integer.toString(distanceDay) + " days ago");
                } else {
                    strDistance = distanceYear < 2 ? Integer.toString(distanceYear) + " year" : Integer.toString(distanceYear) + " years";
                    strDistance += (distanceMonth == 0 ? " ago"
                            : distanceMonth < 2 ? " and " + Integer.toString(distanceMonth) + " month ago"
                            : " and " + Integer.toString(distanceMonth) + " months ago");
                }
                etDate.setText(strDistance);
            }
    }


    void changeDateToDistanceOnClick() {
        TextView etDate = (TextView) dialogMeaning.findViewById(R.id.dmDate);
        if (isDistance == "date") {
            boolean thisHour = false;
            boolean today = false;
            boolean thisMonth = false;
            boolean thisYear = false;

            String originalDate = arrayItems.get(getPosition(dialogMeaningWordPosition)).getDate();
            String completeDate[] = originalDate.split(" ");
            String justDate[] = completeDate[0].split("/");
            String justTime[] = completeDate[1].split(":");
            int year = Integer.parseInt(justDate[0]);
            int month = Integer.parseInt(justDate[1]);
            int day = Integer.parseInt(justDate[2]);
            int hour = Integer.parseInt(justTime[0]);
            int minute = Integer.parseInt(justTime[1]);


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String currentDateAndTime = simpleDateFormat.format(new Date());
            String cCompleteDate[] = currentDateAndTime.split(" ");
            String cJustDate[] = cCompleteDate[0].split("/");
            String cJustTime[] = cCompleteDate[1].split(":");
            int cYear = Integer.parseInt(cJustDate[0]);
            int cMonth = Integer.parseInt(cJustDate[1]);
            int cDay = Integer.parseInt(cJustDate[2]);
            int cHour = Integer.parseInt(cJustTime[0]);
            int cMinute = Integer.parseInt(cJustTime[1]);

            if (year == cYear && month == cMonth && day == cDay && hour == cHour) {
                thisHour = true;
            } else if (year == cYear && month == cMonth && day == cDay) {
                today = true;
            } else if (year == cYear && month == cMonth) {
                thisMonth = true;
            } else if (year == cYear) {
                thisYear = true;
            }

            if (thisHour) {
                int distanceMin = cMinute - minute;
                etDate.setText(distanceMin == 0 ? "just now" : distanceMin < 2 ? Integer.toString(distanceMin) + " minute ago" : Integer.toString(distanceMin) + " minutes ago");

            } else if (today) {
                int distanceHour = cHour - hour;
                etDate.setText(distanceHour < 2 ? Integer.toString(distanceHour) + " hour ago" : Integer.toString(distanceHour) + " hours ago");

            } else if (thisMonth) {
                int distanceDay = cDay - day;
                int distanceHour = cHour - hour;
                String strDistance;
                strDistance = distanceDay < 2 ? Integer.toString(distanceDay) + " day" : Integer.toString(distanceDay) + " days";
                strDistance += (distanceHour == 0 ? " ago"
                        : distanceHour < 2 ? " and " + Integer.toString(distanceHour) + " hour ago"
                        : " and " + Integer.toString(distanceHour) + " hours ago");

                etDate.setText(strDistance);

            } else if (thisYear) {
                int distanceYear = cYear - year;
                int distanceMonth = cMonth - month;
                int distanceDay = cDay - day;
                String strDistance;

                if (distanceYear == 0) {
                    strDistance = distanceMonth < 2 ? Integer.toString(distanceMonth) + " month" : Integer.toString(distanceMonth) + " months";
                    strDistance += (distanceDay == 0 ? " ago"
                            : distanceDay < 2 ? " and " + Integer.toString(distanceDay) + " day ago"
                            : " and " + Integer.toString(distanceDay) + " days ago");
                } else {
                    strDistance = distanceYear < 2 ? Integer.toString(distanceYear) + " year" : Integer.toString(distanceYear) + " years";
                    strDistance += (distanceMonth == 0 ? " ago"
                            : distanceMonth < 2 ? " and " + Integer.toString(distanceMonth) + " month ago"
                            : " and " + Integer.toString(distanceMonth) + " months ago");
                }
                etDate.setText(strDistance);
            }
            isDistance = "distance";
        } else {
            etDate.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getDate());
            isDistance = "date";
        }
    }


;
}
