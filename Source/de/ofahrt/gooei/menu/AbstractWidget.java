package de.ofahrt.gooei.menu;

import gooei.ContainerWidget;
import gooei.Desktop;
import gooei.Element;
import gooei.ElementContainer;
import gooei.PopupMenuElement;
import gooei.Renderer;
import gooei.ToolTipOwner;
import gooei.Widget;
import gooei.font.Font;
import gooei.utils.MethodInvoker;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import de.ofahrt.gooei.impl.PopupMenuElementImpl;
import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public abstract class AbstractWidget implements Widget, ToolTipOwner
{

protected final Desktop desktop;

private String name;
private boolean enabled = true;
private boolean visible = true;

private int width = 0;
private int height = 0;

private String tooltip = null;

private Font font = null;

protected ContainerWidget<?> parentWidget;

private Rectangle bounds = new Rectangle();
private Rectangle tooltipbounds;

private PopupMenuElement popupmenuWidget;

private MethodInvoker initMethod;

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

// FIXME: Make this relative to the component
public void repaint(Rectangle area)
{ desktop.repaint(this, area); }

// FIXME: Make this relative to the component
public void repaint(int x, int y, int w, int h)
{ repaint(new Rectangle(x, y, w, h)); }

/**
 * Convenience method to repaint this component.
 * Registers the entire area of this component for repainting
 * by calling {@link Desktop#repaint(Widget, Rectangle)}:
 * <pre><code>
 * desktop.repaint(this, getBounds());
 * </code></pre>
 */
public void repaint()
{ desktop.repaint(this, getBounds()); }

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

}
