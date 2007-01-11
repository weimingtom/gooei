package gooei;

import gooei.input.Keys;

import java.util.Iterator;

/**
 * A {@link Widget} that contains other {@link Widget}s.
 * 
 * @author Ulf Ochsenfahrt
 */
public interface ContainerWidget<T extends Widget> extends MouseRouterWidget, Iterable<T>
{

/**
 * Returns an iterator over all children, even those that are not visible or
 * not enabled.
 */
Iterator<T> iterator();

/**
 * Adds a child {@link Widget} at the given index.
 * If the index is negative, the child is appended.
 */
void addChild(Widget child, int index);

/**
 * Remove the given immediate child.
 * @throws IllegalArgumentException if the given widget is not a child
 */
void removeChild(Widget widget);

// FIXME: Make specification simpler!
/**
 * Returns whether the given immediate child is focusable. The most basic code
 * must do the following:
 * <pre><code>
 * if (!isEnabled() || !isVisible()) return false;
 * if (parent() == null) return false;
 * return parent().isChildFocusable(this);
 * </code></pre>
 * Additional checks are allowed and implementor-dependent. For example, a
 * tabbed pane may check if the given child is on the currently visible tab.
 */
boolean isChildFocusable(Widget child);

/**
 * Check this widget and recursively its children (except checked) for the given mnemonic.
 * @see MnemonicWidget
 */
boolean checkMnemonic(Object checked, Keys keycode, int modifiers);

/**
 * Calling this method informs this widget that its contents need to be
 * re-layouted. For example, the preferred size of one of its children has
 * changed.
 */
void validate();

/**
 * Paint the current component and all its visible subcomponents.
 * 
 * ContainerWidgets must take particular care to paint subcomponents
 * correctly.
 * <ul>
 *   <li>Invisible components must not be painted.</li>
 *   <li>Before painting the subcomponent, the renderer's clip must be updated
 *       as follows:<br/>
 *     <pre><code>
 * renderer.pushState();
 * if (renderer.moveCoordSystem(child.getBounds()))
 *   child.paint(renderer);
 * renderer.popState();
 *     </code></pre>
 *   </li>
 * </ul>
 */
void paint(Renderer renderer);

}
