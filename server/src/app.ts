import cors from 'cors';
import express, { type Express } from 'express';
import rateLimit from 'express-rate-limit';
import helmet from 'helmet';
import type { ServerConfig } from './config/env.js';
import { errorHandler, notFound } from './middleware/errorHandler.js';
import { createClassifyRouter } from './routes/classify.js';
import { createHealthRouter } from './routes/health.js';
import type { Classifier } from './services/geminiClient.js';

export interface AppDependencies {
  config: ServerConfig;
  classifier: Classifier;
}

/** Express app factory (no listen) so tests can mount it with fakes. */
export const createApp = ({ config, classifier }: AppDependencies): Express => {
  const app = express();
  app.disable('x-powered-by');
  app.set('trust proxy', 'loopback');
  app.use(helmet());
  app.use(
    cors({
      origin: (origin, callback) => callback(null, !origin || config.corsOrigins.includes(origin)),
      methods: ['GET', 'POST'],
    })
  );
  app.use(express.json({ limit: config.bodyLimit }));
  app.use(
    '/api',
    rateLimit({
      windowMs: config.rateLimit.windowMs,
      limit: config.rateLimit.max,
      standardHeaders: 'draft-7',
      legacyHeaders: false,
      message: { error: { code: 'RATE_LIMITED', message: 'Too many requests' } },
    })
  );
  app.use('/api/health', createHealthRouter(classifier));
  app.use('/api/classify', createClassifyRouter(classifier));
  app.use(notFound);
  app.use(errorHandler);
  return app;
};
