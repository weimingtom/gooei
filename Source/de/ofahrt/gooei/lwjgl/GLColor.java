package de.ofahrt.gooei.lwjgl;

import gooei.utils.TLColor;

public final class GLColor implements TLColor
{

public static final GLColor BLUE = new GLColor(0, 0, 1);
public static final GLColor BLACK = new GLColor(0, 0, 0);
public static final GLColor RED = new GLColor(1, 0, 0);

private final float r, g, b, a;

public GLColor(float r, float g, float b, float a)
{
	this.r = r;
	this.g = g;
	this.b = b;
	this.a = a;
}

private GLColor(float r, float g, float b)
{ this(r, g, b, 1); }

public float getAlpha()
{ return a; }

public float getBlue()
{ return b; }

public float getGreen()
{ return g; }

public float getRed()
{ return r; }

public GLColor brighter()
{ return new GLColor(1-(1-r)/2, 1-(1-g)/2, 1-(1-b)/2, a); }

public GLColor darker()
{ return new GLColor(r/2, g/2, b/2, a); }

@Override
public String toString()
{ return "["+r+","+g+","+b+","+a+"]"; }

}
