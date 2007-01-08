package thinlet.impl;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.FocusableWidget;
import thinlet.MouseInteraction;
import thinlet.Widget;
import thinlet.help.Orientation;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.Keys;
import de.ofahrt.utils.input.MouseEvent;

public final class SplitPaneWidget extends AbstractContainerWidget<Widget> implements FocusableWidget
{

private int divider = -1;

private Orientation orientation = Orientation.HORIZONTAL;

public SplitPaneWidget(ThinletDesktop desktop)
{ super(desktop); }

public int getDivider()
{ return divider; }

public void setDivider(int divider)
{ this.divider = divider; }

public Orientation getOrientation()
{ return orientation; }

public void setOrientation(Orientation orientation)
{
	 if (orientation == null) throw new NullPointerException();
	 this.orientation = orientation;
}

@Override
public boolean acceptChild(Widget child)
{ return !(child instanceof DialogWidget) && (getChildCount() < 2); }

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
	final Widget comp1 = getChild(0);
	final Widget comp2 = getChild(1);
	Dimension result = ((comp1 == null) || !comp1.isVisible()) ?
		new Dimension() : comp1.getPreferredSize();
	if ((comp2 != null) && comp2.isVisible())
	{
		Dimension d = comp2.getPreferredSize();
		result.width = horizontal ? (result.width + d.width) : Math.max(result.width, d.width);
		result.height = horizontal ? Math.max(result.height, d.height) : (result.height + d.height);
	}
	if (horizontal)
		result.width += 5;
	else
		result.height += 5;
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void doLayout()
{
//	SplitPaneWidget splitpane = (SplitPaneWidget) this;
	Rectangle bounds = getBounds();
	boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
	int div = getDivider();
	int maxdiv = Math.max(0, (horizontal ? bounds.width : bounds.height) - 5);
	
	final Widget comp1 = getChild(0);
	final Widget comp2 = getChild(1);
	final boolean visible1 = (comp1 != null) && comp1.isVisible();
	if (div == -1)
	{
		int d1 = 0;
		if (visible1)
		{
			Dimension d = comp1.getPreferredSize();
			d1 = horizontal ? d.width : d.height;
		}
		div = Math.min(d1, maxdiv);
		setDivider(div);
	}
	else if (div > maxdiv)
		setDivider(div);
	
	if (visible1)
	{
		comp1.setBounds(0, 0, horizontal ? div : bounds.width,
			horizontal ? bounds.height : div);
	}
	
	if ((comp2 != null) && comp2.isVisible())
	{
		comp2.setBounds(horizontal ? (div + 5) : 0,
			horizontal ? 0 : (div + 5),
			horizontal ? (bounds.width - 5 - div) : bounds.width,
			horizontal ? bounds.height : (bounds.height - 5 - div));
	}
	
	needsLayout = false;
}

@Override
public boolean checkMnemonic(Object checked, Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	for (Widget comp : this)
	{
		if (checkMnemonic(comp, checked, keycode, modifiers))
			return true;
	}
	return false;
}

public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	int d = 0;
	if (keycode == Keys.HOME)
		d = -divider;
	else if ((keycode == Keys.LEFT) || (keycode == Keys.UP))
		d = Math.max(-10, -divider);
	else if ((keycode == Keys.END) ||
			(keycode == Keys.RIGHT) || (keycode == Keys.DOWN))
	{
		boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
		Rectangle bounds = getBounds();
		int max = (horizontal ? bounds.width : bounds.height) - 5;				
		d = max - divider;
		if (keycode != Keys.END) d = Math.min(d, 10);
	}
	if (d != 0)
	{
		setDivider(divider+d);
		validate();
		return true;
	}
	return false;
}

@Override
public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
{
	final Widget comp1 = getChild(0);
	final Widget comp2 = getChild(1);
	if (comp1 != null)
	{
		if (!comp1.findComponent(mouseInteraction, x, y))
		{
			if (comp2 != null)
				comp2.findComponent(mouseInteraction, x, y);
		}
	}
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
//	SplitPaneWidget splitpane = this;
	if (id == InputEventType.MOUSE_PRESSED)
	{
		mouseInteraction.setReference(this, 2, 2);
	}
	else if (id == InputEventType.MOUSE_DRAGGED)
	{
//		int divider = splitpane.getDivider();
		boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
		int moveto = horizontal ? (event.getX() - mouseInteraction.referencex) : (event.getY() - mouseInteraction.referencey);
		Rectangle bounds = getBounds();
		moveto = Math.max(0, Math.min(moveto, (horizontal ? bounds.width : bounds.height) - 5));
		if (divider != moveto)
		{
			setDivider(moveto);
			validate();
		}
	}
	else if ((id == InputEventType.MOUSE_ENTERED) && (mouseInteraction.mousepressed == null))
	{
		boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
		desktop.setCursor(Cursor.getPredefinedCursor(horizontal ?
			Cursor.E_RESIZE_CURSOR : Cursor.S_RESIZE_CURSOR));
	}
	else if (((id == InputEventType.MOUSE_EXITED) && (mouseInteraction.mousepressed == null)) ||
			((id == InputEventType.MOUSE_RELEASED) && (mouseInteraction.mouseinside != this)))
	{
		desktop.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}

@Override
public void paint(LwjglWidgetRenderer renderer)
{
	if (needsLayout()) doLayout();
	Rectangle bounds = getBounds();
	boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
//	int divider = getDivider();
	renderer.paintRect(horizontal ? divider : 0, horizontal ? 0 : divider,
		horizontal ? 5 : bounds.width, horizontal ? bounds.height : 5,
		renderer.c_border, renderer.c_bg, false, false, false, false, true);
	if (hasFocus())
	{
		if (horizontal) renderer.drawFocus(divider, 0, 4, bounds.height - 1);
		else renderer.drawFocus(0, divider, bounds.width - 1, 4);
	}
	final boolean enabled = isEnabled();
	renderer.setColor(enabled ? renderer.c_border : renderer.c_disable);
	int xy = horizontal ? bounds.height : bounds.width;
	int xy1 = Math.max(0, xy / 2 - 12);
	int xy2 = Math.min(xy / 2 + 12, xy - 1);
	for (int i = divider + 1; i < divider + 4; i += 2)
	{
		if (horizontal) renderer.drawLine(i, xy1, i, xy2);
		else renderer.drawLine(xy1, i, xy2, i);
	}
	
	paintAll(renderer, enabled);
}

}
