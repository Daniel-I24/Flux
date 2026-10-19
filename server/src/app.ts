import cors from 'cors';
import express, { type Express } from 'express';
import rateLimit from 'express-rate-limit';
import helmet from 'helmet';
import type { ServerConfig } from './config/env.js';
import { errorHandler, notFound } from './middleware/errorHandler.js';
import { createClassifyRouter } from './routes/classify.js';
import { createHealthRouter } from './routes/health.js';
import type { RequestVerifier } from './services/firebaseVerifier.js';
import type { Classifier } from './services/geminiClient.js';

export interface AppDependencies {
  config: ServerConfig;
  classifier: Classifier;
  verifier: RequestVerifier;
}

/** Express app factory (no listen) so tests can mount it with fakes. */
export const createApp = ({ config, classifier, verifier }: AppDependencies): Express => {
  const app = express();
  app.disable('x-powered-by');
  // Render (and most PaaS) terminate TLS in one proxy hop.
  app.set('trust proxy', 1);
  app.use(helmet());
  app.use(
    cors({
      // Only listed browser origins; native apps send no Origin header and are authenticated by token.
      origin: (origin, callback) => callback(null, !origin || config.corsOrigins.includes(origin)),
      methods: ['GET', 'POST'],
    })
  );
  app.use(express.json({ limit: config.bodyLimit }));
  app.use(
    '/api',
    rateLimit({
      windowMs: config.rateLimit.windowMs,
      limit: config.rateLimit.perIp,
      standardHeaders: 'draft-7',
      legacyHeaders: false,
      message: { error: { code: 'RATE_LIMITED', message: 'Too many requests' } },
    })
  );
  app.use('/api/health', createHealthRouter(classifier, verifier));
  app.use('/api/classify', createClassifyRouter(classifier, verifier, config));
  app.use(notFound);
  app.use(errorHandler);
  return app;
};
