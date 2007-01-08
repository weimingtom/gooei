package gooei.input;

public interface InputSource
{

void update();
boolean hasNext();
InputEvent next();

}
