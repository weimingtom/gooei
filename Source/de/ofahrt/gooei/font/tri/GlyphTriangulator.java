package de.ofahrt.gooei.font.tri;


import java.util.ArrayList;
import java.util.List;

import de.ofahrt.gooei.font.ttf.TtfContour;
import de.ofahrt.gooei.font.ttf.TtfGlyph;
import de.yvert.algorithms.triangulation.Contour;
import de.yvert.algorithms.triangulation.PolygonTriangulator2D;
import de.yvert.algorithms.triangulation.TriangulationListener;
import de.yvert.geometry.Vector2;

class GlyphTriangulator
{

public GlyphTriangulator()
{/*OK*/}

public TriGlyph triangulate(TtfGlyph glyph)
{
	final List<Vector2> allvecs = new ArrayList<Vector2>();
	final List<int[]> triangles = new ArrayList<int[]>();
	
	int subdivision = 1;
	List<Contour> contours = new ArrayList<Contour>();
	for (int k = 0; k < glyph.size(); k++)
	{
		TtfContour contour = glyph.getContour(k);
		List<Vector2> vecs = new ArrayList<Vector2>();
		for (int i = 0; i < contour.count-1; i++)
		{
			if ((i & 1) == 1)
			{
				if ((contour.data[i][0] != (contour.data[i-1][0]+contour.data[i+1][0])/2) ||
						(contour.data[i][1] != (contour.data[i-1][1]+contour.data[i+1][1])/2))
				{
					for (int j = 0; j < subdivision; j++)
					{
						float t = (j+1.0f)/(subdivision+1.0f);
						float x = contour.data[i-1][0]*(1-t)*(1-t)+2*contour.data[i][0]*t*(1-t)+contour.data[i+1][0]*t*t;
						float y = contour.data[i-1][1]*(1-t)*(1-t)+2*contour.data[i][1]*t*(1-t)+contour.data[i+1][1]*t*t;
						vecs.add(new Vector2(x, y));
					}
				}
			}
			else
				vecs.add(new Vector2(contour.data[i][0], contour.data[i][1]));
		}
		
		Vector2[] v = vecs.toArray(new Vector2[0]);
		// automatically done
/*		if (reverse)
		{
			for (int i = 0; i < v.length/2; i++)
			{
				Vector2 temp = v[i];
				v[i] = v[v.length-1-i];
				v[v.length-1-i] = temp;
			}
		}*/
		
		for (int i = 0; i < v.length; i++)
			allvecs.add(v[i]);
		contours.add(new Contour(v));
	}
	
	final Vector2[] v = allvecs.toArray(new Vector2[0]);
	if (v.length > 0)
	{
		PolygonTriangulator2D triangulator = new PolygonTriangulator2D();
//		System.out.println(glyph.getName());
		triangulator.calculate(contours.toArray(new Contour[0]), new TriangulationListener()
			{
				private boolean reverse = false;
				public void addEdge(int a, int b)
				{/*OK*/}
				public void addTriangle(int a, int b, int c)
				{
					if (reverse)
						triangles.add(new int[] { c, b, a });
					else
						triangles.add(new int[] { a, b, c });
				}
				public void reversing()
				{ reverse = true; }
			});
	}
	
	return new TriGlyph(glyph, v, triangles.toArray(new int[0][]));
}

}
