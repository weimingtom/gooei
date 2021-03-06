package gooei;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * By convention, all Widgets have a one argument constructor taking a single
 * {@link Desktop} object as parameter. After their creation, Widgets cannot
 * be moved to a different {@link Desktop}.
 * <p>
 * FIXME: Describe sub-interfaces!
 * 
 * @author Ulf Ochsenfahrt
 */
public interface Widget
{

/** Gets this components immediate parent. */
ContainerWidget<?> parent();

/** Sets this components immediate parent. */
void setParent(ContainerWidget<?> parent);


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
 * 
 * @see ScrollableWidget
 */
Rectangle getBounds();

/** Sets this components bounds. */
void setBounds(int x, int y, int width, int height);


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

}
