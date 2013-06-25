package ir.khaled.mydictionary;


/**
 * Created by khaled on 6/21/13.
 */

public class Custom {
    private String word;
    private String meaning;
    private String date;
    private int count;

    private boolean chChecked;
    private boolean chVisible;
    private boolean isFavorite;
    private boolean isMeaningVisible;

    public Custom(){
    }

    public Custom(String word, String meaning, String date, int count, boolean chVisible, boolean isFavorite){
        this.word = word;
        this.meaning = meaning;
        this.date = date;
        this.count = count;
        this.chVisible = chVisible;
        this.isFavorite = isFavorite;
    }

//    public Custom(String word, String meaning,  boolean chVisible){
//        this.word = word;
//        this.meaning = meaning;
//        this.chVisible = chVisible;
//    }

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

    public boolean isMeaningVisible() {
        return isMeaningVisible;
    }

    public void setMeaningVisible(boolean isMeaningVisible) {
        this.isMeaningVisible = isMeaningVisible;
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

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public boolean isFavorite() {
        return this.isFavorite;
    }
}