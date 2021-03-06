package gooei;

import java.awt.Rectangle;

/**
 * This class contains mouse event routing information as well as some state
 * to support drag operations within a single widget.
 */
public final class MouseInteraction
{

public int referencex, referencey;
public MouseableWidget mouseinside;
public Object insidepart;
public MouseableWidget mousepressed;
public Object pressedpart;

public MouseInteraction()
{/*OK*/}

/**
 * Calculate the given point in a component relative to the thinlet desktop and
 * set as reference value
 * @param component a widget
 * @param x reference point relative to the component left edge 
 * @param y relative to the top edge
 */
public void setReference(Widget component, int x, int y)
{
	referencex = x;
	referencey = y;
	for (; component != null; component = component.parent())
	{
		Rectangle bounds = component.getBounds();
		referencex += bounds.x;
		referencey += bounds.y;
		if (component instanceof ScrollableWidget)
		{
			Rectangle port = ((ScrollableWidget) component).getPort();
			if (port != null)
			{ // content scrolled
				Rectangle view = ((ScrollableWidget) component).getView();
				referencex -= view.x - port.x;
				referencey -= view.y - port.y;
			}
		}
	}
}

}
