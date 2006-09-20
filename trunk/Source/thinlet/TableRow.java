package thinlet;

import thinlet.api.Element;

/** A table row. */
public final class TableRow extends AbstractDataElement<TableCell> implements SelectableWidget
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
