/** Fixed-window counter used to throttle socket messages per connection. */
export class RateLimiter {
  private windowStart = 0;
  private count = 0;

  constructor(
    private readonly limit: number,
    private readonly windowMs: number,
    private readonly now: () => number = Date.now
  ) {}

  tryConsume(): boolean {
    const current = this.now();
    if (current - this.windowStart >= this.windowMs) {
      this.windowStart = current;
      this.count = 0;
    }
    this.count += 1;
    return this.count <= this.limit;
  }
}
