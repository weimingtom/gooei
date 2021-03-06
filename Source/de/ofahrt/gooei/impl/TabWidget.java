package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.IconAndText;
import gooei.Widget;
import gooei.utils.Alignment;
import gooei.utils.Icon;

import java.awt.Dimension;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class TabWidget extends AbstractContainerWidget<Widget> implements IconAndText
{

private String text = null;
private Icon icon = null;
private Alignment alignment = Alignment.LEFT;
private int mnemonic = -1;

public TabWidget(Desktop desktop)
{ super(desktop); }

public Widget getContent()
{ return getChild(0); }

public String getText()
{ return text; }

public void setText(String text)
{ this.text = text; }

public Icon getIcon()
{ return icon; }

public void setIcon(Icon icon)
{ this.icon = icon; }

public Alignment getAlignment()
{ return alignment; }

public void setAlignment(Alignment alignment)
{
	if (alignment == null) throw new NullPointerException();
	this.alignment = alignment;
}

public int getMnemonic()
{ return mnemonic; }

public void setMnemonic(int mnemonic)
{ this.mnemonic = mnemonic; }

public Dimension getSize(int dx, int dy)
{ return desktop.getSize(this, dx, dy); }

@Override
public boolean acceptChild(Widget child)
{ return !(child instanceof DialogWidget); }

@Override
public void doLayout()
{
	needsLayout = false;
}

@Override
public void paint(LwjglRenderer renderer)
{ throw new UnsupportedOperationException(); }

}
