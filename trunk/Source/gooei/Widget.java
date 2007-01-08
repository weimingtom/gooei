package gooei;

import gooei.input.MouseEvent;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * By convention, all Widgets have a one argument constructor taking a single
 * {@link Desktop} object as parameter. After their creation, Widgets cannot
 * be moved to a different {@link Desktop}.
 * 
 * @author Ulf Ochsenfahrt
 */
public interface Widget
{

/** Gets this components immediate parent. */
ContainerWidget parent();

/** Sets this components immediate parent. */
void setParent(ContainerWidget parent);


/**
 * Returns this components name.
 * The name of a component is used solely for identification purposes in the
 * XML representation.
 */
String getName();

/**
 * Returns the visibility of this component.
 * Even if this is true, a component is not visible if one of its parents is
 * not visible.
 */
boolean isVisible();

/**
 * Returns whether this component is enabled.
 * Even if this is true, a component is not enabled if one of its parents is
 * not enabled.
 */
boolean isEnabled();


/**
 * Returns this components bounds relative to the parent component.
 * If the parent component is scrollable, the bounds are relative to the
 * parents scrollable area.
 * <p>
 * May return null if the bounds have never been set.
 * @see ScrollableWidget
 */
Rectangle getBounds();

/** Sets this components bounds. */
void setBounds(int x, int y, int width, int height);


/** Gets this components popup menu. */
PopupMenuElement getPopupMenu();

/** Sets this components popup menu. */
void setPopupMenu(PopupMenuElement popupmenuElement);


/**
 * Returns the preferred size of this widget.
 * When anything changes that influences the preferred size of this widget,
 * the parents {@link ContainerWidget#validate()} method should be called.
 */
Dimension getPreferredSize();

/**
 * Paint the current component.
 * The renderer is prepared such that painting is always relative to the
 * top-left corner of the widget.
 * <p>
 * If you wish to implement a {@link ContainerWidget}, see also
 * {@link ContainerWidget#paint(Renderer)}.
 */
void paint(Renderer renderer);

/**
 * Registers the entire area of this component for repainting.
 * MUST call {@link Desktop#repaint(Widget, Rectangle)}:
 * <pre><code>
 * desktop.repaint(this, getBounds());
 * </code></pre>
 */
void repaint();


/**
 * Finds the component that contains the mouse pointer.
 * The x,y coordinates are the mouse coordinates relative to this components
 * origin. The results of this method are stored in the given
 * {@link MouseInteraction}. An implementation of this method should check if
 * the current component is visible.
 * <p>
 * After the component that contains the mouse is determined, mouse events
 * are directly delivered to that component.
 * 
 * @return true if a component was found at the given coordinates,
 *         false if the component is not visible
 */
boolean findComponent(MouseInteraction mouseInteraction, int x, int y);

/**
 * Handle mouse event.
 * The parameter part contains the part of this component, which is supposed
 * to handle this mouse event. Parts can only be objects that were returned
 * as {@link MouseInteraction#insidepart} by a previous
 * {@link #findComponent(MouseInteraction, int, int)} invocation.
 * The part parameter may differ from {@link MouseInteraction#insidepart}.
 */
void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event);

}
