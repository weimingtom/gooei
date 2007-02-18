package de.ofahrt.gooei.impl;

import gooei.ContainerWidget;
import gooei.Desktop;
import gooei.Element;
import gooei.ElementContainer;
import gooei.FocusableWidget;
import gooei.PopupMenuElement;
import gooei.PopupMenuWidget;
import gooei.Renderer;
import gooei.ScrollableWidget;
import gooei.ToolTipOwner;
import gooei.Widget;
import gooei.font.Font;
import gooei.input.Keys;
import gooei.input.Modifiers;
import gooei.utils.MethodInvoker;
import gooei.utils.ScrollbarSupport;
import gooei.utils.SortOrder;
import gooei.utils.TLColor;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import de.ofahrt.gooei.lwjgl.GLColor;
import de.ofahrt.gooei.lwjgl.LwjglRenderer;
import de.ofahrt.gooei.lwjgl.LwjglScrollbarSupport;

public abstract class AbstractWidget implements Widget, ToolTipOwner, PopupMenuWidget
{

protected final Desktop desktop;

private String name;
private boolean enabled = true;
private boolean visible = true;

private int width = 0;
private int height = 0;

private String tooltip = null;

private Font font = null;
private TLColor foreground = null;
private TLColor background = null;

protected ContainerWidget<?> parentWidget;

private Rectangle bounds = new Rectangle();
private Rectangle port, view;
private Rectangle tooltipbounds;
private Rectangle horizontal, vertical;

private PopupMenuElement popupmenuWidget;

private MethodInvoker initMethod, focusgainedMethod, focuslostMethod;

private ScrollbarSupport scrollbarSupport;

public AbstractWidget(Desktop desktop)
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

/** Repaint area relative to this component. */
public void repaint(Rectangle area)
{ desktop.repaint(this, new Rectangle(bounds.x+area.x, bounds.y+area.y, area.width, area.height)); }

/** Repaint area relative to this component. */
public void repaint(int x, int y, int w, int h)
{ desktop.repaint(this, new Rectangle(bounds.x+x, bounds.y+y, w, h)); }

/**
 * Convenience method to repaint this component.
 * Registers the entire area of this component for repainting
 * by calling {@link Desktop#repaint(Widget, Rectangle)}:
 * <pre><code>
 * desktop.repaint(this, getBounds());
 * </code></pre>
 */
public void repaint()
{ desktop.repaint(this); }

public abstract void paint(LwjglRenderer renderer);

public void paint(Renderer renderer)
{ paint((LwjglRenderer) renderer); }


public ContainerWidget<?> parent()
{ return parentWidget; }

public void setParent(ContainerWidget<?> parentWidget)
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
{ bounds.setBounds(x, y, width, height); }

public String getToolTip()
{ return tooltip; }

public void setToolTip(String tooltip)
{ this.tooltip = tooltip; }

public Rectangle getToolTipBounds()
{ return tooltipbounds; }

public void setToolTipBounds(int x, int y, int width, int height)
{ tooltipbounds = updateRect(tooltipbounds, x, y, width, height); }

public void removeToolTipBounds()
{ tooltipbounds = null; }

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


public void remove()
{ parent().removeChild(this); }


public final boolean isMousePressed()
{ return desktop.getMouseInteraction().mousepressed == this; }

public final boolean isMouseInside()
{
	return (desktop.getMouseInteraction().mouseinside == this) &&
		((desktop.getMouseInteraction().mousepressed == null) ||
			(desktop.getMouseInteraction().mousepressed == this));
}


// MnemonicWidget support
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


// FocusableWidget support
public void setFocusGained(MethodInvoker method)
{ focusgainedMethod = method; }

public void handleFocusGained()
{
	invokeIt(focusgainedMethod, null);
	repaint();
}

public void setFocusLost(MethodInvoker method)
{ focuslostMethod = method; }

public void handleFocusLost()
{
	invokeIt(focuslostMethod, null);
	repaint();
}

/** Convenience method to check if this component has the focus. */
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
{ return desktop.setFocus(this); }


// ScrollableWidget support
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

protected ScrollbarSupport getScrollbarSupport()
{
	if (scrollbarSupport == null)
		scrollbarSupport = new LwjglScrollbarSupport(desktop, (ScrollableWidget) this);
	return scrollbarSupport;
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
	if (vneed) portwidth -= iscroll; // subtract by vertical scrollbar width
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

public final boolean handleScrollEvent(Object part)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	return getScrollbarSupport().handleScrollEvent(part);
}

protected void paintScrollableContent(LwjglRenderer renderer)
{
	if (this instanceof ScrollableWidget)
		throw new UnsupportedOperationException();
	throw new UnsupportedOperationException();
}

/** Paint background, content and scrollbar of a scrollable widget. */
public final void paintScroll(LwjglRenderer renderer, boolean drawfocus)
{
	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	
	final boolean pressed = isMousePressed();
	final boolean inside = isMouseInside();
	final boolean isenabled = isEnabled() && renderer.isEnabled();
	final boolean focus = hasFocus();
	
	// draw scrollbars
	getScrollbarSupport().paintScrollbars(renderer, pressed, inside, isenabled);
	
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
					final int block = desktop.getBlockSize();
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
	renderer.pushState();
	if (renderer.moveCoordSystem(port, view))
		paintScrollableContent(renderer);
	renderer.popState();
	
	// draw focus
	if (focus && drawfocus)
		renderer.drawFocus(port.x, port.y, port.width - 1, port.height - 1);
}

}
