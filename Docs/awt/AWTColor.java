package thinlet.awt;

import java.awt.Color;

import thinlet.help.TLColor;

final class AWTColor implements TLColor
{

public static final AWTColor BLUE = new AWTColor(0, 0, 1);

private final Color color;

public AWTColor(Color color)
{ this.color = color; }

public AWTColor(float r, float g, float b, float a)
{ color = new Color(r, g, b, a); }

public AWTColor(float r, float g, float b)
{ this(r, g, b, 1); }

public AWTColor(int rgb)
{ color = new Color(rgb); }

public Color getColor()
{ return color; }

public float getAlpha()
{ return color.getAlpha()/255.0f; }

public float getBlue()
{ return color.getBlue()/255.0f; }

public float getGreen()
{ return color.getGreen()/255.0f; }

public float getRed()
{ return color.getRed()/255.0f; }

public AWTColor brighter()
{ return new AWTColor(color.brighter()); }

public AWTColor darker()
{ return new AWTColor(color.darker()); }

}
