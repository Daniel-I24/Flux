/** Server error hierarchy; `status` and `code` become the JSON error response. */
export class AppError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly code: string,
    options?: { cause?: unknown }
  ) {
    super(message, options);
    this.name = new.target.name;
  }
}

export class ValidationError extends AppError {
  constructor(message: string, public readonly issues: unknown = undefined) {
    super(message, 400, 'VALIDATION');
  }
}

export class NotFoundError extends AppError {
  constructor(message = 'Resource not found') {
    super(message, 404, 'NOT_FOUND');
  }
}

export class AiUnavailableError extends AppError {
  constructor(message = 'Remote AI is not configured') {
    super(message, 503, 'AI_UNAVAILABLE');
  }
}

export class UpstreamError extends AppError {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, 502, 'UPSTREAM', options);
  }
}
