package ir.khaled.mydictionary;

import java.io.File;

/**
 * Created by khaled on 7/29/13.
 */
public class Names {
    
    final public String TABLE_LEITNER = "leitner";
    final public String TABLE_DONT_ADD = "dontAdd";
    final public String TABLE_ARCHIVE = "archive";
    final String TABLE_MAIN = "main";
    final String TABLE_LAST_CHECK_DAY = "indexesLastCheckDay";
    final String TABLE_LAST_CHECK_DAY_DATE = "indexesLastCheckDayDate";
    final String TABLE_PER_DAY_TODAY = "perDayToday";
    final String KEY_ID = "_id";
    static final String KEY_NAME = "name";
    static final String KEY_MEANING_FA = "meaningFa";
    static final String KEY_MEANING_EN = "meaningEn";
    static final String KEY_EXAMPLES_EN = "examplesEn";
    static final String KEY_EXAMPLES_FA = "examplesFa";
    final String KEY_lAST_DATE = "lastCheckDate";
    final String KEY_lAST_DAY = "lastCheckDay";
    final String KEY_DECK = "deck";
    final String KEY_INDEX = "index1";
    final String KEY_COUNT_CORRECT = "countCorrect";
    final String KEY_COUNT_INCORRECT = "countInCorrect";
    final String KEY_COUNT = "count";

     final String KEY_LAST_DAY = "lastDay";
     final String KEY_LAST_DAY_DATE = "lastDayDate";

     final String KEY_MAIN_LAST_DATE = "lastDate";
     final String KEY_MAIN_LAST_DAY = "lastDay";


    final public int BILLING_RESPONSE_RESULT_OK = 0;
    final public int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    final public int BILLING_RESPONSE_RESULT_ERROR = 6;
    final public int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;

    final String TRUE_HAS_BUY = "621614632583634638";//hasBuy
    final String TRUE_HAS_IN = "621614632590627";//hasIn
    final String FALSE = "619614625632618";


    private String s = File.separator;
    final String rootMydictionary = s + "mydictionary" + s;
    final String rootBackups = s + "mydictionary" + s + "backups" + s;
    final String fileLastDateServer = s + "mydictionary" + s + "backups" + s + "lastDateServer";


    final String DATABASE_PACKAGE504 = "package504.db";

    final String DEVELOPER_PAY_LOAD = "629614616624614620618570565569";//package504

}
