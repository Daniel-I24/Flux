import 'dotenv/config';
import { createApp } from './app.js';
import { loadConfig } from './config/env.js';
import { logger } from './config/logger.js';
import { FirebaseVerifier } from './services/firebaseVerifier.js';
import { GeminiClient } from './services/geminiClient.js';

const config = loadConfig();
const classifier = new GeminiClient(config.gemini);
const verifier = new FirebaseVerifier(config.firebase);
const server = createApp({ config, classifier, verifier }).listen(config.port, () => {
  logger.info(`Listening on port ${config.port}`);
  logger.info(classifier.enabled ? `Gemini enabled (model ${config.gemini.model})` : 'Gemini disabled: apps use the local classifier');
  logger.info(verifier.enabled ? 'Firebase Admin enabled' : 'Firebase Admin disabled: /api/classify answers 503');
});

const shutdown = () => {
  logger.info('Shutting down');
  server.close(() => process.exit(0));
};
process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
