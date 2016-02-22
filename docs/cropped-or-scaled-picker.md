
Presenting the Scandit Barcode Scanner Plugin {#cordova-cropped-or-scaled-picker}
===================================

## Displaying the scanner in fullscreen mode

The standard way to display the plugin is in fullscreen mode. The main reason for this is that Phonegap expects plugins to do this and does not provide a friendly way of manipulating the view hierarchy of the web view to display a plugin inside this hierarchy.

Under iOS the scanner will display a toolbar at the bottom to allow the user to cancel scanning and return to the web view. Under Android such a toolbar is not shown as Android devices have a back button that lets the user cancel the scanning.


## Displaying a cropped/scaled scanner

The plugin allows you to display it as a cropped and/or scaled subview such that parts of your UI are still visible and can be interacted with while scanning. You can accomplish this by setting the portraitMargins and landscapeMargins setting when calling the scan function.

~~~~~~~~~~~~~~~~{.java}
picker.setMargins(new Scandit.Margins(0, 0, 0, 200), new Scandit.Margins(0, 0, 200, 0), 0);
~~~~~~~~~~~~~~~~

This code will show the scanner with a 200 unit bottom margin in portrait orientation and a 200 unit right margin in landscape orientation. The units used depend on the platform, but are independent of the pixel density. The margin can also be specified in percent, e.g. following code will make the picker occupy the upper half of the screen:

~~~~~~~~~~~~~~~~{.java}
picker.setMargins(new Scandit.Margins('0%', '0%', '0%', '50%'), new Scandit.Margins('0%', '0%', '0%', '50%'), 0);
~~~~~~~~~~~~~~~~

It is possible to adjust these margins while the scanner is running by using the resize function. The last argument is the animation duration which you only want to set to something greater than 0 if the picker is already displayed and should animate to a new size:

~~~~~~~~~~~~~~~~{.java}
picker.setMargins(new Scandit.Margins(0, 0, 0, 100), new Scandit.Margins(0, 0, 100, 0), 0.5);
~~~~~~~~~~~~~~~~

This code will reduce the margins to 100 units and animate that change over 0.5 seconds.


### Closing the scanner

When you are adding the plugin as a subview there will not be any default way of closing the scanner again (like the toolbar or Android back button). This means you will have to implement a way to close the scanner yourself through a button or similar. To close the scanner you simply call the cancel function:

~~~~~~~~~~~~~~~~{.java}
picker.cancel();
~~~~~~~~~~~~~~~~


### Known problems

Since Phonegap does not properly support non-fullscreen display of plugins there are some oddities that you should be aware of:

* Under Android touch events are intercepted by the web view even though the scanner is in front of it. This means that it is possible to press buttons/links which are behind the scanner. This is a property of the web view used by Phonegap Android and can not be changed. You should make sure that there are no buttons or links behind the scanner while it is active.
* It is possible that the implementation of this feature will break with future updates of Phonegap as the view hierarchy might change. Always make sure to use the most recent version of the plugin where such issues will be fixed.

