# Build the Ionic project

_Insert your app key or license key in `src/providers/scanner.ts`._

Run the following commands from the Ionic project root, e.g. `/samples/ocr`:
```
> npm install
> npm run build
```

Running `npm run build` is necessary to update the build files in the `www` folder if there were changes to the source files.

# Create Cordova project based on Ionic Sample
Run from a different folder
```
> phonegap create helloScandit --id "com.scandit.helloScandit" --link-to <path to www of ionic project, e.g. ./samples/ocr/www>
> cd helloScandit
> phonegap plugin add <path to Scandit Cordova plugin>
> phonegap platform add android
> phonegap run android --device
```
