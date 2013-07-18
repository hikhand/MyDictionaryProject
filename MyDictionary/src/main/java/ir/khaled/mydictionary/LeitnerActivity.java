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
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LeitnerActivity extends Activity {

    DatabaseHandler databaseMain;
    DatabaseHandlerLeitner databaseLeitner;

    SharedPreferences prefs;
    SharedPreferences mainPrefs;
    SharedPreferences.Editor editorMainPrefs;

    public AlertDialog dialogAddNew;
    public AlertDialog dialogMeaning;
    public AlertDialog dialogEdit;
    public AlertDialog dialogAskDelete;

    EditText etSearch;
    Button btnAddNew;

    //    ArrayList<Item> deck0;
    ArrayList<Item> deck1;
    ArrayList<ArrayList<Item>> deck2;
    ArrayList<ArrayList<Item>> deck3;
    ArrayList<ArrayList<Item>> deck4;
    ArrayList<ArrayList<Item>> deck5;

    ArrayList<Custom> arrayItemsInMD;
    ArrayList<Item> arrayItemsDontAdd;
    ArrayList<Item> arrayItems;
    ArrayList<Item> itemsToShow;
    ArrayList<Integer> checkedPositionsInt;
    ArrayList<Integer> arrayIndexesLastDay;
    ArrayList<String> arrayIndexesLastDayDate;

    ListView items;
    AdapterLeitner adapter;

    boolean markSeveral = false;
    boolean showItemNumber = true;
    boolean isFromSearch = false;
    boolean isLongClick = false;
    boolean isToMarkAll = true;
    boolean dialogAddNewIsOpen = false;
    boolean dialogMeaningIsOpen = false;
    boolean dialogAskDeleteIsOpen = false;
    boolean dialogEditIsOpen = false;

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
    Parcelable listViewPosition = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitner);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setElementsId();
        setElementsValue();
        getPrefs();
        putNewFromMdToDatabase();
        listViewPosition = items.onSaveInstanceState();
        refreshListViewData();
        listeners();
        restore(savedInstanceState);
    }

    void setElementsId() {
        setIndexesId();

        mainPrefs = getSharedPreferences("main", MODE_PRIVATE);
        editorMainPrefs = mainPrefs.edit();

        databaseMain = new DatabaseHandler(this);
        databaseLeitner = new DatabaseHandlerLeitner(this);

        dialogAddNew = new AlertDialog.Builder(this).create();
        dialogMeaning = new AlertDialog.Builder(this).create();
        dialogEdit = new AlertDialog.Builder(this).create();
        dialogAskDelete = new AlertDialog.Builder(this).create();

        etSearch = (EditText) findViewById(R.id.leitnerSearchET);
        btnAddNew = (Button) findViewById(R.id.leitnerAddNewBtn);

        arrayItemsInMD = new ArrayList<Custom>();
        arrayItemsDontAdd = new ArrayList<Item>();
        arrayItems = new ArrayList<Item>();
        itemsToShow = new ArrayList<Item>();
        checkedPositionsInt = new ArrayList<Integer>();

        items = (ListView) findViewById(R.id.leitnerListView);
        adapter = new AdapterLeitner(LeitnerActivity.this, R.layout.row, itemsToShow);

        arrayIndexesLastDay = new ArrayList<Integer>();
        arrayIndexesLastDayDate = new ArrayList<String>();
    }

    void setElementsValue() {
        arrayItemsInMD.addAll(databaseMain.getAllItems());
        arrayItemsDontAdd.addAll(databaseLeitner.getAllItems(false));
        arrayItems.addAll(databaseLeitner.getAllItems(true));
        arrayIndexesLastDay.addAll(databaseLeitner.getAllItemsLastDay());
        arrayIndexesLastDayDate.addAll(databaseLeitner.getAllItemsLastDayDate());


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        todayDate = simpleDateFormat.format(new Date());
        lastDate = mainPrefs.getString("lastDate", "today");

        if (lastDate.equals(todayDate)) {
            todayNum = mainPrefs.getInt("lastDay", 0);
        } else {
            todayNum = mainPrefs.getInt("lastDay", 0) + 1;
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


    }

    void putNewFromMdToDatabase() {
        for (Custom itemInMD : arrayItemsInMD) {
            boolean exists = false;
            for (Item itemInLeitner : arrayItems) {
                if (itemInMD.getWord().equals(itemInLeitner.getName()) &&
                        itemInMD.getMeaning().equals(itemInLeitner.getMeaning())) {
                    exists = true;
                }
            }
            for (Item itemInDontAdd : arrayItemsDontAdd) {
                if (itemInMD.getWord().equals(itemInDontAdd.getName()) &&
                        itemInMD.getMeaning().equals(itemInDontAdd.getMeaning())) {
                    exists = true;
                }
            }
            if (!exists) {
                databaseLeitner.addItem(new Item(itemInMD.getWord(), itemInMD.getMeaning(), itemInMD.getDate()), true);
            }
        }
    }

    void setIndexesId() {
//        deck0 = new ArrayList<Item>();
        deck1 = new ArrayList<Item>();//0
        deck2 = new ArrayList<ArrayList<Item>>();
        deck3 = new ArrayList<ArrayList<Item>>();
        deck4 = new ArrayList<ArrayList<Item>>();
        deck5 = new ArrayList<ArrayList<Item>>();

        deck2.add(new ArrayList<Item>());//0--1
        deck2.add(new ArrayList<Item>());//1--2

        deck3.add(new ArrayList<Item>());//0--3
        deck3.add(new ArrayList<Item>());//1--4
        deck3.add(new ArrayList<Item>());//2--5
        deck3.add(new ArrayList<Item>());//3--6

        deck4.add(new ArrayList<Item>());//0--7
        deck4.add(new ArrayList<Item>());//1--8
        deck4.add(new ArrayList<Item>());//2--9
        deck4.add(new ArrayList<Item>());//3--10
        deck4.add(new ArrayList<Item>());//4--11
        deck4.add(new ArrayList<Item>());//5--12
        deck4.add(new ArrayList<Item>());//6--13
        deck4.add(new ArrayList<Item>());//7--14

        deck5.add(new ArrayList<Item>());//0--15
        deck5.add(new ArrayList<Item>());//1--16
        deck5.add(new ArrayList<Item>());//2--17
        deck5.add(new ArrayList<Item>());//3--18
        deck5.add(new ArrayList<Item>());//4--19
        deck5.add(new ArrayList<Item>());//5--20
        deck5.add(new ArrayList<Item>());//6--21
        deck5.add(new ArrayList<Item>());//7--22
        deck5.add(new ArrayList<Item>());//8--23
        deck5.add(new ArrayList<Item>());//9--24
        deck5.add(new ArrayList<Item>());//10--25
        deck5.add(new ArrayList<Item>());//11--26
        deck5.add(new ArrayList<Item>());//12--27
        deck5.add(new ArrayList<Item>());//13--28
        deck5.add(new ArrayList<Item>());//14--29
        deck5.add(new ArrayList<Item>());//15--30
    }

    void refreshListViewData() {
        arrayItems.clear();
        itemsToShow.clear();
        if (databaseLeitner.getItemsCount(true) > 0) {
            arrayItems.addAll(databaseLeitner.getAllItems(true));
            itemsToShow.addAll(databaseLeitner.getAllItems(true));

            refreshShow();

            if (itemsToShow.size() > 0) {
                int k = 1;
                for (int i = 0; i < itemsToShow.size(); i++) {
                    itemsToShow.get(i).setChVisible(markSeveral);
                    itemsToShow.get(i).setName(showItemNumber ? k + ". " + itemsToShow.get(i).getName() : itemsToShow.get(i).getName());
                    k++;
                }
            }
        }
        adapter.notifyDataSetChanged();
        items.setAdapter(adapter);

//        if (isFromSearch) {
//            search(etSearch.getText().toString());
//        } else {
//            setImgAddVisibility();
//        }
        if (itemsToShow.size() > 0)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (listViewPosition != null)
            items.onRestoreInstanceState(listViewPosition);
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
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if (itemsToShow.get(i).getIndex() != 0 || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 2: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 3: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 4: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 5: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 6: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
            }
            case 7: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 8: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 9: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 10: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 11: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 12: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 13: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 14: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 15: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 7 && itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 16: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 8 && itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 17: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 9 && itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 18: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 10 && itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 19: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 11 && itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 20: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 12 && itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 21: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 13 && itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 22: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 14 && itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 23: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 7 && itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 24: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 8 && itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 25: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 9 && itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 26: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 10 && itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 27: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 11 && itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 28: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 12 && itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 29: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 13 && itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 30: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 14 && itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    void refreshMoreThan30() {
        int lastIndexDeck5 = lastIndexMore30(deck5);
        switch (lastIndexDeck5) {
            case 15: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 15 && itemsToShow.get(i).getIndex() != 7 && itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 16: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 16 && itemsToShow.get(i).getIndex() != 8 && itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 17: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 17 && itemsToShow.get(i).getIndex() != 9 && itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 18: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 18 && itemsToShow.get(i).getIndex() != 10 && itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 19: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 19 && itemsToShow.get(i).getIndex() != 11 && itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 20: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 20 && itemsToShow.get(i).getIndex() != 12 && itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 21: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 21 && itemsToShow.get(i).getIndex() != 13 && itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 22: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 22 && itemsToShow.get(i).getIndex() != 14 && itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 23: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 23 && itemsToShow.get(i).getIndex() != 7 && itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 24: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 24 && itemsToShow.get(i).getIndex() != 8 && itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 25: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 25 && itemsToShow.get(i).getIndex() != 9 && itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 26: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 26 && itemsToShow.get(i).getIndex() != 10 && itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 27: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 27 && itemsToShow.get(i).getIndex() != 11 && itemsToShow.get(i).getIndex() != 3 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 28: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 28 && itemsToShow.get(i).getIndex() != 12 && itemsToShow.get(i).getIndex() != 4 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 29: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 29 && itemsToShow.get(i).getIndex() != 13 && itemsToShow.get(i).getIndex() != 5 && itemsToShow.get(i).getIndex() != 1 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            case 30: {
                int i = 0;
                boolean found;
                while (i < itemsToShow.size()) {
                    found = false;
                    if ((itemsToShow.get(i).getIndex() != 30 && itemsToShow.get(i).getIndex() != 14 && itemsToShow.get(i).getIndex() != 6 && itemsToShow.get(i).getIndex() != 2 && itemsToShow.get(i).getIndex() != 0) || itemsToShow.get(i).getLastCheckDay() == todayNum) {
                        itemsToShow.remove(i);
                        found = true;
                        i = 0;
                    }
                    if (!found) i++;
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    void listeners() {
        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                } else if (!(itemsToShow.get(position).getName().equals("   Nothing found") &&
                        itemsToShow.get(position).getMeaning().equals("My Dictionary") &&
                        itemsToShow.get(position).getAddDate().equals("KHaledBLack73")) /*&& position1 != 0*/) {
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
                    listViewPosition = items.onSaveInstanceState();
                    refreshListViewData();
                    if (isFromSearch) {
                        search(etSearch.getText().toString());
                    }
                    itemsToShow.get(position).setChChecked(true);
                    adapter.notifyDataSetChanged();
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
                    listViewPosition = items.onSaveInstanceState();
                    refreshListViewData();
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
        for (int i = 0; i < itemsToShow.size(); i++) {
            checkedPositionsInt.add(i, itemsToShow.get(i).isChChecked() ? 0 : 1);
        }
    }


    public void name_click(View view) {
        TextView tvNameMeaning = (TextView) dialogMeaning.findViewById(R.id.leitnerNameAndMeaning);
        if (tvNameMeaning.getText().toString().equals(arrayItems.get(getPosition(dialogMeaningWordPosition)).getName())) {
            tvNameMeaning.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getMeaning());
        } else {
            tvNameMeaning.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getName());
        }
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

        EditText etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
        EditText etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
        if (fromSearch) {
            etNewMeaning.setText(itemsToShow.get(fakePosition).getMeaning());
            etNewWord.setText(arrayItems.get(realPosition).getName());

        } else {
            etNewWord.setText(arrayItems.get(realPosition).getName());
            etNewMeaning.setText(itemsToShow.get(fakePosition).getMeaning());
        }

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
            if (isReadyEdit()) {
                EditText etNewWord = (EditText) dialog.findViewById(R.id.etWord);
                EditText etNewMeaning = (EditText) dialog.findViewById(R.id.etMeaning);
                newWordEdit = etNewWord.getText().toString();
                newMeaningEdit = etNewMeaning.getText().toString();

                Item current = databaseLeitner.getItem(databaseLeitner.getItemId(word, meaning));
                databaseLeitner.updateItem(new Item(
                        databaseLeitner.getItemId(word, meaning), newWordEdit,
                        newMeaningEdit, current.getAddDate(), current.getLastCheckDate(),
                        current.getLastCheckDay(), current.getWitchDay(), current.getDeck(), current.getIndex(), current.getCountCorrect(),
                        current.getCountInCorrect(), current.getCount()));

                listViewPosition = items.onSaveInstanceState();
                refreshListViewData();
                dialog.dismiss();
                Toast.makeText(LeitnerActivity.this, "Successfully edited.", Toast.LENGTH_SHORT).show();
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

    public void AddNew(View view) {
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
                String newWord = etNewWord.getText().toString();
                String newMeaning = etNewMeaning.getText().toString();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                String currentDateAndTime = simpleDateFormat.format(new Date());

                databaseLeitner.addItem(new Item(newWord, newMeaning, currentDateAndTime), true);

                listViewPosition = items.onSaveInstanceState();
                refreshListViewData();
                dialog.dismiss();
                Toast.makeText(LeitnerActivity.this, "Successfully added.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isReadyToAddNew() {
        EditText etNewWord = (EditText) dialogAddNew.findViewById(R.id.etWord);
        EditText etNewMeaning = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
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
        for (int i = 0; i < databaseLeitner.getItemsCount(true); i++) {
            if (newWord.equals(arrayItems.get(i).getName()) && newMeaning.equals(arrayItems.get(i).getMeaning())) {
                Toast.makeText(this, "The Word exists in the database", Toast.LENGTH_SHORT).show();
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
//            final long diffMonth = diff / (60 * 60 * 1000 * 24 * 30);
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
//            return "nothing";
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
        for (int i = 0; i < databaseLeitner.getItemsCount(true); i++) {
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
        databaseLeitner.addItem(new Item(item.getName(), item.getMeaning(), item.getAddDate()), false);
        arrayItems.remove(realPosition);
        itemsToShow.remove(showPosition);

        listViewPosition = items.onSaveInstanceState();
        refreshListViewData();

        if (isFromSearch && itemsToShow.size() == 0) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public boolean isReadyEdit() {
        EditText etNewWord = (EditText) dialogEdit.findViewById(R.id.etWord);
        EditText etNewMeaning = (EditText) dialogEdit.findViewById(R.id.etMeaning);
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

        if (arrayItems.get(getPosition(dialogMeaningWordPosition)).getName().equals(newWord) && arrayItems.get(getPosition(dialogMeaningWordPosition)).getMeaning().equals(newMeaning)) {
            Toast.makeText(this, "Nothing has changed", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < databaseLeitner.getItemsCount(true); i++) {
            if (newWord.equals(arrayItems.get(i).getName()) && newMeaning.equals(arrayItems.get(i).getMeaning())) {
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

    void search(String key) {
        int found = 0;

        for (int i = 0; i < itemsToShow.size(); i++)
            itemsToShow.get(i).setName(showItemNumber ? itemsToShow.get(i).getName().substring(Integer.toString(i).length()+2) : itemsToShow.get(i).getName());

        if (itemsToShow.size() > 0) {
            int i = 0;
            while (i < itemsToShow.size()) {
                key = key.toUpperCase();
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
            } else {
                itemsToShow.add(new Item("   Nothing found", "My Dictionary", "KHaledBLack73"));
            }
            adapter.notifyDataSetChanged();
        }

//        if (databaseLeitner.getItemsCount(true) > 0) {
//            itemsToShow.clear();
//            for (int i = 0; i < databaseLeitner.getItemsCount(true); i++) {
//                key = key.toUpperCase();
//                String word = arrayItems.get(i).getName().toUpperCase();
//                String meaning = arrayItems.get(i).getMeaning().toUpperCase();
//
//                if (searchMethod.equals("wordsAndMeanings") ? word.contains(key) || meaning.contains(key) :
//                        searchMethod.equals("justWords") ? word.contains(key) :
//                                meaning.contains(key)) {
//
//                    itemsToShow.add(databaseLeitner.getItem(databaseLeitner.getItemId(arrayItems.get(i).getName(), arrayItems.get(i).getMeaning())));
//                    found++;
//                }
//            }
//            if (found > 0) {
//                adapter.notifyDataSetChanged();
//                items.setAdapter(adapter);
//            } else {
//                itemsToShow.add(new Item("   Nothing found", "My Dictionary", "KHaledBLack73"));
//                adapter.notifyDataSetChanged();
//            }
//        }

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
        }
    }

    void getPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        searchMethod = prefs.getString("searchMethod", "wordsAndMeanings");
        showItemNumber = prefs.getBoolean("showItemNumber", true);
        isDistance = prefs.getString("timeMethod", "distance");
//        sortMethod = prefs.getString("sortMethod", "date");
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
                        databaseLeitner.addItem(new Item(item.getName(), item.getMeaning(), item.getAddDate()), false);
                        checkedPositionsInt.set(i, 1);
                        i = 0;
                        continue;
                    }
                    i++;
                }

                if (databaseLeitner.getItemsCount(true) < 1) {
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
            tvLastDate.setText(getDistance(arrayItems.get(getPosition(dialogMeaningWordPosition)).getAddDate()));
        } else {
            tvLastDate.setText(arrayItems.get(getPosition(dialogMeaningWordPosition)).getAddDate());
            isDistanceTempLast = "date";
        }
    }


    void dialogMeaning(final int position, final int realPosition) {
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflater.inflate(R.layout.dialog_meaning_leitner, null));
        builder.setPositiveButton(R.string.correct, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                answer_Correct(realPosition);
            }
        });
        builder.setNegativeButton(R.string.Incorrect, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                answer_Incorrect(realPosition);
            }
        });
        builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogEdit(isFromSearch, position, realPosition);
            }
        });
        int currentApi = android.os.Build.VERSION.SDK_INT;
//        if (currentApi >= Build.VERSION_CODES.HONEYCOMB){
//            builder.setIconAttribute(android.R.drawable.ic_dialog_info);
//        }else {
        builder.setIcon(android.R.drawable.ic_dialog_info);
//        }
        int x = 1;
        dialogMeaning = builder.create();
        dialogMeaning.show();
        dialogMeaningWordPosition = position;
        Item item = arrayItems.get(realPosition);

        TextView tvAddDate = (TextView) dialogMeaning.findViewById(R.id.leitnerAddDate);
        TextView tvLastDate = (TextView) dialogMeaning.findViewById(R.id.leitnerLastDate);
        TextView tvPosition = (TextView) dialogMeaning.findViewById(R.id.leitnerPosition);
        TextView tvCountCorrect = (TextView) dialogMeaning.findViewById(R.id.leitnerCountCorrect);
        TextView tvCount = (TextView) dialogMeaning.findViewById(R.id.leitnerCount);
        TextView tvCountInCorrect = (TextView) dialogMeaning.findViewById(R.id.leitnerCountInCorrect);
        TextView tvNameMeaning = (TextView) dialogMeaning.findViewById(R.id.leitnerNameAndMeaning);

        int index = item.getIndex();

        switch (item.getIndex()) {
            case 0:
                index = 1;
                break;

            case 1:
                index = 1;
                break;
            case 2:
                index = 2;
                break;

            case 3:
                index = 1;
                break;
            case 4:
                index = 2;
                break;
            case 5:
                index = 3;
                break;
            case 6:
                index = 4;
                break;

            case 7:
                index = 1;
                break;
            case 8:
                index = 2;
                break;
            case 9:
                index = 3;
                break;
            case 10:
                index = 4;
                break;
            case 11:
                index = 5;
                break;
            case 12:
                index = 6;
                break;
            case 13:
                index = 7;
                break;
            case 14:
                index = 8;
                break;

            case 15:
                index = 1;
                break;
            case 16:
                index = 2;
                break;
            case 17:
                index = 3;
                break;
            case 18:
                index = 4;
                break;
            case 19:
                index = 5;
                break;
            case 20:
                index = 6;
                break;
            case 21:
                index = 7;
                break;
            case 22:
                index = 8;
                break;
            case 23:
                index = 9;
                break;
            case 24:
                index = 10;
                break;
            case 25:
                index = 11;
                break;
            case 26:
                index = 12;
                break;
            case 27:
                index = 13;
                break;
            case 28:
                index = 14;
                break;
            case 29:
                index = 15;
                break;
            case 30:
                index = 16;
                break;
            default:
                break;
        }

        tvPosition.setText("at deck '" + Integer.toString(item.getDeck()) + "', index '" + Integer.toString(index) + "'");
        tvCountCorrect.setText(Integer.toString(item.getCountCorrect()));
        tvCount.setText(Integer.toString(item.getCount()));
        tvCountInCorrect.setText(Integer.toString(item.getCountInCorrect()));
        tvNameMeaning.setText(item.getName());

        isDistanceTempAdd = isDistance;
        isDistanceTempLast = isDistance;
        if (isDistance.equals("distance")) {
            tvAddDate.setText(getDistance(item.getAddDate()));
            tvLastDate.setText(getDistance(item.getLastCheckDate()));
        } else {
            tvAddDate.setText(item.getAddDate());
            tvLastDate.setText(item.getLastCheckDate());
        }

        TextView tvPos = (TextView) dialogMeaning.findViewById(R.id.leitnerPos);
        tvPos.setText(Integer.toString(position + 1) + " of " + Integer.toString(itemsToShow.size()));


        dialogMeaning.setCanceledOnTouchOutside(true);
    }

    void answer_Correct(int realPosition) {
        move_Next_Correct(realPosition);
        update_Info_After_Answer(realPosition, true);
    }

    void move_Next_Correct(int realPosition) {
        int currentDeck = arrayItems.get(realPosition).getDeck();
        int nextIndex = 0;
        switch (currentDeck) {
            case 0: //to deck 1 / /////// / / not going to happen
                arrayItems.get(realPosition).setDeck(1);
                arrayItems.get(realPosition).setIndex(0);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 1, 0);
                break;

            case 1:  //to deck 2
                nextIndex = whichIndexTurnDeck(deck2);

                databaseLeitner.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databaseLeitner.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(2);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 2, nextIndex);
                break;

            case 2: // to deck 3
                nextIndex = whichIndexTurnDeck(deck3);

                databaseLeitner.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databaseLeitner.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(3);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 3, nextIndex);
                break;

            case 3: // to deck 4
                nextIndex = whichIndexTurnDeck(deck4);

                databaseLeitner.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databaseLeitner.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(4);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 4, nextIndex);
                break;

            case 4: // to deck 5
                nextIndex = whichIndexTurnDeck(deck5);

                databaseLeitner.updateItemLastDays(nextIndex + 1, todayNum);//update third one
                databaseLeitner.updateItemLastDaysDate(nextIndex + 1, todayDate);//update third one
                arrayIndexesLastDay.set(nextIndex, todayNum);
                arrayIndexesLastDayDate.set(nextIndex, todayDate);

                arrayItems.get(realPosition).setDeck(5);
                arrayItems.get(realPosition).setIndex(nextIndex);
                databaseLeitner.updatePosition(arrayItems.get(realPosition).getId(), 5, nextIndex);
                break;
            case 5: // to archive

                break;
        }

    }

    int whichIndexTurnDeck(ArrayList<ArrayList<Item>> deck) {
        int lastIndex = -1;
        int lastDay = -1;

        if (deck.size() == 2) {
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

        } else if (deck.size() == 4) {
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

        } else if (deck.size() == 8) {
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

        } else if (deck.size() == 16) {
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

    int lastIndexMore30(ArrayList<ArrayList<Item>> deck) {
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

    int nextIndexMore30(ArrayList<ArrayList<Item>> deck) {
        int lastIndex = -1;
        int lastDay = -1;

        if (deck.size() == 16) {
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


    void answer_Incorrect(int realPosition) {
        arrayItems.get(realPosition).setDeck(1);
        arrayItems.get(realPosition).setIndex(0);

        update_Info_After_Answer(realPosition, false);
    }


    void update_Info_After_Answer(int realPosition, boolean correct) {
        Item currentItem = arrayItems.get(realPosition);

        editorMainPrefs.putString("lastDate", this.todayDate);
        editorMainPrefs.putInt("lastDay", todayNum);
        lastDate = todayDate;
        editorMainPrefs.commit();

        int countCorrect = correct ? currentItem.getCountCorrect() + 1 : -1;
        int countIncorrect = !correct ? currentItem.getCountCorrect() + 1 : -1;
        int count = currentItem.getCount() + 1;

        if (correct) {
            arrayItems.get(realPosition).setCountCorrect(countCorrect);
        } else {
            arrayItems.get(realPosition).setCountCorrect(countIncorrect);
        }
        arrayItems.get(realPosition).setCount(count);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String currentDateAndTime = simpleDateFormat.format(new Date());

        arrayItems.get(realPosition).setLastCheckDate(currentDateAndTime);
        arrayItems.get(realPosition).setLastCheckDay(todayNum);

        currentItem = arrayItems.get(realPosition);

        databaseLeitner.updateItem(new Item(currentItem.getId(), currentItem.getName(), currentItem.getMeaning(),
                currentItem.getAddDate(), currentDateAndTime, todayNum,
                correct ? currentItem.getWitchDay() : 1, currentItem.getDeck(), currentItem.getIndex(),
                correct ? countCorrect : currentItem.getCountCorrect(), !correct ? countIncorrect : currentItem.getCountInCorrect(), count
        ));

        listViewPosition = items.onSaveInstanceState();
        refreshListViewData();
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
        int nextIndexDeck5 = nextIndexMore30(deck5);
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

        EditText wordAddNew = (EditText) dialogAddNew.findViewById(R.id.etWord);
        EditText meaningAddNew = (EditText) dialogAddNew.findViewById(R.id.etMeaning);


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
            listViewPosition = icicle.getParcelable("listViewPosition");
            markSeveral = icicle.getBoolean("markSeveral");
            isFromSearch = icicle.getBoolean("isFromSearch");


        }
        if (dialogAddNewIsOpen) {
            dialogAddNew();
            EditText wordAddNew = (EditText) dialogAddNew.findViewById(R.id.etWord);
            EditText meaningAddNew = (EditText) dialogAddNew.findViewById(R.id.etMeaning);
            wordAddNew.setText(icicle.getString("editTextWordAddNew"));
            meaningAddNew.setText(icicle.getString("editTextMeaningAddNew"));
        }
        if (dialogMeaningIsOpen) {
            refreshListViewData();
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            dialogMeaning(dialogMeaningWordPosition, getPosition(dialogMeaningWordPosition));
        }
        if (dialogEditIsOpen) {
            dialogMeaningWordPosition = icicle.getInt("dialogMeaningWordPosition");
            dialogEdit(isFromSearch, dialogMeaningWordPosition, getPosition(dialogMeaningWordPosition));
            EditText wordAddNew = (EditText) dialogEdit.findViewById(R.id.etWord);
            EditText meaningAddNew = (EditText) dialogEdit.findViewById(R.id.etMeaning);
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
            checkedPositionsInt = icicle.getIntegerArrayList("checkedPositionsInt");
            refreshListViewData();
        }
    }

    @Override
    public void onBackPressed() {
        if (markSeveral) {
            adapter = new AdapterLeitner(LeitnerActivity.this, R.layout.row, itemsToShow);
            markSeveral = false;
            setElementsId();
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
        final View view = getLayoutInflater().inflate(R.layout.row_header, items, false);
        items.removeHeaderView(view);

    }

    @Override
    public void onResume() {
        super.onResume();
        getPrefs();
        listViewPosition = items.onSaveInstanceState();
        refreshListViewData();
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
        if (markSeveral && databaseLeitner.getItemsCount(true) > 0) {
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
            getMenuInflater().inflate(R.menu.main, menu);
        }

        MenuItem itemLeitner = menu.findItem(R.id.action_leitner);
        if (itemLeitner != null) {
            itemLeitner.setTitle("My Dictionary");
        }

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                LeitnerActivity.this.startActivity(new Intent(LeitnerActivity.this, Preferences.class));
                return true;
            case R.id.action_about:
                LeitnerActivity.this.startActivity(new Intent(LeitnerActivity.this, AboutActivity.class));
                return true;


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

            case R.id.action_leitner:
                LeitnerActivity.this.startActivity(new Intent(LeitnerActivity.this, MainActivity.class));
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


}