package gooei.xml;

import gooei.Desktop;
import gooei.Element;
import gooei.Widget;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public abstract class AbstractWidgetFactory implements WidgetFactory
{

private HashMap<String,Class<?>> map = new HashMap<String,Class<?>>();

public AbstractWidgetFactory()
{/*OK*/}

public final void add(String name, Class<?> clazz)
{
	if (!Widget.class.isAssignableFrom(clazz) && !Element.class.isAssignableFrom(clazz))
		throw new IllegalArgumentException();
	map.put(name, clazz);
}

public final Object createWidget(Desktop desktop, String classname)
{
	try
	{
		NoSuchMethodException originale;
		Class<?> clazz = map.get(classname);
		try
		{
			Constructor<?> c = clazz.getConstructor(new Class[] { Desktop.class });
			return c.newInstance(new Object[] { desktop });
		}
		catch (NoSuchMethodException e)
		{
			originale = e;
		}
		
		try
		{
			Constructor<?> c = clazz.getConstructor(new Class[0]);
			return c.newInstance(new Object[0]);
		}
		catch (NoSuchMethodException e)
		{
			originale.printStackTrace();
			e.printStackTrace();
		}
	}
	catch (Exception e)
	{ e.printStackTrace(); }
	
	throw new RuntimeException("INVALID: "+classname);
}

}
