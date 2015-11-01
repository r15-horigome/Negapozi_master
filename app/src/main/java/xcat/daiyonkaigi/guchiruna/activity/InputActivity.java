package xcat.daiyonkaigi.guchiruna.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.reduls.sanmoku.Morpheme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import xcat.daiyonkaigi.guchiruna.R;
import xcat.daiyonkaigi.guchiruna.db.GuchiCommonDBOpenHelper;
import xcat.daiyonkaigi.guchiruna.negapozi.AsyncHttpRequest;
import xcat.daiyonkaigi.guchiruna.tokenize.StringToToken;


public class InputActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DBの初期化処理
        GuchiCommonDBOpenHelper helper = new GuchiCommonDBOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        //テキスト及び関連する情報の内容を取得
        final EditText editText = (EditText) findViewById(R.id.editText);

        //ボタン押下時の登録処理
        Button confirmButton = (Button) this.findViewById(R.id.button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String article = editText.getText().toString();

                //空なら何もさせずに次画面へ飛ばす
                if (!"".equals(article) && null != article) {
                    //記事と日付を格納
                    ContentValues articleInsertValues = new ContentValues();
                    articleInsertValues.put("article", article);
                    articleInsertValues.put("date", getCurrentDate());

                    db.insert("article", "null", articleInsertValues);

                    //品詞分解処理
                    List<Morpheme> tokenLists = new ArrayList();
                    tokenLists = StringToToken.tokenize(article);

                    AsyncHttpRequest asyncTask = new AsyncHttpRequest(InputActivity.this);
                    asyncTask.execute(article);     //asyncTask.execute(  このなかにつぶやきの内容を格納する  );

                    //品詞を判定し、対象の品詞と文字列をDBに格納
                    for (Morpheme m : tokenLists) {
                        if (judgeInsertToken(m.feature)) {
                            String hinsi = m.feature.split(",")[0];
                            String word = m.surface;

                            ContentValues wordInsertValues = new ContentValues();
                            wordInsertValues.put("hinsi", hinsi);
                            wordInsertValues.put("word", word);

                            db.insert("rankings", "null", wordInsertValues);
                        }
                    }
                }
                //TODO デバック用
                //コメントアウトするなり削除するなり自由です。
                //ネガポジ機能デバック用データの作成
                createNegapoziData();
                //次画面での表示処理
                Intent timelineIntent = new Intent(InputActivity.this, TimelineActivity.class);
                startActivity(timelineIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_bottom, menu);
        //TODO メニュー処理の実装（ここじゃない気がする）
        menu.add(0 , Menu.FIRST , Menu.NONE , "メニュー1");
        menu.add(0, Menu.FIRST + 1, Menu.NONE, "メニュー2");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    /**
     * 現在日時をyyyy/MM/dd HH:mm:ss形式で取得するメソッドです。
     *
     * @return yyyy/MM/dd HH:mm:ssで表記された時刻表記
     */
    private String getCurrentDate() {
        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    /**
     * 単語１つの解析情報を受け取り、DBに格納対象であるかを判定するメソッドです。
     * 仕様上、品詞及び動詞を格納対象としています。
     *
     * @return true(格納対象である) / false(格納対象でない)
     */
    private boolean judgeInsertToken(String token) {
        String[] tokens = token.split(",");
        //品詞情報はトークン情報中、インデックス1に設定されています
        String hinsi = tokens[0];
        //クソ判定
        return ("動詞".equals(hinsi) || "名詞".equals(hinsi) || "形容詞".equals(hinsi));
    }
    /**
     *
     *  ネガポジ機能デバック用データを登録するメソッドです。
     *  現在日時から過去一年分のデータを作成します。
     *  1日1回愚痴を入力したと想定して、ポジティブ/ネガティブ度数を乱数で設定しています。
     *  削除していただいても結構です。
     */
    public void createNegapoziData(){
        // 現在日時の取得
        Calendar cal = Calendar.getInstance();
        String yearStr = "" + cal.get(Calendar.YEAR);
        String monthStr = "" + cal.get(Calendar.MONTH);
        String dayStr = "" + cal.get(Calendar.DATE);
        int year = Integer.parseInt(yearStr);
        int month = Integer.parseInt(monthStr);
        int day = Integer.parseInt(dayStr);
        //DBの初期化処理
        GuchiCommonDBOpenHelper helper = new GuchiCommonDBOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        /*
         * テストデータ初期化用
         * テーブル negapozi 中身を空にします。
         */
        db.delete( "negapozi", null, null );

        // 月の最大値
        int max = 0;
        int monthmax = 0;
        Random rnd;
        int kekka = 0;
        long ret;
        Log.e("Negapozi", "------------TESTDATA CREATE START-------------");
        try {
            while (monthmax < 12) {
                Log.e("Negapozi", "1カ月目");
                // 月の最大値初期化
                max = 0;
                while (max < 31) {
                    //乱数の取得
                    //-3～3の乱数を取得する
                    rnd = new Random();
                    kekka = rnd.nextInt(7) - 3;
                    if (kekka > 0) {
                        Log.e("Negapozi","ポジティブ度"+ kekka);
                        //DBに保存 ポジティブ度数、年月日
                        ContentValues values = new ContentValues();
                        values.put("Pozi", kekka);
                        values.put("Year", year);
                        values.put("Month", monthmax+ 1);
                        values.put("Day", max + 1);
                        ret = db.insert("negapozi", null, values);
                        Log.e("Negapozi", ret + "レコード目");
                    } else {
                        Log.e("Negapozi","ネガティブ度"+ kekka);
                        //DBに保存 ネガティブ度数、年月日
                        ContentValues values = new ContentValues();
                        values.put("Nega", kekka * -1);
                        values.put("Year", year);
                        values.put("Month", monthmax + 1);
                        values.put("Day", max + 1);
                        ret = db.insert("negapozi", null, values);
                        Log.e("Negapozi", ret + "レコード目");
                    }
                    max++;
                    day++;
                }
                monthmax++;
            }
        }finally {
            db.close();
        }
    }
}
