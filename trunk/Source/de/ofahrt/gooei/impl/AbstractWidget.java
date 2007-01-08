package de.ofahrt.gooei.impl;

import gooei.ContainerWidget;
import gooei.Element;
import gooei.ElementContainer;
import gooei.FocusableWidget;
import gooei.MouseInteraction;
import gooei.PopupMenuElement;
import gooei.Renderer;
import gooei.ScrollableWidget;
import gooei.ToolTipOwner;
import gooei.Widget;
import gooei.font.Font;
import gooei.input.InputEventType;
import gooei.input.Keys;
import gooei.input.Modifiers;
import gooei.input.MouseEvent;
import gooei.utils.MethodInvoker;
import gooei.utils.SortOrder;
import gooei.utils.TLColor;
import gooei.utils.TimerEventType;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import de.ofahrt.gooei.lwjgl.GLColor;
import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public abstract class AbstractWidget implements Widget, ToolTipOwner
{

protected final ThinletDesktop desktop;

private String name;
private boolean enabled = true;
private boolean visible = true;

private int width = 0;
private int height = 0;

private String tooltip = null;

private Font font = null;
private TLColor foreground = null;
private TLColor background = null;

protected ContainerWidget parentWidget;

private Rectangle bounds;
private Rectangle port, view;
private Rectangle tooltipbounds;
private Rectangle horizontal, vertical;

private PopupMenuElement popupmenuWidget;

private MethodInvoker initMethod, focusgainedMethod, focuslostMethod;

public AbstractWidget(ThinletDesktop desktop)
{ this.desktop = desktop; }

public PopupMenuElement getPopupMenu()
{ return popupmenuWidget; }

public void setPopupMenu(PopupMenuElement newpopup)
{
	if (popupmenuWidget != null)
	{
		popupmenuWidget.setParent(null);
		popupmenuWidget = null;
	}
	if (newpopup != null)
	{
		if (newpopup.parent() != null) throw new IllegalArgumentException();
		popupmenuWidget = newpopup;
		popupmenuWidget.setParent(new ElementContainer<PopupMenuElementImpl>()
			{
				public Iterator<PopupMenuElementImpl> iterator()
				{ throw new UnsupportedOperationException(); }
				public void validate()
				{/*OK*/}
				public void addChild(Element child, int index)
				{ throw new UnsupportedOperationException(); }
				public void removeChild(Element element)
				{ throw new UnsupportedOperationException(); }
				public int getElementCount()
				{ throw new UnsupportedOperationException(); }
			});
	}
}

public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	throw new IllegalArgumentException(this.toString());
}

public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
{/*OK*/}

public boolean findComponent(MouseInteraction mouseInteraction, int x, int y)
{
	if (!isVisible() || !isEnabled()) return false;
//	Rectangle bounds = getBounds();
	if ((bounds == null) || !(bounds.contains(x, y))) return false;
	mouseInteraction.mouseinside = this;
	findSubComponent(mouseInteraction, x-bounds.x, y-bounds.y);
	return true;
}

public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{/*OK*/}

public void repaint(Rectangle area)
{ desktop.repaint(this, area); }

public void repaint(int x, int y, int w, int h)
{ repaint(new Rectangle(x, y, w, h)); }

public void repaint()
{ desktop.repaint(this, getBounds()); }

public abstract void paint(LwjglRenderer renderer);

public void paint(Renderer renderer)
{ paint((LwjglRenderer) renderer); }


public ContainerWidget<?> parent()
{ return parentWidget; }

public void setParent(ContainerWidget parentWidget)
{ this.parentWidget = parentWidget; }

public String getName()
{ return name; }

public void setName(String name)
{ this.name = name; }

// get/set preferred width/height of this component
public int getWidth()
{ return width; }

public void setWidth(int width)
{
	this.width = width;
	if (parentWidget != null) parentWidget.validate();
}

public int getHeight()
{ return height; }

public void setHeight(int height)
{
	this.height = height;
	if (parentWidget != null) parentWidget.validate();
}

public boolean isEnabled()
{ return enabled; }

public void setEnabled(boolean enabled)
{ this.enabled = enabled; }

public boolean isVisible()
{ return visible; }

public void setVisible(boolean visible)
{ this.visible = visible; }

public String getToolTip()
{ return tooltip; }

public void setToolTip(String tooltip)
{ this.tooltip = tooltip; }

public Font getFont(Font def)
{ return font != null ? font : def; }

public void setFont(Font font)
{ this.font = font; }

public TLColor getForeground(TLColor def)
{ return foreground != null ? foreground : def; }

public void setForeground(TLColor foreground)
{ this.foreground = foreground; }

public TLColor getBackground(TLColor def)
{ return background != null ? background : def; }

public void setBackground(TLColor background)
{ this.background = background; }

private Rectangle updateRect(Rectangle rect, int x, int y, int w, int h)
{
	if (rect == null) rect = new Rectangle();
	rect.setBounds(x, y, w, h);
	return rect;
}

public Rectangle getBounds()
{ return bounds; }

public void setBounds(int x, int y, int width, int height)
{ bounds = updateRect(bounds, x, y, width, height); }

public Rectangle getPort()
{ return port; }

public void setPort(Rectangle port)
{ this.port = port; }

public void setPort(int x, int y, int width, int height)
{ port = updateRect(port, x, y, width, height); }

public Rectangle getView()
{ return view; }

public void setView(Rectangle view)
{ this.view = view; }

public void setView(int x, int y, int width, int height)
{ view = updateRect(view, x, y, width, height); }

public Rectangle getToolTipBounds()
{ return tooltipbounds; }

public void setToolTipBounds(Rectangle tooltipbounds)
{ this.tooltipbounds = tooltipbounds; }

public void setToolTipBounds(int x, int y, int width, int height)
{ tooltipbounds = updateRect(tooltipbounds, x, y, width, height); }

/** Horizontal scrollbar, or null if none exists. */
public Rectangle getHorizontal()
{ return horizontal; }

public void setHorizontal(Rectangle horizontal)
{ this.horizontal = horizontal; }

public void setHorizontal(int x, int y, int width, int height)
{ horizontal = updateRect(horizontal, x, y, width, height); }

/** Vertical scrollbar, or null if none exists. */
public Rectangle getVertical()
{ return vertical; }

public void setVertical(Rectangle vertical)
{ this.vertical = vertical; }

public void setVertical(int x, int y, int width, int height)
{ vertical = updateRect(vertical, x, y, width, height); }

protected boolean invokeIt(MethodInvoker method, Object part)
{
	if (method != null)
	{
		method.invoke(part);
		return true;
	}
	return false;
}

public void setInit(MethodInvoker method)
{ initMethod = method; }

public boolean invokeInit(Element part)
{ return invokeIt(initMethod, part); }

public void setFocusGained(MethodInvoker method)
{ focusgainedMethod = method; }

public boolean invokeFocusGained()
{ return invokeIt(focusgainedMethod, null); }

public void setFocusLost(MethodInvoker method)
{ focuslostMethod = method; }

public boolean invokeFocusLost()
{ return invokeIt(focuslostMethod, null); }


public void remove()
{ parent().removeChild(this); }


// Support method for mnemonics
public static boolean isAccelerator(Keys keycode, int modifiers, String text, int index)
{
	if (modifiers == Modifiers.ALT)
	{
		if (index != -1)
		{
			return (text != null) && (text.length() > index) &&
				(Character.toUpperCase(text.charAt(index)) == keycode.charRepresentation());
		}
	}
	return false;
}

public final boolean isMousePressed()
{ return desktop.currentMouseInteraction.mousepressed == this; }

public final boolean isMouseInside()
{
	return (desktop.currentMouseInteraction.mouseinside == this) &&
		((desktop.currentMouseInteraction.mousepressed == null) ||
			(desktop.currentMouseInteraction.mousepressed == this));
}


// Support functions for FocusableWidget:

public final boolean hasFocus()
{ return desktop.hasFocus(this); }

/** Convenience method to set the focus to this Widget. */
protected final boolean setFocus()
{
	if (!(this instanceof FocusableWidget)) throw new UnsupportedOperationException();
	return desktop.setFocus(this);
}

/** Requests that both the container and this widget gain the focus, if this widget is focusable. */
public final boolean requestFocus()
{
	if (ThinletDesktop.isFocusable(this))
	{
		desktop.setFocus(this);
		return true;
	}
	return false;
}


// Support functions for ScrollableWidget:

protected final void repaintScrollablePart(Element part)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	Rectangle b = getBounds();
	// repaint the whole line of its subcomponent
	Rectangle p = getPort();
	Rectangle v = getView();
	Rectangle r = part.getBounds();
	if ((r.y + r.height >= v.y) && (r.y <= v.y + p.height))
	{
		Rectangle area = new Rectangle();
		area.x = b.x + p.x;
		area.y = b.y + p.y - v.y + r.y;
		area.width = p.width;
		area.height = r.height;
		repaint(area);
		//? need cut item rectangle above/below viewport
	}
}

protected final void repaintScrollablePart(String part)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	int block = desktop.getBlockSize();
	Rectangle b = getBounds();
	if ("left".equals(part) || "right".equals(part))
	{ // horizontal scrollbar button
		Rectangle area = new Rectangle(getHorizontal());
		area.x = "left".equals(part) ? area.x : (area.x + area.width - block);
		area.width = block;
		area.translate(b.x, b.y);
		repaint(area);
	}
	else if ("up".equals(part) || "down".equals(part))
	{ // vertical scrollbar button
		Rectangle r = getVertical();
		repaint(b.x + r.x, b.y + ((part == "up") ? r.y : (r.y + r.height - block)), r.width, block);
	}
	else if ("text".equals(part) || "horizontal".equals(part) || "vertical".equals(part))
	{
		Rectangle p = getPort(); // textarea or content
		repaint(b.x + p.x, b.y + p.y, p.width, p.height);
		if ("horizontal".equals(part))
		{
			Rectangle r = getHorizontal();
			repaint(b.x + r.x, b.y + r.y, r.width, r.height);
			repaint(b.x + r.x, b.y, r.width, p.y); // paint header too
		}
		else if ("vertical".equals(part))
		{
			Rectangle r = getVertical();
			repaint(b.x + r.x, b.y + r.y, r.width, r.height);
		}
	}
	else
		throw new UnsupportedOperationException();
}

protected final void scrollToVisible(int x, int y, int w, int h)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	Rectangle v = getView();
	Rectangle p = getPort();
	int vx = Math.max(x + w - p.width, Math.min(v.x, x));
	int vy = Math.max(y + h - p.height, Math.min(v.y, y));
	if ((v.x != vx) || (v.y != vy))
	{
		repaint(); // horizontal | vertical
		v.x = vx;
		v.y = vy;
	}
}

/**
 * Set viewport (:port) bounds excluding borders, view position and content
 * size (:view), horizontal (:horizontal), and vertical (:vertical) scrollbar
 * bounds
 *
 * @param contentwidth preferred component width
 * @param contentheight preferred component height
 * @param top top inset (e.g. table header, dialog title, half of panel title)
 * @param left left inset (e.g. dialog border)
 * @param bottom bottom inset (e.g. dialog border)
 * @param right right inset (e.g. dialog border)
 * @param topgap (lower half of panel title)
 * @return true if scrollpane is required, otherwise false
 *
 * list: 0, 0, 0, 0, true, 0
 * table: header, ...
 * dialog: header, 3, 3, 3, true, 0
 * title-border panel: header / 2, 0, 0, 0, true, head
 */
protected final boolean layoutScroll(int contentwidth, int contentheight,
		int top, int left, int bottom, int right, boolean border, int topgap)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	int block = desktop.getBlockSize();
	Rectangle b = getBounds();
	int iborder = border ? 1 : 0;
	int iscroll = block + 1 - iborder;
	int portwidth = b.width - left - right - 2 * iborder; // available horizontal space
	int portheight = b.height - top - topgap - bottom - 2 * iborder; // vertical space
	boolean hneed = contentwidth > portwidth; // horizontal scrollbar required
	boolean vneed = contentheight > portheight - (hneed ? iscroll : 0); // vertical scrollbar needed
	if (vneed) { portwidth -= iscroll; } // subtract by vertical scrollbar width
	hneed = hneed || (vneed && (contentwidth > portwidth));
	if (hneed) portheight -= iscroll; // subtract by horizontal scrollbar height
	
	setPort(left + iborder, top + iborder + topgap, portwidth, portheight);
	if (hneed)
	{ 
		setHorizontal(left, b.height - bottom - block - 1,
			b.width - left - right - (vneed ? block : 0), block + 1);
	}
	else
		setHorizontal(null);
	if (vneed)
	{
		setVertical(b.width - right - block - 1, top,
			block + 1, b.height - top - bottom - (hneed ? block : 0));
	}
	else
		setVertical(null);
	
	contentwidth = Math.max(contentwidth, portwidth);
	contentheight = Math.max(contentheight, portheight);
	int viewx = 0, viewy = 0;
	Rectangle v = getView();
	if (v != null)
	{ // check the previous location
		viewx = Math.max(0, Math.min(v.x, contentwidth - portwidth));
		viewy = Math.max(0, Math.min(v.y, contentheight - portheight));
	}
	setView(viewx, viewy, contentwidth, contentheight);
	return vneed || hneed;
}

/**
 * @param p x or y relative to the scrollbar begin
 * @param size scrollbar width or height
 * @param portsize viewport width or height
 * @param viewp view x or y
 * @param viewsize view width or height
 * @param horiz if true horizontal, vertical otherwise
 */
private void findScroll(MouseInteraction mouseInteraction, int p, int size, int portsize, int viewp, int viewsize, boolean horiz)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	int block = desktop.getBlockSize();
	if (p < block)
		mouseInteraction.insidepart = horiz ? "left" : "up";
	else if (p > size - block)
		mouseInteraction.insidepart = horiz ? "right" : "down";
	else
	{
		int track = size - 2 * block;
		if (track < 10)
		{
			mouseInteraction.insidepart = "corner";
			return;
		} // too small
		int knob = Math.max(track * portsize / viewsize, 10);
		int decrease = viewp * (track - knob) / (viewsize - portsize);
		if (p < block + decrease) mouseInteraction.insidepart = horiz ? "lefttrack" : "uptrack";
		else if (p < block + decrease + knob) mouseInteraction.insidepart = horiz ? "hknob" : "vknob";
		else mouseInteraction.insidepart = horiz ? "righttrack" : "downtrack";
	}
}

/** Determine if the mouse is inside one of the scrollbars. */
protected final boolean findScroll(MouseInteraction mouseInteraction, int x, int y)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
//	Rectangle port = getPort();
	if ((port == null) || port.contains(x, y)) return false;
//	Rectangle view = getView();
//	Rectangle horizontal = getHorizontal();
//	Rectangle vertical = getVertical();
	if ((horizontal != null) && horizontal.contains(x, y))
		findScroll(mouseInteraction, x - horizontal.x, horizontal.width, port.width, view.x, view.width, true);
	else if ((vertical != null) && vertical.contains(x, y))
		findScroll(mouseInteraction, y - vertical.y, vertical.height, port.height, view.y, view.height, false);
	else
		mouseInteraction.insidepart = "corner";
	return true;
}

/** Process scrollbar mouse event. */
protected final boolean processScroll(MouseInteraction mouseInteraction, MouseEvent event, Object part)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
//	final ScrollableWidget component = (ScrollableWidget) this;
	InputEventType id = event.getType();
	int x = event.getX(), y = event.getY();
	int block = desktop.getBlockSize();
	if (("up".equals(part)) || ("down".equals(part)) ||
			("left".equals(part)) || ("right".equals(part)))
	{
		if ((id == InputEventType.MOUSE_ENTERED) ||
				(id == InputEventType.MOUSE_EXITED) ||
				(id == InputEventType.MOUSE_PRESSED) ||
				(id == InputEventType.MOUSE_RELEASED))
		{
			if (id == InputEventType.MOUSE_PRESSED)
			{
				if (processScroll(part))
				{
					desktop.setTimer(TimerEventType.SCROLL, 300L);
					return true;
				}
			}
			else
			{
				if (id == InputEventType.MOUSE_RELEASED)
					desktop.setTimer(null, 0L);
				repaintScrollablePart((String) part);
			}
		}
	}
	else if (("uptrack".equals(part)) || ("downtrack".equals(part)) ||
			("lefttrack".equals(part)) || ("righttrack".equals(part)))
	{
		if (id == InputEventType.MOUSE_PRESSED)
		{
			if (processScroll(part))
			{
				desktop.setTimer(TimerEventType.SCROLL, 300L);
			}
		}
		else if (id == InputEventType.MOUSE_RELEASED)
		{
			desktop.setTimer(null, 0L);
		}
	}
	else if (("vknob".equals(part)) || ("hknob".equals(part)))
	{
		if (id == InputEventType.MOUSE_PRESSED)
		{
//			Rectangle port = getPort();
//			Rectangle view = getView();
			if (part == "hknob")
				mouseInteraction.referencex = x - view.x * (port.width - 2 * block) / view.width;
			else
				mouseInteraction.referencey = y - view.y * (port.height - 2 * block) / view.height;
		}
		else if (id == InputEventType.MOUSE_DRAGGED)
		{
//			Rectangle port = getPort();
//			Rectangle view = getView();
			if (part == "hknob")
			{
				int viewx = (x - mouseInteraction.referencex) * view.width / (port.width - 2 * block);
				viewx = Math.max(0, Math.min(viewx, view.width - port.width));
				if (view.x != viewx)
				{
					view.x = viewx;
					repaintScrollablePart("horizontal");
				}
			}
			else
			{ // (part == "vknob")
				int viewy = (y - mouseInteraction.referencey) * view.height / (port.height - 2 * block);
				viewy = Math.max(0, Math.min(viewy, view.height - port.height));
				if (view.y != viewy)
				{
					view.y = viewy;
					repaintScrollablePart("vertical");
				}
			}
		}
	}
	else if (part == "corner")
	{
		part = "corner"; // compiler bug
	}
	else
	{
		if (id == InputEventType.MOUSE_PRESSED)
		{
//			Rectangle port = getPort();
			if (port != null) mouseInteraction.setReference(this, port.x, port.y);
		}
		return false;
	}
	return true;
}

public final boolean processScroll(Object part)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
//	Rectangle view = getView();
	Rectangle iport = ("left".equals(part) || "up".equals(part)) ? null : getPort();
	int dx = 0;
	int dy = 0;
	if (part == "left") dx = -10;
	else if (part == "lefttrack") dx = -iport.width;
	else if (part == "right") dx = 10;
	else if (part == "righttrack") dx = iport.width;
	else if (part == "up") dy = -10;
	else if (part == "uptrack") dy = -iport.height;
	else if (part == "down") dy = 10;
	else if (part == "downtrack") dy = iport.height;
	if (dx != 0)
	{
		dx = (dx < 0) ? Math.max(-view.x, dx) :
			Math.min(dx, view.width - iport.width - view.x);
	}
	else if (dy != 0)
	{
		dy = (dy < 0) ? Math.max(-view.y, dy) :
			Math.min(dy, view.height - iport.height - view.y);
	}
	else
		return false;
	if ((dx == 0) && (dy == 0)) return false;
	view.x += dx; view.y += dy;
	repaintScrollablePart((dx != 0) ? "horizontal" : "vertical");
	return (((part == "left") || (part == "lefttrack")) && (view.x > 0)) ||
		(((part == "right") || (part == "righttrack")) &&
			(view.x < view.width - iport.width)) ||
		(((part == "up") || (part == "uptrack")) && (view.y > 0)) ||
		(((part == "down") || (part == "downtrack")) &&
			(view.y < view.height - iport.height));
}

protected void paintScrollableContent(LwjglRenderer renderer, boolean isenabled)
{
	throw new UnsupportedOperationException();
}

/** Paint background, content and scrollbar of a scrollable widget. */
public final void paintScroll(LwjglRenderer renderer, boolean drawfocus, boolean isenabled)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	
	final boolean pressed = isMousePressed();
	final boolean inside = isMouseInside();
	final int block = desktop.getBlockSize();
	
	final int clipx = renderer.getClipX(), clipy = renderer.getClipY();
	final int clipwidth = renderer.getClipWidth(), clipheight = renderer.getClipHeight();
	
	final boolean focus = hasFocus();
//	Rectangle port = getPort();
//	Rectangle horizontal = getHorizontal();
//	Rectangle vertical = getVertical();
//	Rectangle view = getView();
	
	// draw horizontal scrollbar
	if (horizontal != null)
	{
		int x = horizontal.x;
		int y = horizontal.y;
		int w = horizontal.width;
		int h = horizontal.height;
		renderer.paintArrow(x, y, block, h,
			'W', isenabled, inside, pressed, "left", true, true, true, false, true);
		renderer.paintArrow(x + w - block, y, block, h,
			'E', isenabled, inside, pressed, "right", true, false, true, true, true);
		
		int track = w - (2 * block);
		if (track < 10)
		{
			renderer.paintRect(x + block, y, track, h,
				isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, true, true, true, true, true);
		}
		else
		{
			int knob = Math.max(track * port.width / view.width, 10);
			int decrease = view.x * (track - knob) / (view.width - port.width);
			renderer.paintRect(x + block, y, decrease, h,
				isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, false, true, true, false, true);
			renderer.paintRect(x + block + decrease, y, knob, h,
				isenabled ? renderer.c_border : renderer.c_disable, isenabled ? renderer.c_ctrl : renderer.c_bg, true, true, true, true, true);
			int n = Math.min(5, (knob - 4) / 3);
			renderer.setColor(isenabled ? renderer.c_border : renderer.c_disable);
			int cx = (x + block + decrease) + (knob + 2 - n * 3) / 2;
			for (int i = 0; i < n; i++ )
			{
				renderer.drawLine(cx + i * 3, y + 3, cx + i * 3, y + h - 5);
			}
			int increase = track - decrease - knob;
			renderer.paintRect(x + block + decrease + knob, y, increase, h,
				isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, false, false, true, true, true);
		}
	}
	
	// draw vertical scrollbar
	if (vertical != null)
	{
		int x = vertical.x;
		int y = vertical.y;
		int w = vertical.width;
		int h = vertical.height;
		renderer.paintArrow(x, y, w, block,
			'N', isenabled, inside, pressed, "up", true, true, false, true, false);
		renderer.paintArrow(x, y + h - block, w, block,
			'S', isenabled, inside, pressed, "down", false, true, true, true, false);
			
		int track = h - (2 * block);
		if (track < 10)
		{
			renderer.paintRect(x, y + block, w, track,
				isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, true, true, true, true, false);
		}
		else
		{
			int knob = Math.max(track * port.height / view.height, 10);
			int decrease = view.y * (track - knob) / (view.height - port.height);
			renderer.paintRect(x, y + block, w, decrease,
				isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, true, false, false, true, false);
			renderer.paintRect(x, y + block + decrease, w, knob,
				isenabled ? renderer.c_border : renderer.c_disable, isenabled ? renderer.c_ctrl : renderer.c_bg, true, true, true, true, false);
			int n = Math.min(5, (knob - 4) / 3);
			renderer.setColor(isenabled ? renderer.c_border : renderer.c_disable);
			int cy = (y + block + decrease) + (knob + 2 - n * 3) / 2;
			for (int i = 0; i < n; i++ )
			{
				renderer.drawLine(x + 3, cy + i * 3, x + w - 5, cy + i * 3);
			}
			int increase = track - decrease - knob;
			renderer.paintRect(x, y + block + decrease + knob, w, increase,
				isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, false, false, true, true, false);
		}
	}
	
	// draw border
	if (!(this instanceof PanelWidget) &&
			(!(this instanceof TextAreaWidget) || ((TextAreaWidget) this).hasBorder()))
	{
		boolean hneed = (horizontal != null);
		boolean vneed = (vertical != null);
		renderer.paintRect(port.x - 1, port.y - 1, port.width + (vneed ? 1 : 2), port.height + (hneed ? 1 : 2),
			isenabled ? renderer.c_border : renderer.c_disable, (GLColor) getBackground(renderer.c_textbg),
			true, true, !hneed, !vneed, true); // TODO not editable textarea background color
	}
	
	// draw header (table only)
	if (this instanceof TableWidget)
	{
		TableHeader header = ((TableWidget) this).getHeaderWidget();
		if (header != null)
		{
			int[] columnwidths = ((TableWidget) this).getWidths();
			int x = 0;
			renderer.pushState();
			renderer.clip(0, 0, port.width + 2, port.y); // not 2 and decrease clip area...
			int i = 0;
			for (final TableColumn column : header)
			{
				boolean lastcolumn = (i == columnwidths.length - 1);
				int w = lastcolumn ? (view.width - x + 2) : columnwidths[i];
				
				renderer.paintIconAndText(column, x - view.x, 0, w, port.y - 1,
					true, true, false, lastcolumn, 1, 1, 0, 0, false,
					isenabled ? 'g' : 'd', false);
				
				SortOrder sort = column.getSort(); // "none", "ascent", "descent"
				if (sort != SortOrder.NONE)
				{
					renderer.paintArrow(x - view.x + w - block, 0, block, port.y,
						(sort == SortOrder.ASCENDING) ? 'S' : 'N');
				}
				x += w;
				i++;
			}
			renderer.popState();
		}
	}
	
	// draw content
	final int x = Math.max(clipx, port.x);
	final int y = Math.max(clipy, port.y);
	final int nw = Math.min(clipx + clipwidth, port.x + port.width)-x;
	final int nh = Math.min(clipy + clipheight, port.y + port.height)-y;
	if ((nw > 0) && (nh > 0))
	{
		int dx = port.x-view.x, dy = port.y-view.y;
		renderer.pushState();
		renderer.clip(x, y, nw, nh);
		renderer.translate(dx, dy);
		final int nx = x-dx, ny = y-dy;
		if (nx != renderer.getClipX()) throw new IllegalStateException(this+" "+nx+" "+renderer.getClipX());
		if (ny != renderer.getClipY()) throw new IllegalStateException(this+" "+ny+" "+renderer.getClipY());
		if (nw != renderer.getClipWidth()) throw new IllegalStateException(this+" "+nw+" "+renderer.getClipWidth());
		if (nh != renderer.getClipHeight()) throw new IllegalStateException(this+" "+nh+" "+renderer.getClipHeight());
		paintScrollableContent(renderer, isenabled);
		renderer.popState();
	}
	
	// draw focus
	if (focus && drawfocus)
		renderer.drawFocus(port.x, port.y, port.width - 1, port.height - 1);
}

}
