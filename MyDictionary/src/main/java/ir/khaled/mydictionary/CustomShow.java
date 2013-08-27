package ir.khaled.mydictionary;


/**
 * Created by khaled on 6/21/13.
 */

public class CustomShow {
    private int id;
    private String word;
    private String meaning;
    private String example;
    private String tags;
    private String date;
    private String lastDate;
    private int count;

    private boolean chChecked;
    private boolean chVisible;
    private boolean isMeaningVisible;

    public CustomShow(int id, String word, String meaning, String example, String tags, String date, String lastDate, int count) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.tags = tags;
        this.date = date;
        this.lastDate = lastDate;
        this.count = count;
    }

    public CustomShow(String word, String meaning, String example, String tags, String date, String lastDate, int count){
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.tags = tags;
        this.date = date;
        this.lastDate = lastDate;
        this.count = count;
    }

    public CustomShow(String word, String meaning, String date, boolean chVisible){
        this.word = word;
        this.meaning = meaning;
        this.date = date;
        this.chVisible = chVisible;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int count) {
        this.id = id;
    }

    public boolean isChChecked() {
        return chChecked;
    }

    public void setChChecked(boolean selected) {
        this.chChecked = selected;
    }

    public boolean isChVisible() {
        return chVisible;
    }

    public void setChVisible(boolean selected) {
        this.chVisible = selected;
    }

    public boolean isMeaningVisible() {
        return isMeaningVisible;
    }

    public void setMeaningVisible(boolean isMeaningVisible) {
        this.isMeaningVisible = isMeaningVisible;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}