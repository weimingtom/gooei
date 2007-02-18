package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.MouseInteraction;
import gooei.MouseRouterWidget;
import gooei.input.InputEventType;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.input.MouseEvent;
import gooei.utils.TimerEventType;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class SpinBoxWidget extends TextFieldWidget implements MouseRouterWidget
{

private int minimum = Integer.MIN_VALUE;
private int maximum = Integer.MAX_VALUE;
private int step = 1;
private int value = 0;

public SpinBoxWidget(Desktop desktop)
{ super(desktop); }

public int getMinimum()
{ return minimum; }

public void setMinimum(int minimum)
{ this.minimum = minimum; }

public int getMaximum()
{ return maximum; }

public void setMaximum(int maximum)
{ this.maximum = maximum; }

public int getStep()
{ return step; }

public void setStep(int step)
{ this.step = step; }

public int getValue()
{ return value; }

public void setValue(int value)
{ this.value = value; }

@Override
public Dimension getPreferredSize()
{
	Dimension result = getFieldSize();
	result.width += desktop.getBlockSize();
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void doLayout()
{ layoutField(desktop.getBlockSize(), 0); }

@Override
protected boolean isFilter()
{ return true; }

public boolean processSpin(Object part)
{
	String text = getText();
	try
	{
		int itext = Integer.parseInt(text);
		if ((part == "up") ?
				(itext + step <= maximum) :
				(itext - step >= minimum))
		{
			String val = String.valueOf((part == "up") ? (itext + step) : (itext - step));
			setText(val);
			setStart(val.length());
			setEnd(0);
			repaint();
			invokeAction();
			return true;
		}
	}
	catch (NumberFormatException nfe)
	{/*IGNORED EXCEPTION*/}
	return false;
}

@Override
public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	if ((keycode == Keys.UP) || (keycode == Keys.DOWN))
	{
		processSpin((keycode == Keys.UP)? "up" : "down");
		return true;
	}
	return super.handleKeyPress(event);
}

public void findComponent(MouseInteraction mouseInteraction, int x, int y)
{
	Rectangle bounds = getBounds();
	int block = desktop.getBlockSize();
	mouseInteraction.insidepart = (x <= bounds.width - block) ? null :
			((y <= bounds.height / 2) ? "up" : "down");
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if (part == null)
		processField(mouseInteraction, event, 0);
	else
	{ // part = "up" || "down"
		if ((id == InputEventType.MOUSE_ENTERED) ||
				(id == InputEventType.MOUSE_EXITED) ||
				(id == InputEventType.MOUSE_PRESSED) ||
				(id == InputEventType.MOUSE_RELEASED))
		{
			if (id == InputEventType.MOUSE_PRESSED)
			{
				setFocus();
				if (processSpin(part))
					desktop.setTimer(TimerEventType.SPIN, 300L);
				//settext: start end selection, parse exception...
			}
			else
			{
				if (id == InputEventType.MOUSE_RELEASED)
					desktop.setTimer(null, 0L);
			}
			repaint();
		}
	}
}

@Override
public void paint(LwjglRenderer renderer)
{
	doLayout();
	int block = desktop.getBlockSize();
	Rectangle bounds = getBounds();
	boolean pressed = isMousePressed();
	boolean inside = isMouseInside();
	final boolean enabled = isEnabled() && renderer.isEnabled();
	paintField(renderer, bounds.width - block, bounds.height, enabled, 0);
	renderer.paintArrow(bounds.width - block, 0, block, bounds.height / 2,
		'N', enabled, inside, pressed, "up", true, false, false, true, true);
	renderer.paintArrow(bounds.width - block, bounds.height / 2,
		block, bounds.height - (bounds.height / 2),
		'S', enabled, inside, pressed, "down", true, false, true, true, true);
}

}
