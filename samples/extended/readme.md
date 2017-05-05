# Build the Ionic project
Run from project root
```
> npm install
> npm run build
```

# Create Cordova project based on Ionic Sample
Run from a different folder
```
> phonegap create helloScandit --id "com.scandit.helloScandit" --template <path to root of ionic project, e.g. ./samples/extended>
> cd helloScandit
> phonegap plugin add <path to Scandit Cordova plugin>
> phonegap platform add android
> phonegap run android --device
```
