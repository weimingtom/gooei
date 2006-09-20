package thinlet;

import java.util.Iterator;

import thinlet.api.DataElement;
import thinlet.api.Element;

/** A tree node. */
public final class TreeNode extends AbstractElement implements DataElement<TreeNode>, SelectableWidget
{

private boolean selected = false;
private boolean expanded = true;

private TreeNode dataElement;
private TreeNode nextElement;

public TreeNode()
{/*OK*/}

public boolean isSelected()
{ return selected; }

public void setSelected(boolean selected)
{ this.selected = selected; }

public boolean isExpanded()
{ return expanded; }

public void setExpanded(boolean expanded)
{ this.expanded = expanded; }

public TreeNode data()
{ return dataElement; }

public TreeNode next()
{ return nextElement; }

protected void setNext(TreeNode nextElement)
{ this.nextElement = nextElement; }

protected void setComponent(Element componentWidget)
{ this.dataElement = (TreeNode) componentWidget; }

@SuppressWarnings("unchecked")
public Iterator<TreeNode> iterator()
{
	return new Iterator<TreeNode>()
		{
			TreeNode next = dataElement;
			public boolean hasNext()
			{ return next != null; }
			public TreeNode next()
			{
				TreeNode result = next;
				next = next.next();
				return result;
			}
			public void remove()
			{ throw new UnsupportedOperationException(); }
		};
}

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

/** Inserts item in child chain at specified index. Appends if index is negative. */
protected void insertItem(TreeNode child, int index)
{
	if ((dataElement == null) || (index == 0))
	{
		child.setNext(dataElement);
		setComponent(child);
		return;
	}
	TreeNode item = dataElement, next = item.next();
	for (int i = 1; ; i++)
	{
		if ((i == index) || (next == null))
		{
			item.setNext(child);
			child.setNext(next);
			break;
		}
		item = next;
		next = item.next();
	}
}

public void addChild(Element child, int index)
{
	if (!(child instanceof TreeNode))
		throw new IllegalArgumentException("Cannot add "+child+" to "+this+"!");
	if (child.parent() != null)
		throw new IllegalArgumentException();
	insertItem((TreeNode) child, index);
	child.setParent(this);
	child.update("validate");
}

public void removeChild(TreeNode component)
{
	TreeNode child = component;
	TreeNode previous = null; // the widget before the given component
	for (TreeNode comp = dataElement; comp != null; )
	{
		TreeNode next = comp.next();
		if (next == component)
		{
			previous = comp;
			break;
		}
		comp = next;
	}
	
	if (previous != null)
		previous.setNext(child.next());
	else
	{
		if (dataElement != child)
			throw new IllegalArgumentException();
		setComponent(child.next());
	}
	child.setNext(null);
	child.setParent(null);
}

public void removeChild(Element component)
{ removeChild((TreeNode) component); }

}
