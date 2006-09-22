package de.ofahrt.utils.fonts.tri;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import de.ofahrt.utils.fonts.ttf.TtfContour;
import de.ofahrt.utils.fonts.ttf.TtfData;
import de.ofahrt.utils.fonts.ttf.TtfGlyph;
import de.yvert.geometry.Vector2;

class TriangulationTest extends Canvas
{

private static final long serialVersionUID = 1L;

TtfContour contour;
TriGlyph glyph;

public TriangulationTest() throws Exception
{
	TtfData data = TtfData.load("Serif");
	TtfGlyph original = data.getGlyph('%');
	contour = original.getContour(0);
	try
	{
		glyph = new GlyphTriangulator().triangulate(original);
	}
	catch (Exception e)
	{ e.printStackTrace(); }
}

@Override
public void paint(Graphics gold)
{
	Graphics2D g = (Graphics2D) gold;
	float scale = 0.2f;
	g.translate(100, 400);
	g.scale(scale, -scale);
	if (glyph == null)
	{
		for (int i = 0; i < contour.count-1; i++)
		{
			g.drawLine(contour.data[i][0], contour.data[i][1],
					contour.data[i+1][0], contour.data[i+1][1]);
		}
	}
	
	if (glyph != null)
	{
		for (int[] t : glyph.triangles)
		{
			Vector2 u = glyph.vertices[t[0]];
			Vector2 v = glyph.vertices[t[1]];
			Vector2 w = glyph.vertices[t[2]];
			g.drawLine((int) u.getX(), (int) u.getY(), (int) v.getX(), (int) v.getY());
			g.drawLine((int) u.getX(), (int) u.getY(), (int) w.getX(), (int) w.getY());
			g.drawLine((int) v.getX(), (int) v.getY(), (int) w.getX(), (int) w.getY());
		}
	}
}

public static void main(String[] args) throws Exception
{
	Frame f = new Frame("Test");
	f.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{ System.exit(0); }
		});
	f.setLocation(100, 100);
	f.setSize(800, 600);
	f.add(new TriangulationTest());
	f.setVisible(true);
}

}
