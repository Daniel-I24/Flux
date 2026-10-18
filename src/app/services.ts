import { APP_CONFIG } from '../core/config/appConfig';
import { appEvents } from '../core/events/appEvents';
import { AiClassifierService } from '../modules/ai/services/AiClassifierService';
import { GeminiRemoteStrategy } from '../modules/ai/services/strategies/GeminiRemoteStrategy';
import { LocalNlpStrategy } from '../modules/ai/services/strategies/LocalNlpStrategy';
import { CaptureModeService } from '../modules/capture/services/CaptureModeService';
import { WebSpeechService } from '../modules/capture/services/SpeechService';
import { HttpClient } from '../shared/http/HttpClient';
import { browserStorage } from '../shared/storage/KeyValueStore';

/** Page-wide singletons (settings and stateless adapters) wired once. */
export const captureModeService = new CaptureModeService(browserStorage, appEvents, APP_CONFIG.storageKeys.captureMode);
export const speechService = new WebSpeechService(APP_CONFIG.speechLang);
const httpClient = new HttpClient(APP_CONFIG.apiBaseUrl);

/** Strategy order = priority: remote Gemini first, local NLP as automatic fallback. */
export const createClassifier = (): AiClassifierService =>
  new AiClassifierService(
    [
      new GeminiRemoteStrategy(httpClient, {
        classifyTimeoutMs: APP_CONFIG.timeouts.classifyMs,
        healthTimeoutMs: APP_CONFIG.timeouts.healthMs,
      }),
      new LocalNlpStrategy(),
    ],
    { onStatusChange: (status) => appEvents.emit('ai:status-changed', status) }
  );
