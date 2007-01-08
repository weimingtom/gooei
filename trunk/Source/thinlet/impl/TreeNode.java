package thinlet.impl;

import java.util.Iterator;

import thinlet.Element;
import thinlet.ElementContainer;
import thinlet.SelectableElement;

/** A tree node. */
public final class TreeNode extends AbstractElement implements ElementContainer<TreeNode>, SelectableElement
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

public int getElementCount()
{
	int i = 0;
	TreeNode n = dataElement;
	while (n != null)
	{
		i++;
		n = n.nextElement;
	}
	return i;
}

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

public TreeNode findElement(String fname)
{
	if (fname.equals(getName())) return this;
	// otherwise search in its subcomponents
	for (TreeNode item : this)
	{
		if (fname.equals(item.getName())) return item;
		TreeNode found = item.findElement(fname);
		if (found != null) return found;
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
	validate();
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

public void validate()
{ parentWidget.validate(); }

}
