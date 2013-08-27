package ir.khaled.mydictionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class TagsActivity extends Activity {
    ListView listTags;
    Button btnAdd;
    ArrayAdapter<String> adapter;
    ArrayList<String> arrayTags;
    DatabaseHandler databaseMain;
    DatabaseLeitner databaseLeitner;
    AlertDialog dialogAdd;
    AlertDialog dialogEdit;
    EditText input;
    int itemPosition = 0;
    ProgressDialog progressBar;
    String newTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        databaseMain = new DatabaseHandler(this);
        databaseLeitner = new DatabaseLeitner(this);
        listTags = (ListView) findViewById(R.id.tagList);
        btnAdd = (Button) findViewById(R.id.tagAdd);
        arrayTags = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        setElementsId();
        refreshList();

        listTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                itemPosition = i;
                if (i != 0) {
                    dialogEdit(i);
                }
            }
        });

    }

    void setElementsId() {
    }

    void dialogEdit(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new tag");
        input = new EditText(this);
        input.setHint("Enter tag's name");
        input.setText(arrayTags.get(position));
        builder.setView(input);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialogEdit = builder.create();
        dialogEdit.show();
        Button theButton = dialogEdit.getButton(DialogInterface.BUTTON_POSITIVE);
        Button theButton1 = dialogEdit.getButton(DialogInterface.BUTTON_NEUTRAL);
        theButton.setOnClickListener(new CustomListenerEdit(dialogEdit, true));
        theButton1.setOnClickListener(new CustomListenerEdit(dialogEdit, false));
    }

    class CustomListenerEdit implements View.OnClickListener {
        private final Dialog dialog;
        private boolean isEdit;

        public CustomListenerEdit(Dialog dialog, boolean isEdit) {
            this.dialog = dialog;
            this.isEdit = isEdit;
        }

        @Override
        public void onClick(View v) {
            String str = input.getText().toString();
            if (isEdit) {
                for (String s : arrayTags) {
                    if (str.toLowerCase().equals(s.toLowerCase()) && !str.toLowerCase().equals(arrayTags.get(itemPosition).toLowerCase())) {
                        Toast.makeText(TagsActivity.this, "Tag already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (str.contains(",")) {
                        Toast.makeText(TagsActivity.this, "Tag can't contain ','", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (isStringJustSpace(str) || str == null || str.equals("")) {
                    Toast.makeText(TagsActivity.this, "You haven't inserted any thing.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (str.length() > 15) {
                    Toast.makeText(TagsActivity.this, "Tag can't be more than 20 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (str.contains(" ")) {
                    Toast.makeText(TagsActivity.this, "Tag can't contain space", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (str.toLowerCase().equals(arrayTags.get(itemPosition).toLowerCase())) {
                    Toast.makeText(TagsActivity.this, "Nothing has changed", Toast.LENGTH_SHORT).show();
                    return;
                }
                newTag = str;
                editTags();
            } else {
                newTag = str;
                removeTags();
            }
            dialog.dismiss();
        }
    }

    void editTags() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage("editing tag in cards ...");
        progressBar.show();
        Thread edit = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Item> itemsLeitner = databaseLeitner.getAllItems();
                ArrayList<Custom> itemsMain = databaseMain.getAllItems();
                for (Item j : itemsLeitner) {
//                    if (j.getTags().contains(newTag)) {
//                        if (j.getTags().contains(","+newTag)) {
//                            databaseLeitner.updateItem(new Item(j.getId(), j.getName(), j.getMeaning(), j.getExample(), j.getTags().replace(","+newTag, ""), j.getAddDate(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount()));
//                        } else {
//                            databaseLeitner.updateItem(new Item(j.getId(), j.getName(), j.getMeaning(), j.getExample(), j.getTags().replace(newTag, ""), j.getAddDate(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount()));
//                        }
//                    }
                    if (j.getTags().contains(arrayTags.get(itemPosition))) {
                        databaseLeitner.updateItem(new Item(j.getId(), j.getName(), j.getMeaning(), j.getExample(), j.getTags().replace(arrayTags.get(itemPosition), newTag), j.getAddDate(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount()));
                    }
                }
                for (Custom j : itemsMain) {
//                    if (j.getTags().contains(newTag)) {
//                        if (j.getTags().contains(","+newTag)) {
//                            databaseMain.updateItem(new Custom(j.getId(), j.getWord(), j.getMeaning(), j.getExample(), j.getTags().replace(","+newTag, ""), j.getLastDate(), j.getLastDate(), j.getCount()));
//                        } else {
//                            databaseMain.updateItem(new Custom(j.getId(), j.getWord(), j.getMeaning(), j.getExample(), j.getTags().replace(newTag, ""), j.getLastDate(), j.getLastDate(), j.getCount()));
//                        }
//                    }
                    if (j.getTags().contains(arrayTags.get(itemPosition))) {
                        databaseMain.updateItem(new Custom(j.getId(), j.getWord(), j.getMeaning(), j.getExample(), j.getTags().replace(arrayTags.get(itemPosition), newTag), j.getLastDate(), j.getLastDate(), j.getCount()));
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.dismiss();
                        databaseMain.updateTag(databaseMain.getTagId(arrayTags.get(itemPosition)), newTag);
                        refreshList();
                    }
                });
            }
        });
        edit.start();
    }

    void removeTags() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage("Removing tag from cards ...");
        progressBar.show();
        Thread edit = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Item> itemsLeitner = databaseLeitner.getAllItems();
                ArrayList<Custom> itemsMain = databaseMain.getAllItems();
                for (Item j : itemsLeitner) {
                    if (j.getTags().contains(arrayTags.get(itemPosition))) {
                        if (j.getTags().contains(","+arrayTags.get(itemPosition))) {
                            databaseLeitner.updateItem(new Item(j.getId(), j.getName(), j.getMeaning(), j.getExample(), j.getTags().replace(","+arrayTags.get(itemPosition), ""), j.getAddDate(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount()));
                        } else {
                            databaseLeitner.updateItem(new Item(j.getId(), j.getName(), j.getMeaning(), j.getExample(), j.getTags().replace(arrayTags.get(itemPosition), ""), j.getAddDate(), j.getLastCheckDate(), j.getLastCheckDay(), j.getDeck(), j.getIndex(), j.getCountCorrect(), j.getCountInCorrect(), j.getCount()));
                        }
                    }
                }

                for (Custom j : itemsMain) {
                    if (j.getTags().contains(arrayTags.get(itemPosition))) {
                        if (j.getTags().contains(","+arrayTags.get(itemPosition))) {
                            databaseMain.updateItem(new Custom(j.getId(), j.getWord(), j.getMeaning(), j.getExample(), j.getTags().replace(","+arrayTags.get(itemPosition), ""), j.getLastDate(), j.getLastDate(), j.getCount()));
                            j.setTags(j.getTags().replace(","+arrayTags.get(itemPosition), ""));
                        }
                        if (!j.getTags().contains(","+arrayTags.get(itemPosition)) && j.getTags().contains(arrayTags.get(itemPosition))){
                            databaseMain.updateItem(new Custom(j.getId(), j.getWord(), j.getMeaning(), j.getExample(), j.getTags().replace(arrayTags.get(itemPosition), ""), j.getLastDate(), j.getLastDate(), j.getCount()));
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.dismiss();
                        databaseMain.deleteTag(databaseMain.getTagId(arrayTags.get(itemPosition)));
                        refreshList();
                    }
                });
            }
        });
        edit.start();
    }





    public void btnAdd_click(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new tag");
        input = new EditText(this);
        input.setHint("Enter tag's name");
        builder.setView(input);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialogAdd = builder.create();
        dialogAdd.show();
        Button theButton = dialogAdd.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListenerAddNew(dialogAdd));
    }

    class CustomListenerAddNew implements View.OnClickListener {
        private final Dialog dialog;

        public CustomListenerAddNew(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            String str = input.getText().toString();
            for (String s : arrayTags) {
                if (str.toLowerCase().equals(s.toLowerCase())) {
                    Toast.makeText(TagsActivity.this, "Tag already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (str.toLowerCase().contains(",")) {
                    Toast.makeText(TagsActivity.this, "Tag can't contain ','", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (isStringJustSpace(str) || str == null || str.equals("")) {
                Toast.makeText(TagsActivity.this, "You haven't inserted any thing.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (str.length() > 15) {
                Toast.makeText(TagsActivity.this, "Tag can't be more than 20 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (str.contains(" ")) {
                Toast.makeText(TagsActivity.this, "Tag can't contain space", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseMain.addTag(str);
            dialog.dismiss();
            setElementsId();
            refreshList();
        }
    }

    boolean isStringJustSpace(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch != ' ') return false;
        }
        return true;
    }

    void refreshList() {
        arrayTags = databaseMain.getTags(false);
        adapter.clear();
        for (String str : arrayTags) {
            adapter.add(str);
        }
        adapter.notifyDataSetChanged();
        listTags.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tags, menu);
        return true;
    }

}
