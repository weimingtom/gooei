Gooei started out as a thinlet port to lwjgl, but after a lot of refactoring it has become a project in it's own right.

Gooei is a Widget API specification and implementation using the lwjgl OpenGL binding as a rendering backend. It features a number of ready-made widgets such as dialog windows, buttons, checkboxes, menubars, lists and others. It is also easily extensible.

Font rendering uses an extensible font rendering backend. Right now it supports ttf software rendering, triangulated ttf fonts, and bitmap based fonts generated with the Angel Code (http://www.angelcode.com/) bitmap font generator.

The ttf triangulation code uses the geomlib, and the bitmap font generator uses the jimage library to load images. Both are available here on google code.

The license is the LGPL, in accordance with the thinlet licensing.



