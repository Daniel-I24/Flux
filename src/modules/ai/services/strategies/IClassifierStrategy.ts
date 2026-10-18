import type { AiEngine } from '../../../../core/types/domain';
import type { Classification, ClassifyRequest } from '../../models/classification';

/** Strategy contract: every classifier engine is interchangeable behind this interface. */
export interface IClassifierStrategy {
  readonly engine: AiEngine;
  isAvailable(): Promise<boolean>;
  classify(request: ClassifyRequest): Promise<Classification>;
}
