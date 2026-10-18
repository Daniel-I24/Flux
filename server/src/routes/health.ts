import { Router } from 'express';
import type { Classifier } from '../services/geminiClient.js';

/** GET /api/health → liveness plus whether remote AI is configured (never the key). */
export const createHealthRouter = (classifier: Classifier): Router => {
  const router = Router();
  router.get('/', (_req, res) => {
    res.json({ status: 'ok', ai: { gemini: classifier.enabled, model: classifier.model } });
  });
  return router;
};
