package gooei.xml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.StringTokenizer;

import gooei.ContainerWidget;
import gooei.Desktop;
import gooei.Element;
import gooei.ElementContainer;
import gooei.PopupMenuWidget;
import gooei.UIController;
import gooei.Widget;
import gooei.font.Font;
import gooei.font.FontRegistry;
import gooei.utils.Icon;
import gooei.utils.MethodInvoker;
import gooei.utils.TLColor;
import gooei.utils.WidgetHelper;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import de.ofahrt.gooei.impl.ComboBoxItem;
import de.ofahrt.gooei.impl.ComboBoxWidget;
import de.ofahrt.gooei.impl.ListItem;
import de.ofahrt.gooei.impl.ListWidget;
import de.ofahrt.gooei.impl.PopupMenuElementImpl;
import de.ofahrt.gooei.impl.TabWidget;
import de.ofahrt.gooei.impl.TabbedPaneWidget;
import de.ofahrt.gooei.impl.TableHeader;
import de.ofahrt.gooei.impl.TableWidget;
import de.ofahrt.gooei.impl.TreeNode;
import de.ofahrt.gooei.impl.TreeWidget;

public class NewContentHandler implements ContentHandler
{

	static class MethodFixup
	{
		public final Object w;
		public final String type;
		public final String key;
		public final String value;
		public final Method m;
		public MethodFixup(Object w, String type, String key, String value, Method m)
		{
			this.w = w;
			this.type = type;
			this.key = key;
			this.value = value;
			this.m = m;
		}
	}

private final Desktop desktop;
private final UIController controller;
private final WidgetFactory factory;
private final ResourceBundle bundle;
private final ArrayList<MethodFixup> methodFixups = new ArrayList<MethodFixup>();
private final FontRegistry fontRegistry;

private Widget root;
private Stack<Object> widgetStack = new Stack<Object>();
private Object current;

public NewContentHandler(Desktop desktop, UIController container,
		WidgetFactory factory, ResourceBundle bundle)
{
	this.desktop = desktop;
	this.controller = container;
	this.factory = factory;
	this.bundle = bundle;
	this.fontRegistry = desktop.getFontRegistry();
}

public Widget getRoot()
{ return root; }


// private support code
private Method findMethod(Class<?> c, String mname)
{
	if (c == null) return null;
	Method[] ms = c.getDeclaredMethods();
	for (Method m : ms)
	{
		if (mname.equalsIgnoreCase(m.getName()))
			return m;
	}
	return findMethod(c.getSuperclass(), mname);
}

/**
 * @return an object list including as follows:
 * - handler object,
 * - method,
 * - list of parameters including 3 values:
 * - ("thinlet", null, null) for the single thinlet component,
 * - (target component, null, null) for named widget as parameter, e.g. mybutton,
 * - (target, parameter name, default value) for a widget's given property, e.g. mylabel.enabled,
 * - ("item", null, null) for an item of the target component as parameter, e.g. tree node,
 * - ("item", parameter name, default value) for the item's given property e.g. list item's text,
 * - ("constant", string object, null) for constant number
 * (int, long, double, float) or string given as 'text'.
 */
private MethodInvoker parseMethod(Object thisWidget, String value)
{
	StringTokenizer st = new StringTokenizer(value, "(, \r\n\t)");
	String methodname = st.nextToken();
	int n = st.countTokens();
	Object[] result = new Object[2 + 3 * n];
	Class[] parametertypes = (n > 0) ? new Class[n] : null;
	for (int i = 0; i < n; i++)
	{
		String arg = st.nextToken();
		if ("thinlet".equals(arg))
		{ // the target component
			result[2 + 3 * i] = "thinlet";
			parametertypes[i] = UIController.class;
		}
		else if ((arg.length() > 1) && (arg.charAt(0) == '\'') && (arg.charAt(arg.length() - 1) == '\''))
		{ // constant string value
			result[2 + 3 * i] = "constant";
			result[2 + 3 * i + 1] = new String(arg.substring(1, arg.length() - 1));
			parametertypes[i] = String.class;
		}
		else
		{
			int dot = arg.indexOf('.');
			String compname = (dot == -1) ? arg : arg.substring(0, dot);
			Object comp = null;
			Class<?> paramClass = null;
			if ("item".equals(compname))
			{
				comp = "item";
				if (thisWidget instanceof TreeWidget) paramClass = TreeNode.class;
				else if (thisWidget instanceof TableWidget) paramClass = TableWidget.class;
				else if (thisWidget instanceof ListWidget) paramClass = ListItem.class;
				else if (thisWidget instanceof ComboBoxWidget) paramClass = ComboBoxItem.class;
				else if (thisWidget instanceof TabbedPaneWidget) paramClass = TabWidget.class;
				else throw new IllegalArgumentException(thisWidget+" has no item");
			}
			else if ("this".equals(compname))
			{
				comp = thisWidget;
				paramClass = thisWidget.getClass();
			}
			else if ((comp = WidgetHelper.findWidget(root, compname)) != null)
			{ // a widget's name
				paramClass = comp.getClass();
			}
			else
			{
				try
				{ // maybe constant number
					if (arg.regionMatches(true, arg.length() - 1, "F", 0, 1))
					{ // float
						result[2 + 3 * i + 1] = Float.valueOf(arg.substring(0, arg.length() - 1));
						parametertypes[i] = Float.TYPE;
					}
					else if (arg.regionMatches(true, arg.length() - 1, "L", 0, 1))
					{ // long
						result[2 + 3 * i + 1] = Long.valueOf(arg.substring(0, arg.length() - 1));
						parametertypes[i] = Long.TYPE;
					}
					else if (dot != -1)
					{ // double
						result[2 + 3 * i + 1] = Double.valueOf(arg);
						parametertypes[i] = Double.TYPE;
					}
					else
					{ // integer
						result[2 + 3 * i + 1] = Integer.valueOf(arg);
						parametertypes[i] = Integer.TYPE;
					}
					result[2 + 3 * i] = "constant";
					continue;
				}
				catch (NumberFormatException nfe)
				{ // widget's name not found nor constant
					throw new IllegalArgumentException("unknown " + arg);
				}
			}
			
			result[2 + 3 * i] = comp; // the target component
			if (dot == -1)
				parametertypes[i] = Widget.class;
			else
			{
				String key = arg.substring(dot+1);
				Method m = findMethod(paramClass, "get"+key);
				if (m == null) m = findMethod(paramClass, "is"+key);
				if (m == null) m = findMethod(paramClass, "has"+key);
				if (m == null) throw new IllegalArgumentException("Not found: "+key+" on "+paramClass);
				result[2 + 3 * i + 1] = key; // parameter name, e.g. enabled
				result[2 + 3 * i + 2] = m;   // method to get param from widget
				parametertypes[i] = m.getReturnType();
			}
		}
	}
	
	result[0] = controller;
	try
	{
		result[1] = findMethod(controller.getClass(), methodname); // parametertypes
		return new MethodInvoker(controller, result);
	}
	catch (Exception exc)
	{
		throw new IllegalArgumentException(exc);
	}
}

private Object addElement(Object parent, String name)
{
	Object child = factory.createWidget(desktop, name);
	if ((parent instanceof PopupMenuWidget) && (child instanceof PopupMenuElementImpl))
		((PopupMenuWidget) parent).setPopupMenu((PopupMenuElementImpl) child);
	else if ((parent instanceof TableWidget) && (child instanceof TableHeader))
		((TableWidget) parent).setHeaderWidget((TableHeader) child);
	else if ((parent instanceof ContainerWidget) && (child instanceof Widget))
		((ContainerWidget) parent).addChild((Widget) child, -1);
	else if ((parent instanceof ElementContainer) && (child instanceof Element))
		((ElementContainer) parent).addChild((Element) child, -1);
	else
		throw new IllegalArgumentException("cannot add "+child+" to "+parent);
	return child;
}

private Object findEnum(Class<?> c, String value)
{
	Object[] enums = c.getEnumConstants();
	Object result = null;
	String name = value.toUpperCase();
	for (Object e : enums)
	{
		if (name.equals(((Enum) e).name()))
			result = e;
	}
	if (result == null) throw new IllegalArgumentException("no "+name+" in "+c);
	return result;
}

private Font findFont(String name)
{
	Font result = fontRegistry.getFont(name);
	if (result == null) throw new NullPointerException("Font "+name+" not found!");
	return result;
}

private void setAttribute(Object parent, Object component, String key, String value)
{
	// replace value found in the bundle
	if ((bundle != null) && value.startsWith("i18n."))
		value = bundle.getString(value.substring(5));
	
	Method m = findMethod(component.getClass(), "set"+key);
	if (m == null) m = findMethod(component.getClass(), "set"+key+"Widget");
	if (m != null)
	{
		Class[] cs = m.getParameterTypes();
		try
		{
			if (cs.length == 1)
			{
				Class<?> c = cs[0];
				if (c == int.class)
				{
					m.invoke(component, Integer.valueOf(value));
					return;
				}
				if (c == boolean.class)
				{
					m.invoke(component, Boolean.valueOf(value));
					return;
				}
				if (c == String.class)
				{
					m.invoke(component, new String(value));
					return;
				}
				if (c == Icon.class)
				{
					m.invoke(component, desktop.loadIcon(value));
					return;
				}
				if (c == TLColor.class)
				{
					TLColor color = new ColorParser(desktop).parse(value);
					m.invoke(component, color);
					return;
				}
				if (c == Font.class)
				{
					Font font = findFont(value);
					m.invoke(component, font);
					return;
				}
				if (c.isEnum())
				{
					Object result = findEnum(c, value);
					m.invoke(component, result);
					return;
				}
				
				if (Widget.class.isAssignableFrom(c))
				{
					methodFixups.add(new MethodFixup(component, "component", key, value, m));
					return;
				}
				if (c == MethodInvoker.class)
				{
					methodFixups.add(new MethodFixup(component, "method", key, value, m));
					return;
				}
			}
		}
		catch (Exception e)
		{ throw new RuntimeException(e); }
	}
	
	if (parent != null)
	{
		m = findMethod(parent.getClass(), "set"+key);
		if (m != null)
		{
			Class[] cs = m.getParameterTypes();
			try
			{
				if ((cs.length == 2) && (Widget.class.isAssignableFrom(cs[0])))
				{
					Class<?> c = cs[1];
					if (c == int.class)
					{
						m.invoke(parent, component, Integer.valueOf(value));
						return;
					}
					if (c == boolean.class)
					{
						m.invoke(parent, component, Boolean.valueOf(value));
						return;
					}
					if (c == String.class)
					{
						m.invoke(parent, component, new String(value));
						return;
					}
					if (c == Icon.class)
					{
						m.invoke(parent, component, desktop.loadIcon(value));
						return;
					}
					if (c == TLColor.class)
					{
						TLColor color = new ColorParser(desktop).parse(value);
						m.invoke(parent, component, color);
						return;
					}
					if (c == Font.class)
					{
						Font font = findFont(value);
						m.invoke(parent, component, font);
						return;
					}
					if (c.isEnum())
					{
						Object result = findEnum(c, value);
						m.invoke(parent, component, result);
						return;
					}
				}
			}
			catch (Exception e)
			{ throw new RuntimeException(e); }
		}
	}
	
/*	if ("property".equals(key) && (component instanceof Widget))
	{
		StringTokenizer st = new StringTokenizer(value, ";");
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int equals = token.indexOf('=');
			if (equals == -1) throw new IllegalArgumentException(token);
			((Widget) component).putProperty(new String(token.substring(0, equals)),
				new String(token.substring(equals + 1)));
		}
		return;
	}*/
	
	throw new RuntimeException("no method found for "+key+" on "+component);
}


// ContentHandler interface
public void characters(char[] arg0, int arg1, int arg2) throws SAXException
{
	// TODO Auto-generated method stub
	
}

public void endDocument() throws SAXException
{
	// Fix forward references.
	// Calls init methods and fixes widget references (which may refer to widgets
	// unknown at parse time).
	for (MethodFixup fixup : methodFixups)
	{
		Object component = fixup.w;
		String value = fixup.value;
		
		if ("method".equals(fixup.type))
		{
			MethodInvoker method = parseMethod(component, value);
			if ("init".equals(fixup.key))
				method.invoke(null);
			else
			{
				try
				{
					fixup.m.invoke(component, method);
				}
				catch (Exception e)
				{ throw new RuntimeException(e); }
			}
		}
		else
		{ // ("component" == definition[0])
			Widget reference = WidgetHelper.findWidget(root, value); //+start find from the component
			if (reference == null) throw new IllegalArgumentException(value + " not found"); 
			try
			{ fixup.m.invoke(component, reference); }
			catch (Exception e)
			{ throw new RuntimeException(e); }
		}
	}
}

public void endElement(String arg0, String arg1, String arg2) throws SAXException
{
	current = widgetStack.pop();
}

public void endPrefixMapping(String arg0) throws SAXException
{
	// TODO Auto-generated method stub
	
}

public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException
{
	// TODO Auto-generated method stub
	
}

public void processingInstruction(String arg0, String arg1) throws SAXException
{
	// TODO Auto-generated method stub
	
}

public void setDocumentLocator(Locator arg0)
{
	// TODO Auto-generated method stub
	
}

public void skippedEntity(String arg0) throws SAXException
{
	// TODO Auto-generated method stub
	
}

public void startDocument() throws SAXException
{
	// TODO Auto-generated method stub
	
}

public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException
{
	if (current != null)
		widgetStack.push(current);
	
	Object parent = current;
	if (root == null)
	{
		root = (Widget) factory.createWidget(desktop, localname);
		current = root;
	}
	else
		current = addElement(current, localname);
	
	for (int i = 0; i < attributes.getLength(); i++)
		setAttribute(parent, current, attributes.getLocalName(i), attributes.getValue(i));
}

public void startPrefixMapping(String arg0, String arg1) throws SAXException
{
	// TODO Auto-generated method stub
	
}

}
