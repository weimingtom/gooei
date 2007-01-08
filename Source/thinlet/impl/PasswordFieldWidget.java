package thinlet.impl;


public final class PasswordFieldWidget extends TextFieldWidget
{

public PasswordFieldWidget(ThinletDesktop desktop)
{ super(desktop); }

@Override
public boolean isHidden()
{ return true; }

}
