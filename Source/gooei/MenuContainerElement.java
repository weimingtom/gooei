package gooei;


/**
 * A {@link MenuElement} that contains children, i.e. a menu element with a
 * popup/popdown list of elements.
 */
public interface MenuContainerElement<T extends MenuElement> extends MenuElement, ElementContainer<T>
{

T getChild(int index);

}
