package thinlet.api;

public interface MenuContainerElement<T extends MenuElement> extends MenuElement, DataElement<T>
{

T getChild(int index);

}
