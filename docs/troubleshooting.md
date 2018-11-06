Troubleshoot Cordova/PhoneGap     {#cordova-troubleshooting}
========================


\tableofcontents


# App crashes when the barcode picker is shown {#cordovaappcrash}

An app crash when the barcode picker is shown can occur if there is a problem with our Scandit SDK and/or the way it is used in your app. Contact us at info@scandit.com with the details of your setup (Android version, Android device name, info whether our demo app works correctly, and a description of the problem).

Note that for versions prior to 4.12, app crashes could also be the result of invalid app keys or use of the test version without internet access. This behavior has been removed in 4.12. Instead license validation or connectivity issues are now displayed in the barcode picker view.

<br/>
# Installing under Windows fails {#windowsinstall}

Windows has a maximum file path length which means that it is not possible to create new folders after a certain depth in the file system has been reached. Since adding a plugin copies files in an already quite deep folder structure an install can fail because the maximum file path length has been reached and the system fails to copy all the files necessary. To avoid this move your project as high up in the file system as possible (to something like C:\\yourProject).


<br/>
# You were unable to find a solution to your problem {#cordovanosolution}

If your specific problem does not have a solution on this page, contact us at support@scandit.com with a description of the problem (if possible with a code snippet or project) including details about your setup (iOS version, iOS device name, info whether our demo app works correctly).
