package de.ofahrt.gooei.font.bmp;

import gooei.font.Font;
import gooei.font.FontDrawInterface;
import gooei.font.FontMetrics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.ofahrt.gooei.lwjgl.LwjglPreparedIcon;
import de.yvert.jingle.impl.reader.ImageReader_tga;
import de.yvert.jingle.ldr.LdrImage2D;

public class BmpFont implements Font
{

private final BmpData data;
private final LwjglPreparedIcon preparedIcon;

public BmpFont(BmpData data, LdrImage2D image)
{
	this.data = data;
	this.preparedIcon= new LwjglPreparedIcon(image);
}

BmpData getData()
{ return data; }

public String getName()
{ return data.name; }

public int getSize()
{ return data.pixelSize; }

public float getPointSize()
{ throw new UnsupportedOperationException(); }

public Font deriveFontByPointSize(int newPointSize)
{ throw new UnsupportedOperationException(); }

public Font deriveFontByPixelSize(int pixelSize)
{ throw new UnsupportedOperationException(); }

public FontMetrics getMetrics()
{ return new BmpMetrics(this); }

public int drawGlyph(FontDrawInterface graphics, char c, int x, int y)
{
	CharInfo info = data.charInfos[c];
	
	float u0 = info.x / (float)preparedIcon.getWidth();
	float v0 = info.y / (float)preparedIcon.getHeight();
	float u1 = ( info.x + info.width ) / (float)preparedIcon.getWidth();
	float v1 = ( info.y + info.height ) / (float)preparedIcon.getHeight();
	
	graphics.drawTexturedQuad(
	    preparedIcon,
	    x+info.xoffset,
	    //y+data.lineHeight-(info.yoffset),
	    y+info.yoffset,
	    info.width,
	    info.height,
	    u0, v0, u1, v1
	);
    
	return info.xadvance;
}

public static BmpFont load(String name) throws IOException
{
	ClassLoader classLoader = BmpFont.class.getClassLoader();
	int i = name.lastIndexOf('/');
	String base = i < 0 ? "" : name.substring(0, i+1);
	InputStream in = classLoader.getResourceAsStream(name);
	BmpData data = new BmpFontParser().parse(in);
	in = classLoader.getResourceAsStream(base+data.filename);
	if (in == null) throw new FileNotFoundException(base+data.filename);
	LdrImage2D image = new ImageReader_tga().load(in);
	return new BmpFont(data, image);
}

}
