package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.Element;
import gooei.ElementContainer;
import gooei.FocusableWidget;
import gooei.MouseInteraction;
import gooei.MouseRouterWidget;
import gooei.MouseableWidget;
import gooei.ScrollableWidget;
import gooei.SelectableElement;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.input.Modifiers;
import gooei.input.MouseEvent;
import gooei.utils.MethodInvoker;
import gooei.utils.SelectionType;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public abstract class AbstractElementContainerWidget<T extends Element> extends AbstractWidget
	implements ElementContainer<T>, ScrollableWidget, MouseRouterWidget, MouseableWidget, FocusableWidget
{

private boolean line = true;

private SelectionType selection = SelectionType.SINGLE;

private Element leadWidget = null;
private Element anchorWidget = null;
private MethodInvoker actionMethod, performMethod;

protected boolean needsLayout = true;

private long findtime = 0;
private String findtext = "";

public AbstractElementContainerWidget(Desktop desktop)
{ super(desktop); }

@Override
public final void setBounds(int x, int y, int width, int height)
{
	super.setBounds(x, y, width, height);
	validate();
}

public boolean hasLine()
{ return line; }

public void setLine(boolean line)
{ this.line = line; }

public SelectionType getSelection()
{ return selection; }

public void setSelection(SelectionType selection)
{
	if (selection == null) throw new NullPointerException();
	this.selection = selection;
}

public Element getLeadWidget()
{ return leadWidget; }

public void setLeadWidget(Element leadWidget)
{ this.leadWidget = leadWidget; }

public Element getAnchorWidget()
{ return anchorWidget; }

public void setAnchorWidget(Element anchorWidget)
{ this.anchorWidget = anchorWidget; }

public void setAction(MethodInvoker method)
{ actionMethod = method; }

public boolean invokeAction(Element part)
{ return invokeIt(actionMethod, part); }

public boolean invokeAction()
{ return invokeAction(null); }

public void setPerform(MethodInvoker method)
{ performMethod = method; }

public boolean invokePerform(Element part)
{ return invokeIt(performMethod, part); }


public abstract boolean isEmpty();
public abstract Iterator<T> iterator();
public abstract void addChild(Element child, int index);
public abstract void removeChild(Element component);


/**
 * Gets the first selected item.
 * @return the first selected item or null
 */
public Element getSelectedItem()
{
	for (final Element item : this)
	{
		if (((SelectableElement) item).isSelected())
			return item;
	}
	return null;
}

/**
 * Gets the selected item of the given component
 * when multiple selection is allowed
 *
 * @return the array of selected items, or a 0 length array
 */
public Element[] getSelectedItems()
{
	ArrayList<Element> result = new ArrayList<Element>();
	for (final Element item : this)
	{
		if (((SelectableElement) item).isSelected())
			result.add(item);
	}
	return result.toArray(new Element[0]);
}

protected Element getListItem(Keys keycode, Element lead)
{
	Element row = null;
	if (keycode == Keys.UP)
	{
		for (Element prev : this)
		{
			if (prev == lead) break;
			row = prev; // component -> getParent(lead)
		}
	}
	else if (keycode == Keys.DOWN)
	{
		Element block = lead;
		for (Element e : this)
		{
			if (row == null) row = e;
			if (block != null)
			{
				if (block == e) block = null;
				continue;
			}
			row = e;
			break;
		}
	}
	else if ((keycode == Keys.PRIOR) ||
			(keycode == Keys.NEXT))
	{
		Rectangle view = getView();
		Rectangle port = getPort();
		Rectangle rl = (lead != null) ? lead.getBounds() : null;
		int vy = (keycode == Keys.PRIOR) ?
			view.y : (view.y + port.height);
		if ((keycode == Keys.PRIOR) &&
				(rl != null) && (rl.y <= view.y))
		{
			vy -= port.height;
		}
		if ((keycode == Keys.NEXT) &&
				(rl != null) && (rl.y + rl.height >= view.y + port.height))
		{
			vy += port.height;
		}
		for (final Element item : this)
		{
			Rectangle r = item.getBounds();
			if (keycode == Keys.PRIOR)
			{
				row = item;
				if (r.y + r.height > vy) { break; }
			}
			else
			{
				if (r.y > vy) { break; }
				row = item;
			}
		}
	}
	else if (keycode == Keys.HOME)
	{
		row = iterator().next();
	}
	else if (keycode == Keys.END)
	{
		for (Element last : this)
			row = last;
	}
	return row;
}

/**
 * Search for the next/first appropriate item starting with the collected string
 * or the given single character
 * @param keychar the last typed character
 * @return the appropriate item or null
 */
protected Element findText(char keychar)
{
	if (keychar != 0)
	{
		long current = System.currentTimeMillis();
		int i = (current > findtime + 1000) ? 1 : 0; // clear the starting string after a second
		findtime = current;
		Element lead = getLeadWidget();
		for (; i < 2; i++)
		{ // 0: find the long text, 1: the stating character only
			findtext = (i == 0) ? (findtext + keychar) : String.valueOf(keychar);
			Element block = lead;
			for (final Element item : this)
			{
				if (block != null)
				{
					if (block == item) block = null;
					continue;
				}
				if (item.getText().regionMatches(true, 0, findtext, 0, findtext.length()))
					return item;
			}
			for (final Element item : this)
			{
				if (item == lead) break;
				if (item.getText().regionMatches(true, 0, findtext, 0, findtext.length()))
					return item;
			}
		}
	}
	return null;
}

/**
 * Select all the items
 * @param selected selects or deselects items
 */
protected void selectAll(boolean selected)
{
	boolean changed = false;
	for (final Element item : this)
	{
		if (!((SelectableElement) item).isSelected() ^ selected)
		{
			((SelectableElement) item).setSelected(selected);
			getScrollbarSupport().repaintScrollablePart(item);
			changed = true;
		}
	}
	setAnchorWidget(null);
	if (changed) invokeAction();
}

/**
 * Select a single given item, deselect others
 * @param row the item/node/row to select
 */
protected void selectItem(Element row)
{
	boolean changed = false;
	for (final Element item : this)
	{
		if (((SelectableElement) item).isSelected() ^ (item == row))
		{
			((SelectableElement) item).setSelected(item == row);
			getScrollbarSupport().repaintScrollablePart(item);
			changed = true;
		}
	}
	setAnchorWidget(null);
	if (changed) invokeAction(row);
}

protected void extend(Element lead, Element row)
{
	Element anchor = getAnchorWidget();
	if (anchor == null) setAnchorWidget(anchor = lead);
	char select = 'n';
	boolean changed = false;
	for (final Element item : this)
	{
		if (item == anchor) select = (select == 'n') ? 'y' : 'r';
		if (item == row) select = (select == 'n') ? 'y' : 'r';
		if (((SelectableElement) item).isSelected() ^ (select != 'n'))
		{
			((SelectableElement) item).setSelected(select != 'n');
			getScrollbarSupport().repaintScrollablePart(item);
			changed = true;
		}
		if (select == 'r')
			select = 'n';
	}
	if (changed)
		invokeAction(row);
}

/**
 * Update the lead item of a list/tree/table, repaint, and scroll
 * @param component a list, tree, or table
 * @param oldlead the current lead item
 * @param lead the new lead item
 */
protected void setLead(Element oldlead, Element lead)
{
	if (oldlead != lead)
	{
		if (oldlead != null) getScrollbarSupport().repaintScrollablePart(oldlead);
		setLeadWidget(lead);
		getScrollbarSupport().repaintScrollablePart(lead);
		Rectangle r = lead.getBounds();
		getScrollbarSupport().scrollToVisible(r.x, r.y, 0, r.height);
	}
}

protected void select(Element row, boolean shiftdown, boolean controldown)
{
	Element lead = null;
//	SelectionType selection = getSelection();
	if (shiftdown && (selection != SelectionType.SINGLE) && ((lead = getLeadWidget()) != null))
	{
		extend(lead, row);
	}
	else
	{
		if (controldown && (selection == SelectionType.MULTIPLE))
		{
			((SelectableElement) row).setSelected(!((SelectableElement) row).isSelected());
			getScrollbarSupport().repaintScrollablePart(row);
			invokeAction(row);
			setAnchorWidget(null);
		}
		else if (controldown && ((SelectableElement) row).isSelected())
		{
			Iterator<T> it = iterator();
			while (it.hasNext())
				if (it.next() == row) break;
			while (it.hasNext())
			{
				Element item = it.next();
				if (((SelectableElement) item).isSelected())
				{
					((SelectableElement) item).setSelected(false);
					getScrollbarSupport().repaintScrollablePart(item);
				}
			}
			invokeAction(row);
			setAnchorWidget(null);
		}
		else
			selectItem(row);
	}
	setLead(lead != null ? lead : getLeadWidget(), row);
}

protected boolean processList(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	boolean shiftdown = event.isModifierDown(Modifiers.SHIFT);
	boolean controldown = event.isModifierDown(Modifiers.CTRL);
	
//	SelectionType selection = getSelection();
	if ((keycode == Keys.UP) || // select previous/next/first/... item
			(keycode == Keys.DOWN) || (keycode == Keys.PRIOR) ||
			(keycode == Keys.NEXT) ||
			(keycode == Keys.HOME) || (keycode == Keys.END))
	{
		Element lead = getLeadWidget();
		Element row = getListItem(keycode, lead);
		if (row != null)
		{
			if (shiftdown && (selection != SelectionType.SINGLE) && (lead != null))
				extend(lead, row);
			else if (!controldown)
				selectItem(row);
			setLead(lead, row);
			return true;
		}
	}
	else if (keycode == Keys.LEFT)
		return handleScrollEvent("left");
	else if (keycode == Keys.RIGHT)
		return handleScrollEvent("right");
	else if (keycode == Keys.SPACE)
	{ // select the current item
		select(getLeadWidget(), shiftdown, controldown);
		return true;
	}
	else if (controldown)
	{ // KeyEvent.VK_SLASH
		if (((keycode == Keys.A) /*|| (keycode == 0xBF)*/) && (selection != SelectionType.SINGLE))
		{ // select all
			selectAll(true);
			return true;
		}
		else if (keycode == Keys.BACKSLASH)
		{ //KeyEvent.VK_BACK_SLASH // deselect all
			selectAll(false);
			return true;
		}
	}
	else
	{
		Element item = findText(event.getKeyChar());
		if (item != null)
		{
			select(item, false, false);
			return true;
		}
	}
	return false;
}


@Override
public abstract Dimension getPreferredSize();

public abstract boolean handleKeyPress(KeyboardEvent event);
public abstract void findComponent(MouseInteraction mouseInteraction, int x, int y);
public abstract void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event);


public boolean needsLayout()
{ return needsLayout; }

public abstract void doLayout();

public void validate()
{
	repaint();
	needsLayout = true;
}


@Override
public final void paint(LwjglRenderer renderer)
{
	if (needsLayout()) doLayout();
	paintScroll(renderer, hasFocus() && isEmpty());
}

@Override
public abstract void paintScrollableContent(LwjglRenderer renderer);

}
