package thinlet;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.help.MouseInteraction;
import thinlet.help.TimerEventType;
import thinlet.lwjgl.LwjglWidgetRenderer;

import de.ofahrt.utils.games.Keys;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.MouseEvent;

public final class SpinBoxWidget extends TextFieldWidget
{

private int minimum = Integer.MIN_VALUE;
private int maximum = Integer.MAX_VALUE;
private int step = 1;
private int value = 0;

public SpinBoxWidget(ThinletDesktop desktop)
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
	Dimension size = super.getPreferredSize();
	size.width += desktop.getBlockSize();
	return size;
}

@Override
public void doLayout()
{ layoutField(desktop.getBlockSize(), 0); }

@Override
protected boolean isFilter()
{ return true; }

private void repaintComponent(Object part)
{
	int block = desktop.getBlockSize();
	Rectangle b = getBounds();
	if ("text".equals(part))
	{ // spinbox textfield content
		repaint(b.x, b.y, b.width - block, b.height);
	}
	else
	{ // spinbox increase or decrease button
		repaint(b.x + b.width - block,
			(part == "up") ? b.y : (b.y + b.height - b.height / 2), block, b.height / 2);
	}
}

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
			repaintComponent("text");
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

@Override
public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
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
			repaintComponent(part);
		}
	}
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{
	int block = desktop.getBlockSize();
	Rectangle bounds = getBounds();
	boolean pressed = isMousePressed();
	boolean inside = isMouseInside();
	paintField(renderer, bounds.width - block, bounds.height, enabled, 0);
	renderer.paintArrow(bounds.width - block, 0, block, bounds.height / 2,
		'N', enabled, inside, pressed, "up", true, false, false, true, true);
	renderer.paintArrow(bounds.width - block, bounds.height / 2,
		block, bounds.height - (bounds.height / 2),
		'S', enabled, inside, pressed, "down", true, false, true, true, true);
}

}
