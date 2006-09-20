package thinlet;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.api.FocusableWidget;
import thinlet.help.MethodInvoker;
import thinlet.help.MouseInteraction;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.games.Keys;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.MouseEvent;

public final class ButtonWidget extends LabelWidget implements FocusableWidget
{

	public static enum Type
	{ NORMAL, DEFAULT, CANCEL, LINK; }

private Type type = Type.NORMAL;
private MethodInvoker actionMethod;

public ButtonWidget(ThinletDesktop desktop)
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
	return getSize(link ? 0 : 12, link ? 0 : 6);
}

@Override
public boolean checkMnemonic(Object checked, Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	if (((modifiers == 0) &&
			(((keycode == Keys.RETURN) && (getType() == ButtonWidget.Type.DEFAULT)) ||
			((keycode == Keys.ESCAPE) && (getType() == ButtonWidget.Type.CANCEL)))) ||
				hasMnemonic(keycode, modifiers))
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

@Override
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
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{
	Rectangle bounds = getBounds();
	boolean pressed = isMousePressed();
	boolean inside = isMouseInside();
	boolean link = getType() == ButtonWidget.Type.LINK;
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
