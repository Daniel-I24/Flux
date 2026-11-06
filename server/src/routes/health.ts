import { Router } from 'express';
import type { RequestVerifier } from '../services/firebaseVerifier.js';
import type { Classifier } from '../services/geminiClient.js';

/** GET /api/health → liveness, the deployed commit and what is configured (never secrets). */
export const createHealthRouter = (classifier: Classifier, verifier: RequestVerifier, release: string): Router => {
  const router = Router();
  router.get('/', (_req, res) => {
    res.json({
      status: 'ok',
      release,
      ai: { gemini: classifier.enabled, model: classifier.model },
      auth: { firebase: verifier.enabled },
    });
  });
  return router;
};
