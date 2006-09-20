package thinlet.api;

import java.util.Iterator;

public interface ContainerWidget<T extends Widget> extends Widget, Iterable<T>
{

Iterator<T> iterator();
void addChild(Widget child, int index);
void removeChild(Widget widget);
boolean isChildFocusable(Widget child);

}
