import '@fontsource-variable/inter';
import './style.css';
import { bootstrap } from './app/bootstrap';
import { registerServiceWorker } from './app/registerServiceWorker';

bootstrap(document.getElementById('app'));
registerServiceWorker();
