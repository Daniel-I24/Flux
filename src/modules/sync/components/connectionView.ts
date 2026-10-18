import type { ConnectionView } from '../../../shared/components/ConnectionBadge';
import { t } from '../../../shared/i18n';
import type { SyncState } from '../models/syncEnvelope';

/**
 * Translates transport state to what the user sees: connected only counts when the
 * other device is reachable; otherwise we are "waiting" (amber) or offline/reconnecting.
 */
export const toConnectionView = (state: SyncState, linkLabel?: string): ConnectionView => {
  if (state.status === 'connected' && state.peers > 0) {
    return { status: 'connected', label: linkLabel, pending: state.pending };
  }
  if (state.status === 'connected') {
    return { status: 'reconnecting', label: t('connection.waitingPeer'), pending: state.pending };
  }
  return { status: state.status, pending: state.pending };
};
