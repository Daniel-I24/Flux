import type { esDemo } from '../es/demo';
import type { DeepStrings } from '../../types';

export const enDemo: DeepStrings<typeof esDemo> = {
  demo: {
    title: 'Demo',
    casesLabel: 'Test cases',
    micOn: 'Real microphone enabled',
    micOff: 'Microphone off: text input',
    micToggle: 'Use real microphone',
    reset: 'Reset notes',
    connector: 'Sync channel between devices',
    navLabel: 'Views',
    openWatch: 'Watch only',
    openPhone: 'Phone only',
    cases: {
      reminder: 'Reminder',
      task: 'Task',
      idea: 'Idea',
      info: 'Info',
      fastAlarm: 'Alarm in 10 s',
    },
    caseHint: 'Run case: {name}',
  },
};
