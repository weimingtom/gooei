package de.ofahrt.utils.fonts.ttf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import de.ofahrt.utils.fonts.FontDrawInterface;
import de.ofahrt.utils.fonts.Font;
import de.ofahrt.utils.fonts.FontMetrics;

public class TtFont implements Font
{

protected static final Logger logger = Logger.getLogger("de.ofahrt.utils.fonts.ttf");

final TtfData fontData;
private final float pointSize;

public TtFont(TtfData fontData, float pointSize)
{
	this.fontData = fontData;
	this.pointSize = pointSize;
}

public String getName()
{ return fontData.getName(); }

public int getSize()
{ return Math.round(pointSize); }

public float getPointSize()
{ return pointSize; }

public FontMetrics getMetrics()
{ return new TtfMetrics(this); }

public Font deriveFontByPointSize(int newPointSize)
{ return new TtFont(fontData, newPointSize); }

public Font deriveFontByPixelSize(int pixelSize)
{
	float cfak = (pixelSize-0.5f)/(fontData.getAscender()-fontData.getDescender());
	float size = (cfak*72.0f*fontData.getUpem())/96.0f;
	return new TtFont(fontData, size);
}

int drawGlyph(FontDrawInterface graphics, int c, int xoff, int yoff)
{
	TtfGlyph glyph = fontData.getGlyph(c);
	ArrayList splines = glyph.splines;
	
	float cfak = (pointSize*96.0f)/(72.0f*fontData.getUpem());
	int lsb = glyph.metric.leftSideBearing;
	int baseline = Math.round(-fontData.getDescender()*cfak);
	int height = Math.round((fontData.getAscender()-fontData.getDescender())*cfak);
//	int width = Math.round((glyph.maxx-glyph.minx)*cfak);
	int advanceWidth = Math.round(glyph.metric.advanceWidth*cfak+0.5f);
	
	BezierSpline[] current = new BezierSpline[30];
	double[] isections = new double[30];
	for (int yc = 0; yc < height; yc++)
	{
		float y = Math.round(64*(yc-baseline)/cfak)/64.0f+1/128.0f;
		int count = 0;
		int icount = 0;
		for (int i = 0; i < splines.size(); i++)
		{
			BezierSpline bs = (BezierSpline) splines.get(i);
			if ((bs.getMinY() <= y) && (bs.getMaxY() > y))
				current[count++] = bs;
		}
		
		for (int i = 0; i < count; i++)
		{
			BezierSpline bs = current[i];
			// berechne Schnittpunkte
			if (bs.d == 0)
			{
				double t = (y-bs.f)/(2*bs.e);
				if ((0 <= t) && (t <= 1))
				{
					double x = bs.a*t*t + 2*bs.b*t + bs.c;
					isections[icount++] = x;
				}
			}
			else
			{
				double h = bs.e*bs.e - bs.d*(bs.f-y);
				// if we only touch the curve, we don't take it
				if (h <= 0) continue;
				double t0 = (-bs.e+Math.sqrt(h))/bs.d;
				double t1 = (-bs.e-Math.sqrt(h))/bs.d;
				
				if ((0 <= t0) && (t0 <= 1))
				{
					double x = bs.a*t0*t0 + 2*bs.b*t0 + bs.c;
					isections[icount++] = x;
				}
				
				if ((0 <= t1) && (t1 <= 1))
				{
					double x = bs.a*t1*t1 + 2*bs.b*t1 + bs.c;
					isections[icount++] = x;
				}
			}
		}
		
		// no intersections? continue!
		if (icount == 0) continue;
		
		// sort intersections
		Arrays.sort(isections, 0, icount);
		// subtract left side-bearing
		for (int i = 0; i < icount; i++)
			isections[i] -= lsb;
		
		if ((icount & 1) != 0)
		{
			logger.warning("ARGH: Odd number of intersections!");
			logger.warning("for character '"+(char) c+"'");
			logger.warning("in line "+yc+" -> "+y);
			for (int i = 0; i < icount; i++)
				logger.warning(""+isections[i]);
		}
		
		for (int i = 0; i < icount-1; i += 2)
		{
			double xs = isections[i]*cfak+xoff;
			double xe = isections[i+1]*cfak+xoff;
			int x0 = (int) Math.floor(xs+1.5);
			int x1 = (int) Math.floor(xe-0.5);
			float frac = (float) (x0-xs-0.5);
			graphics.drawPixel(x0-1, yc+yoff, frac);
			for (int j = x0; j <= x1; j++)
				graphics.drawPixel(j, yc+yoff, 1);
			frac = (float) (xe-x1-0.5);
			graphics.drawPixel(x1+1, yc+yoff, frac);
		}
		
/*		for (int i = 0; i < icount; i++)
		{
			int xc = (int) Math.round(isections[i]*cfak+xoff);
			image.setPixel(xc, yc+yoff, 1, 1, 1);
		}*/
	}
	
	return advanceWidth;
}

/*private void drawBezierSpline(Image image, int xoff, int yoff, float size, int[] p0, int[] p1, int[] p2)
{
	int max = 10;
	for (int i = 0; i < max; i++)
	{
		float t = i / (max-1.0f);
		float xc = p0[0]*(1-t)*(1-t) + 2*p1[0]*t*(1-t) + p2[0]*t*t;
		float yc = p0[1]*(1-t)*(1-t) + 2*p1[1]*t*(1-t) + p2[1]*t*t;
		int x = Math.round(xoff+xc*size);
		int y = Math.round(yoff+yc*size);
		image.setPixel(x, y, 1, 1, 1);
	}
}

private void printGlyph(Image image, Glyph glyph, int size, int xoff, int yoff)
{
  float cfak = (size*96.0f)/(72.0f*upem);
  int xadd = 10;
  int yadd = 100;
	for (int i = 0; i < glyph.count; i++)
	{
		Contour contour = glyph.data[i];
		for (int j = 0; j < contour.count-1; j += 2)
		{
			int x = Math.round(contour.data[j][0]*cfak+xoff);
			int y = Math.round(contour.data[j][1]*cfak+yoff);
			image.setPixel(x, y, 1, 1, 1);
//			drawBezierSpline(image, xoff, yoff, cfak, contour.data[j], contour.data[j+1], contour.data[j+2]);
		}
	}
}

private void testString(String s)
{
	Image image = new Image(800, 800);
	DrawInterface graphics = new DrawInterface(image);
	int position = 0;
	for (int i = 0; i < s.length(); i++)
		position += drawGlyph(graphics, s.charAt(i), 21, position, 100);
	new ImageViewer(image, "Test");
}

private void testGlyph(int c)
{
	Image image = new Image(800, 800);
	printGlyph(image, glyphs[c], 212, 0, 100);
	drawGlyph(new DrawInterface(image), c, 212, 0, 100);
	new ImageViewer(image, "Test");
}

public void testFont()
{
	testString("HilaLg und ABC!\"&@,;:'/\\|{}()<>=?#^.+*0175XM");
	// "Hallihallog und so weiter!"
//result.testGlyph('%');
}*/

}
