package thinlet.api;

import java.util.Iterator;

public interface DataWidget<T extends Element> extends Widget, Iterable<T>
{

int getElementCount();
Iterator<T> iterator();
void addChild(Element child, int index);
void removeChild(Element element);

}
