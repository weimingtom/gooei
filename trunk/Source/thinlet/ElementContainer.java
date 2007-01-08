package thinlet;

import java.util.Iterator;

public interface ElementContainer<T extends Element> extends Iterable<T>
{

int getElementCount();
Iterator<T> iterator();
void addChild(Element child, int index);
void removeChild(Element element);
void validate();

}
