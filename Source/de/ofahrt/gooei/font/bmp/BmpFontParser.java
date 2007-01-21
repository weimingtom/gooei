package de.ofahrt.gooei.font.bmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BmpFontParser
{

private static final String VALUE_STR = "(\\w+)=(([\\w,-]+)|(\"[^\"]*\"))";

private static final Pattern PATTERN =
	Pattern.compile("(\\w+)( +"+VALUE_STR+")* *");
private static final Pattern VALUE_PATTERN =
	Pattern.compile(VALUE_STR);

public BmpFontParser selftest() throws Exception
{
	String s = "info x=y ";
	System.out.println(PATTERN.matcher(s).matches());
	s = "x=y";
	System.out.println(VALUE_PATTERN.matcher(s).matches());
	return this;
}

private static int getInt(HashMap<String,String> map, String key)
{ return Integer.parseInt(map.get(key)); }

private static String getString(HashMap<String,String> map, String key)
{ return map.get(key); }

public BmpData parse(InputStream inStream) throws IOException
{
	BmpData data = new BmpData();
	BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
	String s;
	while ((s = in.readLine()) != null)
	{
		if (s.equals("")) continue;
		Matcher m = PATTERN.matcher(s);
		if (!m.matches()) throw new IOException("unrecognized \""+s+"\"");
		
		String id = m.group(1);
		
		HashMap<String,String> map = new HashMap<String,String>();
		m = VALUE_PATTERN.matcher(s);
		while (m.find())
		{
			String key = m.group(1);
			String value = m.group(2);
			if (value.startsWith("\""))
				value = value.substring(1, value.length()-1);
			map.put(key, value);
		}
		
		if ("info".equals(id))
		{
			data.name = getString(map, "face");
			data.pixelSize = getInt(map, "size");
		} else if ("common".equals(id))
		{
			data.lineHeight = getInt(map, "lineHeight");
			data.base = getInt(map, "base");
		} else if ("page".equals(id))
		{
			data.filename = getString(map, "file");
		} else if ("char".equals(id))
		{
			int cid = Integer.parseInt(map.get("id"));
			CharInfo cinfo = new CharInfo();
			cinfo.x = getInt(map, "x");
			cinfo.y = getInt(map, "y");
			cinfo.width = getInt(map, "width");
			cinfo.height = getInt(map, "height");
			cinfo.xoffset = getInt(map, "xoffset");
			cinfo.yoffset = getInt(map, "yoffset");
			cinfo.xadvance = getInt(map, "xadvance");
			cinfo.page = getInt(map, "page");
			cinfo.chnl = getInt(map, "chnl");
			data.charInfos[cid] = cinfo;
			if (cinfo.width+cinfo.xoffset > cinfo.xadvance)
				cinfo.xadvance = cinfo.width+cinfo.xoffset;
//				throw new RuntimeException(cid+" "+(cinfo.width+cinfo.xoffset)+" "+cinfo.xadvance);
		} else if ("kerning".equals(id))
		{
			// Ok for now.
		}
		else
			throw new IOException("unrecognized id in \""+s+"\"");
	}
	return data;
}

}
