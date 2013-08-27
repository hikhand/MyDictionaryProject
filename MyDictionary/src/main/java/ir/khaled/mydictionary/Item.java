package ir.khaled.mydictionary;

/**
 * Created by khaled on 7/4/13 at 2:05 AM.
 */

public class Item {

    private int id;
    private String name;
    private String meaning;
    private String example;
    private String tags;
    private String addDate;
    private String lastCheckDate;
    private int lastCheckDay;
    private int deck;
    private int index;
    private int countCorrect;
    private int countInCorrect;
    private int count;

    private boolean chChecked;
    private boolean chVisible;

    public Item(int id, String name, String meaning, String example, String tags, String addDate, String lastCheckDate,int lastCheckDay, int deck, int index, int countCorrect, int countInCorrect, int count) {
        this.id = id;
        this.name = name;
        this.meaning = meaning;
        this.example = example;
        this.tags = tags;
        this.addDate = addDate;
        this.lastCheckDate= lastCheckDate;
        this.lastCheckDay = lastCheckDay;
        this.deck = deck;
        this.index = index;
        this.countCorrect = countCorrect;
        this.countInCorrect =countInCorrect;
        this.count = count;
    }

    public Item(int id, String name, String meaning, String addDate, int deck, int index) {
        this.id = id;
        this.name = name;
        this.meaning = meaning;
        this.addDate = addDate;
        this.deck = deck;
        this.index = index;
    }

    public Item(String name, String meaning, String example, String tags, String addDate) {
//        this.id = id;
        this.name = name;
        this.meaning = meaning;
        this.example = example;
        this.tags = tags;
        this.addDate = addDate;
        this.lastCheckDate = addDate;
        this.lastCheckDay = 0;
        this.deck = 1;
        this.index = 0;
        this.countCorrect = 0;
        this.countInCorrect = 0;
        this.count = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getAddDate() {
        return addDate;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public String getLastCheckDate() {
        return lastCheckDate;
    }

    public void setLastCheckDate(String lastCheckDate) {
        this.lastCheckDate = lastCheckDate;
    }

    public int getLastCheckDay() {
        return lastCheckDay;
    }

    public void setLastCheckDay(int lastCheckDay) {
        this.lastCheckDay = lastCheckDay;
    }

    public int[] getPosition() {
        return new int[]{deck, index};
    }

    public int getDeck() {
        return deck;
    }

    public int getIndex() {
        return index;
    }

    public void setPosition(int[] position) {
        this.deck = position[0];
        this.index = position[1];
    }

    public void setPosition(int deck, int index) {
        this.deck = deck;
        this.index= index;
    }

    public void setDeck(int deck) {
        this.deck = deck;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getCountCorrect() {
        return countCorrect;
    }

    public void setCountCorrect(int countCorrect) {
        this.countCorrect = countCorrect;
    }

    public int getCountInCorrect() {
        return countInCorrect;
    }

    public void setCountInCorrect(int countInCorrect) {
        this.countInCorrect = countInCorrect;
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
