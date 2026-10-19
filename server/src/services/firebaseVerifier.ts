import { cert, getApps, initializeApp, type App } from 'firebase-admin/app';
import { getAppCheck } from 'firebase-admin/app-check';
import { getAuth } from 'firebase-admin/auth';
import type { ServerConfig } from '../config/env.js';

/** Verifies the caller: a Firebase ID token (who) and optionally an App Check token (which app). */
export interface RequestVerifier {
  readonly enabled: boolean;
  verifyIdToken(token: string): Promise<{ uid: string }>;
  verifyAppCheck(token: string): Promise<void>;
}

/**
 * firebase-admin adapter. Credentials come from FIREBASE_SERVICE_ACCOUNT_BASE64
 * (a base64 service-account JSON set in Render), never from the repository.
 */
export class FirebaseVerifier implements RequestVerifier {
  private readonly app: App | null;

  constructor(config: ServerConfig['firebase']) {
    this.app = FirebaseVerifier.initialize(config);
  }

  get enabled(): boolean {
    return this.app !== null;
  }

  async verifyIdToken(token: string): Promise<{ uid: string }> {
    const decoded = await getAuth(this.requireApp()).verifyIdToken(token, true);
    return { uid: decoded.uid };
  }

  async verifyAppCheck(token: string): Promise<void> {
    await getAppCheck(this.requireApp()).verifyToken(token);
  }

  private requireApp(): App {
    if (!this.app) throw new Error('Firebase Admin is not configured');
    return this.app;
  }

  private static initialize(config: ServerConfig['firebase']): App | null {
    if (!config.projectId || !config.serviceAccountBase64) return null;
    const existing = getApps()[0];
    if (existing) return existing;
    const serviceAccount = JSON.parse(Buffer.from(config.serviceAccountBase64, 'base64').toString('utf8'));
    return initializeApp({ credential: cert(serviceAccount), projectId: config.projectId });
  }
}
