package de.ofahrt.gooei.impl;

import gooei.Desktop;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class SpacerWidget extends AbstractWidget
{

public SpacerWidget(Desktop desktop)
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
public void paint(LwjglRenderer renderer)
{
	Rectangle bounds = getBounds();
	final boolean enabled = isEnabled();
	renderer.setColor(enabled ? renderer.c_border : renderer.c_disable);
	renderer.fillRect(0, 0, bounds.width, bounds.height);
}

}