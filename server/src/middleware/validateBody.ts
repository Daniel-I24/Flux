import type { RequestHandler } from 'express';
import type { ZodType } from 'zod';
import { ValidationError } from '../errors/AppError.js';

/** Replaces req.body with the parsed value or forwards a 400 ValidationError. */
export const validateBody =
  <T>(schema: ZodType<T>): RequestHandler =>
  (req, _res, next) => {
    const result = schema.safeParse(req.body);
    if (!result.success) return next(new ValidationError('Invalid request body', result.error.flatten()));
    req.body = result.data;
    return next();
  };
