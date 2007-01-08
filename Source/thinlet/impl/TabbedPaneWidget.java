package thinlet.impl;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.FocusableWidget;
import thinlet.MouseInteraction;
import thinlet.Widget;
import thinlet.help.MethodInvoker;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.Keys;
import de.ofahrt.utils.input.MouseEvent;

public final class TabbedPaneWidget extends AbstractContainerWidget<TabWidget> implements FocusableWidget
{

	public static enum Placement
	{
		TOP, LEFT, BOTTOM, RIGHT, STACKED;
	}

private int selected = 0;
private Placement placement = Placement.TOP;
private MethodInvoker actionMethod;

public TabbedPaneWidget(ThinletDesktop desktop)
{ super(desktop); }

public int getSelected()
{ return selected; }

public void setSelected(int selected)
{ this.selected = selected; }

public Placement getPlacement()
{ return placement; }

public void setPlacement(Placement placement)
{
	if (placement == null) throw new NullPointerException();
	this.placement = placement;
}

public void setAction(MethodInvoker method)
{ actionMethod = method; }

public boolean invokeAction(TabWidget part)
{ return invokeIt(actionMethod, part); }

public TabWidget getSelectedItem()
{
	int index = getSelected();
	return (index != -1) ? getTab(index) : null;
}

@Override
public boolean acceptChild(Widget child)
{ return child instanceof TabWidget; }

@Override
public boolean isChildFocusable(Widget child)
{
	if (!isEnabled() || !isVisible()) return false;
	if (parent() == null) return false;
	if (child != getSelectedItem()) return false;
	return parent().isChildFocusable(this);
}

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	boolean horizontal = (placement != Placement.LEFT) && (placement != Placement.RIGHT);
	int tabsize = 0; // max tab height (for horizontal),
	// max tabwidth (for vertical), or sum of tab heights for stacked
	int contentwidth = 0; int contentheight = 0; // max content size
	for (TabWidget tab : this)
	{
		Dimension d = tab.getSize(0, 0);
		if (placement == Placement.STACKED)
			tabsize += d.height + 3;
		else
			tabsize = Math.max(tabsize, horizontal ? d.height + 5 : d.width + 9);
		Widget comp = tab.getContent();
		if ((comp != null) && comp.isVisible())
		{
			Dimension dc = comp.getPreferredSize();
			contentwidth = Math.max(contentwidth, dc.width);
			contentheight = Math.max(contentheight, dc.height);
		}
	}
	
	Dimension result = new Dimension(contentwidth + (horizontal ? 4 : (tabsize + 3)),
		contentheight + (horizontal ? (tabsize + 3) : 4));
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void doLayout()
{
	// tabbedpane (not selected) tab padding are 1, 3, 1, and 3 pt
	Rectangle bounds = getBounds();
	boolean horizontal = (placement == Placement.TOP) || (placement == Placement.BOTTOM);
	boolean stacked = placement == Placement.STACKED;
	
	// draw up tabs in row/column
	int tabd = 0; Rectangle first = null; // x/y location of tab left/top
	int tabsize = 0; // max height/width of tabs
	for (final TabWidget tab : this)
	{
		if ((tabd == 0) && ((first = tab.getBounds()) != null))
			tabd = horizontal ? first.x : first.y; // restore previous offset
		Dimension d = tab.getSize(stacked ? 8 : horizontal ? 12 : 9,
			stacked ? 3 : horizontal ? 5 : 8);
		tab.setBounds(horizontal ? tabd : 0, horizontal ? 0 : tabd,
				stacked ? bounds.width : d.width, d.height);
		if (stacked)
			tabd += d.height;
		else
		{
			tabd += (horizontal ? d.width : d.height) - 3;
			tabsize = Math.max(tabsize, horizontal ? d.height : d.width);
		}
	}
	
	// match tab height/width, set tab content size
	int cx = placement == Placement.LEFT ? (tabsize + 1) : 2;
	int cy = placement == Placement.TOP ? (tabsize + 1) : 2;
	int cwidth = bounds.width - ((horizontal || stacked) ? 4 : (tabsize + 3));
	int cheight = bounds.height - (stacked ? (tabd + 3) : (horizontal ? (tabsize + 3) : 4));
	for (final TabWidget tab : this)
	{
		Rectangle r = tab.getBounds();
		if (!stacked)
		{
			if (horizontal)
			{
				if (placement == Placement.BOTTOM) r.y = bounds.height - tabsize;
				r.height = tabsize;
			}
			else
			{
				if (placement == Placement.RIGHT) r.x = bounds.width - tabsize;
				r.width = tabsize;
			}
		}
		
		Widget comp = tab.getContent(); // relative to the tab location
		if (comp != null)
			comp.setBounds(cx - r.x, stacked ? (r.height + 1) : (cy - r.y), cwidth, cheight);
	}
	checkOffset();
	
	needsLayout = false;
}

/** Scroll tabs to make the selected one visible. */
void checkOffset()
{
//	int selected = ((TabbedPaneWidget) this).getSelected();
	int i = 0;
	if (placement == Placement.STACKED)
	{
		int dy = 0;
		for (final TabWidget tab : this)
		{
			Rectangle r = tab.getBounds();
			r.y = dy;
			dy += r.height;
			if (i == selected) dy += tab.getContent().getBounds().height + 2;
			i++;
		}
		desktop.checkLocation(this); // layout changed, check the hovered tab
		return;
	}
	
	boolean horizontal = (placement == Placement.TOP) || (placement == Placement.BOTTOM);
	Rectangle bounds = getBounds();
	int panesize = horizontal ? bounds.width : bounds.height;
	int first = 0; int last = 0; int d = 0;
	for (final TabWidget tab : this)
	{
		Rectangle r = tab.getBounds();
		if (i == 0) first = horizontal ? r.x : r.y;
		last = horizontal ? (r.x + r.width) : (r.y + r.height);
		if (i == selected)
		{
			int ifrom = (horizontal ? r.x : r.y) - 6;
			int ito = (horizontal ? (r.x + r.width) : (r.y + r.height)) + 6;
			if (ifrom < 0)
				d = -ifrom;
			else if (ito > panesize)
				d = panesize - ito;
		}
		i++;
	}
	d = Math.min(-first, Math.max(d, panesize - last));
	if (d != 0)
	{
		for (final TabWidget tab : this)
		{
			Rectangle r = tab.getBounds();
			if (horizontal)
				r.x += d;
			else
				r.y += d;
			Widget comp = tab.getContent(); // relative to the tab location
			if ((comp != null) && comp.isVisible())
			{
				Rectangle rc = comp.getBounds();
				if (horizontal)
					rc.x -= d;
				else
					rc.y -= d;
			}
		}
		desktop.checkLocation(this); // layout changed, check the hovered tab
	}
}

private TabWidget getTab(int index)
{ return getChild(index); }

@Override
public boolean checkMnemonic(Object checked, Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
//	int selected = tabbedpane.getSelected();
	int i = 0;
	for (TabWidget tab : this)
	{
		if (AbstractWidget.isAccelerator(keycode, modifiers, tab.getText(), tab.getMnemonic()))
		{
			if (selected != i)
			{
				setSelected(i);
				repaint();
				invokeAction(getTab(i));
			}
			return true;
		}
		i++;
	}
	Widget comp = getSelectedItem().getContent();
	if ((comp != null) && checkMnemonic(comp, checked, keycode, modifiers))
		return true;
	return false;
}

private void repaintComponent(TabWidget part)
{
	Rectangle b = getBounds();
	Rectangle r = part.getBounds();
	repaint(b.x + r.x, b.y + r.y, r.width, r.height);
}

public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	if ((keycode == Keys.RIGHT) || (keycode == Keys.DOWN) ||
			(keycode == Keys.LEFT) || (keycode == Keys.UP))
	{
		boolean increase = (keycode == Keys.RIGHT) || (keycode == Keys.DOWN);
		int newvalue = selected;
		int n = increase ? getChildCount() : 0;
		int d = (increase ? 1 : -1);						
		for (int i = selected + d; (i < n) && (i >= 0); i += d)
		{
			if (getTab(i).isEnabled())
			{
				newvalue = i;
				break;
			}	
		}
		if (newvalue != selected)
		{
			setSelected(newvalue);
			checkOffset();
			repaint();
			invokeAction(getTab(newvalue));
		}
		return true;
	}
	return false;
}

@Override
public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
{
//	int selected = getSelected();
	int i = 0;
	for (final TabWidget tab : this)
	{
		Rectangle r = tab.getBounds();
		if (i == selected)
		{
			Widget tabcontent = tab.getContent();
			if ((tabcontent != null) && tabcontent.findComponent(mouseInteraction, x - r.x, y - r.y))
				break;
		}
		if (r.contains(x, y))
		{
			mouseInteraction.insidepart = tab;
			break;
		}
		i++;
	}
}

private int getIndex(TabWidget value)
{
	int index = 0;
	for (TabWidget tab : this)
	{
		if (value == tab) return index;
		index++;
	}
	return -1;
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if ((id == InputEventType.MOUSE_ENTERED) ||
			(id == InputEventType.MOUSE_EXITED))
	{
		if ((part != null) && ((TabWidget) part).isEnabled() &&
				(getSelected() != getIndex((TabWidget) part)))
		{
			repaintComponent((TabWidget) part);
		}
	}
	else if ((part != null) && (id == InputEventType.MOUSE_PRESSED) &&
			((TabWidget) part).isEnabled())
	{
//		int selected = getSelected();
		int current = getIndex((TabWidget) part);
		if (selected == current)
		{
			setFocus();
			repaintComponent((TabWidget) part);
		}
		else
		{
			setSelected(current);
			desktop.setNextFocusable(this);
			checkOffset();
			repaint();
			invokeAction((TabWidget) part);
		}
	}
}

@Override
public void paint(LwjglWidgetRenderer renderer)
{
	if (needsLayout()) doLayout();
	Rectangle bounds = getBounds();
	boolean inside = isMouseInside();
	final boolean enabled = isEnabled();
	
	TabWidget selectedtab = null;
//	int selected = getSelected();
//	Placement placement = getPlacement();
	boolean horizontal = (placement == Placement.TOP) || (placement == Placement.BOTTOM);
	boolean stacked = placement == Placement.STACKED;
	int bx = stacked ? 0 : horizontal ? 2 : 1, by = stacked ? 0 : horizontal ? 1 : 2,
		bw = 2 * bx, bh = 2 * by;
		
/*	final int clipx = getClipX(), clipy = getClipY();
	final int clipwidth = getClipWidth(), clipheight = getClipHeight();
	final int nx = Math.max(0, clipx);
	final int ny = Math.max(0, clipy);
	final int nwidth = Math.min(bounds.width, clipx + clipwidth) - clipx;
	final int nheight = Math.min(bounds.height, clipy + clipheight) - clipy;*/
	
	// paint tabs except the selected one
//	pushState();
//	clip(nx, ny, nwidth, nheight);
	int i = 0; // count the tab components
	for (final TabWidget tab : this)
	{
		final Rectangle r = tab.getBounds();
		if (selected != i)
		{
			boolean hover = inside && (desktop.currentMouseInteraction.mousepressed == null) && (desktop.currentMouseInteraction.insidepart == tab);
			boolean tabenabled = enabled && tab.isEnabled();
			renderer.paintIconAndText(tab, r.x + bx, r.y + by, r.width - bw, r.height - bh,
				(placement != Placement.BOTTOM), (placement != Placement.RIGHT),
				!stacked && (placement != Placement.TOP), (placement != Placement.LEFT),
				1, 3, 1, 3, false, tabenabled ? (hover ? 'h' : 'g') : 'd', false);
		}
		else
		{
			selectedtab = tab;
			// paint tabbedpane border
			renderer.paintBorderAndBackground(tab, (placement == Placement.LEFT) ? r.width - 1 : 0,
				stacked ? (r.y + r.height - 1) : (placement == Placement.TOP) ? r.height - 1 : 0,
				(horizontal || stacked) ? bounds.width : (bounds.width - r.width + 1),
				stacked ? (bounds.height - r.y - r.height + 1) :
				horizontal ? (bounds.height - r.height + 1) : bounds.height,
				true, true, true, true, enabled ? 'e' : 'd');
			Widget comp = selectedtab.getContent();
			if ((comp != null) && comp.isVisible())
			{
				renderer.pushState();
				renderer.translate(r.x, r.y); // relative to tab
				paintChild(comp, renderer, enabled);
				renderer.popState();
			}
		}
		i++;
	}
//	popState();
	
	// paint selected tab
	if (selectedtab != null)
	{
		Rectangle r = selectedtab.getBounds();
		// paint selected tab
		int ph = stacked ? 3 : (horizontal ? 5 : 4);
		int pv = stacked ? 1 : (horizontal ? 2 : 3);
		renderer.paintIconAndText(selectedtab, r.x, r.y, r.width, r.height,
			(placement != Placement.BOTTOM), (placement != Placement.RIGHT),
			!stacked && (placement != Placement.TOP), (placement != Placement.LEFT),
			pv, ph, pv, ph, hasFocus(), enabled ? 'b' : 'i', false);
	}
}

}
