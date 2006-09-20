package thinlet.help;

import java.awt.Rectangle;

import thinlet.api.ScrollableWidget;
import thinlet.api.Widget;

public class MouseInteraction
{

public int referencex, referencey;
public Widget mouseinside;
public Object insidepart;
public Widget mousepressed;
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
