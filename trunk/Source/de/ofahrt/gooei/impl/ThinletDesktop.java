package de.ofahrt.gooei.impl;

import gooei.ContainerWidget;
import gooei.Desktop;
import gooei.FocusableWidget;
import gooei.IconAndText;
import gooei.MnemonicWidget;
import gooei.MouseInteraction;
import gooei.PopupMenuElement;
import gooei.PopupOwner;
import gooei.PopupWidget;
import gooei.ScrollableWidget;
import gooei.SimpleClipboard;
import gooei.ToolTipOwner;
import gooei.UIController;
import gooei.Widget;
import gooei.font.Font;
import gooei.font.FontMetrics;
import gooei.input.InputEventType;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.input.Modifiers;
import gooei.input.MouseEvent;
import gooei.input.MouseMotionEvent;
import gooei.input.MouseWheelEvent;
import gooei.utils.Icon;
import gooei.utils.TLColor;
import gooei.utils.TimerEventType;
import gooei.xml.SimpleXMLParser;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public abstract class ThinletDesktop implements Desktop
{

private final DesktopPaneWidget pane = new DesktopPaneWidget(this);

public void insertItem(Widget child, int index)
{ pane.insertItem(child, index); }

public void addChild(Widget child, int index)
{ pane.addChild(child, index); }

public void removeChild(Widget child)
{ pane.removeChild(child); }

public Widget findWidget(String name)
{ return findWidget(pane, name); }

public Rectangle getBounds()
{ return pane.getBounds(); }

public void setBackground(TLColor background)
{ pane.setBackground(background); }

public void updateFocusForRemove(Widget child)
{
	for (Widget comp = focusowner; comp != null; comp = comp.parent())
	{
		if (comp == child)
		{
			setNextFocusable(pane);
			break;
		}
	}
}

public boolean hasFocus(Widget w)
{ return focusinside && (w == focusowner); }

public final void paint(final LwjglRenderer renderer)
{
	if (pane.isVisible())
	{
		if (pane.needsLayout())
			pane.doLayout();
		renderer.pushState();
		pane.paint(renderer);
		renderer.popState();
	}
	if (tooltipowner != null)
	{
		Rectangle r = tooltipowner.getToolTipBounds();
		renderer.paintRect(r.x, r.y, r.width, r.height,
			renderer.c_border, renderer.c_bg, true, true, true, true, true);
		String text = tooltipowner.getToolTip();
		renderer.setColor(renderer.c_text);
		renderer.drawString(text, r.x + 2, r.y + renderer.getFontMetrics().getAscent() + 2);
	}
}



// ThinletDesktop API

public abstract void setTimer(TimerEventType type, long delay);
public abstract Font getDefaultFont();
public abstract FontMetrics getFontMetrics(Font font);
public abstract Font createFont(String name, int style, int size);
public abstract TLColor createColor(int red, int green, int blue);
public abstract TLColor createColor(int red, int green, int blue, int alpha);
public abstract int getBlockSize();
public abstract void show();
public abstract SimpleClipboard getSystemClipboard();
public abstract void requestFocus();
public abstract void transferFocus();
public abstract void transferFocusBackward();
public abstract boolean isDesktopEnabled();
public abstract Dimension getSize();
public abstract void setCursor(Cursor cursor);
public abstract void repaintDesktop(int tx, int ty, int width, int height);
public abstract Icon loadIcon(String path);

public void repaintDesktop(Rectangle rect)
{ repaintDesktop(rect.x, rect.y, rect.width, rect.height); }

public void repaint(Widget widget, final Rectangle area)
{
	if (area == null) return;
	Rectangle rect = new Rectangle(area);
	Widget current;
	while ((current = widget.parent()) != null)
	{
		if (!widget.isVisible()) return;
		Rectangle b = current.getBounds();
		rect.translate(b.x, b.y);
		
		if (current instanceof ScrollableWidget)
		{
			Rectangle v = ((ScrollableWidget) current).getView();
			if (v != null)
			{
				Rectangle p = ((ScrollableWidget) current).getPort();
				rect.translate(-v.x+p.x, -v.y+p.y);
			}
		}
		
		if (current instanceof TabbedPaneWidget)
		{
			if (((TabbedPaneWidget) current).getSelectedItem() != widget)
				return;
		}
		
		widget = current;
	}
	repaintDesktop(rect);
}

public Dimension getSize(IconAndText widget, int dx, int dy)
{
	int tw = 0, th = 0;
	String text = widget.getText();
	if (text != null)
	{
		Font customfont = widget.getFont(getDefaultFont());
		FontMetrics fm = getFontMetrics(customfont);
		tw = fm.stringWidth(text);
		th = fm.getAscent() + fm.getDescent();
	}
	int iw = 0, ih = 0;
	Icon icon = widget.getIcon();
	if (icon != null)
	{
		iw = icon.getWidth();
		ih = icon.getHeight();
		if (text != null) iw += 2;
	}
	return new Dimension(tw + iw + dx, Math.max(th, ih) + dy);
}


public Widget parse(UIController controller, InputStream in, ResourceBundle bundle) throws IOException
{ return new SimpleXMLParser(this, controller, bundle).parse(in); }

public Widget parse(UIController controller, String path, ResourceBundle bundle) throws IOException
{
	InputStream inputstream = null;
	try
	{
		inputstream = getClass().getClassLoader().getResourceAsStream(path);
		if (inputstream == null)
			inputstream = new URL(path).openStream();
	}
	catch (Throwable e)
	{/*IGNORED EXCEPTION*/}
	if (inputstream == null) throw new NullPointerException();
	return parse(controller, inputstream, bundle);
}

public Widget parse(UIController controller, String path) throws IOException
{ return parse(controller, path, null); }

public void parseAndAdd(UIController controller, String path) throws IOException
{ pane.addChild(parse(controller, path, null), 0); }



// UI management

private final MouseInteraction currentMouseInteraction = new MouseInteraction();

private long mousetime;
private int mousemods, mousex, mousey;
private FocusableWidget focusowner;
private boolean focusinside;
private PopupOwner popupowner;
private ToolTipOwner tooltipowner;

public MouseInteraction getMouseInteraction()
{ return currentMouseInteraction; }

/**
 * Request focus for the given component
 * @param component a focusable component
 * @return true if the focusowner was changed, otherwise false
 */
public boolean setFocus(Widget component)
{
	if (!(component instanceof FocusableWidget)) throw new IllegalArgumentException();
	if (!focusinside)
	{ // request focus for the thinlet component
		requestFocus();
	}
	if (focusowner != component)
	{
		FocusableWidget focused = focusowner;
		if (focusowner != null)
		{
			focusowner = null; // clear focusowner
			focused.repaint();
			// invoke the focus listener of the previously focused component
			focused.invokeFocusLost();
		}
		if (focusowner == null)
		{ // it won't be null, if refocused
			focusowner = (FocusableWidget) component;
			// invoke the focus listener of the new focused component
			focusowner.invokeFocusGained();
			focusowner.repaint();
		}
		return true;
	}
	return false;
}

public void setPopupOwner(PopupOwner widget)
{ popupowner = widget; }

public void setPopup(Widget widget, PopupOwner owner)
{
	insertItem(widget, 0);
	setPopupOwner(owner);
}

public void checkLocation()
{
	onMouse(new MouseMotionEvent(mousetime, mousemods, mousex, mousey));
}

public void closePopup()
{
	if (popupowner != null)
	{
		popupowner.closePopup();
		popupowner = null;
	}
}

public void showTip()
{
	tooltipowner = null;
	
	if (currentMouseInteraction.mouseinside instanceof ToolTipOwner)
	{
		ToolTipOwner temp = (ToolTipOwner) currentMouseInteraction.mouseinside;
		if (currentMouseInteraction.insidepart instanceof ToolTipOwner)
			temp = (ToolTipOwner) currentMouseInteraction.insidepart;
		String text = temp.getToolTip();
		
		if (text != null)
		{
			FontMetrics fm = getFontMetrics(getDefaultFont());
			int width = fm.stringWidth(text) + 4;
			int height = fm.getAscent() + fm.getDescent() + 4;
			if (tooltipowner == null) tooltipowner = temp;
			Rectangle bounds = pane.getBounds();
			int tx = Math.max(0, Math.min(mousex + 10, bounds.width - width));
			int ty = Math.max(0, Math.min(mousey + 10, bounds.height - height));
			tooltipowner.setToolTipBounds(tx, ty, width, height);
			repaintDesktop(tx, ty, width, height);
		}
	}
}

private void hideTip()
{
	if (tooltipowner != null)
	{
		Rectangle bounds = tooltipowner.getToolTipBounds();
		tooltipowner.removeToolTipBounds();
		tooltipowner = null;
		repaintDesktop(bounds.x, bounds.y, bounds.width, bounds.height);
	}
}

private boolean findMnemonic(Widget component, Object checked, Keys keycode, int modifiers)
{
	if (component == null) return false;
	if (!component.isVisible() || !component.isEnabled())
		return false;
	
	if (component instanceof ContainerWidget)
	{
		if (((ContainerWidget) component).checkMnemonic(checked, keycode, modifiers))
			return true;
	}
	
	if (component instanceof MnemonicWidget)
	{
		if (((MnemonicWidget) component).checkMnemonic(keycode, modifiers))
			return true;
	}
	
	// check parent
	if (!(component instanceof DialogWidget) || !((DialogWidget) component).isModal())
	{
		if (findMnemonic(component.parent(), component instanceof TabWidget ? checked : component, keycode, modifiers))
			return true;
	}
	return false;
}

/** Search the widget hierarchy for a mnemonic, starting at component, first going down, then up. */
private boolean findMnemonic(Widget component, Keys keycode, int modifiers)
{ return findMnemonic(component, null, keycode, modifiers); }

private void handleMouseEvent(Widget component, Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if (id == InputEventType.MOUSE_ENTERED)
		setTimer(TimerEventType.TIP, 750L);
	else if (id == InputEventType.MOUSE_EXITED)
		hideTip();
	
	if (component == null) return;
	if (!component.isEnabled()) return;
	
	component.handleMouseEvent(part, mouseInteraction, event);
	
	if (event.isPopupTrigger())
	{
		PopupMenuElement popupmenu = component.getPopupMenu();
		if (popupmenu != null)
		{
			PopupWidget popup = popupmenu.popupPopup(event.getX(), event.getY());
			mouseInteraction.mouseinside = popup;
			mouseInteraction.mousepressed = popup;
			mouseInteraction.insidepart = null;
			mouseInteraction.pressedpart = null;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}

public void onFocusLost()
{
	focusinside = false;
	if (focusowner != null)
		focusowner.repaint();
	closePopup();
}

public void onFocusGained()
{
	focusinside = true;
	if (focusowner != null)
		focusowner.repaint();
//	else
//		setFocus(null);
}

public void onResize()
{
	Dimension d = getSize();
	pane.setBounds(0, 0, d.width, d.height);
	closePopup();
	if (!focusinside) requestFocus();
}

/** Returns whether this widget can become focusowner. */
public static boolean isFocusable(Widget widget)
{
	if (widget instanceof FocusableWidget)
	{
		if (!widget.isEnabled() || !widget.isVisible()) return false;
		if (widget.parent() == null) return false;
		return widget.parent().isChildFocusable(widget);
	}
	return false;
}

private Widget findFirstFocusable(Widget block, ContainerWidget<?> container)
{
	if (container == null) return null;
	for (Widget w : container)
	{
		if (block != null)
		{
			if (w == block) block = null;
			continue;
		}
		
		if (isFocusable(w)) return w;
		if (w instanceof ContainerWidget)
		{
			Widget temp = findFirstFocusable(null, (ContainerWidget<?>) w);
			if (temp != null) return temp;
		}
	}
	return null;
}

private Widget findLastFocusable(Widget block, ContainerWidget<?> container)
{
	if (container == null) return null;
	Widget result = null;
	for (Widget w : container)
	{
		if (w == block) break;
		if (isFocusable(w)) result = w;
		if (w instanceof ContainerWidget)
		{
			Widget temp = findLastFocusable(null, (ContainerWidget<?>) w);
			if (temp != null) result = w;
		}
	}
	return result;
}

/** Finds the next focusable component widget and focuses it. */
public boolean setNextFocusable(Widget widget, boolean outgo)
{
	boolean consumed = false;
	Widget result = findFirstFocusable(widget, widget.parent());
	
	Widget previous = widget;
	ContainerWidget<?> container = widget.parent();
	while ((result == null) && (container != null))
	{
		result = findFirstFocusable(previous, container);
		previous = container;
		container = container.parent();
	}
	if (result != null)
	{
		setFocus(result);
		consumed = true;
	}
	return consumed;
}

public boolean setNextFocusable(Widget widget)
{ return setNextFocusable(widget, false); }

/** Finds the previous focusable component and focuses it. */
public final boolean setPreviousFocusable(Widget widget, boolean outgo)
{
	boolean consumed = false;
	Widget result = findLastFocusable(widget, widget.parent());
	
	Widget previous = widget;
	ContainerWidget<?> container = widget.parent();
	while ((result == null) && (container != null))
	{
		result = findLastFocusable(previous, container);
		previous = container;
		container = container.parent();
	}
	if (result != null)
	{
		setFocus(result);
		consumed = true;
	}
	return consumed;
}

public final boolean setPreviousFocusable(Widget widget)
{ return setPreviousFocusable(widget, false); }

public boolean onKey(KeyboardEvent event, boolean actionKey)
{
	InputEventType id = event.getType();
	Keys keycode = event.getKeyCode();
	boolean shiftDown = event.isModifierDown(Modifiers.SHIFT);
	boolean controlDown = event.isModifierDown(Modifiers.CTRL);
	boolean altDown = event.isModifierDown(Modifiers.ALT);
	
/*	boolean control = (keychar <= 0x1f) ||
			((keychar >= 0x7f) && (keychar <= 0x9f)) ||
			(keychar >= 0xffff) || controlDown;*/
	
	if (focusinside && ((popupowner != null) || (focusowner != null)))
	{
		hideTip(); // remove tooltip
		
		if ((popupowner != null) || (focusowner != null))
		{
			if (popupowner != null)
			{
				if (popupowner.handleKeyPress(event))
					return true;
			}
			else
			{
				if (focusowner.handleKeyPress(event))
					return true;
			}
		}
		
		if ((keycode == Keys.TAB) || ((keycode == Keys.F6) && (altDown || controlDown)))
		{
			boolean consumed = false;
			boolean outgo = (keycode == Keys.F6);
			if (!shiftDown ? setNextFocusable(focusowner, outgo) : setPreviousFocusable(focusowner, outgo))
			{
				consumed = true;
			}
			else
			{ // 1.4
				if (!shiftDown)
					transferFocus();
				else
					transferFocusBackward();
			}
			focusowner.repaint();
			closePopup();
			return consumed;
		}
		
		if (keycode == Keys.F8)
		{
			for (Widget splitpane = focusowner; splitpane != null; splitpane = splitpane.parent())
			{
				if (splitpane instanceof SplitPaneWidget)
				{
					setFocus(splitpane);
					splitpane.repaint();
					return true;
				}
			}
		}
		
		if ((id == InputEventType.KEY_DOWN) && ((event.getKeyChar() != 0) || actionKey) &&
				findMnemonic(focusowner, keycode, event.getModifiers()))
		{
			return true;
		}
	}
	
	return false;
}

public void onMouse(MouseEvent event)
{
	mousetime = event.getTime();
	mousemods = event.getModifiers();
	mousex = event.getX();
	mousey = event.getY();
	
	InputEventType id = event.getType();
	int x = event.getX(), y = event.getY();
	if (id == InputEventType.MOUSE_WHEEL)
	{
		int rotation = ((MouseWheelEvent) event).getWheelRotation();
		if (currentMouseInteraction.mouseinside instanceof ScrollableWidget)
		{ // is scrollable
			ScrollableWidget scrollable = (ScrollableWidget) currentMouseInteraction.mouseinside;
			Rectangle port = scrollable.getPort();
			if (port != null)
			{
				hideTip();
				Rectangle bounds = currentMouseInteraction.mouseinside.getBounds();	
				if (port.x + port.width < bounds.width)
				{ // has vertical scrollbar
					// TODO scroll panels too
					scrollable.processScroll(rotation > 0 ? "down" : "up");
				}
				else if (port.y + port.height < bounds.height)
				{ // has horizontal scrollbar
					scrollable.processScroll(rotation > 0 ? "right" : "left");
				}
			}
		}
	}
	else if (id == InputEventType.MOUSE_ENTERED)
	{
		if (currentMouseInteraction.mousepressed == null)
		{
			pane.findComponent(currentMouseInteraction, x, y);
			handleMouseEvent(currentMouseInteraction.mouseinside, currentMouseInteraction.insidepart, currentMouseInteraction, event);
		}
	}
	else if (id == InputEventType.MOUSE_MOVED)
	{
		Widget previnside = currentMouseInteraction.mouseinside;
		Object prevpart = currentMouseInteraction.insidepart;
		pane.findComponent(currentMouseInteraction, x, y);
		if ((previnside == currentMouseInteraction.mouseinside) && (prevpart == currentMouseInteraction.insidepart))
		{
			handleMouseEvent(currentMouseInteraction.mouseinside, currentMouseInteraction.insidepart, currentMouseInteraction, event);
		}
		else
		{
			// synthetic events
			handleMouseEvent(previnside, prevpart, currentMouseInteraction,
				new MouseEvent(InputEventType.MOUSE_EXITED, event.getTime(), event.getModifiers(), x, y));
			handleMouseEvent(currentMouseInteraction.mouseinside, currentMouseInteraction.insidepart, currentMouseInteraction,
				new MouseEvent(InputEventType.MOUSE_ENTERED, event.getTime(), event.getModifiers(), x, y));
		}
	}
	else if (id == InputEventType.MOUSE_EXITED)
	{
		if (currentMouseInteraction.mousepressed == null)
		{
			Widget mouseexit = currentMouseInteraction.mouseinside;
			Object exitpart = currentMouseInteraction.insidepart;
			currentMouseInteraction.mouseinside = null;
			currentMouseInteraction.insidepart = null;
			handleMouseEvent(mouseexit, exitpart, currentMouseInteraction, event);
		}
	}
	else if (id == InputEventType.MOUSE_PRESSED)
	{
		if (popupowner != null)
		{ // remove popup
			if ((popupowner != currentMouseInteraction.mouseinside) &&
					!(currentMouseInteraction.mouseinside instanceof PopupWidgetImpl) && !(currentMouseInteraction.mouseinside instanceof ComboListWidget))
			{
				closePopup();
			}
		}
		hideTip(); // remove tooltip
		currentMouseInteraction.mousepressed = currentMouseInteraction.mouseinside;
		currentMouseInteraction.pressedpart = currentMouseInteraction.insidepart;
		handleMouseEvent(currentMouseInteraction.mousepressed, currentMouseInteraction.pressedpart, currentMouseInteraction, event);
	}
	else if (id == InputEventType.MOUSE_DRAGGED)
	{
		hideTip(); // remove tooltip
		Widget previnside = currentMouseInteraction.mouseinside;
		Object prevpart = currentMouseInteraction.insidepart;
		pane.findComponent(currentMouseInteraction, x, y);
		boolean same = (previnside == currentMouseInteraction.mouseinside) && (prevpart == currentMouseInteraction.insidepart);
		boolean isin = (currentMouseInteraction.mousepressed == currentMouseInteraction.mouseinside) && (currentMouseInteraction.pressedpart == currentMouseInteraction.insidepart);
		boolean wasin = (currentMouseInteraction.mousepressed == previnside) && (currentMouseInteraction.pressedpart == prevpart);
		
		if (wasin && !isin)
		{
			handleMouseEvent(currentMouseInteraction.mousepressed, currentMouseInteraction.pressedpart, currentMouseInteraction,
				new MouseEvent(InputEventType.MOUSE_EXITED, event.getTime(), event.getModifiers(), x, y));
		}
		else if (!same && (popupowner != null) && !wasin)
		{
			handleMouseEvent(previnside, prevpart, currentMouseInteraction,
				new MouseEvent(InputEventType.DRAG_EXITED, event.getTime(), event.getModifiers(), x, y));
		}
		if (isin && !wasin)
		{
			handleMouseEvent(currentMouseInteraction.mousepressed, currentMouseInteraction.pressedpart, currentMouseInteraction,
				new MouseEvent(InputEventType.MOUSE_ENTERED, event.getTime(), event.getModifiers(), x, y));
		}
		else if (!same && (popupowner != null) && !isin)
		{
			handleMouseEvent(currentMouseInteraction.mouseinside, currentMouseInteraction.insidepart, currentMouseInteraction,
				new MouseEvent(InputEventType.DRAG_ENTERED, event.getTime(), event.getModifiers(), x, y));
		}
		if (isin == wasin)
		{
			handleMouseEvent(currentMouseInteraction.mousepressed, currentMouseInteraction.pressedpart, currentMouseInteraction, event);
		}
	}
	else if (id == InputEventType.MOUSE_RELEASED)
	{
		hideTip(); // remove tooltip
		Widget mouserelease = currentMouseInteraction.mousepressed;
		Object releasepart = currentMouseInteraction.pressedpart;
		currentMouseInteraction.mousepressed = null;
		currentMouseInteraction.pressedpart = null;
		handleMouseEvent(mouserelease, releasepart, currentMouseInteraction, event);
		if ((currentMouseInteraction.mouseinside != null) &&
				((mouserelease != currentMouseInteraction.mouseinside) || (releasepart != currentMouseInteraction.insidepart)))
		{
			handleMouseEvent(currentMouseInteraction.mouseinside, currentMouseInteraction.insidepart, currentMouseInteraction,
				new MouseEvent(InputEventType.MOUSE_ENTERED, event.getTime(), event.getModifiers(), x, y));
		}
	}
}

public void onTimer(TimerEventType timerType)
{
	if (timerType == TimerEventType.SCROLL)
	{
		ScrollableWidget scrollable = (ScrollableWidget) currentMouseInteraction.mousepressed;
		if (scrollable.processScroll(currentMouseInteraction.pressedpart))
			setTimer(TimerEventType.SCROLL, 60L);
	}
	else if (timerType == TimerEventType.SPIN)
	{
		if (((SpinBoxWidget) currentMouseInteraction.mousepressed).processSpin(currentMouseInteraction.pressedpart))
			setTimer(TimerEventType.SPIN, 75L);
	}
	else if (timerType == TimerEventType.TIP)
		showTip();
}

public void moveToFront(Widget child)
{ pane.moveToFront(child); }


public static Widget findWidget(Widget current, String name)
{
	if (name.equals(current.getName())) return current;
	Widget found;
	
	// otherwise search in its subcomponents
	if (current instanceof ContainerWidget<?>)
	{
		for (Widget comp : (ContainerWidget<?>) current)
		{
			found = findWidget(comp, name);
			if (found != null)
				return found;
		}
	}
	
	// search in table header
/*	if (this instanceof TableWidget)
	{
		TableHeader header = ((TableWidget) this).getHeaderWidget();
		if ((header != null) && ((found = header.findWidget(fname)) != null)) return found;
	}*/
	
	// search in component's popupmenu
//	PopupMenuWidget popupmenu = getPopupMenuWidget();
//	if ((popupmenu != null) && ((found = popupmenu.findWidget(fname)) != null)) return found;
	return null;
}

}
