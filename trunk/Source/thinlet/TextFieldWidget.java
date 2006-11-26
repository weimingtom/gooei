package thinlet;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import thinlet.api.Element;
import thinlet.api.FocusableWidget;
import thinlet.help.*;
import thinlet.lwjgl.GLColor;
import thinlet.lwjgl.GLFont;
import thinlet.lwjgl.GLFontMetrics;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.*;

public class TextFieldWidget extends AbstractWidget implements FocusableWidget
{

private int columns = 0;
private int start = 0;
private int end = 0;
private boolean editable = true;

private String text = "";
private char[] textchars;

private Alignment alignment = Alignment.LEFT;

private int offset;
private MethodInvoker actionMethod, insertMethod, removeMethod, caretMethod, performMethod;

public TextFieldWidget(ThinletDesktop desktop)
{ super(desktop); }

public int getColumns()
{ return columns; }

public void setColumns(int columns)
{ this.columns = columns; }

public int getStart()
{ return start; }

public void setStart(int start)
{ this.start = start; }

public int getEnd()
{ return end; }

public void setEnd(int end)
{ this.end = end; }

public boolean isEditable()
{ return editable; }

public void setEditable(boolean editable)
{ this.editable = editable; }

public String getText()
{ return text; }

public void setText(String text)
{ this.text = text; }

public Alignment getAlignment()
{ return alignment; }

public void setAlignment(Alignment alignment)
{
	if (alignment == null) throw new NullPointerException();
	this.alignment = alignment;
}

public void setAction(MethodInvoker method)
{ actionMethod = method; }

public boolean invokeAction(Element part)
{ return invokeIt(actionMethod, part); }

public boolean invokeAction()
{ return invokeAction(null); }


public void setInsert(MethodInvoker method)
{ insertMethod = method; }

public boolean invokeInsert()
{ return invokeIt(insertMethod, null); }

public void setRemove(MethodInvoker method)
{ removeMethod = method; }

public boolean invokeRemove()
{ return invokeIt(removeMethod, null); }

public void setCaret(MethodInvoker method)
{ caretMethod = method; }

public boolean invokeCaret()
{ return invokeIt(caretMethod, null); }

public void setPerform(MethodInvoker method)
{ performMethod = method; }

public boolean invokePerform()
{ return invokeIt(performMethod, null); }


public int getOffset()
{ return offset; }

protected void setTextChars(char[] chars)
{ textchars = chars; }

public char[] getTextChars()
{ return textchars; }

protected boolean isMultiline()
{ return false; }

public boolean isHidden()
{ return false; }

protected boolean isFilter()
{ return false; }

Dimension getFieldSize()
{
	TLFont currentfont = getFont(desktop.getDefaultFont());
	TLFontMetrics fm = desktop.getFontMetrics(currentfont);
	return new Dimension(((columns > 0) ?
		(columns * fm.charWidth('e')) : 76) + 4,
		fm.getAscent() + fm.getDescent() + 4); // fm.stringWidth(text)
}

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	return getFieldSize();
}

void layoutField(int dw, int left)
{
	int width = getBounds().width - left -dw;
	if (start > text.length()) start = text.length();
	if (end > text.length()) end = text.length();
	int off = offset;
	TLFont currentfont = getFont(desktop.getDefaultFont());
	TLFontMetrics fm = desktop.getFontMetrics(currentfont);
	int textwidth = isHidden() ? (fm.charWidth('*') * text.length()) : fm.stringWidth(text);
	int caret = isHidden() ? (fm.charWidth('*') * end) : fm.stringWidth(text.substring(0, end));
	if (textwidth <= width - 4)
	{ // text fits inside the available space
		if (alignment == Alignment.LEFT)
			off = 0; // left alignment
		else
		{
			off = textwidth - width + 4; // right alignment
			if (alignment == Alignment.CENTER) off /= 2; // center alignment
		}
	}
	else
	{ // text is scrollable
		if (off > caret)
			off = caret;
		else if (off < caret - width + 4)
			off = caret - width + 4;
		off = Math.max(0, Math.min(off, textwidth - width + 4)); 
	}
	if (off != offset)
		offset = off;
}

@Override
public void doLayout()
{ layoutField(0, 0); }

int getCaretLocation(int x, int y)
{
	TLFont currentfont = getFont(desktop.getDefaultFont());
	TLFontMetrics fm = desktop.getFontMetrics(currentfont);
	char[] chars = isMultiline() ? getTextChars() : getText().toCharArray(); // update it
	int linestart = 0;
	if (isMultiline())
	{
		int height = fm.getHeight(); // find the line start by y value
		for (int i = 0; (y >= height) && (i < chars.length); i++)
		{
			if ((chars[i] == '\n') || (chars[i] == '\t'))
			{
				linestart = i + 1;
				y -= height;
			}
		}
	}
	for (int i = linestart; i < chars.length; i++)
	{
		if ((chars[i] == '\n') || (chars[i] == '\t')) return i;
		int charwidth = fm.charWidth(isHidden() ? '*' : chars[i]);
		if (x <= (charwidth / 2)) { return i; }
		x -= charwidth;
	}
	return chars.length;
}

/**
 * @param ntext current text
 * @param insert a string to replace the current selection 
 * @param movestart new selection start position
 * @param moveend new caret (selection end) position
 * @param newstart current selection start position
 * @param newend current caret position
 * @return true if selection, caret location, or text content changed
 */
protected boolean changeField(String ntext, String insert, int movestart, int moveend)
{
//	System.out.println("INSERT \""+insert+"\"");
	movestart = Math.max(0, Math.min(movestart, ntext.length()));
	moveend = Math.max(0, Math.min(moveend, ntext.length()));
	if ((insert == null) && (start == movestart) && (end == moveend))
		return false;
	if (insert != null)
	{
		int min = Math.min(movestart, moveend);
		setText(ntext.substring(0, min) + insert + ntext.substring(Math.max(movestart, moveend)));
		movestart = moveend = min + insert.length();
		invokeAction(); // deprecated
	}
	if (start != movestart) setStart(movestart);
	if (end != moveend) setEnd(moveend);
	validate();
	if (insert == null)
		invokeCaret();
	else if (insert.length() > 0)
		invokeInsert();
	else
		invokeRemove();
	return true;
}

protected String filter(String s)
{
	StringBuffer filtered = new StringBuffer(s.length());
	for (int i = 0; i < s.length(); i++)
	{
		char ckey = s.charAt(i);
		if (((ckey > 0x1f) && (ckey < 0x7f)) ||
				((ckey > 0x9f) && (ckey < 0xffff)) ||
				(isMultiline() && (ckey == '\n')))
		{
			filtered.append(ckey);
		}
	}
	return (filtered.length() != s.length()) ? filtered.toString() : s;
}

public boolean handleKeyPress(KeyboardEvent event)
{
//	int keychar = event.getKeyChar();
	Keys keycode = event.getKeyCode();
	boolean shiftdown = event.isModifierDown(Modifiers.SHIFT);
	boolean controldown = event.isModifierDown(Modifiers.CTRL);
	
	int istart = start;
	int iend = end;
	String insert = null;
	if (editable && (event.getKeyChar() != 0) && !event.isModifierDown(Modifiers.ALT))
	{
		insert = String.valueOf(event.getKeyChar());
	}
	else if (editable && (keycode == Keys.RETURN))
	{
		if (isMultiline())
			insert = "\n";
		else
			return invokePerform();
	}
	else if (editable && (keycode == Keys.BACK))
	{
		insert = "";
		if (start == end) istart -= 1;
	}
	else if (keycode == Keys.END)
	{
		iend = text.length();
		if (!shiftdown) istart = iend;
	}
	else if (keycode == Keys.HOME)
	{
		iend = 0;
		if (!shiftdown) { istart = iend; }
	}
	else if (keycode == Keys.LEFT)
	{
		if (controldown)
		{
			for (int i = 0; i < 2; i++)
			{
				while ((iend > 0) && ((i != 0) ==
					Character.isLetterOrDigit(text.charAt(iend - 1)))) { iend--; }	
			}
		}
		else
		{
			iend -= 1;
		}
		if (!shiftdown) istart = iend;
	}
	else if (keycode == Keys.RIGHT)
	{
		if (controldown)
		{
			for (int i = 0; i < 2; i++)
			{
				while ((iend < text.length()) && ((i == 0) ==
					Character.isLetterOrDigit(text.charAt(iend)))) { iend++; }
			}
		}
		else
		{
			iend += 1;
		}
		if (!shiftdown) istart = iend;
	}
	else if (editable && (keycode == Keys.DELETE))
	{
		insert = "";
		if (start == end) { iend += 1; }
	}
	else if (controldown && ((keycode == Keys.A)/* || (keycode == 0xBF)*/))
	{
		istart = 0;
		iend = text.length();
	}
	else if (controldown && (keycode == Keys.END /*0xDC*/))
	{
		istart = iend = text.length();
	}
	else if ((editable && !isHidden() && controldown && (keycode == Keys.X)) ||
			(!isHidden() && controldown && (keycode == Keys.C)))
	{
		if (start != end)
		{
			String clipboard = text.substring(Math.min(start, end), Math.max(start, end));
			try
			{ desktop.getSystemClipboard().copy(clipboard); }
			catch (IOException e)
			{ e.printStackTrace(); }
			if (keycode == Keys.X)
				insert = "";
			else
				return true;
		}
	}
	else if (editable && controldown && (keycode == Keys.V))
	{
		try
		{ insert = desktop.getSystemClipboard().get(); }
		catch (IOException e)
		{ e.printStackTrace(); }
		if (insert != null)
		{ // no text on system clipboard nor internal clipboard text
			insert = filter(insert);
		}
	}
	
	if (isFilter() && (insert != null))
	{ // contributed by Michael Nascimento
		for (int i = insert.length()-1; i >= 0; i--)
		{
			if (!Character.isDigit(insert.charAt(i)))
				return false;
		}
	}
	return changeField(text, insert, istart, iend);
}

protected void processField(MouseInteraction mouseInteraction, MouseEvent event, int left)
{
	InputEventType id = event.getType();
	int x = event.getX(), y = event.getY();
	int clickcount = event.getClickCount();
	if (id == InputEventType.MOUSE_PRESSED)
	{
		//+ middle=alt paste clipboard content
		mouseInteraction.setReference(this, 2 + left, 2);
		int mx = x - mouseInteraction.referencex;
		int my = 0;
		if (!isMultiline())
		{
			mx += getOffset();
		}
		else
		{
			Rectangle port = getPort();
			mx += port.x - 1;
			my = y - mouseInteraction.referencey + port.y - 1;
		}
		int caretstart = getCaretLocation(mx, my);
		if (event.isPopupTrigger())
		{
//			int start = component.getStart();
//			int end = component.getEnd();
			if ((caretstart >= Math.min(start, end)) && // inside selected text
					(caretstart <= Math.max(start, end))) return;
		}
		int caretend = caretstart;
		if (clickcount > 1)
		{
//			String text = component.getText();
			while ((caretstart > 0) && ((clickcount == 2) ?
				Character.isLetterOrDigit(text.charAt(caretstart - 1)) :
					(text.charAt(caretstart - 1) != '\n')))
			{ caretstart--; }
			while ((caretend < text.length()) && ((clickcount == 2) ?
				Character.isLetterOrDigit(text.charAt(caretend)) :
					(text.charAt(caretend) != '\n')))
			{ caretend++; }
		}
		setStart(caretstart);
		setEnd(caretend);
		setFocus();
		validate(); // caret check only
	}
	else if (id == InputEventType.MOUSE_DRAGGED)
	{
		int mx = x - mouseInteraction.referencex;
		int my = 0;
		if (!isMultiline())
		{
			mx += getOffset();
		}
		else
		{
			Rectangle port = getPort();
			mx += port.x - 1;
			my = y - mouseInteraction.referencey + port.y - 1;
		}
		int dragcaret = getCaretLocation(mx, my);
		if (dragcaret != getEnd())
		{
			setEnd(dragcaret);
			validate(); // caret check only
		}
	}
	else if ((id == InputEventType.MOUSE_ENTERED) && (mouseInteraction.mousepressed == null))
	{
		desktop.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}
	else if (((id == InputEventType.MOUSE_EXITED) && (mouseInteraction.mousepressed == null)) ||
		((id == InputEventType.MOUSE_RELEASED) &&
			((mouseInteraction.mouseinside != this) || (mouseInteraction.insidepart != null))))
	{
		desktop.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{ processField(mouseInteraction, event, 0); }

/** Paint a TextFieldWidget or descendant thereof. */
public void paintField(LwjglWidgetRenderer renderer,
		int width, int height, boolean enabled, int left)
{
	boolean focus = hasFocus();
//	boolean hidden = component.isHidden();
//	boolean editable = component.isEditable();
	renderer.paintRect(0, 0, width, height, enabled ? renderer.c_border : renderer.c_disable,
		editable ? (GLColor) getBackground(renderer.c_textbg) : renderer.c_bg,
		true, true, true, true, true);
	renderer.pushState();
	renderer.clip(1 + left, 1, width - left - 2, height - 2);
	
//	String text = component.getText();
//	int offset = component.getOffset();
	GLFont font = renderer.getCurrentFont();
	GLFont myfont = (GLFont) getFont(desktop.getDefaultFont());
	renderer.setCurrentFont(myfont);
	GLFontMetrics fm = renderer.getFontMetrics();
	
	// highlight marked text and draw caret if focused
	if (focus)
	{
//		int start = component.getStart(); 
//		int end = component.getEnd();
		int caret = isHidden() ? (fm.charWidth('*') * end) : fm.stringWidth(text.substring(0, end));
		if (start != end)
		{
			int is = isHidden() ? (fm.charWidth('*') * start) : fm.stringWidth(text.substring(0, start));
			renderer.setColor(renderer.c_select);
			renderer.fillRect(2 + left - offset + Math.min(is, caret), 1,
				Math.abs(caret - is), height - 2);
		}
		
		// draw caret
		renderer.setColor(renderer.c_focus);
		renderer.fillRect(1 + left - offset + caret, 1, 1, height - 2);
	}

	// draw content
	renderer.setColor(enabled ? (GLColor) getForeground(renderer.c_text) : renderer.c_disable);
	int fx = 2 + left - offset;
	int fy = (height + fm.getAscent() - fm.getDescent()) / 2;
	if (isHidden())
	{
		int fh = fm.charWidth('*');
		for (int i = text.length(); i > 0; i--)
		{
			renderer.drawString("*", fx, fy);
			fx += fh;
		}
	}
	else
	{
		renderer.drawString(text, fx, fy);
	}
	
	if (myfont != null) renderer.setCurrentFont(font);
	renderer.popState();
//	renderer.setClip(clipx, clipy, clipwidth, clipheight);
	
	// draw focus
	if (focus)
		renderer.drawFocus(1, 1, width - 3, height - 3);
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{
	Rectangle bounds = getBounds();
	paintField(renderer, bounds.width, bounds.height, enabled, 0);
}

}
