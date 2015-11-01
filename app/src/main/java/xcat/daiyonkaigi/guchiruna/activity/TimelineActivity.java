package xcat.daiyonkaigi.guchiruna.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import xcat.daiyonkaigi.guchiruna.R;
import xcat.daiyonkaigi.guchiruna.db.GuchiCommonDBOpenHelper;

/**
 * データベースの実体を表示させるためのクラスです。
 * activity_timeline.xmlと紐付いたActivityです。
 *
 */
public class TimelineActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        GuchiCommonDBOpenHelper helper = new GuchiCommonDBOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        // queryメソッドの実行例
        Cursor c = db.query("article", new String[]{"article, date"}, null,
                null, null, null, "date" + " DESC");

        boolean mov = c.moveToFirst();
        List<String> timeline = new ArrayList<>();

        while (mov){
            //文字列をテキストビューに設定
            String article = c.getString(0);
            String date = c.getString(1);
            timeline.add(date + "\n" + article);
            mov = c.moveToNext();
        }

        ArrayAdapter<String> adpviewList = new ArrayAdapter<String>(this,
                R.layout.col, timeline);
        this.setListAdapter(adpviewList);

        c.close();
        db.close();

        //TODO メニューボタンに差し替えるため削除
        //ボタン押下時の登録処理
        Button rankButton = (Button) this.findViewById(R.id.ranking);
        rankButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //次画面での表示処理
                Intent timeToRankIntent = new Intent(TimelineActivity.this, RankingActivity.class);
                startActivity(timeToRankIntent);
            }
        });
        //ボタン押下時の登録処理
        Button negaPoziButton = (Button) this.findViewById(R.id.negapoji);
        negaPoziButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //次画面での表示処理
                Intent timeToNegaIntent = new Intent(TimelineActivity.this, NegapoziActivity.class);
                startActivity(timeToNegaIntent);
            }
        });
        //ボタン押下時の登録処理
        Button inputButton = (Button) this.findViewById(R.id.input);
        inputButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //次画面での表示処理
                Intent timeToInputIntent = new Intent(TimelineActivity.this, InputActivity.class);
                startActivity(timeToInputIntent);
            }
        });
    }
}