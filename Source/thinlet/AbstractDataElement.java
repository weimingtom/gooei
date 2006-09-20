package thinlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import thinlet.api.DataElement;
import thinlet.api.Element;

public abstract class AbstractDataElement<T extends Element> extends AbstractElement implements DataElement<T>
{

//protected Element dataElement;
private List<T> data = new ArrayList<T>();

public int getCount()
{ return data.size(); }

public T getChild(int index)
{ return data.get(index); }

public Iterator<T> iterator()
{ return data.iterator(); }

public Element findElement(String fname)
{
	if (fname.equals(getName())) return this;
	// otherwise search in its subcomponents
	for (Element item : this)
	{
		if (fname.equals(item.getName())) return item;
		if (item instanceof DataElement)
		{
			Element found = null;
			if ((found = ((DataElement) item).findElement(fname)) != null)
				return found;
		}
	}
	return null;
}

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
	child.update("validate");
}

public void removeChild(Element child)
{
	if (!data.remove(child))
		throw new IllegalArgumentException();
	child.setParent(null);
}

}
