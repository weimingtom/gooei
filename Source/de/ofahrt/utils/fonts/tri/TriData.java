package de.ofahrt.utils.fonts.tri;

import de.ofahrt.utils.fonts.ttf.TtfData;
import de.ofahrt.utils.fonts.ttf.TtfGlyph;

public class TriData
{

private final String name;

private final int upem;
private final int ascender;
private final int descender;

private final TriGlyph[] glyphs = new TriGlyph[256];

public TriData(TtfData data)
{
	this.name = data.getName();
	this.upem = data.getUpem();
	this.ascender = data.getAscender();
	this.descender = data.getDescender();
	GlyphTriangulator triangulator = new GlyphTriangulator();
	for (int i = 0; i < glyphs.length; i++)
	{
		TtfGlyph glyph = data.getGlyph(i);
		glyphs[i] = glyph == null ? null : triangulator.triangulate(glyph);
	}
}

public String getName()
{ return name; }

public int getUpem()
{ return upem; }

public int getAscender()
{ return ascender; }

public int getDescender()
{ return descender; }

public TriGlyph getGlyph(int c)
{ return glyphs[c]; }

public float pixelToPointSize(int pixelSize)
{
	float cfak = (pixelSize-0.5f)/(getAscender()-getDescender());
	float size = (cfak*72.0f*getUpem())/96.0f;
	return size;
}

}
