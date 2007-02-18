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

public final class TableWidget extends AbstractElementContainerWidget<TableRow>
{

private int[] widths;
private List<TableRow> data = new ArrayList<TableRow>();
private TableHeader headerWidget = null;

public TableWidget(Desktop desktop)
{ super(desktop); }

public int[] getWidths()
{ return widths; }

void setWidths(int[] widths)
{ this.widths = widths; }

public TableHeader getHeaderWidget()
{ return headerWidget; }

public void setHeaderWidget(TableHeader newheader)
{
	if (newheader == null)
	{
		if (headerWidget != null)
		{
			headerWidget.setParent(null);
			headerWidget = null;
		}
	}
	else
	{
		if (newheader.parent() != null) throw new IllegalArgumentException();
		headerWidget = newheader;
		headerWidget.setParent(this);
	}
}

@Override
public boolean isEmpty()
{ return data.size() == 0; }

public int getElementCount()
{ return data.size(); }

@Override
public Iterator<TableRow> iterator()
{ return data.iterator(); }

/** Inserts item in child chain at specified index. Appends if index is negative. */
protected void insertItem(TableRow child, int index)
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
	if (!(child instanceof TableRow))
		throw new IllegalArgumentException("Cannot add "+child+" to "+this+"!");
	if (child.parent() != null)
		throw new IllegalArgumentException();
	insertItem((TableRow) child, index);
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
	int w = 0;
	int columnheight = 0;
	
	TableHeader header = getHeaderWidget();
	int[] columnwidths = null;
	if (header != null)
	{
		columnwidths = new int[header.getCount()];
		int i = 0;
		for (final TableColumn column : header)
		{
			columnwidths[i] = column.getWidth();
			w += columnwidths[i];
			Dimension d = desktop.getSize(column, 2, 2);
			columnheight = Math.max(columnheight, d.height);
			i++;
		}
	}
	setWidths(columnwidths);
	
	int y = 0;
	for (final TableRow row : this)
	{
		int iheight = 0;
		for (final TableCell cell : row)
		{
			Dimension d = desktop.getSize(cell, 2, 2);
			iheight = Math.max(iheight, d.height);
		}
		row.setBounds(0, y, w, iheight);
		y += iheight + l;
	}
	
	layoutScroll(w, y - l, columnheight, 0, 0, 0, true, 0);
	
	needsLayout = false;
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
			for (final TableRow row : this)
			{
				Rectangle r = row.getBounds();
				if (my < r.y + r.height)
				{
					if (id == InputEventType.MOUSE_DRAGGED)
					{
						getScrollbarSupport().scrollToVisible(r.x, r.y, 0, r.height);
					}
					
					if ((id != InputEventType.MOUSE_DRAGGED) || !row.isSelected())
					{
						if (id != InputEventType.MOUSE_DRAGGED)
						{
							if (setFocus())
								getScrollbarSupport().repaintScrollablePart(row);
						}
						if (!event.isPopupTrigger() || !row.isSelected())
						{ // don't update selection
							select(row, shiftdown, controldown);
							if (event.getClickCount() == 2) invokePerform(row);
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
	final int clipx = renderer.getClipX(), clipy = renderer.getClipY();
	final int clipwidth = renderer.getClipWidth(), clipheight = renderer.getClipHeight();
	
	final boolean focus = hasFocus();
	final boolean enabled = isEnabled() && renderer.isEnabled();
	
	TableRow lead = (TableRow) getLeadWidget();
	int iline = hasLine() ? 1 : 0;
	int[] columnwidths = getWidths();
	for (final TableRow row : this)
	{
		// draw first item focused when lead is null
		if (focus && (lead == null))
			setLeadWidget(lead = row);
		Rectangle r = row.getBounds();
		if (clipy + clipheight <= r.y) break; // clip rectangle is above
		
		if (clipy >= r.y + r.height + iline)
			continue; // clip rectangle is below
		
		boolean selected = row.isSelected();
		renderer.paintRect(0, r.y, viewwidth, r.height, null,
			selected ? renderer.c_select : renderer.c_textbg, false, false, false, false, true);
		
		if (focus && (lead == row))
			renderer.drawFocus(0, r.y, viewwidth - 1, r.height - 1);
		
		if (hasLine())
		{
			renderer.setColor(renderer.c_bg);
			renderer.drawLine(0, r.y + r.height, viewwidth-1, r.y + r.height);
		}
		
		// paint row, but only cells at least partially inside the clip
		int x = 0;
		int i = 0;
		for (final TableCell cell : row)
		{
			if (clipx + clipwidth <= x) break;
			//column width is defined by header calculated in layout, otherwise is 80
			int iwidth = 80;
			if ((columnwidths != null) && (columnwidths.length > i))
			{
				iwidth = (i != columnwidths.length - 1) ?
					columnwidths[i] : Math.max(iwidth, viewwidth - x);
			}
			if (clipx < x + iwidth)
			{
				boolean cellenabled = enabled && cell.isEnabled();
				renderer.paintIconAndText(cell, r.x + x, r.y, iwidth, r.height - 1,
					false, false, false, false, 1, 1, 1, 1, false, cellenabled ? 'e' : 'd', false);
			}
			x += iwidth;
			i++;
		}
	}
}

}
