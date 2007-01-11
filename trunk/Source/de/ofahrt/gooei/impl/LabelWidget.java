package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.IconAndText;
import gooei.Mnemonic;
import gooei.MnemonicWidget;
import gooei.input.Keys;
import gooei.utils.Alignment;
import gooei.utils.Icon;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public class LabelWidget extends AbstractWidget implements Mnemonic, MnemonicWidget, IconAndText
{

private int mnemonic = -1;
private String text;
private Icon icon;
private Alignment alignment = Alignment.LEFT;

private AbstractWidget forWidget = null;

public LabelWidget(Desktop desktop)
{ super(desktop); }

public int getMnemonic()
{ return mnemonic; }

public void setMnemonic(int mnemonic)
{ this.mnemonic = mnemonic; }

public String getText()
{ return text; }

public void setText(String text)
{
	this.text = text;
	if (parent() != null) parent().validate();
}

public Icon getIcon()
{ return icon; }

public void setIcon(Icon icon)
{
	this.icon = icon;
	if (parent() != null) parent().validate();
}

public Alignment getAlignment()
{ return alignment; }

public void setAlignment(Alignment alignment)
{
	if (alignment == null) throw new NullPointerException();
	this.alignment = alignment;
	repaint();
}

public AbstractWidget getForWidget()
{ return forWidget; }

public void setForWidget(AbstractWidget forWidget)
{ this.forWidget = forWidget; }

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	
	Dimension result = desktop.getSize(this, 0, 0);
	if (getWidth() > 0) result.width = getWidth();
	if (getHeight() > 0) result.height = getHeight();
	return result;
}

public boolean checkMnemonic(Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	if (isAccelerator(keycode, modifiers, getText(), getMnemonic()))
	{
		AbstractWidget labelfor = getForWidget();
		if (labelfor != null)
		{
			labelfor.requestFocus();
			return true;
		}
	}
	return false;
}

@Override
public void paint(LwjglRenderer renderer)
{
	Rectangle bounds = getBounds();
	final boolean enabled = isEnabled() && renderer.isEnabled();
	renderer.paintIconAndText(this, 0, 0, bounds.width, bounds.height,
		false, false, false, false,
		0, 0, 0, 0, false, enabled ? 'e' : 'd', false);
}

}
