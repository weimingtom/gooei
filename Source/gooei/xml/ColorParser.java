package gooei.xml;

import gooei.utils.TLColor;

import java.util.StringTokenizer;

import de.ofahrt.gooei.impl.ThinletDesktop;

public class ColorParser
{

private final ThinletDesktop desktop;

public ColorParser(ThinletDesktop desktop)
{
	this.desktop = desktop;
}

private TLColor create(int color)
{
	int red = (color >> 16) & 0xff;
	int green = (color >> 8) & 0xff;
	int blue = (color >> 0) & 0xff;
	return desktop.createColor(red, green, blue);
}

public TLColor parse(String value)
{
	int color = 0;
	if (value.startsWith("#")) { color = Integer.parseInt(value.substring(1), 16); }
	else if (value.startsWith("0x")) { color = Integer.parseInt(value.substring(2), 16); }
	else
	{ // three separated integer including red, green, and blue
		StringTokenizer st = new StringTokenizer(value, " \r\n\t,");
		color = 0xff000000 | ((Integer.parseInt(st.nextToken()) & 0xff) << 16) |
			((Integer.parseInt(st.nextToken()) & 0xff) << 8) |
			(Integer.parseInt(st.nextToken()) & 0xff);
	}
	return create(color);
}

}
