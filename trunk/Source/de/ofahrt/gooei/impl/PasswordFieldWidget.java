package de.ofahrt.gooei.impl;

import gooei.Desktop;

public final class PasswordFieldWidget extends TextFieldWidget
{

public PasswordFieldWidget(Desktop desktop)
{ super(desktop); }

@Override
public boolean isHidden()
{ return true; }

}
