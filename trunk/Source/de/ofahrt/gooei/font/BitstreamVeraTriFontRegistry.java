package de.ofahrt.gooei.font;

import gooei.font.Font;
import gooei.font.FontRegistry;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import de.ofahrt.gooei.font.tri.TriData;
import de.ofahrt.gooei.font.tri.TriFont;
import de.ofahrt.gooei.font.ttf.TtfData;

public class BitstreamVeraTriFontRegistry implements FontRegistry
{

private static final String DEFAULT_NAME = "SansSerif";
private static final int DEFAULT_SIZE = 18;

private final HashMap<String,TriData> map = new HashMap<String,TriData>();
private final HashMap<String,TriFont> cache = new HashMap<String,TriFont>();
private Font defaultFont;

public BitstreamVeraTriFontRegistry() throws IOException
{
	map.put("SansSerif", new TriData(TtfData.load("de/ofahrt/fonts/bitstreamvera/Vera.ttf")));
	map.put("Monospaced", new TriData(TtfData.load("de/ofahrt/fonts/bitstreamvera/VeraMono.ttf")));
	map.put("Serif", new TriData(TtfData.load("de/ofahrt/fonts/bitstreamvera/VeraSe.ttf")));
}

public Font getDefaultFont()
{
	if (defaultFont == null)
		defaultFont = getFont("");
	return defaultFont;
}

public Font getFont(String value)
{
	TriFont result = cache.get(value);
	if (result == null)
	{
		String name = null;
		boolean bold = false, italic = false;
		int size = 0;
		StringTokenizer st = new StringTokenizer(value);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if ("bold".equalsIgnoreCase(token)) bold = true;
			else if ("italic".equalsIgnoreCase(token)) italic = true;
			else
			{
				try
				{
					size = Integer.parseInt(token);
				}
				catch (NumberFormatException nfe)
				{
					name = name == null ? new String(token) : (name + " " + token);
				}
			}
		}
		if (name == null) name = DEFAULT_NAME;
		if (size == 0) size = DEFAULT_SIZE;
		if (bold || italic) throw new RuntimeException("Not implemented!");
		TriData data = map.get(name);
		result = new TriFont(data, data.pixelToPointSize(size));
		cache.put(value, result);
	}
	return result;
}

}
