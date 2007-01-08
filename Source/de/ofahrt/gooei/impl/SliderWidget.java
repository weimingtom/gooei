package de.ofahrt.gooei.impl;

import gooei.FocusableWidget;
import gooei.MouseInteraction;
import gooei.input.InputEventType;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.input.MouseEvent;
import gooei.utils.MethodInvoker;
import gooei.utils.Orientation;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class SliderWidget extends ProgressBarWidget implements FocusableWidget
{

private int unit = 5;
private int block = 25;
private MethodInvoker actionMethod;

public SliderWidget(ThinletDesktop desktop)
{ super(desktop); }

public int getUnit()
{ return unit; }

public void setUnit(int unit)
{ this.unit = unit; }

public int getBlock()
{ return block; }

public void setBlock(int block)
{ this.block = block; }

public void setAction(MethodInvoker method)
{ actionMethod = method; }

public boolean invokeAction()
{ return invokeIt(actionMethod, null); }

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
	Dimension result = new Dimension(horizontal ? 76 : 10, horizontal ? 10 : 76);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	int value = getValue();
	int d = 0;
	if ((keycode == Keys.HOME) || (keycode == Keys.LEFT) ||
			(keycode == Keys.UP) || (keycode == Keys.PRIOR))
	{
		d = getMinimum()-value;
		if ((keycode == Keys.LEFT) || (keycode == Keys.UP))
			d = Math.max(d, -getUnit());
		else if (keycode == Keys.PRIOR)
			d = Math.max(d, -getBlock());
	}
	else if ((keycode == Keys.END) || (keycode == Keys.RIGHT) ||
			(keycode == Keys.DOWN) || (keycode == Keys.NEXT))
	{
		d = getMaximum()-value;
		if ((keycode == Keys.RIGHT) || (keycode == Keys.DOWN))
			d = Math.min(d, getUnit());
		else if (keycode == Keys.NEXT)
			d = Math.min(d, getBlock());
	}
	if (d != 0)
	{
		setValue(value+d);
		repaint();
		invokeAction();
		return true;
	}
	return false;
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if ((id == InputEventType.MOUSE_PRESSED) || (id == InputEventType.MOUSE_DRAGGED))
	{
		if (id == InputEventType.MOUSE_PRESSED)
		{
			mouseInteraction.setReference(this, block / 2, block / 2);
			setFocus();
		}
		int minimum = getMinimum();
		int maximum = getMaximum();
		int value = getValue();
		Rectangle bounds = getBounds();
		boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
		int newvalue = minimum +
			(horizontal ? (event.getX() - mouseInteraction.referencex) : (event.getY() - mouseInteraction.referencey)) *
			(maximum - minimum) /
			((horizontal ? bounds.width : bounds.height) - block); //... +0.5
		newvalue = Math.max(minimum, Math.min(newvalue, maximum));
		if (value != newvalue)
		{ // fixed by Andrew de Torres
			setValue(newvalue);
			invokeAction();
		}
		if ((value != newvalue) || (id == InputEventType.MOUSE_PRESSED))
			repaint();
	}
}

@Override
public void paint(LwjglRenderer renderer)
{
	Rectangle bounds = getBounds();
	if (hasFocus()) renderer.drawFocus(0, 0, bounds.width - 1, bounds.height - 1);
	int minimum = getMinimum();
	int maximum = getMaximum();
	int value = getValue();
	boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
	final boolean enabled = isEnabled();
	int length = (value - minimum) *
		((horizontal ? bounds.width : bounds.height) - block) /
		(maximum - minimum);
	renderer.paintRect(horizontal ? 0 : 3, horizontal ? 3 : 0,
		horizontal ? length : (bounds.width - 6),
		horizontal ? (bounds.height - 6) : length,
		enabled ? renderer.c_border : renderer.c_disable,
		renderer.c_bg, true, true, horizontal, !horizontal, true);
	renderer.paintRect(horizontal ? length : 0, horizontal ? 0 : length,
		horizontal ? block : bounds.width, horizontal ? bounds.height : block,
		enabled ? renderer.c_border : renderer.c_disable,
		enabled ? renderer.c_ctrl : renderer.c_bg, true, true, true, true, true);
	renderer.paintRect(horizontal ? (block + length) : 3,
		horizontal ? 3 : (block + length),
		bounds.width - (horizontal ? (block + length) : 6),
		bounds.height - (horizontal ? 6 : (block + length)),
		enabled ? renderer.c_border : renderer.c_disable,
		renderer.c_bg, horizontal, !horizontal, true, true, true);
}

}
