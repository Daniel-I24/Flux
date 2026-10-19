import { Router } from 'express';
import rateLimit from 'express-rate-limit';
import type { ServerConfig } from '../config/env.js';
import { authenticate, type AuthenticatedRequest } from '../middleware/authenticate.js';
import { validateBody } from '../middleware/validateBody.js';
import { ClassificationSchema, ClassifyRequestSchema, type ClassifyRequest } from '../schemas/classification.js';
import type { RequestVerifier } from '../services/firebaseVerifier.js';
import type { Classifier } from '../services/geminiClient.js';

/**
 * POST /api/classify → classification from Gemini for an authenticated user.
 * 503 when Gemini or Firebase Admin is not configured and 502 on upstream failure:
 * the app then falls back to its local classifier automatically.
 */
export const createClassifyRouter = (classifier: Classifier, verifier: RequestVerifier, config: ServerConfig): Router => {
  const router = Router();
  const perUserLimit = rateLimit({
    windowMs: config.rateLimit.windowMs,
    limit: config.rateLimit.perUser,
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    keyGenerator: (req) => (req as AuthenticatedRequest).uid ?? 'anonymous',
    message: { error: { code: 'RATE_LIMITED', message: 'Too many requests' } },
  });
  router.post(
    '/',
    authenticate(verifier, config.firebase.appCheckRequired),
    perUserLimit,
    validateBody(ClassifyRequestSchema),
    async (req, res, next) => {
      try {
        const classification = await classifier.classify(req.body as ClassifyRequest);
        res.json(ClassificationSchema.parse(classification));
      } catch (error) {
        next(error);
      }
    }
  );
  return router;
};
