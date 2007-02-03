package de.ofahrt.gooei.menu;

import gooei.Desktop;
import gooei.Widget;
import gooei.utils.Icon;
import gooei.utils.PreparedIcon;

import java.awt.Dimension;

import de.ofahrt.gooei.lwjgl.GLColor;
import de.ofahrt.gooei.lwjgl.LwjglRenderer;

/**
 * A panel contains absolutely positioned subwidgets.
 * Use x,y on contained widgets to set their absolute positions.
 * The panel does not contain a {@link #getPreferredSize()} implementation,
 * you must either set both width and height, or only use it in a container
 * that ignores the preferred size (such as {@link Stack}).
 */
public class Panel extends AbstractContainerWidget<Widget>
{

	private static class AbsoluteConstraint
	{
		private int x = 0;
		private int y = 0;
		
		public int getX()
		{ return x; }
		public void setX(int x)
		{ this.x = x; }
		
		public int getY()
		{ return y; }
		public void setY(int y)
		{ this.y = y; }
	}

private PreparedIcon backgroundImage;

public Panel(Desktop desktop)
{ super(desktop); }

@Override
protected AbsoluteConstraint getConstraintFor(Widget child)
{ return (AbsoluteConstraint) super.getConstraintFor(child); }

public void setX(Widget child, int x)
{ getConstraintFor(child).setX(x); }

public void setY(Widget child, int y)
{ getConstraintFor(child).setY(y); }

public void setBackground(Icon icon)
{ this.backgroundImage = desktop.prepareIcon(icon); }

@Override
protected Object createConstraints(Widget child)
{ return new AbsoluteConstraint(); }

@Override
public boolean acceptChild(Widget child)
{ return true; }

@Override
public void doLayout()
{
	int index = 0;
	for (final Widget comp : this)
	{
		AbsoluteConstraint constraint = (AbsoluteConstraint) getConstraint(index);
		index++;
		if (!comp.isVisible()) continue;
		Dimension size = comp.getPreferredSize();
		comp.setBounds(constraint.getX(), constraint.getY(), size.width, size.height);
	}
	needsLayout = false;
}

@Override
public void paint(LwjglRenderer renderer)
{
	if (needsLayout()) doLayout();
	if (backgroundImage != null)
	{
		renderer.setColor(GLColor.WHITE);
		renderer.drawImage(backgroundImage, 0, 0);
	}
	paintAll(renderer);
}

}
