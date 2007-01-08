package gooei;

import java.awt.Rectangle;

/**
 * A scrollable widget is a widget that contains a rectangular area through 
 * which an underlying rectangular area is visible. It usually also contains
 * scrollbars with which the underlying rectangular area can be moved around.
 * 
 * This API must be implemented for all Widgets that have a see-through
 * area through which Widgets are visible (otherwise those Widgets will not
 * work correctly).
 * 
 * @author Ulf Ochsenfahrt
 */
public interface ScrollableWidget extends Widget
{

/**
 * Returns the rectangle of the ScrollableWidget that is see-through.
 * The coordinates are relative to the widget origin, i.e. to the top-left 
 * corner.
 */
Rectangle getPort();

/**
 * Returns the rectangle on the underlying rectangular area that is visible
 * through the port. The width and height must be identical to those of the
 * port. The x, y coordinates are relative to the origin of the
 * underlying rectangular area.
 */
Rectangle getView();

/**
 * Part is one of "up", "down", "left", "right", or any object associated with
 * a scroll timer.
 */
boolean processScroll(Object part);

}
