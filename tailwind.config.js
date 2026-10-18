/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,js,html}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        flux: {
          bg: 'var(--flux-bg)',
          elevated: 'var(--flux-elevated)',
          surface: 'var(--flux-surface)',
          hover: 'var(--flux-hover)',
          border: 'var(--flux-border)',
          cyan: '#00E5FF',
          'cyan-dim': 'rgba(0, 229, 255, 0.15)',
          'cyan-deep': '#0099B8',
          violet: '#8B5CF6',
          'violet-dim': 'rgba(139, 92, 246, 0.15)',
          purple: '#7C3AED',
          pink: '#EC4899',
        },
        category: {
          task: '#3B82F6',
          'task-bg': 'rgba(59, 130, 246, 0.12)',
          'task-border': 'rgba(59, 130, 246, 0.3)',
          idea: '#F59E0B',
          'idea-bg': 'rgba(245, 158, 11, 0.12)',
          'idea-border': 'rgba(245, 158, 11, 0.3)',
          reminder: '#10B981',
          'reminder-bg': 'rgba(16, 185, 129, 0.12)',
          'reminder-border': 'rgba(16, 185, 129, 0.3)',
          info: '#A855F7',
          'info-bg': 'rgba(168, 85, 247, 0.12)',
          'info-border': 'rgba(168, 85, 247, 0.3)',
        },
      },
      fontFamily: {
        display: ['Outfit', 'sans-serif'],
        body: ['Plus Jakarta Sans', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        'glow-cyan': '0 0 30px rgba(0, 229, 255, 0.35)',
        'glow-violet': '0 0 30px rgba(139, 92, 246, 0.35)',
        'watch-bezel': '0 16px 45px rgba(0, 0, 0, 0.8), inset 0 2px 5px rgba(255, 255, 255, 0.15), inset 0 -4px 8px rgba(0, 0, 0, 0.8)',
        'phone-frame': '0 25px 60px rgba(0, 0, 0, 0.85), 0 0 0 1px rgba(255, 255, 255, 0.06)',
      },
      borderRadius: {
        'watch': '50%',
        'phone': '46px',
      },
      keyframes: {
        pulseGlow: {
          '0%, 100%': {
            boxShadow: '0 0 20px rgba(0, 229, 255, 0.35)',
            transform: 'scale(1)',
          },
          '50%': {
            boxShadow: '0 0 40px rgba(0, 229, 255, 0.65)',
            transform: 'scale(1.03)',
          },
        },
        alarmFlash: {
          '0%, 100%': {
            borderColor: 'rgba(16, 185, 129, 0.3)',
            backgroundColor: 'rgba(16, 185, 129, 0.08)',
          },
          '50%': {
            borderColor: 'rgba(16, 185, 129, 0.8)',
            backgroundColor: 'rgba(16, 185, 129, 0.22)',
            boxShadow: '0 0 25px rgba(16, 185, 129, 0.4)',
          },
        },
        particleMove: {
          '0%': { left: '0%', opacity: '0' },
          '20%': { opacity: '1' },
          '80%': { opacity: '1' },
          '100%': { left: '95%', opacity: '0' },
        },
      },
      animation: {
        'pulse-glow': 'pulseGlow 2.5s infinite ease-in-out',
        'alarm-flash': 'alarmFlash 1.8s infinite ease-in-out',
        'particle-move': 'particleMove 1.6s infinite ease-in-out',
        'spin-slow': 'spin 4s linear infinite',
        'spin-reverse': 'spin 1.4s linear infinite reverse',
      },
    },
  },
  plugins: [],
};
