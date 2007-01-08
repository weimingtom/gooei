package thinlet.awt;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

import thinlet.SimpleClipboard;

public class AWTClipboard implements SimpleClipboard
{

private final Clipboard clipboard;

public AWTClipboard(Clipboard clipboard)
{ this.clipboard = clipboard; }

public void copy(String s) throws IOException
{
	try
	{ clipboard.setContents(new StringSelection(s), null); }
	catch (Exception e)
	{
		if (e instanceof IOException)
			throw (IOException) e;
		throw (IOException) new IOException().initCause(e);
	}
}

public String get() throws IOException
{
	try
	{ return (String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor); }
	catch (Exception e)
	{
		if (e instanceof IOException)
			throw (IOException) e;
		throw (IOException) new IOException().initCause(e);
	}
}

}
