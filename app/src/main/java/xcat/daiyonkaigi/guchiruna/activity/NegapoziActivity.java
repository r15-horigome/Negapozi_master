package xcat.daiyonkaigi.guchiruna.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.axis.NumberTickUnit;
import org.afree.chart.axis.TickUnit;
import org.afree.chart.axis.TickUnits;
import org.afree.chart.axis.ValueAxis;
import org.afree.chart.labels.StandardPieSectionLabelGenerator;
import org.afree.chart.plot.PiePlot;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYItemRenderer;
import org.afree.chart.title.LegendTitle;
import org.afree.chart.title.TextTitle;
import org.afree.data.general.DefaultPieDataset;
import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;
import org.afree.graphics.SolidColor;
import org.afree.graphics.geom.Font;

import java.util.Calendar;

import xcat.daiyonkaigi.guchiruna.R;
import xcat.daiyonkaigi.guchiruna.db.GuchiCommonDBOpenHelper;
import xcat.daiyonkaigi.guchiruna.negapozi.ChartView;

/**
 * ネガポジ画面を表示させるためのクラスです。
 * negapozi_layout.xmlと紐付いたActivityです。
 *
 */
public class NegapoziActivity extends Activity implements View.OnClickListener {

    // 各月の日数
    public static final int[] MONTHDAYS = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    // グラフ表示の基準となる日付
    private int YEAR;
    private int MONTH;
    private int DAY;
    /* 登録されている年月の最大値(現在の月) */
    private int MAXMONTH;
    private int MAXYEAR;
    /*  登録されている年月の最小値  */
    private int MINMONTH;
    private int MINYEAR;
    /* 現在の年月日 */
    private int NOWMONTH;
    private int NOWYEAR;
    private int NOWDAY;
    /* view関係 */
    private int totalpozi[];
    private int totalnega[];
    /* 次月 */
    private Button mirai;
    /* 月/年切り替え  */
    private Button all;
    /* 前月 */
    private Button kako;
    /* メニューボタン */
    private Button rank;
    private Button negapozi;
    private Button form;
    /* 折れ線/円切り替え */
    private Button en;
    /* グラフコンポーネントのインスタンス */
    private ChartView graph2;
    /* グラフが円なのか折れ線なのかのフラグ 1:折れ線 2:円 */
    int graphflag;
    /* 1年単位か1ヶ月単位かのフラグ  1:月 2:年 */
    int graphallflag;
    // グラフ描画（年単位）
    private int totalpoziforyear[];
    private int totalnegaforyear[];
    /* DB関係 */
    private SQLiteDatabase db;
    private GuchiCommonDBOpenHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.negapozi_layout);
        /*  viewのID取得 */
        mirai = (Button) findViewById(R.id.mirai);
        all = (Button) findViewById(R.id.all);
        kako = (Button) findViewById(R.id.kako);
        form = (Button) findViewById(R.id.form);
        //TODO デバッグ用なので消す
        form.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //次画面での表示処理
                Intent timeToRankIntent = new Intent(NegapoziActivity.this, InputActivity.class);
                startActivity(timeToRankIntent);
            }
        });
        negapozi = (Button) findViewById(R.id.negapozi);
        rank = (Button) findViewById(R.id.rank);
        //TODO デバッグ用なので消す
        rank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //次画面での表示処理
                Intent timeToRankIntent = new Intent(NegapoziActivity.this, RankingActivity.class);
                startActivity(timeToRankIntent);
            }
        });
        en = (Button) findViewById(R.id.en);
        mirai.setOnClickListener(this);
        all.setOnClickListener(this);
        kako.setOnClickListener(this);
        rank.setOnClickListener(this);
        negapozi.setOnClickListener(this);
        form.setOnClickListener(this);
        en.setOnClickListener(this);
        /* DBの接続セット */
        helper = new GuchiCommonDBOpenHelper(this);
        db = helper.getReadableDatabase();

        // 日付の取得　年月日
        Calendar cal = Calendar.getInstance();
        String yearStr = "" + cal.get(Calendar.YEAR);
        String monthStr = "" + cal.get(Calendar.MONTH);
        String dayStr = "" + cal.get(Calendar.DATE);
        /* 表示している年月日の設定 */
        this.YEAR = Integer.parseInt(yearStr);
        //日付の月が-1で取得される。原因不明。
        this.MONTH = Integer.parseInt(monthStr) + 1;
        this.DAY = Integer.parseInt(dayStr);
        /* 現在の年月日を設定 */
        this.NOWYEAR = Integer.parseInt(yearStr);
        this.NOWMONTH = Integer.parseInt(monthStr) + 1;
        this.NOWDAY = Integer.parseInt(dayStr);
        /* 年月の最大値をセット */
        this.MAXMONTH = Integer.parseInt(monthStr) + 1;
        this.MAXYEAR = Integer.parseInt(yearStr);
        /* 年月の最小値をセット */
        String getMinDateSQL = "SELECT distinct MIN(Year),MIN(Month) FROM negapozi";

        Cursor mindate = db.rawQuery(getMinDateSQL, null);
        boolean mindateroop = mindate.moveToFirst();
        while (mindateroop) {
            this.MINYEAR = mindate.getInt(0);
            this.MINMONTH = mindate.getInt(1);
            Log.e("NEGAPOZI", "最小年：" + this.MINYEAR + "最小月" + this.MINMONTH);
            mindateroop = mindate.moveToNext();
        }

        Log.e("NEGAPOZI--START--", yearStr + monthStr + dayStr);
        // グラフ描画（月単位用）
        graph2 = (ChartView) findViewById(R.id.graphview2);
        totalpozi = new int[32];
        totalnega = new int[32];
        this.createGraph();
        // 折れ線
        graphflag = 1;
        // 月表示
        graphallflag = 1;
         /* ボタンの有効状態設定 */
        setButtonEnabled();

    }

    /*  各ボタンクリック時の処理 */
    public void onClick(View v) {

        /* 年か月か  */
        // 年単位（ALL）
        if (v == all) {
            //　今が月単位なら
            if (graphallflag == 1) {
                // 今が円なら円表示
                if (graphflag == 2) {
                    graphallflag = 2;
                    this.createPieChart();
                    all.setText("月");
                    // それ以外は折れ線表示
                } else {
                    graphallflag = 2;
                    this.createAllGraph();
                    all.setText("月");
                }
                //　今が年単位なら
            } else {
                // 今が円なら円表示
                if (graphflag == 2) {
                    graphallflag = 1;
                    this.createPieChart();
                    all.setText("年");
                    // それ以外は折れ線表示
                } else {
                    this.createGraph();
                    graphallflag = 1;
                    all.setText("年");
                }
            }
        }
        // 次月または次年
        if (v == mirai) {
            if(graphallflag == 2) {
                YEAR = YEAR + 1;
                // 折れ線か円か
                if (graphflag == 1) {
                    this.createAllGraph();
                    all.setText("月");
                } else {
                    this.createPieChart();
                    all.setText("月");
                }
            } else {
                MONTH = MONTH + 1;
                if (MONTH == 13) {
                    MONTH = 1;
                    YEAR = YEAR + 1;
                }
                //折れ線か円か
                if (graphflag == 1) {
                    this.createGraph();
                    all.setText("年");
                } else {
                    this.createPieChart();
                    all.setText("年");
                }
            }

            // 前月または前年
        } else if (v == kako) {
            // 年単位なら
            if(graphallflag == 2){
                YEAR = YEAR +1;
                // 折れ線か円か
                if (graphflag == 1) {
                    this.createAllGraph();
                    all.setText("月");
                } else {
                    this.createPieChart();
                    all.setText("月");
                }
                // 月単位なら
            } else {
                MONTH = MONTH - 1;
                if (MONTH == 0) {
                    MONTH = 12;
                    YEAR = YEAR - 1;
                }
                // 折れ線か円か
                if (graphflag == 1) {
                    this.createGraph();
                    all.setText("年");
                } else {
                    this.createPieChart();
                    all.setText("年");
                }
            }

            /*  ランキング画面に遷移  */
        } else if (v == rank) {
            /*  ネガポジ画面に遷移  */
        } else if (v == negapozi) {
            /* 入力画面に遷移 */
        } else if (v == form) {
            /* 円グラフ表示 */
        } else if (v == en) {
            // 今が折れ線なら円表示
            if (graphflag == 1) {
                this.createPieChart();
                graphflag = 2;
                en.setText("折");
                // それ以外は折れ線表示
            } else {
                // 年単位か月単位か
                if(graphallflag == 1) {
                    this.createGraph();
                }else{
                    this.createAllGraph();
                }
                graphflag = 1;
                en.setText("円");
            }
        }
        /* ボタンの有効状態設定 */
        setButtonEnabled();
    }

    /*
     *
     * 各ボタンの有効状態を設定
     *
     * */
    private void setButtonEnabled() {
        /* ボタンの有効状態 */
        /* 月表示の場合(graphallflag = 1) */
        if (this.graphallflag == 1) {
        /* 現在表示日付 >= 年月の最大値 なら次月ボタン無効。それ以外は有効 */
            if ((this.YEAR >= this.MAXYEAR) && (this.MONTH >= this.MAXMONTH)) {
                mirai.setEnabled(false);
            } else {
                mirai.setEnabled(true);
            }
        /* 現在表示日付 <= 年月の最小値 なら前月ボタン無効。それ以外は有効 */
            if ((this.YEAR <= this.MINYEAR) && (this.MONTH <= this.MINMONTH)) {
                kako.setEnabled(false);
            } else {
                kako.setEnabled(true);
            }
            /* 年表示の場合(graphallflag=2) */
        } else if(this.graphallflag == 2) {
            /* 現在表示日付 >= 年月の最大値 なら次月ボタン無効。それ以外は有効 */
            if ((this.YEAR >= this.MAXYEAR)) {
                mirai.setEnabled(false);
            } else {
                mirai.setEnabled(true);
            }
        /* 現在表示日付 <= 年月の最小値 なら前月ボタン無効。それ以外は有効 */
            if ((this.YEAR <= this.MINYEAR)) {
                kako.setEnabled(false);
            } else {
                kako.setEnabled(true);
            }
        }
    }

    /*
     *
     *  折れ線グラフ(月単位)の描画
     *
     */
    private void createGraph(){

        /* 指定月の日数ごとのネガポジ度数、日にちの取得 */
        String sqlformonth = "SELECT distinct Day,SUM(Pozi),SUM(Nega) FROM negapozi WHERE Year = "+ this.YEAR + " AND Month = "+ this.MONTH + " GROUP BY Day ORDER BY day ASC";
        Cursor cu = db.rawQuery(sqlformonth, null);
        boolean mov2 = cu.moveToFirst();

        /* グラフ表示用配列の初期化 */
        int count = 0;
        while (count < 32 ){
            totalpozi[count] = 0;
            totalnega[count] = 0;
            count++;
        }
        /* 配列の要素 = 日にち にネガポジ度数を設定　※ネガポジ度数の無い日は0 */
        int countday = 0;
        while (mov2){
            countday = cu.getInt(0)-1 ;
            totalpozi[countday] = cu.getInt(1);
            totalnega[countday] = cu.getInt(2);
            mov2 = cu.moveToNext();
        }
        /* X軸、Y軸生成 */
        int graphcount = 0;
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries seriespozi = new XYSeries("ポジティブ");
        XYSeries seriesnega = new XYSeries("ネガティブ");
        int pozisum = 0;
        int negasum = 0;
        int maxDays = 0;
        /* 0からスタート */
        seriespozi.add(0.0,0.0);
        seriesnega.add(0.0,0.0);
        /*
         * 折れ線グラフ描画
         * 表示月が現在月の場合、現在日に設定
         * */
        if ( (this.YEAR == this.NOWYEAR) && (this.MONTH == this.NOWMONTH) ){
            maxDays = this.NOWDAY;
        } else {
            maxDays = MONTHDAYS[this.MONTH-1];
        }
        while (graphcount < maxDays){
            pozisum = pozisum + totalpozi[graphcount];
            negasum = negasum + totalnega[graphcount];
            seriespozi.add(graphcount+1,pozisum);
            seriesnega.add(graphcount+1,negasum);
            graphcount++;
        }
        dataset.addSeries(seriespozi);
        dataset.addSeries(seriesnega);

        TextTitle title = new TextTitle(this.YEAR+"年"+this.MONTH + "月");
        title.setFont(new Font(Typeface.SANS_SERIF, Typeface.BOLD, 44));
        /* グラフインスタンスの生成 */
        AFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "日",
                "度数",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        chart.setTitle(title);

        //*** フォントの設定 *****
        Font xyTitleFont = new Font(Typeface.SANS_SERIF, Typeface.BOLD, 26);
        Font xyTitleFontLabel = new Font(Typeface.SANS_SERIF, Typeface.BOLD, 30);
        //*** 凡例のフォント *****
        Font legendFont = new Font(Typeface.SANS_SERIF, Typeface.BOLD, 30);
        //*** 背景の色 ******
        chart.setBackgroundPaintType(new SolidColor(Color.WHITE));
        //*** 枠線の色 ******
        chart.setBorderPaintType(new SolidColor(Color.BLACK));
        XYPlot plot = (XYPlot) chart.getPlot();
        //*** グラフ領域の背景とY軸を黒にする *****
        plot.setBackgroundPaintType(new SolidColor(Color.BLACK));
        plot.setDomainGridlinePaintType(new SolidColor(Color.BLACK));
        // *** Y軸の目盛間隔とフォントの変更
        ValueAxis yAxis = plot.getRangeAxis();
        TickUnits ty = new TickUnits();
        TickUnit uniY = new NumberTickUnit(10);
        ty.add(uniY);
        yAxis.setStandardTickUnits(ty);
        yAxis.setTickLabelFont(xyTitleFont);
        yAxis.setRange(0, 50);
        yAxis.setLabelFont(xyTitleFontLabel);
        // *** x軸の目盛間隔とフォントの変更
        ValueAxis xAxis = plot.getDomainAxis();
        TickUnits tx = new TickUnits();
        TickUnit uniX = new NumberTickUnit(10);
        tx.add(uniX);
        xAxis.setStandardTickUnits(tx);
        xAxis.setTickLabelFont(xyTitleFont);
        xAxis.setRange(0,31);
        xAxis.setLabelFont(xyTitleFontLabel);

        //*** 各線の太さ *****
        XYItemRenderer renderer = plot.getRenderer();
        float aLine = 5f;
        renderer.setSeriesStroke(0, aLine);
        renderer.setSeriesStroke(1, aLine);
        //*** 凡例設定 ***
        LegendTitle legend = new LegendTitle(chart.getPlot());
        legend.setItemFont(legendFont);
        chart.addLegend(legend);

        // グラフの描画
        graph2.setChart(chart);
        graph2.invalidate();
    }

    /*
     *
     *  円グラフの描画
     *
     *  */
    private void createPieChart(){

        String sqlformonth = null;
        if (graphallflag == 1) {
            /* 指定月の日数ごとのネガポジ度数、日にちの取得 */
            sqlformonth = "SELECT distinct Day,SUM(Pozi),SUM(Nega) FROM negapozi WHERE Year = " + this.YEAR + " AND Month = " + this.MONTH + " GROUP BY Day ORDER BY day ASC";
        } else {
            /* 指定年の日数ごとのネガポジ度数、日にちの取得 */
            sqlformonth = "SELECT distinct Month,SUM(Pozi),SUM(Nega) FROM negapozi WHERE Year = "+ this.YEAR +  " GROUP BY Month ORDER BY Month ASC";
        }
        Cursor cu = db.rawQuery(sqlformonth, null);
        boolean mov2 = cu.moveToFirst();

        /* グラフ表示用配列の初期化 */
        int countgra = 0;
        while (countgra < 32 ){
            totalpozi[countgra] = 0;
            totalnega[countgra] = 0;
            countgra++;
        }
        /* 配列の要素 = 日にち にネガポジ度数を設定　※ネガポジ度数の無い日は0 */
        int countday = 0;
        while (mov2){
            countday = cu.getInt(0)-1 ;
            totalpozi[countday] = cu.getInt(1);
            totalnega[countday] = cu.getInt(2);
            mov2 = cu.moveToNext();
        }

        /*  ネガポジ度数、月合計値取得  */
        /*  年単位か月単位かで描画する設定値を変更  */
        TextTitle title;
        int max;
        if( graphallflag == 1){
            max = 32;
         title =  new TextTitle(this.YEAR+"年"+this.MONTH + "月");
        } else {
            max = 13;
            title = new TextTitle(this.YEAR+"年");
        }
        title.setFont(new Font(Typeface.SANS_SERIF, Typeface.BOLD, 44));
        int count = 0;
        int sumpozi = 0;
        int sumnega = 0;
        while(count < max){
            sumpozi = totalpozi[count] + sumpozi;
            sumnega = totalnega[count] + sumnega;
            count++;
        }
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("ポジティブ",sumpozi);
        dataset.setValue("ネガティブ",sumnega);
        AFreeChart chart = ChartFactory.createPieChart("", dataset,
                true, false, false);
        //*** タイトルの設定 *****
        chart.setTitle(title);
        PiePlot plot = (PiePlot) chart.getPlot();
        //*** 背景の色 ****
        chart.setBackgroundPaintType(new SolidColor(Color.WHITE));
        //*** 枠線の色 ****
        chart.setBorderPaintType(new SolidColor(Color.BLACK));
        //*** フォントの設定 *****
        Font TitleFont = new Font(Typeface.SANS_SERIF, Typeface.BOLD, 28);
        //*** ラベルのフォント ******
        Font TitleFontLabel = new Font(Typeface.SANS_SERIF, Typeface.BOLD, 26);
        //*** グラフ領域の背景
        plot.setBackgroundPaintType(new SolidColor(Color.BLACK));
        //*** ラベルの設定 *****
        // ラベルの設定
        plot.setSimpleLabels(true);
        plot.setLabelFont(TitleFontLabel);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}={2}"));
        plot.setLabelBackgroundPaintType(null);
        plot.setLabelOutlineStroke(null);
        plot.setLabelShadowPaint(null);
        //*** 凡例の設定 *****
        LegendTitle leg = chart.getLegend();
        leg.setItemFont(new Font(Typeface.SANS_SERIF, Typeface.BOLD, 30));
        leg.setBorder(0d,0d,0d,0d);
        //*** 透明度の設定 ****
        //plot.setForegroundAlpha(1);
        //plot.setBackgroundAlpha(1);
        // グラフの描画
        graph2.setChart(chart);
        graph2.invalidate();
    }

    /*
     *
     * 折れ線グラフ（年単位）の描画
     *
     * */
    private void createAllGraph(){
        /* 指定年の日数ごとのネガポジ度数、日にちの取得 */
        String sqlformonth = "SELECT distinct Month,SUM(Pozi),SUM(Nega) FROM negapozi WHERE Year = "+ this.YEAR +  " GROUP BY Month ORDER BY Month ASC";
        Cursor cur = db.rawQuery(sqlformonth, null);
        boolean mov3 = cur.moveToFirst();

        totalpoziforyear = new int[13];
        totalnegaforyear = new int[13];

        /* グラフ表示用配列の初期化 */
        int count = 0;
        while (count < 13 ){
            totalpozi[count] = 0;
            totalnega[count] = 0;
            count++;
        }
        /* 配列の要素 = 日にち にネガポジ度数を設定　※ネガポジ度数の無い日は0 */
        int countday = 0;
        while (mov3){
            countday = cur.getInt(0)-1 ;
            totalpozi[countday] = cur.getInt(1);
            totalnega[countday] = cur.getInt(2);
            mov3 = cur.moveToNext();
        }
        /* X軸、Y軸生成 */
        int graphcount = 0;
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries seriespozi = new XYSeries("ポジティブ");
        XYSeries seriesnega = new XYSeries("ネガティブ");
        int pozisum = 0;
        int negasum = 0;
        /* 0からスタート */
        seriespozi.add(0.0,0.0);
        seriesnega.add(0.0,0.0);
        /* 折れ線グラフ描画 */
        while (graphcount < MONTHDAYS[this.MONTH-1]){
            pozisum = pozisum + totalpozi[graphcount];
            negasum = negasum + totalnega[graphcount];
            seriespozi.add(graphcount+1,pozisum);
            seriesnega.add(graphcount+1,negasum);
            graphcount++;
        }
        dataset.addSeries(seriespozi);
        dataset.addSeries(seriesnega);
        /* グラフインスタンスの生成 */
        AFreeChart chart = ChartFactory.createXYLineChart(
                this.YEAR+"年",
                "月",
                "度数",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        //*** タイトルの設定 *****
        TextTitle title = new TextTitle(this.YEAR+"年");
        title.setFont(new Font(Typeface.SANS_SERIF, Typeface.BOLD, 44));
        chart.setTitle(title);
        //*** フォントの設定 *****
        //*** メモリのタイトルの設定 *****
        Font xyTitleFont = new Font(Typeface.SANS_SERIF, Typeface.BOLD, 26);
        Font xyTitleFontLabel = new Font(Typeface.SANS_SERIF, Typeface.BOLD, 30);
        //*** 凡例のフォント *****
        Font legendFont = new Font(Typeface.SANS_SERIF, Typeface.BOLD, 30);
        //*** 背景の色 ****
        chart.setBackgroundPaintType(new SolidColor(Color.WHITE));
        //*** 枠線の色 ****
        chart.setBorderPaintType(new SolidColor(Color.BLACK));
        XYPlot plot = (XYPlot) chart.getPlot();
        //*** グラフ領域の背景とY軸を黒にする *****
        plot.setBackgroundPaintType(new SolidColor(Color.BLACK));
        plot.setDomainGridlinePaintType(new SolidColor(Color.BLACK));
        // *** Y軸の目盛間隔とフォントの変更
        ValueAxis yAxis = plot.getRangeAxis();
        TickUnits ty = new TickUnits();
        TickUnit uniY = new NumberTickUnit(120);
        ty.add(uniY);
        yAxis.setStandardTickUnits(ty);
        yAxis.setTickLabelFont(xyTitleFont);
        yAxis.setRange(0, 600);
        yAxis.setLabelFont(xyTitleFontLabel);
        // *** x軸の目盛間隔とフォントの変更
        ValueAxis xAxis = plot.getDomainAxis();
        TickUnits tx = new TickUnits();
        TickUnit uniX = new NumberTickUnit(3);
        tx.add(uniX);
        xAxis.setStandardTickUnits(tx);
        xAxis.setTickLabelFont(xyTitleFont);
        xAxis.setRange(0,12);
        xAxis.setLabelFont(xyTitleFontLabel);
        //*** 各線の太さ *****
        XYItemRenderer renderer = plot.getRenderer();
        float aLine = 6f;
        renderer.setSeriesStroke(0, aLine);
        renderer.setSeriesStroke(1, aLine);
        //*** 凡例作成 ***
        LegendTitle legend = new LegendTitle(chart.getPlot());
        legend.setItemFont(legendFont);
        chart.addLegend(legend);
        // グラフの描画
        graph2.setChart(chart);
        graph2.invalidate();
    }

    /* DB切断 */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}