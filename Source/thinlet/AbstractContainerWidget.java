package thinlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import thinlet.api.ContainerWidget;
import thinlet.api.Widget;
import thinlet.lwjgl.LwjglWidgetRenderer;

public abstract class AbstractContainerWidget<T extends Widget> extends AbstractWidget implements ContainerWidget<T>
{

private final List<T> data = new ArrayList<T>();
private final List<Object> constraints = new ArrayList<Object>();

public AbstractContainerWidget(ThinletDesktop desktop)
{ super(desktop); }

protected int getChildCount()
{ return data.size(); }

public T getChild(int index)
{ return data.get(index); }

public abstract boolean acceptChild(Widget child);

protected Object getConstraint(int index)
{ return constraints.get(index); }

protected Object getConstraintFor(T child)
{
	int index = data.indexOf(child);
	return constraints.get(index);
}

public boolean isChildFocusable(Widget child)
{
	if (!isEnabled() || !isVisible()) return false;
	if (parent() == null) return false;
	return parent().isChildFocusable(this);
}

public Iterator<T> iterator()
{ return data.iterator(); }

protected Object createConstraints(T child)
{ return null; }

/** Inserts item in child chain at specified index. Appends if index is negative. */
@SuppressWarnings("unchecked")
protected final void insertItem(Widget child, int index)
{
	if (child.parent() != null)
		throw new IllegalArgumentException();
	Object o = createConstraints((T) child);
	if ((index >= 0) && (index < data.size()))
	{
		data.add(index, (T) child);
		constraints.add(index, o);
	}
	else
	{
		data.add((T) child);
		constraints.add(o);
	}
	child.setParent(this);
}

public final void addChild(Widget child, int index)
{
	if (!acceptChild(child))
		throw new IllegalArgumentException("Cannot add "+child+" to "+this+"!");
	insertItem(child, index);
	child.update("validate");
}

public void removeChild(Widget child)
{
	int i = data.indexOf(child);
	if (i < 0)
		throw new IllegalArgumentException();
	data.remove(i);
	constraints.remove(i);
	child.setParent(null);
	desktop.updateFocusForRemove(child);
}

public void paintAll(LwjglWidgetRenderer renderer, boolean enabled)
{
	for (Widget comp : data)
		comp.render(renderer, enabled);
}

}
