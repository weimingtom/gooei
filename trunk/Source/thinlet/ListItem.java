package thinlet;

/** A list item. */
public final class ListItem extends AbstractElement implements SelectableWidget
{

private boolean selected = false;

public ListItem()
{/*OK*/}

public boolean isSelected()
{ return selected; }

public void setSelected(boolean selected)
{ this.selected = selected; }

}
