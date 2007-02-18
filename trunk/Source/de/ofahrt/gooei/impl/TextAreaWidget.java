package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.MouseInteraction;
import gooei.MouseRouterWidget;
import gooei.ScrollableWidget;
import gooei.font.Font;
import gooei.font.FontMetrics;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.input.Modifiers;
import gooei.input.MouseEvent;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.GLColor;
import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class TextAreaWidget extends TextFieldWidget
	implements ScrollableWidget, MouseRouterWidget
{

private int rows = 1;
private boolean border = true;
private boolean wrap = false;

public TextAreaWidget(Desktop desktop)
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
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	int block = desktop.getBlockSize();
	int columns = getColumns();
//	int rows = getRows(); // 'e' -> 'm' ?
	Font currentfont = getFont(desktop.getDefaultFont());
	FontMetrics fm = desktop.getFontMetrics(currentfont);
	Dimension result = new Dimension(
		((columns > 0) ? (columns * fm.charWidth('e') + 2) : 76) + 2 + block,
		((rows > 0) ? (rows * fm.getHeight() - fm.getLeading() + 2) : 76) + 2 + block);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

private StringBuffer getChars(int w, int h)
{
	StringBuffer chars = new StringBuffer(getTextChars());
	if (wrap)
	{
		Font currentfont = getFont(desktop.getDefaultFont());
		FontMetrics fm = desktop.getFontMetrics(currentfont);
		int lines = (h - 4 + fm.getLeading()) / fm.getHeight();
		boolean prevletter = false;
		int n = chars.length();
		int linecount = 0;
		for (int i = 0, j = -1, k = 0; k <= n; k++)
		{ // j is the last space index (before k)
			if (((k == n) || (chars.charAt(k) == '\n') || (chars.charAt(k) == ' ')) &&
					(j > i) && (fm.stringWidth(chars, i, k - i) > w))
			{
				chars.setCharAt(j, '\n');
				k--; // draw line to the begin of the current word (+ spaces) if it is out of width
			}
			else if ((k == n) || (chars.charAt(k) == '\n'))
			{ // draw line to the text/line end
				j = k;
				prevletter = false;
			}
			else
			{
				// keep spaces starting the line
				if ((chars.charAt(k) == ' ') && (prevletter || (j > i))) { j = k; }
				prevletter = (chars.charAt(k) != ' ');
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
	StringBuffer chars = null;
	if (wrap)
	{
		Rectangle bounds = getBounds();
		chars = getChars(bounds.width - 4, bounds.height);
		if (chars == null) // need scrollbars
			chars = getChars(bounds.width - block - 4, 0);
	}
	else
		chars = getChars(0, 0);
	
	Font currentfont = getFont(desktop.getDefaultFont());
	FontMetrics fm = desktop.getFontMetrics(currentfont);
	int w = 0, h = 0;
	int caretx = 0; int carety = 0;
	for (int i = 0, j = 0; j <= chars.length(); j++)
	{
		if ((j == chars.length()) || (chars.charAt(j) == '\n'))
		{
			w = Math.max(w, fm.stringWidth(chars, i, j - i));
			if ((end >= i) && (end <= j))
			{
				caretx = fm.stringWidth(chars, i, end - i);
				carety = h;
			}
			h += fm.getHeight();
			i = j + 1;
		}
	}
	layoutScroll(w + 2, h - fm.getLeading() + 2, 0, 0, 0, 0, hasBorder(), 0);
	getScrollbarSupport().scrollToVisible(caretx, carety, 2, fm.getAscent() + fm.getDescent() + 2);
}

@Override
public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	boolean shiftdown = event.isModifierDown(Modifiers.SHIFT);
	boolean controldown = event.isModifierDown(Modifiers.CTRL);
	
	StringBuffer chars = getTextChars();
	int start = getStart();
	int end = getEnd();
	
//	System.out.println(event);
	
	int istart = start;
	int iend = end;
	if ((keycode == Keys.HOME) && !controldown)
	{
		while ((iend > 0) && (chars.charAt(iend - 1) != '\n')) iend--;
		if (!shiftdown) istart = iend;
	}
	else if ((keycode == Keys.END) && !controldown)
	{
		while ((iend < chars.length()) && (chars.charAt(iend) != '\n')) iend++;
		if (!shiftdown) istart = iend;
	}
	else if ((keycode == Keys.UP) || (keycode == Keys.PRIOR) ||
			(keycode == Keys.DOWN) || (keycode == Keys.NEXT))
	{
		Font currentfont = getFont(desktop.getDefaultFont());
		FontMetrics fm = desktop.getFontMetrics(currentfont);
		int fh = fm.getHeight();
		int y = 0; int linestart = 0;
		for (int i = 0; i < iend; i++)
		{
			if ((chars.charAt(i) == '\n') || (chars.charAt(i) == '\t'))
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
		int x = fm.stringWidth(chars, linestart, iend - linestart);
		iend = getCaretLocation(x, y);
		if (!shiftdown) istart = iend;
	}
	else
		return super.handleKeyPress(event);
	return changeField(getText(), null, istart, iend);
}

public void findComponent(MouseInteraction mouseInteraction, int x, int y)
{ getScrollbarSupport().findScroll(mouseInteraction, x, y); }

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	if (!getScrollbarSupport().handleMouseEvent(part, mouseInteraction, event))
		processField(mouseInteraction, event, 0);
}

@Override
public void paint(LwjglRenderer renderer)
{
	doLayout();
	paintScroll(renderer, true);
}

@Override
public void paintScrollableContent(LwjglRenderer renderer)
{
	int viewwidth = getView().width;
	
	// clip is used for rendering acceleration
	final int clipy = renderer.getClipY();
	final int clipheight = renderer.getClipHeight();
	
	final boolean focus = hasFocus();
	final boolean enabled = isEnabled() && renderer.isEnabled();
	
	StringBuffer chars = getTextChars();
	int start = focus ? getStart() : 0;
	int end = focus ? getEnd() : 0;
	int is = Math.min(start, end);
	int ie = Math.max(start, end);
	Font font = renderer.getCurrentFont();
	Font customfont = getFont(desktop.getDefaultFont());
	renderer.setCurrentFont(customfont);
	FontMetrics fm = renderer.getFontMetrics();
	int fontascent = fm.getAscent();
	int fontheight = fm.getHeight();
	int ascent = 1;
	
	GLColor textcolor = enabled ? (GLColor) getForeground(renderer.c_text) : renderer.c_disable;
	for (int i = 0, j = 0; j <= chars.length(); j++)
	{
		if ((j == chars.length()) || (chars.charAt(j) == '\n'))
		{
			// the next lines are bellow paint rectangle
			if (clipy + clipheight <= ascent) break;
			if (clipy < ascent + fontheight) { // this line is not above painting area
				if (focus && (is != ie) && (ie >= i) && (is <= j))
				{
					int xs = (is < i) ? -1 : ((is > j) ? (viewwidth - 1) :
						fm.stringWidth(chars, i, is - i));
					int xe = ((j != -1) && (ie > j)) ? (viewwidth - 1) :
						fm.stringWidth(chars, i, ie - i);
					renderer.setColor(renderer.c_select);
					renderer.fillRect(1 + xs, ascent, xe - xs, fontheight);
				}
				renderer.setColor(textcolor);
				renderer.drawString(chars, i, j - i, 1, ascent + fontascent);
				if (focus && (end >= i) && (end <= j))
				{
					int caret = fm.stringWidth(chars, i, end - i);
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
