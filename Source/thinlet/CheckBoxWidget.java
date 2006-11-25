package thinlet;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.api.FocusableWidget;
import thinlet.api.Widget;
import thinlet.help.MethodInvoker;
import thinlet.help.MouseInteraction;
import thinlet.lwjgl.GLColor;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.Keys;
import de.ofahrt.utils.input.MouseEvent;

public class CheckBoxWidget extends LabelWidget implements FocusableWidget
{

private boolean selected = false;
private String group = null;
private MethodInvoker actionMethod;

public CheckBoxWidget(ThinletDesktop desktop)
{ super(desktop); }

public boolean isSelected()
{ return selected; }

public void setSelected(boolean selected)
{ this.selected = selected; }

public String getGroup()
{ return group; }

public void setGroup(String group)
{ this.group = group; }

public void setAction(MethodInvoker method)
{ actionMethod = method; }

public boolean invokeAction()
{ return invokeIt(actionMethod, null); }

@Override
public Dimension getPreferredSize()
{
	int block = desktop.getBlockSize();
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	Dimension d = getSize(0, 0);
	d.width = d.width + block + 3;
	d.height = Math.max(block, d.height);
	return d;
}

boolean changeCheck(boolean box)
{
	if (group != null)
	{
		if (isSelected()) return false;
		for (Widget comp : parent())
		{
			if (comp == this)
				setSelected(true);
			else if (comp instanceof CheckBoxWidget)
			{
				CheckBoxWidget partner = (CheckBoxWidget) comp;
				if (group.equals(partner.getGroup()) && partner.isSelected())
				{
					partner.setSelected(false);
					if (box) partner.repaint(); //checkbox only
				}
			}
		}
	}
	else
		setSelected(!isSelected());
	invokeAction();
	return true;
}

@Override
public boolean checkMnemonic(Object checked, Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	if (hasMnemonic(keycode, modifiers))
	{
		changeCheck(true);
		repaint();
		return true;
	}
	return false;
}

public boolean handleKeyPress(KeyboardEvent event)
{
	if (event.getKeyCode() == Keys.SPACE)
	{
		changeCheck(true);
		repaint();
		return true;
	}
	return false;
}

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if ((id == InputEventType.MOUSE_ENTERED) || (id == InputEventType.MOUSE_EXITED) ||
			(id == InputEventType.MOUSE_PRESSED) || (id == InputEventType.MOUSE_RELEASED))
	{
		if (id == InputEventType.MOUSE_PRESSED)
			setFocus();
		if ((id == InputEventType.MOUSE_RELEASED) && (mouseInteraction.mouseinside == this))
			changeCheck(true);
		repaint();
	}
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{
	Rectangle bounds = getBounds();
	boolean pressed = isMousePressed();
	boolean inside = isMouseInside();
	int block = desktop.getBlockSize();
	
	renderer.paintIconAndText(this, 0, 0, bounds.width, bounds.height,
			false, false, false, false,
			0, block + 3, 0, 0, false, enabled ? 'e' : 'd', false);
	
//	boolean selected = checkbox.isSelected();
//	String group = checkbox.getGroup();
	GLColor border = enabled ? renderer.c_border : renderer.c_disable;
	GLColor foreground = enabled ? ((inside != pressed) ? renderer.c_hover :
		(pressed ? renderer.c_press : renderer.c_ctrl)) : renderer.c_bg;
	int dy = (bounds.height - block + 2) / 2;
	
	boolean checked = (!selected && inside && pressed) ||
			(selected && (!inside || !pressed));
	if (group == null)
	{
		GLColor check = enabled ? renderer.c_text : renderer.c_disable;
		renderer.paintCheckbox(1, dy+1, checked, border, foreground, check);
	}
	else
	{
		renderer.setColor((foreground != renderer.c_ctrl) ? foreground : renderer.c_bg);
		renderer.fillOval(1, dy + 1, block - 3, block - 3);
		renderer.setColor(border);
		renderer.drawOval(1, dy + 1, block - 3, block - 3);
		if (checked)
		{
			renderer.setColor(enabled ? renderer.c_text : renderer.c_disable);
			renderer.fillOval(5, dy + 5, block - 10, block - 10);
			renderer.drawOval(4, dy + 4, block - 9, block - 9);
		}
	}
	
	if (hasFocus())
		renderer.drawFocus(0, 0, bounds.width - 1, bounds.height - 1);
}

}
