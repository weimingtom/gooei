package de.ofahrt.gooei.font.ttf;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class TtfData
{

private static final char[] HEAD = new char[] {'h', 'e', 'a', 'd'};
private static final char[] CMAP = new char[] {'c', 'm', 'a', 'p'};
private static final char[] GLYF = new char[] {'g', 'l', 'y', 'f'};
private static final char[] LOCA = new char[] {'l', 'o', 'c', 'a'};
private static final char[] MAXP = new char[] {'m', 'a', 'x', 'p'};
private static final char[] HHEA = new char[] {'h', 'h', 'e', 'a'};
private static final char[] HMTX = new char[] {'h', 'm', 't', 'x'};

class TableEntry
{
	char[] id = new char[4];
	int checksum2;
	int offset;
	int length;
	@Override
	public String toString()
	{ return (""+id[0]+id[1]+id[2]+id[3])+" offset "+offset+" length "+length+" checksum "+checksum2; }
}

private final String name;

private int numTables;
private int searchRange;
private int entrySelector;
private int rangeShift;
private ArrayList<TableEntry> tableEntries = new ArrayList<TableEntry>();

private int numGlyphs;
private int ascender;
private int descender;

private int flags;
private int upem;
private int xmin, ymin, xmax, ymax;
private int style;
private int lowppm;
private int locformat;
private int dataformat;

private int[] map = new int[256];
private int[] locs = new int[256];
private int[] reallocs;

private MetricInfo[] metrics;

private TtfGlyph[] glyphs = new TtfGlyph[256];

private TtfData(String name)
{
	this.name = name;
}

public String getName()
{ return name; }

public int getUpem()
{ return upem; }

public int getAscender()
{ return ascender; }

public int getDescender()
{ return descender; }

public float pixelToPointSize(int pixelSize)
{
	float cfak = (pixelSize-0.5f)/(getAscender()-getDescender());
	float size = (cfak*72.0f*getUpem())/96.0f;
	return size;
}

public MetricInfo[] getMetrics()
{ return metrics.clone(); }

public TtfGlyph getGlyph(int c)
{ return glyphs[c]; }

private TableEntry find(char[] tag)
{
	for (int i = 0; i < tableEntries.size(); i++)
	{
		TableEntry entry = tableEntries.get(i);
		if ((entry.id[0] == tag[0]) && (entry.id[1] == tag[1]) &&
				(entry.id[2] == tag[2]) && (entry.id[3] == tag[3]))
		{
			return entry;
		}
	}
	return null;
}

private void readHeader(Buffer in) throws IOException
{
	TableEntry head = find(HEAD);
	in.seek(head.offset);
	int versionHi = in.readUnsignedShort();
	int versionLo = in.readUnsignedShort();
	if ((versionHi != 1) || (versionLo != 0))
		throw new IOException("Version mismatch!");
	
	in.readInt(); // revision
	in.readInt(); // checksum
	int magic = in.readInt();
	if (magic != 0x5F0F3CF5)
		throw new IOException("Magic number invalid!");
	flags = in.readUnsignedShort();
	upem = in.readUnsignedShort();
	in.readLong(); // created date
	in.readLong(); // modified date
	
	xmin = in.readShort();
	ymin = in.readShort();
	xmax = in.readShort();
	ymax = in.readShort();
	style = in.readUnsignedShort();
	lowppm = in.readUnsignedShort();
	in.readUnsignedShort(); // macStyle
	locformat = in.readUnsignedShort();
	dataformat = in.readUnsignedShort();
}

private void readMaxP(Buffer in) throws IOException
{
	TableEntry head = find(MAXP);
	in.seek(head.offset);
	int versionHi = in.readUnsignedShort();
	int versionLo = in.readUnsignedShort();
	if ((versionHi != 1) || (versionLo != 0))
		throw new IOException("Version mismatch!");
	
	numGlyphs = in.readUnsignedShort();
//	System.out.println("NumGlyphs: "+numGlyphs);
}

private void readHorizontalMetrics(Buffer in) throws IOException
{
	TableEntry head = find(HHEA);
	in.seek(head.offset);
	int versionHi = in.readUnsignedShort();
	int versionLo = in.readUnsignedShort();
	if ((versionHi != 1) || (versionLo != 0))
		throw new IOException("Version mismatch!");
	
	ascender = in.readShort();
	descender = in.readShort();
	in.readShort(); // linegap
	in.readUnsignedShort(); // maxAdvanceWidth
	in.readShort(); // minLeftSideBearing
	in.readShort(); // minRightSideBearing
	in.readShort(); // maxExtend
	in.readShort(); // caretSlopeRise
	in.readShort(); // caretSlopeRun
	in.readShort(); in.readShort(); in.readShort(); in.readShort(); in.readShort();
	int metricDataFormat = in.readShort();
	if (metricDataFormat != 0)
		throw new IOException("Metric data format mismatch!");
	int numberOfHMetrics = in.readUnsignedShort();
	
//	System.out.println("Number of H-Metrics: "+numberOfHMetrics);
	
	head = find(HMTX);
	in.seek(head.offset);
	metrics = new MetricInfo[numberOfHMetrics];
	for (int i = 0; i < numberOfHMetrics; i++)
	{
		MetricInfo info = new MetricInfo();
		info.advanceWidth = in.readUnsignedShort();
		info.leftSideBearing = in.readShort();
		metrics[i] = info;
//		System.out.println(info.leftSideBearing+" "+info.advanceWidth);
	}
}

private void readCMap(Buffer in) throws IOException
{
	TableEntry head = find(CMAP);
	in.seek(head.offset);
	
	in.readUnsignedShort();
	int count = in.readUnsignedShort();
	int offset = -1;
	
	for (int i = 0; i < count; i++)
	{
		int id1 = in.readUnsignedShort();
		int id2 = in.readUnsignedShort();
		int value = in.readInt();
		if ((id1 == 3) && (id2 == 1)) offset = value;
	}
	if (offset == -1) throw new RuntimeException("Argh!");
	in.seek(head.offset+offset);
	
	int id1 = in.readUnsignedShort();
	in.readUnsignedShort();
	in.readUnsignedShort();
	
	if (id1 != 4) throw new RuntimeException("Not implemented!");
	
	int segCount = in.readUnsignedShort()/2;
	if (segCount > 100) throw new RuntimeException("Argh!");
	int[] endCount = new int[2*segCount];
	int[] startCount = new int[2*segCount];
	int[] rangeOffset = new int[2*segCount];
	int[] delta = new int[2*segCount];
	
	in.readUnsignedShort();
	in.readUnsignedShort();
	in.readUnsignedShort();
	for (int i = 0; i < segCount; i++)
		endCount[i] = in.readUnsignedShort();
	
	if (endCount[segCount-1] != 0xffff)
		throw new RuntimeException("Argh: "+endCount[segCount]);
	int flag = in.readUnsignedShort();
	if (flag != 0)
		throw new RuntimeException("Argh2?");
	
	for (int i = 0; i < segCount; i++)
		startCount[i] = in.readUnsignedShort();
	for (int i = 0; i < segCount; i++)
		delta[i] = in.readShort();
	for (int i = 0; i < segCount; i++)
		rangeOffset[i] = in.readUnsignedShort();
	
	for (int i = 0; i < 256; i++)
	{
		int j = 0;
		while (i > endCount[j]) j++;
		if (i < startCount[j])
			map[i] = 0;
		else
		{
			if (rangeOffset[j] == 0)
				map[i] = i+delta[j];
			else
				map[i] = 0;
		}
	}
	
	
	head = find(LOCA);
	if (locformat == 0)
	{
		in.seek(head.offset);
		reallocs = new int[numGlyphs];
		for (int i = 0; i < numGlyphs; i++)
			reallocs[i] = 2*in.readUnsignedShort();
		
		StringBuffer allChars = new StringBuffer();
		for (int i = 0; i < 256; i++)
		{
			in.seek(head.offset+2*map[i]);
			if (map[i] != 0) allChars.append((char) i);
			locs[i] = 2*in.readUnsignedShort();
		}
//		logger.info(allChars.toString());
	}
	else if (locformat == 1)
	{
		StringBuffer allChars = new StringBuffer();
		for (int i = 0; i < 256; i++)
		{
			in.seek(head.offset+4*map[i]);
			if (map[i] != 0) allChars.append((char) i);
			locs[i] = 2*in.readInt();
		}
//		logger.info(allChars.toString());
	}
	else
		throw new RuntimeException("Argh!");
}

private TtfGlyph readGlyph(Buffer in, int c) throws IOException
{
	TableEntry head = find(GLYF);
	in.seek(head.offset+locs[c]);
	
	TtfGlyph result = new TtfGlyph((char) c+" ("+c+")");
	result.metric = metrics[map[c]];
	int count = in.readShort();
	result.minx = in.readShort();
	result.miny = in.readShort();
	result.maxx = in.readShort();
	result.maxy = in.readShort();
	if (count <= 0) return result;
	
	int[][] lastCont = new int[20][3];
	lastCont[0][0] = 0;
	for (int i = 0; i < count; i++)
	{
		int value = in.readUnsignedShort();
		lastCont[i][1] = value;
		lastCont[i+1][0] = value+1;
		lastCont[i][2] = lastCont[i][1]-lastCont[i][0]+1;
	}
	
	int ilen = in.readUnsignedShort();
	for (int i = 0; i < ilen; i++)
		in.readByte();
	
	int[][] pointFlags = new int[count][];
	int flagValue = 0;
	int r = 0;
	for (int i = 0; i < count; i++)
	{
		pointFlags[i] = new int[lastCont[i][2]+1];
		for (int j = 0; j < lastCont[i][2]; j++)
		{
			if (r == 0)
			{
				flagValue = in.readUnsignedByte();
				if (Flags.REPEAT.isSet(flagValue))
					r = in.readUnsignedByte()+1;
				else
					r = 1;
			}
			pointFlags[i][j] = flagValue;
			r--;
		}
	}
	if (r != 0) throw new RuntimeException("too many flags!");
	
	int[][] xs = new int[count][300];
	int[][] ys = new int[count][300];
	r = 0;
	for (int i = 0; i < count; i++)
	{
		for (int j = 0; j < lastCont[i][2]; j++)
		{
			int dr = 0;
			if (!Flags.X_SHORT.isSet(pointFlags[i][j]))
			{
				if (!Flags.X.isSet(pointFlags[i][j]))
					dr = in.readShort();
			}
			else
			{
				dr = in.readUnsignedByte();
				if (!Flags.X.isSet(pointFlags[i][j]))
					dr = -dr;
			}
	    r = r+dr;
	    xs[i][j] = r;
		}
	}
	
	r = 0;
	for (int i = 0; i < count; i++)
	{
		for (int j = 0; j < lastCont[i][2]; j++)
		{
			int dr = 0;
			if (!Flags.Y_SHORT.isSet(pointFlags[i][j]))
			{
				if (!Flags.Y.isSet(pointFlags[i][j]))
					dr = in.readShort();
			}
			else
			{
				dr = in.readUnsignedByte();
				if (!Flags.Y.isSet(pointFlags[i][j]))
					dr = -dr;
			}
	    r = r+dr;
	    ys[i][j] = r;
		}
	}
	
/*	for (int i = 0; i < count; i++)
	{
		for (int j = 0; j < lastCont[i][2]; j++)
		{
			System.out.println(pointFlags[i][j]+" "+xs[i][j]+" "+ys[i][j]);
		}
	}*/
	
	for (int i = 0; i < count; i++)
	{
		int j = lastCont[i][2];
		xs[i][j] = xs[i][0];
		ys[i][j] = ys[i][0];
		pointFlags[i][j] = pointFlags[i][0];
	}
	
	for (int i = 0; i < count; i++)
	{
		TtfContour contour = new TtfContour();
		int k = 0;
		boolean needsOnPoint = false;
		for (int j = 0; j < lastCont[i][2]+1; j++)
		{
			boolean newOnPoint = !Flags.ON_POINT.isSet(pointFlags[i][j]);
			if ((needsOnPoint == newOnPoint) && (j > 0))
			{
				contour.data[k][0] = (xs[i][j-1]+xs[i][j]) / 2;
				contour.data[k][1] = (ys[i][j-1]+ys[i][j]) / 2;
				k++;
			}
			needsOnPoint = newOnPoint;
			contour.data[k][0] = xs[i][j];
			contour.data[k][1] = ys[i][j];
			k++;
		}
		contour.count = k;
		result.addContour(contour);
	}
	
	return result;
}

private void readGlyphs(Buffer in) throws IOException
{
	for (int i = 0; i < 256; i++)
	{
		if (i == 32)
		{
			glyphs[i] = new TtfGlyph("<unknown>");
			glyphs[i].metric = metrics[i];
		}
		else
			glyphs[i] = readGlyph(in, i);
		glyphs[i].update();
	}
}

private void read(Buffer in) throws IOException
{
	int versionHi = in.readUnsignedShort();
	int versionLo = in.readUnsignedShort();
	if ((versionHi != 1) || (versionLo != 0))
		throw new IOException("Version mismatch!");
	
	numTables = in.readUnsignedShort();
	searchRange = in.readUnsignedShort();
	entrySelector = in.readUnsignedShort();
	rangeShift = in.readUnsignedShort();
	for (int i = 0; i < numTables; i++)
	{
		TableEntry entry = new TableEntry();
		entry.id[0] = (char) (in.readByte() & 0xff);
		entry.id[1] = (char) (in.readByte() & 0xff);
		entry.id[2] = (char) (in.readByte() & 0xff);
		entry.id[3] = (char) (in.readByte() & 0xff);
		entry.checksum2 = in.readInt();
		entry.offset = in.readInt();
		entry.length = in.readInt();
		tableEntries.add(entry);
	}
	
//	for (TableEntry entry : tableEntries)
//		System.out.println(entry);
	
	readHeader(in);
	readMaxP(in);
	readHorizontalMetrics(in);
	readCMap(in);
	readGlyphs(in);
}

private static HashMap<String,String> systemFonts = initSystemFonts();

private static HashMap<String,String> initSystemFonts()
{
	HashMap<String,String> result = new HashMap<String,String>();
	result.put("SansSerif", "de/ofahrt/fonts/bitstreamvera/Vera.ttf");
	result.put("Monospaced", "de/ofahrt/fonts/bitstreamvera/VeraMono.ttf");
	result.put("Serif", "de/ofahrt/fonts/bitstreamvera/VeraSe.ttf");
	return result;
}

private static TtfData load(String name, byte[] data) throws IOException
{
	Buffer in = new Buffer(data);
	TtfData result = new TtfData(name);
	result.read(in);
	return result;
}

private static TtfData load(String name, InputStream in) throws IOException
{
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	byte[] data = new byte[1024];
	int len = 0;
	while ((len = in.read(data)) > 0)
		out.write(data, 0, len);
	return load(name, out.toByteArray());
}

public static TtfData load(String name) throws IOException
{
	String resname = systemFonts.get(name);
	if (resname != null)
		return load(name, TtfData.class.getClassLoader().getResourceAsStream(resname));
	
	String filename = "Fonts/"+name+".ttf";
	File f = new File(filename);
	byte[] data = new byte[(int) f.length()];
	FileInputStream fin = new FileInputStream(f);
	
	int amount = 0;
	while (amount < data.length)
	{
		int count = fin.read(data, amount, data.length-amount);
		if (count <= 0) throw new IOException("ARGH!");
		amount += count;
	}
	
	return load(name, data);
}

}
