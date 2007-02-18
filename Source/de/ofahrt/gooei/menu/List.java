package de.ofahrt.gooei.menu;

import gooei.Desktop;
import gooei.MouseInteraction;
import gooei.MouseableWidget;
import gooei.ScrollableWidget;
import gooei.Widget;
import gooei.input.MouseEvent;
import gooei.utils.ScrollbarSupport;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.GLColor;
import de.ofahrt.gooei.lwjgl.LwjglRenderer;
import de.ofahrt.gooei.lwjgl.LwjglScrollbarSupport;

public class List extends AbstractContainerWidget<Widget> implements ScrollableWidget, MouseableWidget
{

private Rectangle port = new Rectangle();
private Rectangle view = new Rectangle();
private Rectangle vertical = new Rectangle();
private final ScrollbarSupport scrollbarSupport;

public List(Desktop desktop)
{
	super(desktop);
	scrollbarSupport = new LwjglScrollbarSupport(desktop, this);
}

public Rectangle getPort()
{ return port; }

public Rectangle getView()
{ return view; }

public Rectangle getHorizontal()
{ return null; }

public Rectangle getVertical()
{ return vertical; }

@Override
public boolean acceptChild(Widget child)
{ return true; }

@Override
public void doLayout()
{
	Rectangle bounds = getBounds();
	int vwidth = bounds.width-20, vheight = bounds.height;
	port.setBounds(0, 0, vwidth, vheight);
	vertical.setBounds(bounds.width-20, 0, 20, bounds.height);
	int index = 0;
	int y = 0;
	for (final Widget comp : this)
	{
		index++;
		if (!comp.isVisible()) continue;
		Dimension size = comp.getPreferredSize();
		comp.setBounds(0, y, vwidth, size.height);
		y += size.height;
	}
	if (y < vheight) y = vheight;
	view.setBounds(0, 0, vwidth, y);
	needsLayout = false;
}

@Override
protected boolean findScroll(MouseInteraction mouseInteraction, int x, int y)
{ return scrollbarSupport.findScroll(mouseInteraction, x, y); }

public void paintBackground(LwjglRenderer renderer)
{
	Rectangle bounds = getBounds();
	renderer.setColor(GLColor.RED);
	renderer.fillRect(0, 0, bounds.width, bounds.height);
}

@Override
public void paint(LwjglRenderer renderer)
{
	if (needsLayout()) doLayout();
	paintBackground(renderer);
	
	final boolean pressed = isMousePressed();
	final boolean inside = isMouseInside();
	final boolean isenabled = isEnabled() && renderer.isEnabled();
	
	// paint scroll bar
	scrollbarSupport.paintScrollbars(renderer, pressed, inside, isenabled);
	
	// paint clipped content
	renderer.pushState();
	if (renderer.moveCoordSystem(port, view))
		paintAll(renderer);
	renderer.popState();
}

public boolean handleScrollEvent(Object part)
{ return scrollbarSupport.handleScrollEvent(part); }

public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{ scrollbarSupport.handleMouseEvent(part, mouseInteraction, event); }

}
