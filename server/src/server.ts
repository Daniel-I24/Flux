import { createServer, type Server as HttpServer } from 'node:http';
import { Server as SocketServer } from 'socket.io';
import { createApp } from './app.js';
import type { ServerConfig } from './config/env.js';
import type { Classifier } from './services/geminiClient.js';
import { registerSyncHandlers } from './sockets/syncSocket.js';

export interface FluxServer {
  http: HttpServer;
  io: SocketServer;
  close(): Promise<void>;
}

/** Wires Express and Socket.IO on one HTTP server (one port for the Vite proxy). */
export const createFluxServer = (config: ServerConfig, classifier: Classifier): FluxServer => {
  const http = createServer(createApp({ config, classifier }));
  const io = new SocketServer(http, {
    cors: { origin: config.corsOrigins, methods: ['GET', 'POST'] },
    maxHttpBufferSize: config.socket.maxPayloadBytes,
    serveClient: false,
  });
  registerSyncHandlers(io, config);
  return {
    http,
    io,
    close: () =>
      new Promise((resolve) => {
        io.close(() => resolve());
      }),
  };
};
