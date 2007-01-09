package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.Element;
import gooei.ElementContainer;
import gooei.MenuContainerElement;
import gooei.MenuElement;
import gooei.input.Keys;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class SimpleMenuContainer extends AbstractMenuElement implements ElementContainer<MenuElement>, MenuContainerElement<MenuElement>
{

private int mnemonic = -1;
private List<MenuElement> data = new ArrayList<MenuElement>();

public SimpleMenuContainer()
{/*OK*/}

public int getMnemonic()
{ return mnemonic; }

public void setMnemonic(int mnemonic)
{ this.mnemonic = mnemonic; }


public MenuElement getChild(int index)
{ return data.get(index); }

public int getElementCount()
{ return data.size(); }

public Iterator<MenuElement> iterator()
{ return data.iterator(); }

public MenuElement findElement(String name)
{ throw new UnsupportedOperationException(); }

protected void insertItem(MenuElement child, int index)
{
	if ((index >= 0) && (index < data.size()))
		data.add(index, child);
	else
		data.add(child);
	child.setParent(this);
}

public void addChild(Element child, int index)
{
	if (!(child instanceof MenuElement))
		throw new IllegalArgumentException("Cannot add "+child+" to "+this+"!");
	if (child.parent() != null)
		throw new IllegalArgumentException();
	insertItem((MenuElement) child, index);
	child.setParent(this);
	validate();
}

public boolean checkMnemonic(Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	for (final MenuElement comp : this)
	{
		if (comp.checkMnemonic(keycode, modifiers))
			return true;
	}
	return false;
}

public void removeChild(Element child)
{
	if (!data.remove(child))
		throw new IllegalArgumentException();
	child.setParent(null);
}

@Override
public Dimension getSize(Desktop desktop, int dx, int dy)
{
	Dimension result = desktop.getSize(this, dx, dy);
	result.width += desktop.getBlockSize();
	return result;
}

public void validate()
{ parentWidget.validate(); }

@Override
public void paint(LwjglRenderer renderer, boolean armed)
{
	Rectangle r = getBounds();
	int block = renderer.getBlockSize();
	renderer.paintIconAndText(this, r.x, r.y, r.width, r.height,
		false, false, false, false,
		2, 4, 2, 4, false,
		isEnabled() ? (armed ? 's' : 't') : 'd', false);
	renderer.paintArrow(r.x + r.width - block, r.y, block, r.height, 'E');
}

}
