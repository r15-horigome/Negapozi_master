package xcat.daiyonkaigi.guchiruna.negapozi;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import org.afree.chart.AFreeChart;
import org.afree.graphics.geom.RectShape;

public class ChartView extends View {

    private AFreeChart chart;
    private RectShape chartArea;
    private Canvas canvas;

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        chartArea = new RectShape();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chartArea.setWidth(w);
        chartArea.setHeight(h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.chart.draw(canvas, chartArea);
    }

    public void setChart(AFreeChart chart) {
        this.chart = chart;
    }
}