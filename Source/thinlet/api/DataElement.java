package thinlet.api;

import java.util.Iterator;

public interface DataElement<T extends Element> extends Element, Iterable<T>
{

Iterator<T> iterator();
void addChild(Element element, int i);
void removeChild(Element element);

Element findElement(String name);

}
