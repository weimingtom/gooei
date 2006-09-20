package thinlet;

import thinlet.help.SortOrder;

public final class TableColumn extends AbstractElement
{

private SortOrder sort = SortOrder.NONE;

public TableColumn()
{/*OK*/}

public SortOrder getSort()
{ return sort; }

public void setSort(SortOrder sort)
{
	if (sort == null) throw new NullPointerException();
	this.sort = sort;
}

}
