package de.ofahrt.gooei.impl;

import gooei.ContainerWidget;
import gooei.Desktop;
import gooei.MnemonicWidget;
import gooei.Widget;
import gooei.input.Keys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public abstract class AbstractContainerWidget<T extends Widget> extends AbstractWidget implements ContainerWidget<T>
{

private final List<T> data = new ArrayList<T>();
private final List<Object> constraints = new ArrayList<Object>();
protected boolean needsLayout = true;

public AbstractContainerWidget(Desktop desktop)
{ super(desktop); }

@Override
public final void setBounds(int x, int y, int width, int height)
{
	super.setBounds(x, y, width, height);
	validate();
}

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
	validate();
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
	validate();
}

/**
 * Returns whether the component needs layouting.
 */
public boolean needsLayout()
{ return needsLayout; }

/**
 * Performs layouting of this component.
 */
public abstract void doLayout();

public void validate()
{
	repaint();
	needsLayout = true;
}

protected void paintChild(Widget child, LwjglRenderer renderer, boolean enabled)
{
	if (!child.isVisible()) return;
	renderer.pushState();
	if (renderer.moveCoordSystem(child.getBounds()))
		child.paint(renderer);
	renderer.popState();
}

public void paintAll(LwjglRenderer renderer, boolean enabled)
{
	for (Widget comp : data)
		paintChild(comp, renderer, enabled);
}

@Override
public abstract void paint(LwjglRenderer renderer);


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

}
