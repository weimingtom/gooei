package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.FocusableWidget;
import gooei.MouseInteraction;
import gooei.MouseableWidget;
import gooei.input.InputEventType;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.input.MouseEvent;
import gooei.utils.MethodInvoker;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class ButtonWidget extends LabelWidget
	implements MouseableWidget, FocusableWidget
{

	public static enum Type
	{ NORMAL, DEFAULT, CANCEL, LINK; }

private Type type = Type.NORMAL;
private MethodInvoker actionMethod;

public ButtonWidget(Desktop desktop)
{ super(desktop); }

public Type getType()
{ return type; }

public void setType(Type type)
{
	if (type == null) throw new NullPointerException();
	this.type = type;
}

public void setAction(MethodInvoker method)
{ actionMethod = method; }

public boolean invokeAction()
{ return invokeIt(actionMethod, null); }

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	boolean link = getType() == Type.LINK;
	Dimension result = desktop.getSize(this, link ? 0 : 12, link ? 0 : 6);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public boolean checkMnemonic(Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	if (((modifiers == 0) &&
			(((keycode == Keys.RETURN) && (getType() == ButtonWidget.Type.DEFAULT)) ||
			((keycode == Keys.ESCAPE) && (getType() == ButtonWidget.Type.CANCEL)))) ||
				isAccelerator(keycode, modifiers, getText(), getMnemonic()))
	{
		invokeAction();
		repaint();
		return true;
	}
	return false;
}

public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	if ((keycode == Keys.SPACE) ||
			((keycode == Keys.RETURN) && (getType() == Type.DEFAULT)) ||
			((keycode == Keys.ESCAPE) && (getType() == Type.CANCEL)))
	{
		//pressedkey = keychar;
		invokeAction();
		repaint();
		return true;
	}
	return false;
}

public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if ((id == InputEventType.MOUSE_ENTERED) ||
				(id == InputEventType.MOUSE_EXITED) ||
				(id == InputEventType.MOUSE_PRESSED) ||
				(id == InputEventType.MOUSE_RELEASED))
	{
		if (id == InputEventType.MOUSE_PRESSED)
			setFocus();
		if (((mouseInteraction.mousepressed == null) || (mouseInteraction.mousepressed == this)) &&
				((id == InputEventType.MOUSE_ENTERED) ||
					(id == InputEventType.MOUSE_EXITED)) &&
				(getType() == ButtonWidget.Type.LINK))
		{
			desktop.setCursor(Cursor.getPredefinedCursor(
				(id == InputEventType.MOUSE_ENTERED) ?
					Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
		}
		else if ((id == InputEventType.MOUSE_RELEASED) &&
				(mouseInteraction.mouseinside == this))
		{
			invokeAction();
		}
		repaint();
	}
}

@Override
public void paint(LwjglRenderer renderer)
{
	Rectangle bounds = getBounds();
	boolean pressed = isMousePressed();
	boolean inside = isMouseInside();
	boolean link = getType() == ButtonWidget.Type.LINK;
	boolean enabled = isEnabled() && renderer.isEnabled();
	if (link)
	{
		char mode = enabled ? (pressed ? 'e' : 'l') : 'd';
		renderer.paintIconAndText(this, 0, 0, bounds.width, bounds.height,
			false, false, false, false, 0, 0, 0, 0, hasFocus(), mode, enabled && (inside != pressed));
	}
	else
	{ // disabled toggled
		char mode = enabled ? ((inside != pressed) ? 'h' : (pressed ? 'p' : 'g')) : 'd';
		renderer.paintIconAndText(this, 0, 0, bounds.width, bounds.height,
			true, true, true, true, 2, 5, 2, 5, hasFocus(), mode, false);
	}
}

}
