import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app.module';

const isReadyToBootstrap = (window as any).Scandit && (window as any).Scandit.ScanOverlay && (window as any).Scandit.BarcodePicker;

const bootstrap = () => { platformBrowserDynamic().bootstrapModule(AppModule); };
const tryToBootstrap = () => {
  console.log('Trying to bootstrap...')
  if (isReadyToBootstrap) {
    console.log('Bootstrapping.')
    bootstrap();
  } else {
    console.log('Not ready to bootstrap yet');
    setTimeout(bootstrap.bind(this), 100);
  }
}

/**
 * Need to wait for the deviceready event to fire before bootstrapping, otherwise the Cordova plugins are not properly
 * loaded yet, e.g. the Scandit namespace is not available yet at the time of dependency injection.
 * See https://github.com/driftyco/ionic2-app-base/issues/114 for more info.
 */
if ((window as any).cordova) {
  document.addEventListener('deviceready', () => tryToBootstrap());
} else {
  tryToBootstrap();
}