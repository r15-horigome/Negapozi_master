package xcat.daiyonkaigi.guchiruna.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import xcat.daiyonkaigi.guchiruna.R;

public class BottomMenuActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
    }

    // Option Menu が最初に表示される時に1度だけ呼び出される
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // MenuInflater の取得
        MenuInflater menuInflater = getMenuInflater();
        // MenuInflater から XML の取得
        //menuInflater.inflate(R.menu.menu_bottom, menu);
        return true;
    }
}