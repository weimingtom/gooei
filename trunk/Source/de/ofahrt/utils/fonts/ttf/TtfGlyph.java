package de.ofahrt.utils.fonts.ttf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TtfGlyph
{

String name;
MetricInfo metric;
int minx, maxx, miny, maxy;
int count;
TtfContour[] data = new TtfContour[30];
ArrayList<BezierSpline> splines;

public TtfGlyph(String name)
{ this.name = name; }

public String getName()
{ return name; }

public MetricInfo getMetric()
{ return metric; }

public int getMinX()
{ return minx; }

public int getMaxX()
{ return maxx; }

public int getMinY()
{ return miny; }

public int getMaxY()
{ return maxy; }

public int size()
{ return count; }

public TtfContour getContour(int i)
{ return data[i]; }

public void addContour(TtfContour contour)
{
	data[count++] = contour;
}

public void update()
{
	splines = new ArrayList<BezierSpline>();
	for (int i = 0; i < count; i++)
	{
		TtfContour contour = data[i];
		for (int j = 0; j < contour.count-1; j += 2)
		{
			BezierSpline bs = new BezierSpline();
			bs.x0 = contour.data[j][0];
			bs.y0 = contour.data[j][1];
			bs.x1 = contour.data[j+1][0];
			bs.y1 = contour.data[j+1][1];
			bs.x2 = contour.data[j+2][0];
			bs.y2 = contour.data[j+2][1];
			bs.update();
			splines.add(bs);
		}
	}
	Collections.sort(splines, new Comparator<BezierSpline>()
		{
			public int compare(BezierSpline a, BezierSpline b)
			{
				if (a.getMinY() < b.getMinY())
					return -1;
				if (a.getMinY() > b.getMinY())
					return 1;
				return 0;
			}
		});
}

}
