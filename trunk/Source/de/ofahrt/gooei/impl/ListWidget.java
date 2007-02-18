package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.Element;
import gooei.MouseInteraction;
import gooei.input.InputEventType;
import gooei.input.KeyboardEvent;
import gooei.input.Modifiers;
import gooei.input.MouseEvent;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class ListWidget extends AbstractElementContainerWidget<ListItem>
{

private List<ListItem> data = new ArrayList<ListItem>();

public ListWidget(Desktop desktop)
{ super(desktop); }

@Override
public boolean isEmpty()
{ return data.size() == 0; }

public ListItem getChild(int index)
{ return data.get(index); }

public int getElementCount()
{ return data.size(); }

@Override
public Iterator<ListItem> iterator()
{ return data.iterator(); }

/** Inserts item in child chain at specified index. Appends if index is negative. */
protected void insertItem(ListItem child, int index)
{
	if ((index >= 0) && (index < data.size()))
		data.add(index, child);
	else
		data.add(child);
	child.setParent(this);
}

@Override
public void addChild(Element child, int index)
{
	if (!(child instanceof ListItem))
		throw new IllegalArgumentException("Cannot add "+child+" to "+this+"!");
	if (child.parent() != null)
		throw new IllegalArgumentException();
	insertItem((ListItem) child, index);
	child.setParent(this);
	validate();
}

@Override
public void removeChild(Element child)
{
	if (!data.remove(child))
		throw new IllegalArgumentException();
	child.setParent(null);
	validate();
}

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	int block = desktop.getBlockSize();
	Dimension result = new Dimension(76 + 2 + block, 76 + 2 + block);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void doLayout()
{
	int l = hasLine() ? 1 : 0;
	int w = 0, y = 0;
	for (ListItem item : this)
	{
		Dimension d = desktop.getSize(item, 6, 2);
		w = Math.max(w, d.width);
		item.setBounds(0, y, d.width, d.height);
		y += d.height + l;
	}
	layoutScroll(w, y - l, 0, 0, 0, 0, true, 0);
	
	needsLayout = false;
}

public int getSelectedIndex()
{
	int i = 0;
	for (final ListItem item : this)
	{
		if (item.isSelected()) return i;
		i++;
	}
	return -1;
}

@Override
public boolean handleKeyPress(KeyboardEvent event)
{ return processList(event); }

@Override
public void findComponent(MouseInteraction mouseInteraction, int x, int y)
{ getScrollbarSupport().findScroll(mouseInteraction, x, y); }

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	boolean shiftdown = event.isModifierDown(Modifiers.SHIFT);
	boolean controldown = event.isModifierDown(Modifiers.CTRL);
	
	if (!getScrollbarSupport().handleMouseEvent(part, mouseInteraction, event))
	{
		if (((id == InputEventType.MOUSE_PRESSED) ||
				((id == InputEventType.MOUSE_DRAGGED) && !shiftdown && !controldown)))
		{
			Rectangle port = getPort();
			int my = event.getY() + port.y - mouseInteraction.referencey;
			for (final ListItem item : this)
			{
				Rectangle r = item.getBounds();
				if (my < r.y + r.height)
				{
					if (id == InputEventType.MOUSE_DRAGGED)
					{
						getScrollbarSupport().scrollToVisible(r.x, r.y, 0, r.height);
					}
					
					if ((id != InputEventType.MOUSE_DRAGGED) || !item.isSelected())
					{
						if (id != InputEventType.MOUSE_DRAGGED)
						{
							if (setFocus())
								getScrollbarSupport().repaintScrollablePart(item);
						}
						if (!event.isPopupTrigger() || !item.isSelected())
						{ // don't update selection
							select(item, shiftdown, controldown);
							if (event.getClickCount() == 2) invokePerform(item);
						}
					}
					break;
				}
			}
		}
	}
}

@Override
public void paintScrollableContent(LwjglRenderer renderer)
{
	int viewwidth = getView().width;
	
	// clip is used for rendering acceleration
	final int clipy = renderer.getClipY();
	final int clipheight = renderer.getClipHeight();
	
	final boolean focus = hasFocus();
	final boolean enabled = isEnabled() && renderer.isEnabled();
	
	ListItem lead = (ListItem) getLeadWidget();
	int iline = hasLine() ? 1 : 0;
	for (final ListItem item : this)
	{
		// draw first item focused when lead is null
		if (focus && (lead == null))
			setLeadWidget(lead = item);
		Rectangle r = item.getBounds();
		if (clipy + clipheight <= r.y) break; // clip rectangle is above
		
		if (clipy >= r.y + r.height + iline)
			continue; // clip rectangle is below
		
		boolean selected = item.isSelected();
		renderer.paintRect(0, r.y, viewwidth, r.height, null,
			selected ? renderer.c_select : renderer.c_textbg, false, false, false, false, true);
		
		if (focus && (lead == item))
			renderer.drawFocus(0, r.y, viewwidth - 1, r.height - 1);
		
		if (hasLine())
		{
			renderer.setColor(renderer.c_bg);
			renderer.drawLine(0, r.y + r.height, viewwidth-1, r.y + r.height);
		}
		
		boolean itemenabled = enabled && item.isEnabled();
		renderer.paintIconAndText(item, r.x, r.y, viewwidth, r.height,
			false, false, false, false,
			1, 3, 1, 3, false, itemenabled ? 'e' : 'd', false);
	}
}

}
