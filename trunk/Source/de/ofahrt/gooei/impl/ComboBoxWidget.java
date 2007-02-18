package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.Element;
import gooei.ElementContainer;
import gooei.IconAndText;
import gooei.MouseInteraction;
import gooei.MouseRouterWidget;
import gooei.PopupOwner;
import gooei.ScrollableWidget;
import gooei.Widget;
import gooei.font.Font;
import gooei.font.FontMetrics;
import gooei.input.InputEventType;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.input.MouseEvent;
import gooei.utils.Icon;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

// FIXME: move ElementContainer functionality to ComboListWidget
public final class ComboBoxWidget extends TextFieldWidget
	implements MouseRouterWidget, ElementContainer<ComboBoxItem>, IconAndText, PopupOwner
{

private int selected = -1;
private Icon icon;

private ComboListWidget combolistWidget = null;
private List<ComboBoxItem> data = new ArrayList<ComboBoxItem>();

private long findtime = 0;
private String findtext = "";

public ComboBoxWidget(Desktop desktop)
{ super(desktop); }

public int getSelected()
{ return selected; }

public void setSelected(int selected)
{ this.selected = selected; }

public Icon getIcon()
{ return icon; }

public void setIcon(Icon icon)
{ this.icon = icon; }

public ComboListWidget getComboListWidget()
{ return combolistWidget; }

public void setComboListWidget(ComboListWidget combolistWidget)
{ this.combolistWidget = combolistWidget; }


public ComboBoxItem getChild(int index)
{ return data.get(index); }

public int getElementCount()
{ return data.size(); }

public int getElementIndex(Element value)
{ return data.indexOf(value); }

public Iterator<ComboBoxItem> iterator()
{ return data.iterator(); }

/** Inserts item in child chain at specified index. Appends if index is negative. */
protected void insertItem(ComboBoxItem child, int index)
{
	if ((index >= 0) && (index < data.size()))
		data.add(index, child);
	else
		data.add(child);
	child.setParent(this);
}

public void addChild(Element child, int index)
{
	if (!acceptChild(child))
		throw new IllegalArgumentException("Cannot add "+child+" to "+this+"!");
	if (child.parent() != null)
		throw new IllegalArgumentException();
	insertItem((ComboBoxItem) child, index);
	child.setParent(this);
	if (combolistWidget != null)
		combolistWidget.repaint();
}

public void removeChild(Element child)
{
	if (!data.remove(child))
		throw new IllegalArgumentException();
	child.setParent(null);
}


public ComboBoxItem getSelectedItem()
{
	int index = getSelected();
	return (index != -1) ? getChild(index) : null;
}

public boolean acceptChild(Element node)
{ return node instanceof ComboBoxItem; }

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	Dimension result;
	if (isEditable())
	{
		result = super.getFieldSize();
		if (icon != null)
		{
			result.width += icon.getWidth();
			result.height = Math.max(result.height, icon.getHeight() + 2);
		}
		result.width += desktop.getBlockSize();
	}
	else
	{
		// maximum size of current values and choices including 2-2-2-2 insets
		result = desktop.getSize(this, 4 , 4);
		for (final ComboBoxItem item : this)
		{
			Dimension d = desktop.getSize(item, 4 , 4);
			result.width = Math.max(d.width, result.width);
			result.height = Math.max(d.height, result.height);
		}
		result.width += desktop.getBlockSize();
		if (result.height == 4)
		{ // no content nor items, set text height
			Font customfont = getFont(desktop.getDefaultFont());
			FontMetrics fm = desktop.getFontMetrics(customfont);
			result.height = fm.getAscent() + fm.getDescent() + 4;
		}
	}
	
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

@Override
public void doLayout()
{
	if (isEditable()) // set editable -> validate (overwrite textfield repaint)
		layoutField(desktop.getBlockSize(), (icon != null) ? icon.getWidth() : 0);
	else
	{
		if (selected != -1)
		{
			ComboBoxItem choice = getChild(selected);
			setText(choice.getText());
			setIcon(choice.getIcon());
		}
	}
}

public void validate()
{/*OK*/}

/**
 * Pop up the list of choices for the given combobox
 * @param combobox
 * @return the created combolist
 */
ComboListWidget popupCombo()
{
	// combobox bounds relative to the root desktop
	int combox = 0, comboy = 0, combowidth = 0, comboheight = 0;
	for (Widget comp = this; comp != null; comp = comp.parent())
	{
		Rectangle r = comp.getBounds();
		combox += r.x;
		comboy += r.y;
		if (comp instanceof ScrollableWidget)
		{
			Rectangle view = ((ScrollableWidget) comp).getView();
			if (view != null)
			{
				combox -= view.x;
				comboy -= view.y;
				Rectangle port = ((ScrollableWidget) comp).getPort();
				combox += port.x;
				comboy+= port.y;
			}
		}
		if (comp == this)
		{
			combowidth = r.width;
			comboheight = r.height;
		}
	}
	
	// :combolist -> combobox and combobox -> :combolist 
	ComboListWidget combolist = new ComboListWidget(desktop);
	combolist.setComboBoxWidget(this);
	setComboListWidget(combolist);
	
	// add :combolist to the root desktop and set the combobox as popupowner
	desktop.setPopup(combolist, this);
	// lay out choices verticaly and calculate max width and height sum
	int pw = 0; int ph = 0;
	for (final ComboBoxItem item : this)
	{
		Dimension d = desktop.getSize(item, 8 , 4);
		item.setBounds(0, ph, d.width, d.height);
		pw = Math.max(pw, d.width);
		ph += d.height;
	}
	// set :combolist bounds
	int listy = 0, listheight = 0;
	int bellow = desktop.getBounds().height - comboy - comboheight - 1;
	if ((ph + 2 > bellow) && (comboy - 1 > bellow))
	{ // popup above combobox
		listy = Math.max(0, comboy - 1 - ph - 2);
		listheight = Math.min(comboy - 1, ph + 2);
	}
	else
	{ // popup bellow combobox
		listy = comboy + comboheight + 1;
		listheight = Math.min(bellow, ph + 2);
	}
	combolist.setBounds(combox, listy, combowidth, listheight);
	combolist.layoutScroll(pw, ph, 0, 0, 0, 0, true, 0);
	combolist.repaint();
	// hover the selected item
	combolist.setInside((selected != -1) ? getChild(selected) : null, true);
	return combolist;
}

void closeCombo(ComboListWidget combolist, ComboBoxItem item)
{
	if ((item != null) && item.isEnabled())
	{
		ComboBoxItem choice = item;
		String text = choice.getText();
		setText(text); // if editable
		setStart(text.length());
		setEnd(0);
		setIcon(choice.getIcon());
		setSelected(getElementIndex(item));
		invokeAction(item);
	}
	combolist.setComboBoxWidget(null);
	setComboListWidget(null);
	desktop.removeChild(combolist);
	combolist.repaint();
	combolist.setParent(null);
	desktop.setPopupOwner(null);
	desktop.checkLocation();
}

public void closePopup()
{ closeCombo(getComboListWidget(), null); }

private ComboBoxItem getListItem(ComboListWidget scrollpane, Keys keycode, ComboBoxItem lead)
{
	ComboBoxItem result = null;
	if (keycode == Keys.UP)
	{
		for (final ComboBoxItem prev : this)
		{
			if (prev == lead) break;
			result = prev;
		}
	}
	else if (keycode == Keys.DOWN)
	{
		int index = lead == null ? -1 : data.indexOf(lead);
		result = getChild(index+1);
	}
	else if ((keycode == Keys.PRIOR) ||
			(keycode == Keys.NEXT))
	{
		Rectangle view = scrollpane.getView();
		Rectangle port = scrollpane.getPort();
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
		for (final ComboBoxItem item : this)
		{
			Rectangle r = item.getBounds();
			if (keycode == Keys.PRIOR)
			{
				result = item;
				if (r.y + r.height > vy) break;
			}
			else
			{
				if (r.y > vy) break;
				result = item;
			}
		}
	}
	else if (keycode == Keys.HOME)
	{
		result = getChild(0);
	}
	else if (keycode == Keys.END)
	{
		result = getChild(data.size()-1);
	}
	return result;
}

private ComboBoxItem findText(char keychar, ComboListWidget leadowner)
{
	if (keychar != 0)
	{
		long current = System.currentTimeMillis();
		int i = (current > findtime + 1000) ? 1 : 0; // clear the starting string after a second
		findtime = current;
		ComboBoxItem lead = leadowner.getLeadWidget();
		for (; i < 2; i++)
		{ // 0: find the long text, 1: the stating character only
			findtext = (i == 0) ? (findtext + keychar) : String.valueOf(keychar);
			ComboBoxItem block = lead;
			for (ComboBoxItem item : this)
			{
				if (block != null)
				{
					if (item == block) block = null;
					continue;
				}
				if (item.getText().regionMatches(true, 0, findtext, 0, findtext.length()))
					return item;
			}
			for (ComboBoxItem item : this)
			{
				if (item == lead) break;
				if (item.getText().regionMatches(true, 0, findtext, 0, findtext.length()))
					return item;
			}
		}
	}
	return null;
}

@Override
public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	
	ComboListWidget combolist = getComboListWidget();
	if (combolist == null)
	{ // the drop down list is not visible
		boolean editable = isEditable();
		if (editable && super.handleKeyPress(event))
		{
			setSelected(-1);
			return true;
		}
		if ((keycode == Keys.SPACE) || (keycode == Keys.DOWN))
			popupCombo();
		else //+findText
			return false;
	}
	else
	{
		if ((keycode == Keys.UP) ||
				(keycode == Keys.DOWN) || (keycode == Keys.PRIOR) ||
				(keycode == Keys.NEXT) ||
				(keycode == Keys.HOME) || (keycode == Keys.END))
		{
			ComboBoxItem next = getListItem(combolist, keycode, combolist.getLeadWidget());
			if (next != null)
				combolist.setInside(next, true);
		}
		else if ((keycode == Keys.RETURN) || (keycode == Keys.SPACE))
			closeCombo(combolist, combolist.getLeadWidget());
		else if (keycode == Keys.ESCAPE)
			closeCombo(combolist, null);
		else if (!super.handleKeyPress(event))
		{
			ComboBoxItem item = findText(event.getKeyChar(), combolist);
			if (item != null)
				combolist.setInside(item, true);
			else
				return false;
		}
	}
	return true;
}

public void findComponent(MouseInteraction mouseInteraction, int x, int y)
{
	Rectangle bounds = getBounds();
	int block = desktop.getBlockSize();
	if (isEditable() && (x <= bounds.width - block))
	{
//			Icon icon = combobox.getIcon();
		mouseInteraction.insidepart = ((icon != null) && (x <= 2 + icon.getWidth())) ?
			"icon" : null;
	}
	else
		mouseInteraction.insidepart = "down";
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	boolean editable = isEditable();
	if (editable && (part == null))
	{ // textfield area
//			Icon icon = getIcon();
		int left = (id == InputEventType.MOUSE_PRESSED) && (icon != null) ? icon.getWidth() : 0;
		processField(mouseInteraction, event, left);
	}
	else if (part != "icon")
	{ // part = "down"
		if (((id == InputEventType.MOUSE_ENTERED) ||
				(id == InputEventType.MOUSE_EXITED)) && (mouseInteraction.mousepressed == null))
		{
			if (editable) repaint(); // hover the arrow button
			else repaint(); // hover the whole combobox
		}
		else if (id == InputEventType.MOUSE_PRESSED)
		{
			ComboListWidget combolist = getComboListWidget();
			if (combolist == null)
			{ // combolist is closed
				setFocus();
				repaint();
				popupCombo();
			}
			else
			{ // combolist is visible
				closeCombo(combolist, null);
			}
		}
		else if (id == InputEventType.MOUSE_RELEASED)
		{
			if (mouseInteraction.mouseinside != this)
			{
				ComboListWidget combolist = getComboListWidget();
				closeCombo(combolist,
					((mouseInteraction.mouseinside == combolist) &&
							(mouseInteraction.insidepart instanceof ComboBoxItem)) ?
							(ComboBoxItem) mouseInteraction.insidepart : null);
			}
			else
				repaint();
		}
	}
}

@Override
public void paint(LwjglRenderer renderer)
{
	doLayout();
	Rectangle bounds = getBounds();
	final boolean pressed = isMousePressed();
	final boolean inside = isMouseInside();
	final boolean enabled = isEnabled() && renderer.isEnabled();
	int block = desktop.getBlockSize();
	
	if (isEditable())
	{
//		Icon icon = getIcon();
		int left = (icon != null) ? icon.getWidth() : 0;
		paintField(renderer, bounds.width - block, bounds.height, enabled, left);
		if (icon != null)
		{
			renderer.drawImage(icon, 2, (bounds.height - icon.getHeight()) / 2);
		}
		renderer.paintArrow(bounds.width - block, 0, block, bounds.height,
			'S', enabled, inside, pressed, "down", true, false, true, true, true);
	}
	else
	{
		renderer.paintIconAndText(this, 0, 0, bounds.width, bounds.height,
			true, true, true, true, 1, 1, 1, 1 + block, hasFocus(),
			enabled ? ((inside != pressed) ? 'h' : (pressed ? 'p' : 'g')) : 'd',
			false);
		renderer.setColor(enabled ? renderer.c_text : renderer.c_disable);
		renderer.paintArrow(bounds.width - block, 0, block, bounds.height, 'S');
	}
}

}
