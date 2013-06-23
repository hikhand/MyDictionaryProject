package ir.khaled.mydictionary;


/**
 * Created by khaled on 6/21/13.
 */

public class Custom {
    private String word;
    private String date;
    private boolean chChecked;
    private boolean chVisible;

    public Custom(){
    }

    public Custom(String word, String date, boolean chVisible){
        this.word = word;
        this.date = date;
        this.chVisible = chVisible;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
}