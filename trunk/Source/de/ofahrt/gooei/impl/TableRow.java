package de.ofahrt.gooei.impl;

import gooei.Element;
import gooei.SelectableElement;

/** A table row. */
public final class TableRow extends AbstractContainerElement<TableCell> implements SelectableElement
{

private boolean selected = false;

public TableRow()
{/*OK*/}

public boolean isSelected()
{ return selected; }

public void setSelected(boolean selected)
{ this.selected = selected; }

@Override
public boolean acceptChild(Element node)
{ return node instanceof TableCell; }

}
