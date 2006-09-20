package thinlet;

import thinlet.api.Element;

public final class TableHeader extends AbstractDataElement<TableColumn>
{

public TableHeader()
{/*OK*/}

@Override
public TableWidget parent()
{ return (TableWidget) parentWidget; }

@Override
public boolean acceptChild(Element node)
{ return node instanceof TableColumn; }

@Override
public void remove()
{
//	update("validate");
	parent().setHeaderWidget(null);
}

}
