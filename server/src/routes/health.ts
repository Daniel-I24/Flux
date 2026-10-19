import { Router } from 'express';
import type { RequestVerifier } from '../services/firebaseVerifier.js';
import type { Classifier } from '../services/geminiClient.js';

/** GET /api/health → liveness plus what is configured (never secrets). */
export const createHealthRouter = (classifier: Classifier, verifier: RequestVerifier): Router => {
  const router = Router();
  router.get('/', (_req, res) => {
    res.json({ status: 'ok', ai: { gemini: classifier.enabled, model: classifier.model }, auth: { firebase: verifier.enabled } });
  });
  return router;
};
