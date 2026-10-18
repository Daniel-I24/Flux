import { logger } from '../../../core/logger/logger';

export type NotificationPermissionState = NotificationPermission | 'unsupported';

export interface SystemNotification {
  title: string;
  body: string;
  tag: string;
}

const isSupported = () => typeof window !== 'undefined' && 'Notification' in window;

/**
 * System notifications. Permission is only requested from a user gesture (button),
 * never on load. Uses the service worker when available (required on Android Chrome).
 */
export class NotificationManager {
  get permission(): NotificationPermissionState {
    return isSupported() ? Notification.permission : 'unsupported';
  }

  async requestPermission(): Promise<NotificationPermissionState> {
    if (!isSupported()) return 'unsupported';
    try {
      return await Notification.requestPermission();
    } catch (error) {
      logger.warn('Notification permission request failed', error);
      return Notification.permission;
    }
  }

  async notify(notification: SystemNotification): Promise<void> {
    if (this.permission !== 'granted') return;
    const options: NotificationOptions = { body: notification.body, tag: notification.tag, icon: '/icons/icon-192.png', badge: '/icons/icon-192.png' };
    try {
      const registration = await navigator.serviceWorker?.getRegistration();
      if (registration) {
        await registration.showNotification(notification.title, options);
        return;
      }
      new Notification(notification.title, options);
    } catch (error) {
      logger.warn('Could not show notification', error);
    }
  }
}
