package de.ofahrt.utils.fonts.tri;

import de.ofahrt.utils.fonts.ttf.MetricInfo;
import de.ofahrt.utils.fonts.ttf.TtfGlyph;
import de.yvert.geometry.Vector2;

public class TriGlyph
{

final MetricInfo metric;
final int minx, maxx, miny, maxy;
public final Vector2[] vertices;
public final int[][] triangles;

public TriGlyph(TtfGlyph original, Vector2[] vertices, int[][] triangles)
{
	this.metric = original.getMetric();
	this.minx = original.getMinX();
	this.maxx = original.getMaxX();
	this.miny = original.getMinY();
	this.maxy = original.getMaxY();
	this.vertices = vertices;
	this.triangles = triangles;
}

}
