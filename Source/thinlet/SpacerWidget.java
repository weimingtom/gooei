package thinlet;

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
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	return new Dimension(1, 1);
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{
	Rectangle bounds = getBounds();
	renderer.setColor(enabled ? renderer.c_border : renderer.c_disable);
	renderer.fillRect(0, 0, bounds.width, bounds.height);
}

}