/** @type {import('tailwindcss').Config} */
const token = (name) => `rgb(var(--color-${name}) / <alpha-value>)`;

export default {
  content: ['./index.html', './src/**/*.ts'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        bg: token('bg'),
        surface: token('surface'),
        'surface-2': token('surface-2'),
        border: token('border'),
        fg: token('fg'),
        muted: token('muted'),
        primary: token('primary'),
        'on-primary': token('on-primary'),
        accent: token('accent'),
        success: token('success'),
        warning: token('warning'),
        danger: token('danger'),
        task: token('task'),
        idea: token('idea'),
        reminder: token('reminder'),
        info: token('info'),
      },
      fontFamily: {
        sans: ['"Inter Variable"', 'Inter', 'system-ui', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 2px rgb(0 0 0 / 0.06), 0 4px 12px rgb(0 0 0 / 0.06)',
        raised: '0 8px 24px rgb(0 0 0 / 0.18)',
        device: '0 24px 64px rgb(0 0 0 / 0.35), 0 0 0 1px rgb(255 255 255 / 0.04)',
      },
      keyframes: {
        'fade-in': { from: { opacity: '0' }, to: { opacity: '1' } },
        'slide-up': {
          from: { opacity: '0', transform: 'translateY(8px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        'slide-down': {
          from: { opacity: '0', transform: 'translateY(-12px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        'pop-in': {
          '0%': { opacity: '0', transform: 'scale(0.6)' },
          '70%': { opacity: '1', transform: 'scale(1.08)' },
          '100%': { transform: 'scale(1)' },
        },
        'mic-pulse': {
          '0%': { boxShadow: '0 0 0 0 rgb(var(--color-primary) / 0.45)' },
          '100%': { boxShadow: '0 0 0 22px rgb(var(--color-primary) / 0)' },
        },
        flow: {
          from: { transform: 'translateX(-100%)' },
          to: { transform: 'translateX(100%)' },
        },
        'flow-y': {
          from: { transform: 'translateY(-100%)' },
          to: { transform: 'translateY(100%)' },
        },
      },
      animation: {
        'fade-in': 'fade-in 200ms ease-out',
        'slide-up': 'slide-up 240ms ease-out',
        'slide-down': 'slide-down 280ms ease-out',
        'pop-in': 'pop-in 420ms ease-out',
        'mic-pulse': 'mic-pulse 1.6s ease-out infinite',
        'spin-slow': 'spin 2.4s linear infinite',
        flow: 'flow 900ms ease-in-out',
        'flow-y': 'flow-y 900ms ease-in-out',
      },
    },
  },
  plugins: [],
};
