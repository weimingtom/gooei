package thinlet;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import thinlet.api.ContainerWidget;
import thinlet.api.ModalWidget;
import thinlet.api.Widget;
import thinlet.help.MouseInteraction;
import thinlet.lwjgl.GLColor;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.Keys;
import de.ofahrt.utils.input.MouseEvent;

public class DesktopPaneWidget extends AbstractWidget implements ContainerWidget<Widget>
{

private final List<Widget> data = new ArrayList<Widget>();

public DesktopPaneWidget(ThinletDesktop desktop)
{ super(desktop); }

public Iterator<Widget> iterator()
{ return data.iterator(); }

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
	child.update("validate");
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
	
	Dimension size = new Dimension();
	for (final Widget comp : this)
	{
		if (!(comp instanceof DialogWidget) && !(comp instanceof ModalWidget))
		{
			Dimension d = comp.getPreferredSize();
			size.width = Math.max(d.width, size.width);
			size.height = Math.max(d.height, size.height);
		}
	}
	return size;
}

@Override
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
		comp.doLayout();
	}
}

@Override
public boolean checkMnemonic(Object checked, Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	for (final Widget comp : this)
	{
		if ((comp != checked) && comp.checkMnemonic(null, keycode, modifiers))
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

/** Paint all siblings in reverse order. */
private void paintReverse(final LwjglWidgetRenderer renderer, final boolean enabled)
{
	ListIterator<Widget> it = data.listIterator(data.size());
	while (it.hasPrevious())
	{
		Widget widget = it.previous();
		widget.render(renderer, enabled);
	}
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{
	Rectangle bounds = getBounds();
	GLColor background = (GLColor) getBackground(renderer.c_bg);
	renderer.paintRect(0, 0, bounds.width, bounds.height,
		renderer.c_border, background, false, false, false, false, true);
	paintReverse(renderer, enabled);
}

}
