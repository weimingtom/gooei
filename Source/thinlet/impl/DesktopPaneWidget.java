package thinlet.impl;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import thinlet.ContainerWidget;
import thinlet.MnemonicWidget;
import thinlet.ModalWidget;
import thinlet.MouseInteraction;
import thinlet.Widget;
import thinlet.lwjgl.GLColor;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.Keys;
import de.ofahrt.utils.input.MouseEvent;

public final class DesktopPaneWidget extends AbstractWidget implements ContainerWidget<Widget>
{

private final List<Widget> data = new ArrayList<Widget>();
private boolean needsLayout = true;

public DesktopPaneWidget(ThinletDesktop desktop)
{ super(desktop); }

public Iterator<Widget> iterator()
{ return data.iterator(); }

@Override
public void setBounds(int x, int y, int width, int height)
{
	super.setBounds(x, y, width, height);
	validate();
}

/** Inserts item in child chain at specified index. Appends if index is negative. */
protected final void insertItem(Widget child, int index)
{
	if (child.parent() != null)
		throw new IllegalArgumentException();
	if ((index >= 0) && (index < data.size()))
		data.add(index, child);
	else
		data.add(child);
	child.setParent(this);
}

public final void addChild(Widget child, int index)
{
	insertItem(child, index);
	validate();
}

public void removeChild(Widget child)
{
	if (!data.remove(child))
		throw new IllegalArgumentException();
	child.setParent(null);
	desktop.updateFocusForRemove(child);
}

public boolean isChildFocusable(Widget child)
{
	if (!isEnabled() || !isVisible()) return false;
	return true;
}

public void moveToFront(DialogWidget child)
{
	if (!(data.contains(child))) throw new IllegalArgumentException();
	if (data.get(0) != child)
	{ // to front
		removeChild(child);
		insertItem(child, 0);
		child.setParent(this);
		child.repaint(); // to front always...
		desktop.setNextFocusable(child);
	}
}

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	Dimension result = new Dimension();
	for (final Widget comp : this)
	{
		if (!(comp instanceof DialogWidget) && !(comp instanceof ModalWidget))
		{
			Dimension d = comp.getPreferredSize();
			result.width = Math.max(d.width, result.width);
			result.height = Math.max(d.height, result.height);
		}
	}
	
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

public boolean needsLayout()
{ return needsLayout; }

public void doLayout()
{
	Rectangle bounds = getBounds();
	for (final Widget comp : this)
	{
		if (comp instanceof DialogWidget)
		{
			Dimension d = comp.getPreferredSize();
			if (comp.getBounds() == null)
				comp.setBounds(Math.max(0, (bounds.width - d.width) / 2),
					Math.max(0, (bounds.height - d.height) / 2),
					Math.min(d.width, bounds.width),
					Math.min(d.height, bounds.height));
		}
		else if (!(comp instanceof ModalWidget))
			comp.setBounds(0, 0, bounds.width, bounds.height);
	}
	
	needsLayout = false;
}

public void validate()
{
	repaint();
	needsLayout = true;
}

protected boolean checkMnemonic(Widget child, Object checked, Keys keycode, int modifiers)
{
	if (child == checked) return false;
	if (!child.isVisible() || !child.isEnabled()) return false;
	if (child instanceof ContainerWidget)
	{
		if (((ContainerWidget) child).checkMnemonic(checked, keycode, modifiers))
			return true;
	}
	
	if (child instanceof MnemonicWidget)
	{
		if (((MnemonicWidget) child).checkMnemonic(keycode, modifiers))
			return true;
	}
	return false;
}

public boolean checkMnemonic(Object checked, Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	for (final Widget comp : this)
	{
		if (checkMnemonic(comp, checked, keycode, modifiers))
			return true;
	}
	return false;
}

@Override
public boolean findComponent(MouseInteraction mouseInteraction, int x, int y)
{
	mouseInteraction.mouseinside = null;
	mouseInteraction.insidepart = null;
	
	if (!isVisible()) return false;
	Rectangle bounds = getBounds();
	if ((bounds == null) || !(bounds.contains(x, y))) return false;
	mouseInteraction.mouseinside = this;
	x -= bounds.x;
	y -= bounds.y;
	
	for (final Widget comp : this)
	{
		if (comp.findComponent(mouseInteraction, x, y)) break;
		if (((comp instanceof ModalWidget) && ((ModalWidget) comp).isModal()) ||
					((comp instanceof DialogWidget) && ((DialogWidget) comp).isModal()))
		{
			mouseInteraction.insidepart = "modal";
			break;
		}
	}
	return true;
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if (part == "modal")
	{
		if (id == InputEventType.MOUSE_ENTERED)
			desktop.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else if (id == InputEventType.MOUSE_EXITED)
			desktop.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}

protected void paintChild(Widget child, LwjglWidgetRenderer renderer)
{
	if (!child.isVisible()) return;
	renderer.pushState();
	if (renderer.moveCoordSystem(child.getBounds()))
		child.paint(renderer);
	renderer.popState();
}

/** Paint all siblings in reverse order. */
private void paintReverse(final LwjglWidgetRenderer renderer)
{
	ListIterator<Widget> it = data.listIterator(data.size());
	while (it.hasPrevious())
	{
		Widget widget = it.previous();
		paintChild(widget, renderer);
	}
}

@Override
public void paint(LwjglWidgetRenderer renderer)
{
	if (needsLayout()) doLayout();
	Rectangle bounds = getBounds();
	GLColor background = (GLColor) getBackground(renderer.c_bg);
	renderer.paintRect(0, 0, bounds.width, bounds.height,
		renderer.c_border, background, false, false, false, false, true);
	paintReverse(renderer);
}

}
