package thinlet;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.api.MenuContainerElement;
import thinlet.api.MenuElement;
import thinlet.lwjgl.LwjglWidgetRenderer;

public final class CheckBoxMenuItemWidget extends MenuItemWidget
{

private boolean selected = false;
private String group = null;

public CheckBoxMenuItemWidget()
{/*OK*/}

public boolean isSelected()
{ return selected; }

public void setSelected(boolean selected)
{ this.selected = selected; }

public String getGroup()
{ return group; }

public void setGroup(String group)
{ this.group = group; }

boolean changeCheck()
{
	if ((group != null) && (parent() instanceof MenuContainerElement))
	{
		if (isSelected()) return false;
		for (final MenuElement comp : (MenuContainerElement<?>) parent())
		{
			if (comp == this)
				setSelected(true);
			else if (comp instanceof CheckBoxMenuItemWidget)
			{
				CheckBoxMenuItemWidget partner = (CheckBoxMenuItemWidget) comp;
				if (group.equals(partner.getGroup()) && partner.isSelected())
				{
					partner.setSelected(false);
//					if (box) partner.repaint(); //checkbox only
				}
			}
		}
	}
	else
		setSelected(!isSelected());
	super.invokeAction();
	return true;
}

@Override
public Dimension getSize(ThinletDesktop desktop, int dx, int dy)
{
	Dimension result = super.getSize(desktop, dx, dy);
	int block = desktop.getBlockSize();
	result.width = result.width + block + 3;
	result.height = Math.max(block, result.height);
	return result;
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean armed)
{
	Rectangle r = getBounds();
	int block = renderer.getBlockSize();
	renderer.paintIconAndText(this, r.x, r.y, r.width, r.height,
		false, false, false, false,
		2, block + 7, 2, 4, false,
		isEnabled() ? (armed ? 's' : 't') : 'd', false);
	
	boolean checked = isSelected();
	boolean menuenabled = isEnabled();
	renderer.pushState();
	renderer.translate(r.x + 4, r.y + 2);
	renderer.setColor(menuenabled ? renderer.c_border : renderer.c_disable);
	if (getGroup() == null)
	{
		renderer.drawRect(1, 1, block - 3, block - 3);
		if (checked)
		{
			renderer.setColor(menuenabled ? renderer.c_text : renderer.c_disable);
			renderer.fillRect(3, block - 9, 2, 6);
			renderer.drawLine(3, block - 4, block - 4, 3);
			renderer.drawLine(4, block - 4, block - 4, 4);
		}
	}
	else
	{
		renderer.drawOval(1, 1, block - 3, block - 3);
		if (checked)
		{
			renderer.setColor(menuenabled ? renderer.c_text : renderer.c_disable);
			renderer.fillOval(5, 5, block - 10, block - 10);
			renderer.drawOval(4, 4, block - 9, block - 9);
		}
	}
	renderer.popState();
	
	String accelerator = getAccelerator();
	if (accelerator != null)
	{
		renderer.drawString(accelerator, r.width - 2 -
			renderer.getFontMetrics().stringWidth(accelerator), r.y + 2 + 10);
	}
}

}
