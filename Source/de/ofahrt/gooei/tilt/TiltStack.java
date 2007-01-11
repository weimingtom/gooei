package de.ofahrt.gooei.tilt;

import gooei.Desktop;
import gooei.MouseInteraction;
import gooei.Widget;
import de.ofahrt.gooei.impl.AbstractContainerWidget;
import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public class TiltStack extends AbstractContainerWidget<Widget>
{

private int active = 0;

public TiltStack(Desktop desktop)
{ super(desktop); }

public void setActive(int active)
{
	this.active = active;
	repaint();
	desktop.checkLocation();
//	desktop.setNextFocusable(this);
}

@Override
public boolean acceptChild(Widget child)
{ return true; }

@Override
public void doLayout()
{
	int width = getBounds().width;
	int height = getBounds().height;
	for (final Widget comp : this)
	{
		if (!comp.isVisible()) continue;
		comp.setBounds(0, 0, width, height);
	}
	needsLayout = false;
}

@Override
public void paint(LwjglRenderer renderer)
{
	if (needsLayout()) doLayout();
	renderer.updateEnabled(isEnabled());
	paintChild(getChild(active), renderer);
}

@Override
public void findComponent(MouseInteraction mouseInteraction, int x, int y)
{
	final Widget comp = getChild(active);
	findComponent(comp, mouseInteraction, x, y);
}

}
