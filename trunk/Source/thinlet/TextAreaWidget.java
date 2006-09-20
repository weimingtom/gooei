package thinlet;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.api.ScrollableWidget;
import thinlet.help.MouseInteraction;
import thinlet.help.TLFont;
import thinlet.help.TLFontMetrics;
import thinlet.lwjgl.GLColor;
import thinlet.lwjgl.GLFont;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.games.Keys;
import de.ofahrt.utils.games.Modifiers;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.MouseEvent;

public final class TextAreaWidget extends TextFieldWidget implements ScrollableWidget
{

private int rows = 1;
private boolean border = true;
private boolean wrap = false;

public TextAreaWidget(ThinletDesktop desktop)
{ super(desktop); }

public int getRows()
{ return rows; }

public void setRows(int rows)
{ this.rows = rows; }

public boolean hasBorder()
{ return border; }

public void setBorder(boolean border)
{ this.border = border; }

public boolean isWrap()
{ return wrap; }

public void setWrap(boolean wrap)
{ this.wrap = wrap; }

@Override
protected boolean isMultiline()
{ return true; }

@Override
public Dimension getPreferredSize()
{
	int block = desktop.getBlockSize();
	int columns = getColumns();
//	int rows = getRows(); // 'e' -> 'm' ?
	TLFont currentfont = getFont(desktop.getDefaultFont());
	TLFontMetrics fm = desktop.getFontMetrics(currentfont);
	return new Dimension(
		((columns > 0) ? (columns * fm.charWidth('e') + 2) : 76) + 2 + block,
		((rows > 0) ? (rows * fm.getHeight() - fm.getLeading() + 2) : 76) + 2 + block);
}

private char[] getChars(String text, int w, int h)
{
	char[] chars = getTextChars();
	if ((chars == null) || (chars.length != text.length()))
	{
		chars = text.toCharArray();
		setTextChars(chars);
	}
	else
		text.getChars(0, chars.length, chars, 0);
	
	if (wrap)
	{
		TLFont currentfont = getFont(desktop.getDefaultFont());
		TLFontMetrics fm = desktop.getFontMetrics(currentfont);
		int lines = (h - 4 + fm.getLeading()) / fm.getHeight();
		boolean prevletter = false; int n = chars.length; int linecount = 0;
		for (int i = 0, j = -1, k = 0; k <= n; k++)
		{ // j is the last space index (before k)
			if (((k == n) || (chars[k] == '\n') || (chars[k] == ' ')) &&
					(j > i) && (fm.charsWidth(chars, i, k - i) > w))
			{
				chars[j] = '\n';
				k--; // draw line to the begin of the current word (+ spaces) if it is out of width
			}
			else if ((k == n) || (chars[k] == '\n'))
			{ // draw line to the text/line end
				j = k; prevletter = false;
			}
			else
			{
				// keep spaces starting the line
				if ((chars[k] == ' ') && (prevletter || (j > i))) { j = k; }
				prevletter = (chars[k] != ' ');
				continue;
			}
			linecount++;
			if ((lines != 0) && (linecount == lines)) return null;
			i = j + 1;
		}
	}
	return chars;
}

@Override
public void doLayout()
{
	int block = desktop.getBlockSize();
	String text = getText();
	int start = getStart();
	if (start > text.length()) setStart(start = text.length());
	int end = getEnd();
	if (end > text.length()) setEnd(end = text.length());
	
//	boolean wrap = ((TextAreaWidget) this).isWrap();
	char[] chars = null;
	if (wrap)
	{
		Rectangle bounds = getBounds();
		chars = getChars(text, bounds.width - 4, bounds.height);
		if (chars == null) // need scrollbars
			chars = getChars(text, bounds.width - block - 4, 0);
	}
	else
		chars = getChars(text, 0, 0);
	
	TLFont currentfont = getFont(desktop.getDefaultFont());
	TLFontMetrics fm = desktop.getFontMetrics(currentfont);
	int w = 0, h = 0;
	int caretx = 0; int carety = 0;
	for (int i = 0, j = 0; j <= chars.length; j++)
	{
		if ((j == chars.length) || (chars[j] == '\n'))
		{
			w = Math.max(w, fm.charsWidth(chars, i, j - i));
			if ((end >= i) && (end <= j))
			{
				caretx = fm.charsWidth(chars, i, end - i);
				carety = h;
			}
			h += fm.getHeight();
			i = j + 1;
		}
	}
	layoutScroll(w + 2, h - fm.getLeading() + 2, 0, 0, 0, 0, hasBorder(), 0);
	scrollToVisible(caretx, carety, 2, fm.getAscent() + fm.getDescent() + 2); //?
}

@Override
public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	boolean shiftdown = event.isModifierDown(Modifiers.SHIFT);
	boolean controldown = event.isModifierDown(Modifiers.CTRL);
	
	char[] chars = getTextChars();
	int start = getStart();
	int end = getEnd();
	
//	System.out.println(event);
	
	int istart = start;
	int iend = end;
	if ((keycode == Keys.HOME) && !controldown)
	{
		while ((iend > 0) && (chars[iend - 1] != '\n')) iend--;
		if (!shiftdown) istart = iend;
	}
	else if ((keycode == Keys.END) && !controldown)
	{
		while ((iend < chars.length) && (chars[iend] != '\n')) iend++;
		if (!shiftdown) istart = iend;
	}
	else if ((keycode == Keys.UP) || (keycode == Keys.PRIOR) ||
			(keycode == Keys.DOWN) || (keycode == Keys.NEXT))
	{
		TLFont currentfont = getFont(desktop.getDefaultFont());
		TLFontMetrics fm = desktop.getFontMetrics(currentfont);
		int fh = fm.getHeight();
		int y = 0; int linestart = 0;
		for (int i = 0; i < iend; i++)
		{
			if ((chars[i] == '\n') || (chars[i] == '\t'))
			{
				linestart = i + 1;
				y += fh;
			}
		}
		if (keycode == Keys.UP) y -= fh;
		else if (keycode == Keys.DOWN) y += fh;
		else
		{
			int dy = getPort().height;
			y += (keycode == Keys.PRIOR) ? -dy : dy; // VK_PAGE_DOWN
		}
		int x = fm.charsWidth(chars, linestart, iend - linestart);
		iend = getCaretLocation(x, y);
		if (!shiftdown) istart = iend;
	}
	else
		return super.handleKeyPress(event);
	return changeField(getText(), null, istart, iend);
}

@Override
public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
{ findScroll(mouseInteraction, x, y); }

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	if (!processScroll(mouseInteraction, event, part))
		processField(mouseInteraction, event, 0);
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{ paintScroll(renderer, true, enabled); }

public void paintScrollableContent(LwjglWidgetRenderer renderer, boolean enabled)
{
	int viewwidth = getView().width;
	
	// clip is used for rendering acceleration
	final int clipy = renderer.getClipY();
	final int clipheight = renderer.getClipHeight();
	
	final boolean focus = hasFocus();
	
	char[] chars = getTextChars();
	int start = focus ? getStart() : 0;
	int end = focus ? getEnd() : 0;
	int is = Math.min(start, end);
	int ie = Math.max(start, end);
	GLFont font = renderer.font;
	GLFont customfont = (GLFont) getFont(desktop.getDefaultFont());
	renderer.setCurrentFont(customfont);
	TLFontMetrics fm = renderer.getFontMetrics();
	int fontascent = fm.getAscent();
	int fontheight = fm.getHeight();
	int ascent = 1;
	
	GLColor textcolor = enabled ? (GLColor) getForeground(renderer.c_text) : renderer.c_disable;
	for (int i = 0, j = 0; j <= chars.length; j++)
	{
		if ((j == chars.length) || (chars[j] == '\n'))
		{
			// the next lines are bellow paint rectangle
			if (clipy + clipheight <= ascent) break;
			if (clipy < ascent + fontheight) { // this line is not above painting area
				if (focus && (is != ie) && (ie >= i) && (is <= j))
				{
					int xs = (is < i) ? -1 : ((is > j) ? (viewwidth - 1) :
						fm.charsWidth(chars, i, is - i));
					int xe = ((j != -1) && (ie > j)) ? (viewwidth - 1) :
						fm.charsWidth(chars, i, ie - i);
					renderer.setColor(renderer.c_select);
					renderer.fillRect(1 + xs, ascent, xe - xs, fontheight);
				}
				renderer.setColor(textcolor);
				renderer.drawChars(chars, i, j - i, 1, ascent + fontascent);
				if (focus && (end >= i) && (end <= j))
				{
					int caret = fm.charsWidth(chars, i, end - i);
					renderer.setColor(renderer.c_focus);
					renderer.fillRect(caret, ascent, 1, fontheight);
				}
			}
			ascent += fontheight;
			i = j + 1;
		}
	}
	// restore the default font
	if (customfont != null) renderer.setCurrentFont(font);
}

}
