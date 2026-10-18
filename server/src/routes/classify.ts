import { Router } from 'express';
import { validateBody } from '../middleware/validateBody.js';
import { ClassifyRequestSchema, type ClassifyRequest } from '../schemas/classification.js';
import type { Classifier } from '../services/geminiClient.js';

/**
 * POST /api/classify → classification from Gemini.
 * 503 when no key is configured and 502 on upstream failure; the client then
 * falls back to its local NLP strategy automatically.
 */
export const createClassifyRouter = (classifier: Classifier): Router => {
  const router = Router();
  router.post('/', validateBody(ClassifyRequestSchema), async (req, res, next) => {
    try {
      res.json(await classifier.classify(req.body as ClassifyRequest));
    } catch (error) {
      next(error);
    }
  });
  return router;
};
