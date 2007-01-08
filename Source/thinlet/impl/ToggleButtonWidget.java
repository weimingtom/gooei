package thinlet.impl;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.lwjgl.LwjglWidgetRenderer;

public final class ToggleButtonWidget extends CheckBoxWidget
{

public ToggleButtonWidget(ThinletDesktop desktop)
{ super(desktop); }

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	Dimension result = desktop.getSize(this, 12, 6);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void paint(LwjglWidgetRenderer renderer)
{
	Rectangle bounds = getBounds();
	boolean pressed = isMousePressed();
	boolean inside = isMouseInside();
	boolean toggled = isSelected();
	final boolean enabled = isEnabled();
	// disabled toggled
	char mode = enabled ? ((inside != pressed) ? 'h' : ((pressed || toggled) ? 'p' : 'g')) : 'd';
	renderer.paintIconAndText(this, 0, 0, bounds.width, bounds.height,
		true, true, true, true,
		2, 5, 2, 5, hasFocus(), mode, false);
}

}