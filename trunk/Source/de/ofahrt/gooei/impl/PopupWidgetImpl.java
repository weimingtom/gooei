package de.ofahrt.gooei.impl;

import gooei.*;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class PopupWidgetImpl extends MenuContainerWidget implements PopupWidget, ModalWidget
{

private boolean modal = false;

private MenuContainerElement<?> menuWidget;

PopupWidgetImpl(Desktop desktop, MenuContainerElement<?> menuWidget)
{
	super(desktop);
	this.menuWidget = menuWidget;
}

public boolean isModal()
{ return modal; }

public void setModal(boolean modal)
{ this.modal = modal; }

public MenuContainerElement<?> getMenuWidget()
{ return menuWidget; }

public void setMenuWidget(MenuContainerElement<?> menuWidget)
{ this.menuWidget = menuWidget; }

public MenuElement getMenu(MenuElement part, boolean forward)
{
	if (forward)
	{
		for (MenuElement item : getMenuWidget())
		{
			if (part != null)
			{
				if (item == part) part = null;
				continue;
			}
			if (item.isEnabled())
				return item;
		}
		for (MenuElement item : getMenuWidget())
			if (item.isEnabled()) return item;
		return null;
	}
	
	MenuElement previous = null;
	for (MenuElement item : getMenuWidget())
	{
		if ((item == part) && (previous != null))
			break;
		if (item.isEnabled())
			previous = item;
	}
	return previous;
}

@Override
public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
{
	for (final MenuElement menu : getMenuWidget())
	{
		Rectangle r = menu.getBounds();
		if ((y >= r.y) && (y < r.y + r.height))
		{
			mouseInteraction.insidepart = menu;
			break;
		}
	}
}

private Dimension getInternalSize(int dx, int dy)
{
	int pw = 0;
	int ph = 0;
	for (final MenuElement item : menuWidget)
	{
		Dimension d = item.getSize(desktop, 8, 4);
		item.setBounds(1, 1 + ph, d.width, d.height);
		pw = Math.max(pw, d.width);
		ph += d.height;
	}
	for (final MenuElement item : menuWidget)
	{
		Rectangle bounds = item.getBounds();
		item.setBounds(bounds.x, bounds.y, pw, bounds.height);
	}
	return new Dimension(pw+dx, ph+dy);
}

// Layout
public void popup(char direction, int x, int y, int width, int height, int offset)
{
	// determine size
	Dimension size = getInternalSize(2, 2);
	
	// calculate and set bounds
	Rectangle dbounds = desktop.getBounds();
	if (direction == 'R')
	{
		x += ((x + width - offset + size.width > dbounds.width) &&
			(x >= size.width - offset)) ? (offset - size.width) : (width - offset);
		if ((y + size.height > dbounds.height) && (size.height <= y + height))
			y -= size.height - height;
	}
	else
	{
		boolean topspace = (y >= size.height - offset); // sufficient space above
		boolean bottomspace = (dbounds.height - y - height >= size.height - offset);
		y += ((direction == 'U') ? (topspace || !bottomspace) :
			(!bottomspace && topspace)) ? (offset - size.height) : (height - offset);
	}
	setBounds(Math.max(0, Math.min(x, dbounds.width - size.width)),
		Math.max(0, Math.min(y, dbounds.height - size.height)), size.width, size.height);
	
	// repaint
	repaint();
}

@Override
public void paint(LwjglRenderer renderer)
{
	Rectangle bounds = getBounds();
	renderer.paintRect(0, 0, bounds.width, bounds.height, renderer.c_border, renderer.c_textbg,
			true, true, true, true, true);
	
	// used to only render the menu elements within the clip
	final int miny = renderer.getClipY();
	final int maxy = renderer.getClipY()+renderer.getClipHeight();
	
	MenuElement selected = getSelectedWidget();
	for (final MenuElement menu : getMenuWidget())
	{
		Rectangle r = menu.getBounds();
		if (maxy <= r.y) break;
		if (miny >= r.y + r.height) continue;
		menu.paint(renderer, selected == menu);
	}
}

}
