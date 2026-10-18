import { APP_CONFIG } from '../../core/config/appConfig';
import { appEvents } from '../../core/events/appEvents';
import { browserStorage } from '../storage/KeyValueStore';
import { ThemeService } from './ThemeService';

export const themeService = new ThemeService(browserStorage, appEvents, APP_CONFIG.storageKeys.theme);
