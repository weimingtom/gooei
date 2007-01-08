package thinlet.impl;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.lwjgl.LwjglWidgetRenderer;

public final class SpacerWidget extends AbstractWidget
{

public SpacerWidget(ThinletDesktop desktop)
{ super(desktop); }

@Override
public Dimension getPreferredSize()
{
	Dimension result = new Dimension(1, 1);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void paint(LwjglWidgetRenderer renderer)
{
	Rectangle bounds = getBounds();
	final boolean enabled = isEnabled();
	renderer.setColor(enabled ? renderer.c_border : renderer.c_disable);
	renderer.fillRect(0, 0, bounds.width, bounds.height);
}

}