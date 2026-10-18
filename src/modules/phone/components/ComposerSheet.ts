import { Mic, Square } from 'lucide';
import { toAppError } from '../../../core/errors/AppError';
import type { CaptureMode } from '../../../core/types/domain';
import { createButton } from '../../../shared/components/Button';
import { openOverlay } from '../../../shared/components/Overlay';
import { h } from '../../../shared/dom/h';
import { t } from '../../../shared/i18n';
import { TextCaptureForm } from '../../capture/components/TextCaptureForm';
import type { ISpeechService, SpeechSession } from '../../capture/services/SpeechService';

export interface ComposerDependencies {
  speech: ISpeechService;
  captureMode: () => CaptureMode;
  maxLength: number;
  /** Classifies and stores the text; rejects with an AppError on failure. */
  submit(text: string): Promise<void>;
}

/** Bottom sheet to create a note from the phone by typing or dictating. */
export const openComposer = (host: HTMLElement, deps: ComposerDependencies): void => {
  let session: SpeechSession | null = null;
  let busy = false;
  const status = h('p', { class: 'min-h-[1.25rem] text-sm', attrs: { 'aria-live': 'polite' } });
  const setStatus = (text: string, tone: 'muted' | 'danger' = 'muted') => {
    status.textContent = text;
    status.className = `min-h-[1.25rem] text-sm ${tone === 'danger' ? 'text-danger' : 'text-muted'}`;
  };

  const form = new TextCaptureForm({
    label: t('phone.composerLabel'),
    placeholder: t('phone.composerPlaceholder'),
    submitLabel: t('phone.composerSave'),
    maxLength: deps.maxLength,
    multiline: true,
    onSubmit: (text) => {
      if (busy) return;
      busy = true;
      setStatus(t('watch.processing'));
      deps
        .submit(text)
        .then(() => overlay.close())
        .catch((error: unknown) => setStatus(t(`errors.${toAppError(error).code}`), 'danger'))
        .finally(() => (busy = false));
    },
  });

  const setListening = (listening: boolean) => {
    startButton.classList.toggle('hidden', listening);
    stopButton.classList.toggle('hidden', !listening);
    (listening ? stopButton : startButton).focus();
  };

  const startButton = createButton({
    label: t('phone.composerDictate'),
    icon: Mic,
    className: 'w-full',
    onClick: () => {
      setStatus(t('phone.composerListening'));
      setListening(true);
      session = deps.speech.start({
        onInterim: (text) => form.setValue(text),
        onFinal: (text) => form.setValue(text),
        onError: (error) => setStatus(t(`errors.${error.code}`), 'danger'),
        onEnd: () => {
          session = null;
          setListening(false);
          if (status.textContent === t('phone.composerListening')) setStatus('');
        },
      });
    },
  });
  const stopButton = createButton({
    label: t('phone.composerStop'),
    icon: Square,
    variant: 'danger',
    className: 'hidden w-full',
    onClick: () => session?.stop(),
  });

  const canDictate = deps.captureMode() === 'voice' && deps.speech.isSupported();
  const overlay = openOverlay(host, {
    title: t('phone.composerTitle'),
    placement: 'bottom',
    onClose: () => session?.abort(),
    body: () => [
      h('div', { class: 'flex flex-col gap-3' }, [form.element, canDictate ? startButton : null, canDictate ? stopButton : null, status]),
    ],
  });
  form.focus();
};
