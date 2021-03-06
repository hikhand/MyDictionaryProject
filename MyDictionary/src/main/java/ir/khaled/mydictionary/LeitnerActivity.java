package ir.khaled.mydictionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class LeitnerActivity extends Activity implements TextToSpeech.OnInitListener{

    DatabaseHandler databaseMain;
    DatabaseLeitner databaseLeitner;

    SharedPreferences prefs;
//    SharedPreferences mainPrefs;
//    SharedPreferences.Editor editorMainPrefs;

    public AlertDialog dialogAddNew;
    public AlertDialog dialogMeaning;
    public AlertDialog dialogEdit;
    public AlertDialog dialogAskDelete;
    public AlertDialog dialogSummery;

    EditText etSearch;
    Button btnAddNew;

    ArrayList<Custom> arrayItemsInMD;
    ArrayList<Item> arrayItems;
    ArrayList<ItemShow> itemsToShow;
    ArrayList<Integer> checkedPositionsInt;
    ArrayList<Integer> arrayIndexesLastDay;
    ArrayList<String> arrayIndexesLastDayDate;

    ListView items;
    AdapterLeitner adapter;

    boolean markSeveral = false;
    boolean showItemNumber = true;
    boolean isFromSearch = false;
    boolean isFromSearchDot = false;
    boolean isLongClick = false;
    boolean isToMarkAll = true;
    boolean dialogAddNewIsOpen = false;
    boolean dialogMeaningIsOpen = false;
    boolean dialogAskDeleteIsOpen = false;
    boolean dialogSummeryIsOpen = false;
    boolean dialogEditIsOpen = false;
    boolean answerViewed = false;
    boolean isFront = true;

    int dialogMeaningWordPosition = 0;
    int todayNum = 0;

    String isDistanceTempAdd = "";
    String isDistanceTempLast = "";
    String isDistance = "";
    String newWordEdit;
    String newMeaningEdit;
    String searchMethod;
    String todayDate = "";
    String lastDate = "";
    String sortMethod = "";
    String searchText = "";

    Parcelable listViewPosition = null;

    final String TABLE_LEITNER = "leitner";
    final String TABLE_DONT_ADD = "dontAdd";
    final String TABLE_ARCHIVE = "archive";

    private TextToSpeech tts;


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
        setContentView(R.layout.activity_leitner);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        setElementsId();
        if (savedInstanceState != null) {
            listViewPosition = savedInstanceState.getParcelable("listViewPosition");
            searchText = savedInstanceState.getString("etSearchText");
        }
        if (etSearch == null || searchText.equals(null)) {
            etSearch = (EditText) findViewById(R.id.leitnerSearchET);
            etSearch.setText("");
        } else {
            etSearch.setText(searchText);
        }

        setElementsValue();
        getPrefs();
        putNewFromMdToDatabase();
        refreshListViewData();
        listeners();
        restore(savedInstanceState);
    }

    void setElementsId() {
        tts = new TextToSpeech(this, this);

        databaseMain = new DatabaseHandler(this);
        databaseLeitner = new DatabaseLeitner(this);

        dialogAddNew = new AlertDialog.Builder(this).create();
        dialogMeaning = new AlertDialog.Builder(this).create();
        dialogEdit = new AlertDialog.Builder(this).create();
        dialogAskDelete = new AlertDialog.Builder(this).create();
        dialogSummery = new AlertDialog.Builder(this).create();

        etSearch = (EditText) findViewById(R.id.leitnerSearchET);
        btnAddNew = (Button) findViewById(R.id.leitnerAddNewBtn);

        arrayItemsInMD = new ArrayList<Custom>();
        arrayItems = new ArrayList<Item>();
        itemsToShow = new ArrayList<ItemShow>();
        checkedPositionsInt = new ArrayList<Integer>();

        items = (ListView) findViewById(R.id.leitnerListView);
        adapter = new AdapterLeitner(LeitnerActivity.this, R.layout.row, itemsToShow);

        arrayIndexesLastDay = new ArrayList<Integer>();
        arrayIndexesLastDayDate = new ArrayList<String>();
    }

    void setElementsValue() {
        try {
            if ("".equals("")) {
                int x = databaseLeitner.getItemsCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        arrayItemsInMD.addAll(databaseMain.getAllItems());
        arrayItems.addAll(databaseLeitner.getAllItems());
        arrayIndexesLastDay.addAll(databaseLeitner.getAllItemsLastDay());
        arrayIndexesLastDayDate.addAll(databaseLeitner.getAllItemsLastDayDate());
    }

    void putNewFromMdToDatabase() {
//        for (Custom J : arrayItemsInMD) {
//            boolean exists = false;
//            for (Item itemInLeitner : arrayItems) {
//                if (J.getWord().equals(itemInLeitner.getName()) &&
//                        J.getMeaning().equals(itemInLeitner.getMeaning())) {
//                    exists = true;
//                }
//            }
//            for (Item itemInDontAdd : arrayItemsDontAdd) {
//                if (J.getWord().equals(itemInDontAdd.getName()) &&
//                        J.getMeaning().equals(itemInDontAdd.getMeaning())) {
//                    exists = true;
//                }
//            }
//            if (!exists) {
//                databaseLeitner.addItem(new Item(J.getWord(), J.getMeaning(), J.getExample(), J.getTags(), J.getDate()), TABLE_LEITNER);
//            }
//        }
    }

    void refreshListViewData() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        todayDate = simpleDateFormat.format(new Date());
        lastDate = databaseLeitner.getLastDate();

        if (lastDate.equals(todayDate)) {
            todayNum = databaseLeitner.getLastDay();
        } else {
            todayNum = databaseLeitner.getLastDay() + 1;
            if (todayNum < 31 && todayNum > 1) {
                todayNum--;
                updateIndexesLastDayLessThan30();
                todayNum++;
            } else if (todayNum > 1) {
                todayNum--;
                updateIndexLastDayMoreThan30();
                todayNum++;
            }
        }

        arrayItems.clear();
        itemsToShow.clear();
        if (databaseLeitner.getItemsCount() > 0) {
            arrayItems.addAll(databaseLeitner.getAllItems());

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
                itemsToShow.add(new ItemShow("   Nothing found", "My Dictionary", "KHaledBLack73"));
                databaseLeitner.updateLastDate(todayDate);
                databaseLeitner.updateLastDay(todayNum);
                lastDate = todayDate;

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

        TextView tvSummery = (TextView) findViewById(R.id.leitnerSummeryTV);
        if (itemsToShow.size() > 0) {
            if (!itemsToShow.get(0).getName().equals("   Nothing found") && !itemsToShow.get(0).getMeaning().equals("My Dictionary"))
                tvSummery.setText("'" + Integer.toString(itemsToShow.size()) + "'");
            else tvSummery.setText("'" + Integer.toString(itemsToShow.size()-1) + "'");
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
        for (ItemShow item : itemsToShow) {
            words.add(item.getName());
        }
        Collections.sort(words);
        ArrayList<Item> buff = new ArrayList<Item>();
        for (ItemShow item: itemsToShow) {
            buff.add(convertToItem(item));
        }
        itemsToShow.clear();
        for (int i = 0; i < buff.size(); i++) {
            for (Item j : buff) {
                if (words.get(i).equals(j.getName())) {
                    itemsToShow.add(convertToItemShow(j));
                }
            }
        }
    }

    void sortNameD() {
        sortNameA();
        ArrayList<Item> buff = new ArrayList<Item>();
        for (ItemShow item: itemsToShow) {
            buff.add(convertToItem(item));
        }
        itemsToShow.clear();
        for (int i = buff.size()-1; i >= 0; i--) {
            itemsToShow.add(convertToItemShow(buff.get(i)));
        }
    }

    void sortDateD() {
        ArrayList<Item> buff = new ArrayList<Item>();
        for (ItemShow item: itemsToShow) {
            buff.add(convertToItem(item));
        }
        itemsToShow.clear();
        for (int i = buff.size()-1; i >= 0; i--) {
            itemsToShow.add(convertToItemShow(buff.get(i)));
        }
    }

    void sortCountA() {
        for (int i = 0; i < itemsToShow.size()-1; i++) {
            for (int j = 0; j < itemsToShow.size()-1; j++) {
                if (itemsToShow.get(j).getCount() > itemsToShow.get(j+1).getCount()) {
                    ItemShow temp = itemsToShow.get(j);
                    ItemShow temp1 = itemsToShow.get(j+1);
                    itemsToShow.set(j, temp1);
                    itemsToShow.set(j+1, temp);
                }
            }
        }
    }

    void sortCountD() {
        for (int i = 0; i < itemsToShow.size()-1; i++) {
            for (int j = 0; j < itemsToShow.size()-1; j++) {
                if (itemsToShow.get(j).getCount() < itemsToShow.get(j+1).getCount()) {
                    ItemShow temp = itemsToShow.get(j);
                    ItemShow temp1 = itemsToShow.get(j+1);
                    itemsToShow.set(j, temp1);
                    itemsToShow.set(j+1, temp);
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

        for (ItemShow item : itemsToShow) {
            if (!item.getName().equals("   Nothing found") && !item.getMeaning().equals("My Dictionary")) {
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

        for (Item item : arrayItems) {
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
        for (int i = 0; i < 5; i++)
        {
            deckTv[i].setText(Integer.toString(deck[i]));
        }

        TextView tAnswers = (TextView) dialogSummery.findViewById(R.id.tAnswers);
        TextView tCorrects = (TextView) dialogSummery.findViewById(R.id.tCorrects);
        TextView tIncorrects = (TextView) dialogSummery.findViewById(R.id.tIncorrects);
        TextView tCards = (TextView) dialogSummery.findViewById(R.id.tCards);
        TextView tDays = (TextView) dialogSummery.findViewById(R.id.tDays);

        int totalAnswers = 0, totalCorrects = 0, totalIncorrects = 0, totalCards = arrayItems.size(), totalDays = todayNum;
        for (Item item: arrayItems) {
            totalAnswers+= item.getCount();
            totalCorrects+= item.getCountCorrect();
            totalIncorrects+= item.getCountInCorrect();
        }

        tAnswers.setText("Total Answers: "+ totalAnswers);
        tCorrects.setText("Total Corrects: "+ totalCorrects);
        tIncorrects.setText("Total Incorrects: "+ totalIncorrects);
        tCards.setText("Total Cards: "+ totalCards);
        tDays.setText("Total Days: "+ totalDays);
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
                for (Item item : arrayItems) {
                    if (item.getIndex() == 0 && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 2: {
                for (Item item : arrayItems) {
                    if (item.getIndex() == 0 && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 3: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 4: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 5: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 6: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 7: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 8: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 9: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 10: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 11: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 12: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 13: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 14: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 15: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 7 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 16: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 8 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 17: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 9 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 18: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 10 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 19: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 11 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 20: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 12 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 21: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 13 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 22: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 14 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 23: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 7 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 24: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 8 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 25: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 9 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 26: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 10 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 27: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 11 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 28: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 12 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 29: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 13 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 30: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 14 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
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
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 15 || item.getIndex() == 7 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 16: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 16 || item.getIndex() == 8 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 17: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 17 || item.getIndex() == 9 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 18: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 18 || item.getIndex() == 10 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 19: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 19 || item.getIndex() == 11 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 20: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 20 || item.getIndex() == 12 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 21: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 21 || item.getIndex() == 13 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 22: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 22 || item.getIndex() == 14 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 23: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 23 || item.getIndex() == 7 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 24: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 24 || item.getIndex() == 8 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 25: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 25 || item.getIndex() == 9 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 26: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 26 || item.getIndex() == 10 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 27: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 27 || item.getIndex() == 11 || item.getIndex() == 3 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 28: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 28 || item.getIndex() == 12 || item.getIndex() == 4 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 29: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 29 || item.getIndex() == 13 || item.getIndex() == 5 || item.getIndex() == 1 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            case 30: {
                for (Item item : arrayItems) {
                    if ((item.getIndex() == 30 || item.getIndex() == 14 || item.getIndex() == 6 || item.getIndex() == 2 || item.getIndex() == 0) && item.getLastCheckDay() != todayNum) {
                        itemsToShow.add(convertToItemShow(item));
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    ItemShow convertToItemShow(Item j) {
        return new ItemShow(j.getId(), j.getName(), j.getMeaning(), j.getExample(), j.getTags(), j.getAddDate(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount());
    }

    Item convertToItem(ItemShow j) {
        return new Item(j.getId(), j.getName(), j.getMeaning(), j.getExample(), j.getTags(), j.getAddDate(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount());
    }

    void listeners() {
        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!itemsToShow.get(position).getName().equals("   Nothing found") && !itemsToShow.get(position).getMeaning().equals("My Dictionary")) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                    if (isLongClick) {
                        isLongClick = false;
                        return;
                    }

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
//                            itemsToShow.get(position).getAddDate().equals("KHaledBLack73")) /*&& position1 != 0*/) {
//                        dialogMeaning(position, getPosition(position));
//                    }
                }
            }
        });

        items.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isLongClick = true;
                if (!itemsToShow.get(position).getName().equals("   Nothing found") && !itemsToShow.get(position).getMeaning().equals("My Dictionary")) {
                    if (markSeveral) {
                        openOptionsMenu();
                    } else {
                        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        mVibrator.vibrate(30);
                        markSeveral = true;
                        int currentApi = android.os.Build.VERSION.SDK_INT;
                        if (currentApi >= Build.VERSION_CODES.HONEYCOMB) {
                            invalidateOptionsMenu();
                        }
//                        setElementsId();
                        listViewPosition = items.onSaveInstanceState();
                        refreshListViewData();
                        if (isFromSearch) {
                            search(etSearch.getText().toString());
                        }
                        itemsToShow.get(position).setChChecked(true);
                        adapter.notifyDataSetChanged();
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


    void dialogEdit(boolean fromSearch, int fakePosition) {
        final int fakPositionToSendToDialogDelete = fakePosition;
        final int realPosition = getPosition(fakePosition);
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
                            dialogMeaning(fakPositionToSendToDialogDelete);
                    }
                });

        dialogEdit = d.create();
        dialogEdit.show();

        dialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        EditText etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
        EditText etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);

        etNewWord.setText(arrayItems.get(realPosition).getName());
        etNewMeaning.setText(itemsToShow.get(fakePosition).getMeaning());


        EditText etExample = (EditText) dialogEdit.findViewById(R.id.etExample);

        Item j = arrayItems.get(realPosition);

        etNewWord.setText(j.getName());
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

        ArrayAdapter<String> tags = new ArrayAdapter<String>(LeitnerActivity.this, android.R.layout.simple_spinner_item);
        ArrayList<String> tagsStr = databaseMain.getTags(false);
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
                    EditText etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(etNewWord, InputMethodManager.SHOW_FORCED);
                }
            }
        });

        etNewMeaning.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    EditText etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
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
                EditText etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
                EditText etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
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
                EditText etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
                EditText etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
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
                EditText etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
                EditText etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
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
        theButton.setOnClickListener(new CustomListenerEdit(dialogEdit, arrayItems.get(realPosition).getName(), itemsToShow.get(fakePosition).getMeaning()));
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
                EditText etNewWord = (EditText) dialog.findViewById(R.id.etWord);
                EditText etNewMeaning = (EditText) dialog.findViewById(R.id.etMeaning);
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

                Item j = databaseLeitner.getItem(databaseLeitner.getItemId(word, meaning));
                databaseLeitner.updateItem(new Item(
                        databaseLeitner.getItemId(word, meaning), newWordEdit,
                        newMeaningEdit, newExample, tags, j.getAddDate(), j.getLastCheckDate(),
                        j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(),
                        j.getCountInCorrect(), j.getCount()));

                listViewPosition = items.onSaveInstanceState();
                refreshListViewData();
                dialog.dismiss();
                Toast.makeText(LeitnerActivity.this, "Successfully edited.", Toast.LENGTH_SHORT).show();
                dialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                LeitnerActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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


                Toast.makeText(LeitnerActivity.this, "Successfully deleted.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!dialogEdit.isShowing())
                    dialogEdit(isFromSearch, position);
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

    public void AddNew(View view) {
        if (!dialogAddNew.isShowing())
            dialogAddNew();
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

        CheckBox chDontToLeitner = (CheckBox) dialogAddNew.findViewById(R.id.chDoOrDoNot);
        chDontToLeitner.setText("Add to Dictionary too");


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


        ArrayAdapter<String> tags = new ArrayAdapter<String>(LeitnerActivity.this, android.R.layout.simple_spinner_item);
        ArrayList<String> tagsStr = databaseMain.getTags(false);
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


        EditText etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
        EditText etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.etMeaning);

        etNewWord.setFocusableInTouchMode(true);
        etNewMeaning.setFocusableInTouchMode(true);

        etNewWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    EditText etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(etNewWord, InputMethodManager.SHOW_FORCED);
                }
            }
        });

        etNewMeaning.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    EditText etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
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
                EditText etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
                EditText etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
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
                EditText etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
                EditText etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
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
                EditText etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
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
                EditText etNewWord = (EditText) dialog.findViewById(R.id.etWord);
                EditText etNewMeaning = (EditText) dialog.findViewById(R.id.etMeaning);
                EditText etNewExample = (EditText) dialog.findViewById(R.id.etExample);
                CheckBox chDontToLeitner = (CheckBox) dialogAddNew.findViewById(R.id.chDoOrDoNot);

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

                databaseLeitner.addItem(new Item(newWord, newMeaning, newExample, tags, currentDateAndTime), TABLE_LEITNER);

                if (chDontToLeitner.isChecked()) {
                    databaseMain.addItem(new Custom(newWord, newMeaning, newExample, tags, currentDateAndTime, currentDateAndTime, 0));
                }
                markSeveral = false;
                setElementsId();
                listViewPosition = items.onSaveInstanceState();
                refreshListViewData();
                clearMarks();
                dialog.dismiss();

                Toast.makeText(LeitnerActivity.this, "Successfully added.", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

    public boolean isReadyToAddNew() {
        EditText etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
        EditText etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
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
        for (int i = 0; i < arrayItems.size(); i++) {
//            if (newWord.equals(arrayItems.get(i).getName()) && newMeaning.equals(arrayItems.get(i).getMeaning())) {
            if (newWord.equals(arrayItems.get(i).getName())) {
                Toast.makeText(this, "The Word exists in the databaseMain", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
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

    int getPosition(int position) {
        int realPosition = 0;
        boolean found = false;
        for (int i = 0; i < arrayItems.size(); i++) {
            if (arrayItems.get(i).getName().equals(itemsToShow.get(position).getName()) &&
                    arrayItems.get(i).getMeaning().equals(itemsToShow.get(position).getMeaning())) {

                realPosition = i;
                break;
            }
            for (int j = 0; j < arrayItems.size(); j++) {
                if ((Integer.toString(j + 1) + ". " + arrayItems.get(i).getName()).equals(itemsToShow.get(position).getName()) &&
                        arrayItems.get(i).getMeaning().equals(itemsToShow.get(position).getMeaning())) {

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
            if (arrayItems.get(i).getName().toUpperCase().equals(word) &&
                    arrayItems.get(i).getMeaning().toUpperCase().equals(meaning)) {
                return i;
            }
        }
        return 0;
    }

    void delete(int realPosition, int showPosition) {
        Item item = arrayItems.get(realPosition);
        databaseLeitner.deleteItem(databaseLeitner.getItemId(item.getName(), item.getMeaning()));
            databaseLeitner.addItem(new Item(item.getName(), item.getMeaning(), item.getExample(), item.getTags(), item.getAddDate()), TABLE_DONT_ADD);
        arrayItems.remove(realPosition);
        itemsToShow.remove(showPosition);

        listViewPosition = items.onSaveInstanceState();
        refreshListViewData();

        if (isFromSearch && itemsToShow.size() == 0) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public boolean isReadyEdit(String word) {
        EditText etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
        EditText etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
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

        if (arrayItems.get(getPosition(dialogMeaningWordPosition)).getName().toLowerCase().equals(newWord.toLowerCase()) &&
                arrayItems.get(getPosition(dialogMeaningWordPosition)).getMeaning().toLowerCase().equals(newMeaning.toLowerCase()) &&
                arrayItems.get(getPosition(dialogMeaningWordPosition)).getExample().toLowerCase().equals(newExample.toLowerCase()) &&
                arrayItems.get(getPosition(dialogMeaningWordPosition)).getTags().toLowerCase().equals(tags.toLowerCase())) {            Toast.makeText(this, "Nothing has changed", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < arrayItems.size(); i++) {
            if (newWord.toLowerCase().equals(arrayItems.get(i).getName().toLowerCase()) && !newWord.toLowerCase().equals(word.toLowerCase())) {
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

    void search(String key) {
        char first[] = key.toCharArray();
        if (key.length() > 0 && first[0] == '.') {
            searchAdvanced(key);
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
                    String meaning = itemsToShow.get(i).getMeaning().toUpperCase();

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
                            itemsToShow.get(i).getMeaning().equals("My Dictionary") && itemsToShow.get(i).getAddDate().equals("KHaledBLack73"))) {
                        itemsToShow.get(i).setChVisible(markSeveral);
                        //whether show item's number or not
                        if (!(itemsToShow.get(i).getName().equals("   Nothing found") && itemsToShow.get(i).getMeaning().equals("My Dictionary") && itemsToShow.get(i).getAddDate().equals("KHaledBLack73")))
                            itemsToShow.get(i).setName(showItemNumber ? i + 1 + ". " + itemsToShow.get(i).getName() : itemsToShow.get(i).getName());
                    }
                }
                notifyCheckedPositionsInt();
            } else {
                itemsToShow.add(new ItemShow("   Nothing found", "My Dictionary", "KHaledBLack73"));
                adapter.notifyDataSetChanged();
            }
        }

    }

    void searchAdvanced(String key) {
        char first[] = key.toCharArray();

        if (key.length() > 1 && (first[1] == 't' || first[1] == 'T')) {
            searchByTag(key);
        } else if (key.length() > 1 && (first[1] == 'p' || first[1] == 'P')) {
            searchByPosition(key);
        } else {
            int found = 0;
            itemsToShow.clear();

            if (arrayItems.size() > 0) {
                key = key.toUpperCase();
                key = key.substring(1);
                for (Item arrayItem : arrayItems) {
                    String word = arrayItem.getName().toUpperCase();
                    String meaning = arrayItem.getMeaning().toUpperCase();
                    if (searchMethod.equals("wordsAndMeanings") ? word.contains(key) || meaning.contains(key) :
                            searchMethod.equals("justWords") ? word.contains(key) :
                                    meaning.contains(key)) {
                        found++;
                        itemsToShow.add(convertToItemShow(arrayItem));
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
                            itemsToShow.get(i).getMeaning().equals("My Dictionary") && itemsToShow.get(i).getAddDate().equals("KHaledBLack73"))) {
                        itemsToShow.get(i).setChVisible(markSeveral);
                        //whether show item's number or not
                        if (!(itemsToShow.get(i).getName().equals("   Nothing found") && itemsToShow.get(i).getMeaning().equals("My Dictionary") && itemsToShow.get(i).getAddDate().equals("KHaledBLack73")))
                            itemsToShow.get(i).setName(showItemNumber ? i + 1 + ". " + itemsToShow.get(i).getName() : itemsToShow.get(i).getName());
                    }
                }
                notifyCheckedPositionsInt();
                TextView tvSummery = (TextView) findViewById(R.id.leitnerSummeryTV);
                if (!itemsToShow.get(0).getName().equals("   Nothing found") && !itemsToShow.get(0).getMeaning().equals("My Dictionary")) {
                    tvSummery.setText("'" + Integer.toString(itemsToShow.size()) + "'");
                } else tvSummery.setText("'" + Integer.toString(itemsToShow.size() - 1) + "'");
                adapter.notifyDataSetChanged();
            } else {
                TextView tvSummery = (TextView) findViewById(R.id.leitnerSummeryTV);
                tvSummery.setText("'0'");
                itemsToShow.add(new ItemShow("   Nothing found", "My Dictionary", "KHaledBLack73"));
                adapter.notifyDataSetChanged();
            }
        }
    }

    void searchByTag(String key) {
        int found = 0;
        itemsToShow.clear();
        if (arrayItems.size() > 0) {
            key = key.toLowerCase();
            key = key.substring(2);
            for (Item j : arrayItems) {
                String tags = j.getTags().toLowerCase();
                if (tags.contains(key)) {
                    found++;
                    itemsToShow.add(convertToItemShow(j));
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
                        itemsToShow.get(i).getMeaning().equals("My Dictionary") && itemsToShow.get(i).getAddDate().equals("KHaledBLack73"))) {
                    itemsToShow.get(i).setChVisible(markSeveral);
                    //whether show item's number or not
                    if (!(itemsToShow.get(i).getName().equals("   Nothing found") && itemsToShow.get(i).getMeaning().equals("My Dictionary") && itemsToShow.get(i).getAddDate().equals("KHaledBLack73")))
                        itemsToShow.get(i).setName(showItemNumber ? i + 1 + ". " + itemsToShow.get(i).getName() : itemsToShow.get(i).getName());
                }
            }
            notifyCheckedPositionsInt();
            TextView tvSummery = (TextView) findViewById(R.id.leitnerSummeryTV);
            if (!itemsToShow.get(0).getName().equals("   Nothing found") && !itemsToShow.get(0).getMeaning().equals("My Dictionary")) {
                tvSummery.setText("'" + Integer.toString(itemsToShow.size()) + "'");
            } else tvSummery.setText("'" + Integer.toString(itemsToShow.size() - 1) + "'");
            adapter.notifyDataSetChanged();
        } else {
            TextView tvSummery = (TextView) findViewById(R.id.leitnerSummeryTV);
            tvSummery.setText("'0'");
            itemsToShow.add(new ItemShow("   Nothing found", "My Dictionary", "KHaledBLack73"));
            adapter.notifyDataSetChanged();
        }
    }

    void searchByPosition(String key) {
        int found = 0;
        itemsToShow.clear();

        if (arrayItems.size() > 0) {
            key = key.toLowerCase();
            key = key.substring(2);
            char first[] = key.toCharArray();
            for (Item j : arrayItems) {
                String deck = Integer.toString(j.getDeck());
                String index = Integer.toString(indexDeck(j.getIndex()));
                String key1 = "";
                if (key.length() > 1) {
                    key1 = key.substring(1);
                }
                if (key.length() == 1 && key.equals(deck)) {
                    found++;
                    itemsToShow.add(convertToItemShow(j));
                } else if (key.length() > 1 && first[0] == deck.toCharArray()[0] && index.contains(key1)) {
                    found++;
                    itemsToShow.add(convertToItemShow(j));
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
                        itemsToShow.get(i).getMeaning().equals("My Dictionary") && itemsToShow.get(i).getAddDate().equals("KHaledBLack73"))) {
                    itemsToShow.get(i).setChVisible(markSeveral);
                    //whether show item's number or not
                    if (!(itemsToShow.get(i).getName().equals("   Nothing found") && itemsToShow.get(i).getMeaning().equals("My Dictionary") && itemsToShow.get(i).getAddDate().equals("KHaledBLack73")))
                        itemsToShow.get(i).setName(showItemNumber ? i + 1 + ". " + itemsToShow.get(i).getName() : itemsToShow.get(i).getName());
                }
            }
            notifyCheckedPositionsInt();
            TextView tvSummery = (TextView) findViewById(R.id.leitnerSummeryTV);
            if (!itemsToShow.get(0).getName().equals("   Nothing found") && !itemsToShow.get(0).getMeaning().equals("My Dictionary")) {
                tvSummery.setText("'" + Integer.toString(itemsToShow.size()) + "'");
            } else tvSummery.setText("'" + Integer.toString(itemsToShow.size() - 1) + "'");
            adapter.notifyDataSetChanged();
        } else {
            TextView tvSummery = (TextView) findViewById(R.id.leitnerSummeryTV);
            tvSummery.setText("'0'");
            itemsToShow.add(new ItemShow("   Nothing found", "My Dictionary", "KHaledBLack73"));
            adapter.notifyDataSetChanged();
        }
    }





    void getPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        searchMethod = prefs.getString("searchMethod", "wordsAndMeanings");
        showItemNumber = prefs.getBoolean("showItemNumber", true);
        isDistance = prefs.getString("timeMethod", "distance");
        sortMethod = prefs.getString("sortMethod", "dateA");
    }


    void clearMarks() {
        for (int i = 0; i < itemsToShow.size(); i++) {
            itemsToShow.get(i).setChChecked(false);
            notifyCheckedPositionsInt();
        }
        adapter.notifyDataSetChanged();
    }


    void menu_Delete() {
        boolean arrayItemsCheckedIsEmpty = !checkedPositionsInt.contains(0);
        if (arrayItemsCheckedIsEmpty) {
            Toast.makeText(LeitnerActivity.this, "You haven't selected any item.", Toast.LENGTH_SHORT).show();
        } else {
            dialogAskDeleteByMark();
        }
    }

    void dialogAskDeleteByMark() {
        int countItems = 0;
        for (int i = 0; i < itemsToShow.size(); i++)
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
                    if (itemsToShow.get(i).isChChecked()) {
                        int rPosition = getPosition(i);
                        itemsToShow.get(i).setChChecked(false);
                        Item item = arrayItems.get(rPosition);
                        databaseLeitner.deleteItem(databaseLeitner.getItemId(item.getName(), item.getMeaning()));
                        databaseLeitner.addItem(new Item(item.getName(), item.getMeaning(), item.getExample(), item.getTags(), item.getAddDate()), TABLE_DONT_ADD);
                        checkedPositionsInt.set(i, 1);
                        i = 0;
                        continue;
                    }
                    i++;
                }

                if (databaseLeitner.getItemsCount() < 1) {
                    markSeveral = false;
                }
                listViewPosition = items.onSaveInstanceState();
                refreshListViewData();
                notifyCheckedPositionsInt();
                if (isFromSearch && itemsToShow.size() == 0) {
                    LeitnerActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
                Toast.makeText(LeitnerActivity.this, "Successfully deleted.", Toast.LENGTH_SHORT).show();
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

    public void tvAddDateOnClick(View view) {
        TextView tvAddDate = (TextView) dialogMeaning.findViewById(R.id.leitnerAddDate);

        if (isDistanceTempAdd.equals("date")) {
            isDistanceTempAdd = "distance";
            tvAddDate.setText(getDistance(arrayItems.get(getPosition(dialogMeaningWordPosition)).getAddDate()));
        } else {
            tvAddDate.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getAddDate());
            isDistanceTempAdd = "date";
        }
    }

    public void tvLastDateOnClick(View view) {
        TextView tvLastDate = (TextView) dialogMeaning.findViewById(R.id.leitnerLastDate);
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
        builder.setView(inflater.inflate(R.layout.dialog_meaning_leitner, null));
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
        builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogEdit(isFromSearch, position);
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
        Item j = arrayItems.get(getPosition(position));

        TextView tvAddDate = (TextView) dialogMeaning.findViewById(R.id.leitnerAddDate);
        TextView tvLastDate = (TextView) dialogMeaning.findViewById(R.id.leitnerLastDate);
        TextView tvPosition = (TextView) dialogMeaning.findViewById(R.id.leitnerPosition);
        TextView tvCountCorrect = (TextView) dialogMeaning.findViewById(R.id.leitnerCountCorrect);
        TextView tvCount = (TextView) dialogMeaning.findViewById(R.id.leitnerCount);
        TextView tvCountInCorrect = (TextView) dialogMeaning.findViewById(R.id.leitnerCountInCorrect);
        TextView tvWordFront = (TextView) dialogMeaning.findViewById(R.id.lWordFront);
        TextView tvWordBack = (TextView) dialogMeaning.findViewById(R.id.lWordBack);
        TextView tvMeaning = (TextView) dialogMeaning.findViewById(R.id.lMeaning);
        TextView tvExample = (TextView) dialogMeaning.findViewById(R.id.lExample);
        TextView tvTags = (TextView) dialogMeaning.findViewById(R.id.lTags);


        TextView tvM = (TextView) dialogMeaning.findViewById(R.id.lTMeaning);
        TextView tvE = (TextView) dialogMeaning.findViewById(R.id.lTExamples);
        TextView tvT = (TextView) dialogMeaning.findViewById(R.id.lTTags);

        tvM.setVisibility(View.GONE);
        tvE.setVisibility(View.GONE);
        tvT.setVisibility(View.GONE);
        tvWordBack.setVisibility(View.GONE);
        tvMeaning.setVisibility(View.GONE);
        tvExample.setVisibility(View.GONE);
        tvTags.setVisibility(View.GONE);






        answerViewed = false;

        int index = indexDeck(j.getIndex());
        tvPosition.setText("at deck '" + Integer.toString(j.getDeck()) + "', index '" + Integer.toString(index) + "'");
        tvCountCorrect.setText(Integer.toString(j.getCountCorrect()));
        tvCount.setText(Integer.toString(j.getCount()));
        tvCountInCorrect.setText(Integer.toString(j.getCountInCorrect()));
        tvWordFront.setText(j.getName());
        tvWordBack.setText(j.getName());
        tvMeaning.setText(j.getMeaning());
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

        isDistanceTempAdd = isDistance;
        isDistanceTempLast = isDistance;
        if (isDistance.equals("distance")) {
            tvAddDate.setText(getDistance(j.getAddDate()));
            tvLastDate.setText(getDistance(j.getLastCheckDate()));
        } else {
            tvAddDate.setText(j.getAddDate());
            tvLastDate.setText(j.getLastCheckDate());
        }

        TextView tvPos = (TextView) dialogMeaning.findViewById(R.id.leitnerPos);
        tvPos.setText(Integer.toString(position + 1) + " of " + Integer.toString(itemsToShow.size()));
        dialogMeaning.setCanceledOnTouchOutside(true);

        final String wordFront = tvWordFront.getText().toString();
        final String wordBack = tvWordBack.getText().toString();
        final String meaning = tvMeaning.getText().toString();
        final String example = j.getExample().toString();


        tvWordFront.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(wordFront);
                return true;
            }
        });

        tvWordBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(30);

                speakOut(wordBack);
                return true;
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

        if (isFront) {
            goFront();
        } else {
            goBack();
        }

        Button btnPositive = dialogMeaning.getButton(DialogInterface.BUTTON_POSITIVE);
        Button btnNegative = dialogMeaning.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnPositive.setOnClickListener(new CustomListenerMeaning(dialogMeaning, position, true));
        btnNegative.setOnClickListener(new CustomListenerMeaning(dialogMeaning, position, false));
    }

    class CustomListenerMeaning implements View.OnClickListener {
        private final Dialog dialog;
        private final int position;
        private final boolean correct;
        public CustomListenerMeaning(Dialog dialog, int position, boolean correct) {
            this.dialog = dialog;
            this.position = position;
            this.correct = correct;
        }

        @Override
        public void onClick(View v) {
            if (!isFromSearch) {
                isFromSearchDot = false;
            }
            if (answerViewed && !isFromSearchDot) {
                if (correct) {
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
                Toast.makeText(LeitnerActivity.this, "you can't answer on review mode.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LeitnerActivity.this, "First check the answer by clicking on the word then answer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void name_click(View view) {
        answerViewed = true;
        goBack();
    }

    public void name_click2(View view) {
        answerViewed = true;
        goFront();
    }

    void goFront() {

        TextView tvWordFront = (TextView) dialogMeaning.findViewById(R.id.lWordFront);
        TextView tvWordBack = (TextView) dialogMeaning.findViewById(R.id.lWordBack);
        TextView tvMeaning = (TextView) dialogMeaning.findViewById(R.id.lMeaning);
        TextView tvExample = (TextView) dialogMeaning.findViewById(R.id.lExample);
        TextView tvTags = (TextView) dialogMeaning.findViewById(R.id.lTags);
        TextView tvM = (TextView) dialogMeaning.findViewById(R.id.lTMeaning);
        TextView tvE = (TextView) dialogMeaning.findViewById(R.id.lTExamples);
        TextView tvT = (TextView) dialogMeaning.findViewById(R.id.lTTags);

        tvWordFront.setVisibility(View.VISIBLE);

        tvM.setVisibility(View.GONE);
        tvE.setVisibility(View.GONE);
        tvT.setVisibility(View.GONE);
        tvWordBack.setVisibility(View.GONE);
        tvMeaning.setVisibility(View.GONE);
        tvExample.setVisibility(View.GONE);
        tvTags.setVisibility(View.GONE);
    }

    void goBack() {
        TextView tvWordFront = (TextView) dialogMeaning.findViewById(R.id.lWordFront);
        TextView tvWordBack = (TextView) dialogMeaning.findViewById(R.id.lWordBack);
        TextView tvMeaning = (TextView) dialogMeaning.findViewById(R.id.lMeaning);
        TextView tvExample = (TextView) dialogMeaning.findViewById(R.id.lExample);
        TextView tvTags = (TextView) dialogMeaning.findViewById(R.id.lTags);
        TextView tvM = (TextView) dialogMeaning.findViewById(R.id.lTMeaning);
        TextView tvE = (TextView) dialogMeaning.findViewById(R.id.lTExamples);
        TextView tvT = (TextView) dialogMeaning.findViewById(R.id.lTTags);

        tvWordFront.setVisibility(View.GONE);


        tvM.setVisibility(View.VISIBLE);
        tvE.setVisibility(View.VISIBLE);
        tvT.setVisibility(View.VISIBLE);
        tvWordBack.setVisibility(View.VISIBLE);
        tvMeaning.setVisibility(View.VISIBLE);
        tvExample.setVisibility(View.VISIBLE);
        tvTags.setVisibility(View.VISIBLE);
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
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 1, 0);
                break;

            case 1:  //to deck 2
                nextIndex = whichIndexTurnDeck(2);

                databaseLeitner.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databaseLeitner.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(2);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 2, nextIndex);
                break;

            case 2: // to deck 3
                nextIndex = whichIndexTurnDeck(4);

                databaseLeitner.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databaseLeitner.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(3);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 3, nextIndex);
                break;

            case 3: // to deck 4
                nextIndex = whichIndexTurnDeck(8);

                databaseLeitner.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databaseLeitner.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(4);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 4, nextIndex);
                break;

            case 4: // to deck 5
                nextIndex = whichIndexTurnDeck(16);

                databaseLeitner.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databaseLeitner.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(5);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 5, nextIndex);
                break;
            case 5: // to archive
                databaseLeitner.addItem(arrayItems.get(realPosition), TABLE_ARCHIVE);
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
        Item j = arrayItems.get(realPosition);

        databaseLeitner.updateLastDate(todayDate);
        databaseLeitner.updateLastDay(todayNum);
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

        databaseLeitner.updateItem(new Item(j.getId(), j.getName(), j.getMeaning(),
                j.getExample(), j.getTags(), j.getAddDate(), currentDateAndTime, todayNum,
                j.getDeck(), j.getIndex(),
                countCorrect, countIncorrect, count));

        listViewPosition = items.onSaveInstanceState();
        refreshListViewData();

        if (position != itemsToShow.size() && (!itemsToShow.get(position).getName().equals("   Nothing found"))) {
            dialogMeaning(position);
        }
    }


    void updateIndexesLastDayLessThan30() {
        switch (todayNum) {
            case 1: {
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 2: {
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 3: {
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 4: {
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 5: {
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 6: {
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 7: {
                databaseLeitner.updateItemLastDays(8, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(8, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(7, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(7, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 8: {
                databaseLeitner.updateItemLastDays(9, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(9, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(8, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(8, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 9: {
                databaseLeitner.updateItemLastDays(10, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(102, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(9, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(9, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 10: {
                databaseLeitner.updateItemLastDays(11, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(11, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(10, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(10, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 11: {
                databaseLeitner.updateItemLastDays(12, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(12, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(11, todayNum);
                arrayIndexesLastDay.set(3, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(11, lastDate);
                arrayIndexesLastDayDate.set(3, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 12: {
                databaseLeitner.updateItemLastDays(13, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(13, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(12, todayNum);
                arrayIndexesLastDay.set(4, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(12, lastDate);
                arrayIndexesLastDayDate.set(4, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 13: {
                databaseLeitner.updateItemLastDays(14, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(14, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
                arrayIndexesLastDay.set(13, todayNum);
                arrayIndexesLastDay.set(5, todayNum);
                arrayIndexesLastDay.set(1, todayNum);
                arrayIndexesLastDayDate.set(13, lastDate);
                arrayIndexesLastDayDate.set(5, lastDate);
                arrayIndexesLastDayDate.set(1, lastDate);
                break;
            }
            case 14: {
                databaseLeitner.updateItemLastDays(15, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(15, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
                arrayIndexesLastDay.set(14, todayNum);
                arrayIndexesLastDay.set(6, todayNum);
                arrayIndexesLastDay.set(2, todayNum);
                arrayIndexesLastDayDate.set(14, lastDate);
                arrayIndexesLastDayDate.set(6, lastDate);
                arrayIndexesLastDayDate.set(2, lastDate);
                break;
            }
            case 15: {
                databaseLeitner.updateItemLastDays(16, todayNum);
                databaseLeitner.updateItemLastDays(8, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(16, lastDate);
                databaseLeitner.updateItemLastDaysDate(8, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(17, todayNum);
                databaseLeitner.updateItemLastDays(9, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(17, lastDate);
                databaseLeitner.updateItemLastDaysDate(9, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(18, todayNum);
                databaseLeitner.updateItemLastDays(10, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(18, lastDate);
                databaseLeitner.updateItemLastDaysDate(10, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(19, todayNum);
                databaseLeitner.updateItemLastDays(11, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(19, lastDate);
                databaseLeitner.updateItemLastDaysDate(11, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(20, todayNum);
                databaseLeitner.updateItemLastDays(12, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(20, lastDate);
                databaseLeitner.updateItemLastDaysDate(12, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(21, todayNum);
                databaseLeitner.updateItemLastDays(13, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(21, lastDate);
                databaseLeitner.updateItemLastDaysDate(13, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(22, todayNum);
                databaseLeitner.updateItemLastDays(14, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(22, lastDate);
                databaseLeitner.updateItemLastDaysDate(14, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(23, todayNum);
                databaseLeitner.updateItemLastDays(15, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(23, lastDate);
                databaseLeitner.updateItemLastDaysDate(15, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(24, todayNum);
                databaseLeitner.updateItemLastDays(8, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(24, lastDate);
                databaseLeitner.updateItemLastDaysDate(8, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(25, todayNum);
                databaseLeitner.updateItemLastDays(9, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(25, lastDate);
                databaseLeitner.updateItemLastDaysDate(9, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(26, todayNum);
                databaseLeitner.updateItemLastDays(10, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(26, lastDate);
                databaseLeitner.updateItemLastDaysDate(10, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(27, todayNum);
                databaseLeitner.updateItemLastDays(11, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(27, lastDate);
                databaseLeitner.updateItemLastDaysDate(11, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(28, todayNum);
                databaseLeitner.updateItemLastDays(12, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(28, lastDate);
                databaseLeitner.updateItemLastDaysDate(12, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(29, todayNum);
                databaseLeitner.updateItemLastDays(13, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(29, lastDate);
                databaseLeitner.updateItemLastDaysDate(13, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(30, todayNum);
                databaseLeitner.updateItemLastDays(14, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(30, lastDate);
                databaseLeitner.updateItemLastDaysDate(14, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(31, todayNum);
                databaseLeitner.updateItemLastDays(15, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(31, lastDate);
                databaseLeitner.updateItemLastDaysDate(15, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(16, todayNum);
                databaseLeitner.updateItemLastDays(8, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(16, lastDate);
                databaseLeitner.updateItemLastDaysDate(8, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(17, todayNum);
                databaseLeitner.updateItemLastDays(9, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(17, lastDate);
                databaseLeitner.updateItemLastDaysDate(9, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(18, todayNum);
                databaseLeitner.updateItemLastDays(10, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(18, lastDate);
                databaseLeitner.updateItemLastDaysDate(10, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(19, todayNum);
                databaseLeitner.updateItemLastDays(11, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(19, lastDate);
                databaseLeitner.updateItemLastDaysDate(11, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(20, todayNum);
                databaseLeitner.updateItemLastDays(12, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(20, lastDate);
                databaseLeitner.updateItemLastDaysDate(12, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(21, todayNum);
                databaseLeitner.updateItemLastDays(13, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(21, lastDate);
                databaseLeitner.updateItemLastDaysDate(13, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(22, todayNum);
                databaseLeitner.updateItemLastDays(14, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(22, lastDate);
                databaseLeitner.updateItemLastDaysDate(14, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(23, todayNum);
                databaseLeitner.updateItemLastDays(15, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(23, lastDate);
                databaseLeitner.updateItemLastDaysDate(15, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(24, todayNum);
                databaseLeitner.updateItemLastDays(8, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(24, lastDate);
                databaseLeitner.updateItemLastDaysDate(8, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(25, todayNum);
                databaseLeitner.updateItemLastDays(9, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(25, lastDate);
                databaseLeitner.updateItemLastDaysDate(9, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(26, todayNum);
                databaseLeitner.updateItemLastDays(10, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(26, lastDate);
                databaseLeitner.updateItemLastDaysDate(10, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(27, todayNum);
                databaseLeitner.updateItemLastDays(11, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(27, lastDate);
                databaseLeitner.updateItemLastDaysDate(11, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(28, todayNum);
                databaseLeitner.updateItemLastDays(12, todayNum);
                databaseLeitner.updateItemLastDays(4, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(28, lastDate);
                databaseLeitner.updateItemLastDaysDate(12, lastDate);
                databaseLeitner.updateItemLastDaysDate(4, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(29, todayNum);
                databaseLeitner.updateItemLastDays(13, todayNum);
                databaseLeitner.updateItemLastDays(5, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(29, lastDate);
                databaseLeitner.updateItemLastDaysDate(13, lastDate);
                databaseLeitner.updateItemLastDaysDate(5, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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
                databaseLeitner.updateItemLastDays(30, todayNum);
                databaseLeitner.updateItemLastDays(14, todayNum);
                databaseLeitner.updateItemLastDays(6, todayNum);
                databaseLeitner.updateItemLastDays(2, todayNum);
                databaseLeitner.updateItemLastDaysDate(30, lastDate);
                databaseLeitner.updateItemLastDaysDate(14, lastDate);
                databaseLeitner.updateItemLastDaysDate(6, lastDate);
                databaseLeitner.updateItemLastDaysDate(2, lastDate);
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
                databaseLeitner.updateItemLastDays(31, todayNum);
                databaseLeitner.updateItemLastDays(15, todayNum);
                databaseLeitner.updateItemLastDays(7, todayNum);
                databaseLeitner.updateItemLastDays(3, todayNum);
                databaseLeitner.updateItemLastDaysDate(31, lastDate);
                databaseLeitner.updateItemLastDaysDate(15, lastDate);
                databaseLeitner.updateItemLastDaysDate(7, lastDate);
                databaseLeitner.updateItemLastDaysDate(3, lastDate);
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



    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);

        if (!etSearch.getText().equals(null)) {
            icicle.putString("etSearchText", etSearch.getText().toString());
        } else {
            icicle.putString("etSearchText", "");
        }

        icicle.putParcelable("listViewPosition", items.onSaveInstanceState());
        icicle.putBoolean("isFromSearch", isFromSearch);


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
            icicle.putBoolean("isFront", isFront);
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

        if (dialogSummery.isShowing()) {
            icicle.putBoolean("dialogSummeryIsOpen", dialogSummery.isShowing());
        }

        if (markSeveral) {
            icicle.putBoolean("markSeveral", markSeveral);

            icicle.putIntegerArrayList("checkedPositionsInt", checkedPositionsInt);
        }


    }

    void restore(Bundle icicle) {
        if (icicle != null) {
            dialogAddNewIsOpen = icicle.getBoolean("dialogAddNewIsOpen");
            dialogMeaningIsOpen = icicle.getBoolean("dialogMeaningIsOpen");
            dialogEditIsOpen = icicle.getBoolean("dialogEditIsOpen");
            dialogAskDeleteIsOpen = icicle.getBoolean("dialogAskDeleteIsOpen");
            dialogSummeryIsOpen = icicle.getBoolean("dialogSummeryIsOpen");
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

            ArrayAdapter<String> tags = new ArrayAdapter<String>(LeitnerActivity.this, android.R.layout.simple_spinner_item);
            ArrayList<String> tagsStr = databaseMain.getTags(false);
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
            refreshListViewData();
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            if (!dialogMeaning.isShowing())
                dialogMeaning(dialogMeaningWordPosition);
            isFront = icicle.getBoolean("isFront");
        }
        if (dialogEditIsOpen) {
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            if (!dialogEdit.isShowing())
                dialogEdit(isFromSearch, dialogMeaningWordPosition);
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

            ArrayAdapter<String> tags = new ArrayAdapter<String>(LeitnerActivity.this, android.R.layout.simple_spinner_item);
            ArrayList<String> tagsStr = databaseMain.getTags(false);
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
            if (!dialogAskDelete.isShowing())
                dialogAskDelete(dialogMeaningWordPosition);
            newWordEdit = icicle.getString("dialogEditWordText");
            newMeaningEdit = icicle.getString("dialogEditMeaningText");
        }
        if (dialogSummeryIsOpen) {
            if (!dialogSummery.isShowing())
                dialogSummery();
        }

        if (markSeveral) {
            checkedPositionsInt = icicle.getIntegerArrayList("checkedPositionsInt");
            refreshListViewData();
        }
    }

    @Override
    public void onBackPressed() {
        if (markSeveral) {
            adapter = new AdapterLeitner(LeitnerActivity.this, R.layout.row, itemsToShow);
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
//        listViewPosition = items.onSaveInstanceState();
        refreshListViewData();
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
            getMenuInflater().inflate(R.menu.leitner, menu);
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
                LeitnerActivity.this.startActivity(new Intent(LeitnerActivity.this, Preferences.class));
                return true;
//            case R.id.action_about:
//                LeitnerActivity.this.startActivity(new Intent(LeitnerActivity.this, AboutActivity.class));
//                return true;


            case R.id.action_mark:
                if (itemsToShow.size() > 0) {
                    markSeveral = true;
                    listViewPosition = items.onSaveInstanceState();
                    refreshListViewData();

                } else {
                    Toast.makeText(LeitnerActivity.this, "There is nothing to select!", Toast.LENGTH_SHORT).show();
                }
                return true;


            case R.id.action_delete:
                menu_Delete();
                return true;


            case R.id.action_markAll:
                if (isToMarkAll) {
                    for (int i = 0; i < itemsToShow.size(); i++) {
                        itemsToShow.get(i).setChChecked(true);
                        notifyCheckedPositionsInt();
                    }
                    isToMarkAll = false;
                } else {
                    for (int i = 0; i < itemsToShow.size(); i++) {
                        itemsToShow.get(i).setChChecked(false);
                        notifyCheckedPositionsInt();
                    }
                    isToMarkAll = true;
                }
                adapter.notifyDataSetChanged();
                return true;


            case R.id.action_cancel:
                markSeveral = false;
                clearMarks();
                setElementsId();
                listViewPosition = items.onSaveInstanceState();
                refreshListViewData();
                if (isFromSearch) {
                    search(etSearch.getText().toString());
                }
                return true;

            case R.id.action_dictionary:
//                LeitnerActivity.this.startActivity(new Intent(LeitnerActivity.this, LeitnerActivity.class));
                this.finish();
                return true;

//            case R.id.action_count_today:
//                databaseLeitner.updateLastDate(todayDate);
//                databaseLeitner.updateLastDay(todayNum);
//                lastDate = todayDate;
//                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}