import type { ErrorRequestHandler, RequestHandler } from 'express';
import { logger } from '../config/logger.js';
import { AppError, NotFoundError, ValidationError } from '../errors/AppError.js';

export const notFound: RequestHandler = (_req, _res, next) => next(new NotFoundError());

interface BodyParserError {
  type?: string;
  status?: number;
}

const fromBodyParser = (error: BodyParserError): AppError | null => {
  if (error.type === 'entity.too.large') return new AppError('Request body too large', 413, 'PAYLOAD_TOO_LARGE');
  if (error.type === 'entity.parse.failed') return new ValidationError('Malformed JSON body');
  return null;
};

/** Single exit point for errors: never leaks stack traces or upstream details. */
export const errorHandler: ErrorRequestHandler = (error: unknown, _req, res, _next) => {
  const appError =
    error instanceof AppError ? error : fromBodyParser(error as BodyParserError) ?? new AppError('Unexpected error', 500, 'INTERNAL');
  if (appError.status >= 500) logger.error(appError.message, error);
  res.status(appError.status).json({
    error: {
      code: appError.code,
      message: appError.message,
      ...(appError instanceof ValidationError && appError.issues ? { issues: appError.issues } : {}),
    },
  });
};
