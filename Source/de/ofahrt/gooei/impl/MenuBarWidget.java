package de.ofahrt.gooei.impl;

import gooei.*;
import gooei.input.InputEventType;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.input.MouseEvent;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class MenuBarWidget extends MenuContainerWidget
	implements ElementContainer<MenuContainerElement<?>>, MnemonicWidget, PopupOwner
{

private List<MenuContainerElement<?>> data = new ArrayList<MenuContainerElement<?>>();
private boolean needsLayout = true;

public MenuBarWidget(Desktop desktop)
{ super(desktop); }

public MenuContainerElement<?> getChild(int index)
{ return data.get(index); }

public int getElementCount()
{ return data.size(); }

public Iterator<MenuContainerElement<?>> iterator()
{ return data.iterator(); }

/** Inserts item in child chain at specified index. Appends if index is negative. */
protected void insertItem(MenuContainerElement<?> child, int index)
{
	if ((index >= 0) && (index < data.size()))
		data.add(index, child);
	else
		data.add(child);
	child.setParent(this);
}

public void addChild(Element child, int index)
{
	if (!(child instanceof MenuContainerElement))
		throw new IllegalArgumentException("Cannot add "+child+" to "+this+"!");
	if (child.parent() != null)
		throw new IllegalArgumentException();
	insertItem((MenuContainerElement) child, index);
	child.setParent(this);
	validate();
}

public void removeChild(Element child)
{
	if (!data.remove(child))
		throw new IllegalArgumentException();
	child.setParent(null);
}

public void closePopup()
{
	setSelectedWidget(null);
	popupMenu();
	repaint();
}

MenuElement getMenu(MenuElement part, boolean forward)
{
	int index = part == null ? -1 : data.indexOf(part);
	if (forward)
	{
		for (int i = index+1; i < data.size(); i++)
			if (data.get(i).isEnabled()) return data.get(i);
		return data.get(0);
	}
	
	for (int i = index-1; i >= 0; i--)
		if (data.get(i).isEnabled()) return data.get(i);
	return data.get(data.size()-1);
}

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	Dimension result = new Dimension(0, 0);
	for (final MenuElement menu : this)
	{
		Dimension d = desktop.getSize(menu, 8, 4);
		result.width += d.width;
		result.height = Math.max(result.height, d.height);
	}
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

public boolean needsLayout()
{ return needsLayout; }

public void doLayout()
{
	Rectangle bounds = getBounds();
	int x = 0;
	for (final MenuElement menu : this)
	{
		Dimension d = desktop.getSize(menu, 8, 4);
		menu.setBounds(x, 0, d.width, bounds.height);
		x += d.width;
	}
	needsLayout = false;
}

public void validate()
{
	repaint();
	needsLayout = true;
}

public boolean checkMnemonic(Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	for (final MenuElement menu : this)
	{
		if (menu.checkMnemonic(keycode, modifiers) ||
				((modifiers == 0) && (keycode == Keys.F10)))
		{
			desktop.closePopup();
			setSelectedWidget(menu);
			popupMenu();
			repaintComponent(menu);
			return true;
		}
	}
	
	for (final MenuElement menu : this)
	{
		if (menu.checkMnemonic(keycode, modifiers))
			return true;
	}
	
	return false;
}

public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	
	// find the last open :popup and the previous one
	PopupWidgetImpl previous = null, last = null;
	for (PopupWidgetImpl i = getPopupWidget(); i != null; i = i.getPopupWidget())
	{
		previous = last;
		last = i;
	}
	
	//selected is the current item of the last, or the previous :popup, or null
	MenuElement selected = last.getSelectedWidget();
	PopupWidgetImpl hotpopup = ((selected != null) || (previous == null)) ? last : previous;
	if ((selected == null) && (previous != null))
		selected = previous.getSelectedWidget();
	
	if ((keycode == Keys.UP) || (keycode == Keys.DOWN))
	{
		MenuElement next = hotpopup.getMenu(selected, keycode == Keys.DOWN);
		if (next != null)
		{
			hotpopup.setSelectedWidget(null);
			hotpopup.popupMenu();
			hotpopup.setSelectedWidget(next);
			hotpopup.repaint();
		}
	}
	else if (keycode == Keys.LEFT)
	{
		if (previous != null)
		{ // close the last :popup
			selected = previous.getSelectedWidget();
			previous.setSelectedWidget(null);
			previous.popupMenu();
			previous.setSelectedWidget(selected);
			previous.repaint(); // , selected
		}
		else
		{ // select the previous menubar menu
			MenuElement next = getMenu(getSelectedWidget(), false);
			if (next != null)
			{
				setSelectedWidget(next);
				PopupWidgetImpl popup = popupMenu();
				popup.setSelectedWidget(popup.getMenu(null, true));
				repaint(); // , selected
			}
		}
	}
	else if (keycode == Keys.RIGHT)
	{
		if ((previous != null) && (selected == null))
		{ // ?
			last.setSelectedWidget(last.getMenuWidget().getChild(0));
			last.repaint(); // , selected
		}
		else if ((selected != null) && (selected instanceof MenuContainerElement))
		{ // expand menu
			PopupWidgetImpl popup = last.popupMenu();
			popup.setSelectedWidget(popup.getMenu(null, true));
		}
		else
		{ // select the next menubar menu
			MenuElement next = getMenu(getSelectedWidget(), true);
			if (next != null)
			{
				setSelectedWidget(next);
				PopupWidgetImpl popup = popupMenu();
				popup.setSelectedWidget(popup.getMenu(null, true));
				repaint(); // , selected
			}
		}
	}
	else if ((keycode == Keys.RETURN) || (keycode == Keys.SPACE) || (keycode == Keys.ESCAPE))
	{
		if ((keycode != Keys.ESCAPE) && (selected != null) && selected.isEnabled())
		{
			if (selected instanceof CheckBoxMenuElement)
				((CheckBoxMenuElement) selected).changeCheck();
			else
				((ActionMenuElement) selected).invokeAction();
		}
		desktop.closePopup();
	}
	else
		return false;
	return true;
}

public void findComponent(MouseInteraction mouseInteraction, int x, int y)
{
	for (final MenuElement menu : this)
	{
		Rectangle r = menu.getBounds();
		if ((x >= r.x) && (x < r.x + r.width))
		{
			mouseInteraction.insidepart = menu;
			break;
		}
	}
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	MenuElement selected = getSelectedWidget();
	if (((id == InputEventType.MOUSE_ENTERED) || (id == InputEventType.MOUSE_EXITED)) &&
			(part != null) && (selected == null) && ((Element) part).isEnabled())
	{
		repaintComponent((MenuElement) part);
	}
	else if ((part != null) && ((selected == null) ?
			(id == InputEventType.MOUSE_PRESSED) :
				((id == InputEventType.MOUSE_ENTERED) || (id == InputEventType.DRAG_ENTERED))) &&
			((MenuElement) part).isEnabled())
	{
			// || ((id == MouseEvent.MOUSE_PRESSED) && (insidepart != part))
		setSelectedWidget((MenuElement) part);
		popupMenu();
		repaintComponent((MenuElement) part);
	}
	else if ((id == InputEventType.MOUSE_PRESSED) && (selected != null))
	{
		desktop.closePopup();
	}
	else if (id == InputEventType.MOUSE_RELEASED)
	{
		if ((part != mouseInteraction.insidepart) && ((mouseInteraction.insidepart == null) ||
				((mouseInteraction.insidepart instanceof Element) && !(mouseInteraction.insidepart instanceof MenuContainerElement))))
		{
			if ((mouseInteraction.insidepart != null) && ((Element) mouseInteraction.insidepart).isEnabled())
			{
				if (mouseInteraction.insidepart instanceof CheckBoxMenuElement)
					((CheckBoxMenuElement) mouseInteraction.insidepart).changeCheck();
				else
					((ActionMenuElement) mouseInteraction.insidepart).invokeAction();
			}
			desktop.closePopup();
		}
	}
}

@Override
public void paint(LwjglRenderer renderer)
{
	if (needsLayout()) doLayout();
	Rectangle bounds = getBounds();
	MenuElement selected = getSelectedWidget();
	final int minx = renderer.getClipX();
	final int maxx = renderer.getClipX()+renderer.getClipWidth();
	final boolean enabled = isEnabled() && renderer.isEnabled();
	
	int lastx = 0;
	for (final MenuContainerElement<?> menu : this)
	{
		Rectangle mb = menu.getBounds();
		if (maxx <= mb.x) break;
		if (minx >= mb.x + mb.width) continue;
		boolean menuenabled = enabled && menu.isEnabled();
		boolean armed = (selected == menu);
		boolean hover = (selected == null) && (desktop.getMouseInteraction().insidepart == menu);
		renderer.paintIconAndText(menu, mb.x, 0, mb.width, bounds.height,
			armed, armed, true, armed, 1, 3, 1, 3, false,
			enabled ? (menuenabled ? (armed ? 's' : (hover ? 'h' : 'g')) : 'r') : 'd',
			false);
		lastx = mb.x + mb.width;
	}
	
	renderer.paintRect(lastx, 0, bounds.width-lastx, bounds.height,
		enabled ? renderer.c_border : renderer.c_disable, enabled ? renderer.c_ctrl : renderer.c_bg,
		false, false, true, false, true);
}

}
