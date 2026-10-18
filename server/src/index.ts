import 'dotenv/config';
import { loadConfig } from './config/env.js';
import { logger } from './config/logger.js';
import { createFluxServer } from './server.js';
import { GeminiClient } from './services/geminiClient.js';

const config = loadConfig();
const classifier = new GeminiClient(config.gemini);
const server = createFluxServer(config, classifier);

server.http.listen(config.port, () => {
  logger.info(`Listening on http://localhost:${config.port}`);
  logger.info(classifier.enabled ? `Gemini enabled (model ${config.gemini.model})` : 'Gemini disabled: clients will use the local classifier');
  logger.info(`Allowed origins: ${config.corsOrigins.join(', ')}`);
});

const shutdown = () => {
  logger.info('Shutting down');
  void server.close().then(() => process.exit(0));
};
process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
