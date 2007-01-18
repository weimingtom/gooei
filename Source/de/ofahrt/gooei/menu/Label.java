package de.ofahrt.gooei.menu;

import gooei.Desktop;
import gooei.MouseInteraction;
import gooei.MouseableWidget;
import gooei.font.FontMetrics;
import gooei.input.InputEventType;
import gooei.input.MouseEvent;
import gooei.utils.Icon;
import gooei.utils.MethodInvoker;
import gooei.utils.PreparedIcon;

import java.awt.Dimension;

import de.ofahrt.gooei.impl.AbstractWidget;
import de.ofahrt.gooei.lwjgl.GLColor;
import de.ofahrt.gooei.lwjgl.LwjglRenderer;

/**
 * A label displays either centered text or a centered icon (but not both).
 * It can be highlighted, and has an onclick handler.
 */
public class Label extends AbstractWidget implements MouseableWidget
{

private boolean highlight = false;
private PreparedIcon icon;
private String text;
private MethodInvoker onClickMethod;

public Label(Desktop desktop)
{ super(desktop); }

public void setHighlight(boolean highlight)
{ this.highlight = highlight; }

public void setIcon(Icon icon)
{ this.icon = desktop.prepareIcon(icon); }

public void setText(String text)
{ this.text = text; }

public void setOnClick(MethodInvoker onClickMethod)
{ this.onClickMethod = onClickMethod; }

@Override
public Dimension getPreferredSize()
{
	Dimension result;
	if (icon != null)
		result = new Dimension(icon.getWidth(), icon.getHeight());
	else if (text != null)
	{
		FontMetrics metrics = desktop.getFontMetrics(desktop.getDefaultFont());
		result = new Dimension(metrics.stringWidth(text), metrics.getHeight());
	}
	else
		result = new Dimension(1, 1);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.width = getHeight();
	return result;
}

@Override
public void paint(LwjglRenderer renderer)
{
	if (isMouseInside() && highlight)
		renderer.setColor(GLColor.RED);
	else
		renderer.setColor(GLColor.WHITE);
	if (icon != null)
	{
		int x = (getBounds().width-icon.getWidth())/2;
		renderer.drawImage(icon, x, 0);
	}
	else if (text != null)
	{
		FontMetrics metrics = renderer.getFontMetrics();
		int x = (getBounds().width-metrics.stringWidth(text))/2;
		renderer.drawString(text, x, metrics.getAscent());
	}
}

public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if ((id == InputEventType.MOUSE_RELEASED) &&
			(mouseInteraction.mouseinside == this))
	{
		invokeIt(onClickMethod, null);
	}
}

}
