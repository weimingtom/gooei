package thinlet.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import thinlet.Element;
import thinlet.ElementContainer;

public abstract class AbstractContainerElement<T extends Element> extends AbstractElement implements ElementContainer<T>
{

//protected Element dataElement;
private List<T> data = new ArrayList<T>();

public AbstractContainerElement()
{/*OK*/}

public int getCount()
{ return data.size(); }

public T getChild(int index)
{ return data.get(index); }

public int getElementCount()
{ return data.size(); }

public Iterator<T> iterator()
{ return data.iterator(); }

public abstract boolean acceptChild(Element child);

/** Inserts item in child chain at specified index. Appends if index is negative. */
protected void insertItem(T child, int index)
{
	if ((index >= 0) && (index < data.size()))
		data.add(index, child);
	else
		data.add(child);
	child.setParent(this);
}

@SuppressWarnings("unchecked")
public void addChild(Element child, int index)
{
	if (!acceptChild(child))
		throw new IllegalArgumentException("Cannot add "+child+" to "+this+"!");
	if (child.parent() != null)
		throw new IllegalArgumentException();
	insertItem((T) child, index);
	child.setParent(this);
	validate();
}

public void removeChild(Element child)
{
	if (!data.remove(child))
		throw new IllegalArgumentException();
	child.setParent(null);
}

public void validate()
{ parentWidget.validate(); }

}
