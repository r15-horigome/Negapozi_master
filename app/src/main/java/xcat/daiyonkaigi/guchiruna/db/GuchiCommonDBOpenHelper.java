package xcat.daiyonkaigi.guchiruna.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteデータベースを生成するクラスです。
 */
public class GuchiCommonDBOpenHelper extends SQLiteOpenHelper {

    public GuchiCommonDBOpenHelper(Context context) {
        super(context, "guchi", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //タイムライン用のテーブル
        db.execSQL("CREATE TABLE article(" +
                "article text " +
                ",date text " +
                ");");
        //ランキング用のテーブル
        db.execSQL("CREATE TABLE rankings(" +
                "word TEXT" +
                ",hinsi TEXT" +
                ",sum INTEGER" +
                ");");
        // ネガポジ用のテーブル
        db.execSQL("CREATE TABLE negapozi(" +
                "id INTEGER PRIMARY KEY " +
                ",Pozi REAR " +
                ",Nega REAR " +
                ",Year INTEGER " +
                ",Month INTEGER " +
                ",Day INTEGER " +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exist rankings");

        //作成
        onCreate(db);
    }
}
