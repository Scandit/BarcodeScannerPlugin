import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app.module';

const bootstrap = () => { platformBrowserDynamic().bootstrapModule(AppModule); };

/**
 * Need to wait for the deviceready event to fire before bootstrapping, otherwise the Cordova plugins are not properly
 * loaded yet, e.g. the Scandit namespace is not available yet at the time of dependency injection.
 * See https://github.com/driftyco/ionic2-app-base/issues/114 for more info.
 */
if ((window as any).cordova) {
  console.info('Waiting with bootstrapping...');
  console.time('Device was ready in');
  document.addEventListener('deviceready', () => {
    console.timeEnd('Device was ready in');
    console.info('Bootstrapping now...');
    bootstrap();
  });
} else {
  bootstrap();
}