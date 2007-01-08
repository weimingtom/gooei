package thinlet.impl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import thinlet.Element;
import thinlet.MouseInteraction;
import thinlet.help.MethodInvoker;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.Keys;
import de.ofahrt.utils.input.Modifiers;
import de.ofahrt.utils.input.MouseEvent;

public final class TreeWidget extends AbstractElementContainerWidget<TreeNode>
{

private boolean angle;
private MethodInvoker expandMethod, collapseMethod;

private TreeNode dataElement;

public TreeWidget(ThinletDesktop desktop)
{ super(desktop); }

public boolean hasAngle()
{ return angle; }

public void setAngle(boolean angle)
{ this.angle = angle; }

public void setExpand(MethodInvoker method)
{ expandMethod = method; }

public boolean invokeExpand(Element part)
{ return invokeIt(expandMethod, part); }

public void setCollapse(MethodInvoker method)
{ collapseMethod = method; }

public boolean invokeCollapse(Element part)
{ return invokeIt(collapseMethod, part); }

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

private TreeNode findNext(TreeNode item)
{
	TreeNode node = item;
	TreeNode next = item.data();
	if ((next == null) || !node.isExpanded())
	{
		while (node.next() == null)
		{
			if (!(node.parent() instanceof TreeNode)) break;
			node = (TreeNode) node.parent();
		}
		next = node.next();
	}
	return next;
}

@Override
public boolean isEmpty()
{ return dataElement == null; }

public int getElementCount()
{
	int i = 0;
	for (TreeNode node = dataElement; node != null; node = node.next())
		i++;
	return i;
}

@Override
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
				next = findNext(next);
				return result;
			}
			public void remove()
			{ throw new UnsupportedOperationException(); }
		};
}

/** Inserts item in child chain at specified index. Appends if index is negative. */
protected void insertItem(TreeNode child, int index)
{
	if ((dataElement == null) || (index == 0))
	{
		child.setNext(dataElement);
		dataElement = child;
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

@Override
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
		dataElement = child.next();
	}
	child.setNext(null);
	child.setParent(null);
	validate();
}

@Override
public void removeChild(Element component)
{ removeChild((TreeNode) component); }

@Override
public void doLayout()
{
	int block = desktop.getBlockSize();
	int l = hasLine() ? 1 : 0;
	int w = 0;
	int columnheight = 0;
	
	int y = 0;
	int level = 0;
	for (TreeNode item = dataElement; item != null;)
	{
		int x = 0;
		int iwidth = 0;
		int iheight = 0;
		x = (level + 1) * block;
		Dimension d = desktop.getSize(item, 6, 2);
		iwidth = d.width;
		iheight = d.height;
		w = Math.max(w, x + d.width);
		item.setBounds(x, y, iwidth, iheight);
		y += iheight + l;
		
		TreeNode next = item.data();
		if ((next != null) && item.isExpanded())
			level++;
		else
		{
			while (((next = item.next()) == null) && (level > 0))
			{
				item = (TreeNode) item.parent();
				level--;
			}
		}
		item = next;
	}
	layoutScroll(w, y - l, columnheight, 0, 0, 0, true, 0);
	
	needsLayout = false;
}

@Override
public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	//? clear childs' selection, select this is its 	subnode was selected
	if (keycode == Keys.LEFT)
	{
		TreeNode lead = (TreeNode) getLeadWidget();
		if ((lead.data() != null) && lead.isExpanded())
		{ // collapse
			lead.setExpanded(false);
			selectItem(lead);
			validate();
			invokeCollapse(lead);
			return true;
		}
		else
		{ // select parent
			if (lead.parent() instanceof Element)
			{
				Element parent = (Element) lead.parent();
				selectItem(parent);
				setLead(lead, parent);
				return true;
			}
		}
	}
	//? for interval mode select its all subnode or deselect all after
	else if (keycode == Keys.RIGHT)
	{
		TreeNode lead = (TreeNode) getLeadWidget();
		TreeNode node = lead.data();
		if (node != null)
		{
			if (lead.isExpanded())
			{ // select its first subnode
				selectItem(node);
				setLead(lead, node);
			}
			else
			{ // expand
				lead.setExpanded(true);
				selectItem(lead);
				validate();
				invokeExpand(lead);
			}
			return true;
		}
	}
	return processList(event);
}

@Override
public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
{ findScroll(mouseInteraction, x, y); }

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	boolean shiftdown = event.isModifierDown(Modifiers.SHIFT);
	boolean controldown = event.isModifierDown(Modifiers.CTRL);
	
	if (!processScroll(mouseInteraction, event, part))
	{
		if (((id == InputEventType.MOUSE_PRESSED) ||
				((id == InputEventType.MOUSE_DRAGGED) && !shiftdown && !controldown)))
		{
			Rectangle port = getPort();
			int my = event.getY() + port.y - mouseInteraction.referencey;
			for (final TreeNode item : this)
			{
				Rectangle r = item.getBounds();
				if (my < r.y + r.height)
				{
					if (id == InputEventType.MOUSE_DRAGGED)
					{
						scrollToVisible(r.x, r.y, 0, r.height);
					}
					else
					{
						int mx = event.getX() + port.x - mouseInteraction.referencex;
						if (mx < r.x)
						{
							if ((mx >= r.x - desktop.getBlockSize()) && (item.data() != null))
							{
								boolean expanded = item.isExpanded();
								item.setExpanded(!expanded);
								selectItem(item);
								setLead(getLeadWidget(), item);
								setFocus();
								validate();
								if (expanded)
									invokeExpand(item);
								else
									invokeCollapse(item);
							}
							break;
						}
					}
					
					if ((id != InputEventType.MOUSE_DRAGGED) || !item.isSelected())
					{
						if (id != InputEventType.MOUSE_DRAGGED)
						{
							if (setFocus())
								repaintScrollablePart(item);
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
public void paintScrollableContent(LwjglWidgetRenderer renderer, boolean enabled)
{
	int viewwidth = getView().width;
	
	// clip is used for rendering acceleration
	final int clipy = renderer.getClipY();
	final int clipheight = renderer.getClipHeight();
	
	final int block = desktop.getBlockSize();
	final boolean focus = hasFocus();
	
	// paint rows
	TreeNode lead = (TreeNode) getLeadWidget();
//	boolean line = hasLine();
	int iline = hasLine() ? 1 : 0;
//	boolean angle = hasAngle();
	for (TreeNode item : this)
	{
		// draw first item focused when lead is null
		if (focus && (lead == null))
			setLeadWidget(lead = item);
		Rectangle r = item.getBounds();
		if (clipy + clipheight <= r.y) break; // clip rectangle is above
		
		if (clipy >= r.y + r.height + iline)
		{
			if (angle)
			{ // TODO draw dashed line
				TreeNode nodebelow = item.next();
				if (nodebelow != null)
				{ // and the next node is bellow clipy
					renderer.setColor(renderer.c_bg);
					int x = r.x - block / 2;
					renderer.drawLine(x, r.y, x, nodebelow.getBounds().y);
				}
			}
			continue; // clip rectangle is below
		}
	
		boolean selected = item.isSelected();
		renderer.paintRect(r.x, r.y, r.width, r.height, null,
			selected ? renderer.c_select : renderer.c_textbg, false, false, false, false, true);
		
		if (focus && (lead == item))
			renderer.drawFocus(r.x, r.y, r.width - 1, r.height - 1);
		
		if (hasLine())
		{
			renderer.setColor(renderer.c_bg);
			renderer.drawLine(0, r.y + r.height, viewwidth-1, r.y + r.height);
		}
		
		boolean itemenabled = enabled && item.isEnabled();
		renderer.paintIconAndText(item, r.x, r.y, viewwidth, r.height,
			false, false, false, false,
			1, 3, 1, 3, false, itemenabled ? 'e' : 'd', false);
		int x = r.x - block / 2; int y = r.y + (r.height - 1) / 2;
		if (angle)
		{
			renderer.setColor(renderer.c_bg);
			renderer.drawLine(x, r.y, x, y);
			renderer.drawLine(x, y, r.x-1, y);
			TreeNode nodebelow = item.next();
			if (nodebelow != null)
				renderer.drawLine(x, y, x, nodebelow.getBounds().y);
		}
		if (item.data() != null)
		{
			renderer.paintRect(x - 4, y - 4, 9, 9, itemenabled ? renderer.c_border : renderer.c_disable,
				itemenabled ? renderer.c_ctrl : renderer.c_bg, true, true, true, true, true);
			renderer.setColor(itemenabled ? renderer.c_text : renderer.c_disable);
			renderer.drawLine(x - 2, y, x + 2, y);
			if (!item.isExpanded()) renderer.drawLine(x, y - 2, x, y + 2);
		}
	}
}

}
