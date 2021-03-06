package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.utils.Orientation;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public class ProgressBarWidget extends AbstractWidget
{

private int minimum = 0;
private int maximum = 100;
private int value = 0;

private Orientation orientation = Orientation.HORIZONTAL;

public ProgressBarWidget(Desktop desktop)
{ super(desktop); }

public int getMinimum()
{ return minimum; }

public void setMinimum(int minimum)
{ this.minimum = minimum; }

public int getMaximum()
{ return maximum; }

public void setMaximum(int maximum)
{ this.maximum = maximum; }

public int getValue()
{ return value; }

public void setValue(int value)
{ this.value = value; }

public Orientation getOrientation()
{ return orientation; }

public void setOrientation(Orientation orientation)
{
	 if (orientation == null) throw new NullPointerException();
	 this.orientation = orientation;
}

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
	Dimension result = new Dimension(horizontal ? 76 : 6, horizontal ? 6 : 76);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void paint(LwjglRenderer renderer)
{
	Rectangle bounds = getBounds();
//	int minimum = progressbar.getMinimum();
//	int maximum = progressbar.getMaximum();
//	int value = progressbar.getValue();
	// fixed by by Mike Hartshorn and Timothy Stack
	final boolean horizontal = getOrientation() == Orientation.HORIZONTAL;
	final boolean enabled = isEnabled() && renderer.isEnabled();
	int length = (value - minimum) *
		((horizontal ? bounds.width : bounds.height) - 1) / (maximum - minimum);
	renderer.paintRect(0, 0, horizontal ? length : bounds.width,
		horizontal ? bounds.height : length, enabled ? renderer.c_border : renderer.c_disable,
		renderer.c_select, true, true, horizontal, !horizontal, true);
	renderer.paintRect(horizontal ? length : 0, horizontal ? 0 : length,
		horizontal ? (bounds.width - length) : bounds.width	,
		horizontal ? bounds.height : (bounds.height - length),
		enabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, true, true, true, true, true);
}

}
