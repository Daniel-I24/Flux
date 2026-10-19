import type { Request, RequestHandler } from 'express';
import { logger } from '../config/logger.js';
import { AuthUnavailableError, UnauthorizedError } from '../errors/AppError.js';
import type { RequestVerifier } from '../services/firebaseVerifier.js';

export interface AuthenticatedRequest extends Request {
  uid?: string;
}

const BEARER = /^Bearer\s+(.+)$/i;

/**
 * Requires a valid Firebase ID token (Authorization: Bearer) and, when enabled,
 * a valid App Check token (X-Firebase-AppCheck). Sets req.uid for per-user limits.
 */
export const authenticate =
  (verifier: RequestVerifier, appCheckRequired: boolean): RequestHandler =>
  async (req: AuthenticatedRequest, _res, next) => {
    if (!verifier.enabled) return next(new AuthUnavailableError());
    const token = BEARER.exec(req.header('authorization') ?? '')?.[1];
    if (!token) return next(new UnauthorizedError());
    try {
      req.uid = (await verifier.verifyIdToken(token)).uid;
      if (appCheckRequired) {
        const appCheck = req.header('x-firebase-appcheck');
        if (!appCheck) return next(new UnauthorizedError('Missing App Check token'));
        await verifier.verifyAppCheck(appCheck);
      }
      return next();
    } catch (error) {
      logger.warn('Rejected credentials', error instanceof Error ? error.message : error);
      return next(new UnauthorizedError());
    }
  };
