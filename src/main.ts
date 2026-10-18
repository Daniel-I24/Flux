import './style.css';

/**
 * Entry point for FLUX Professional Web Application.
 */
export function initializeApp(): void {
  const appContainer = document.getElementById('app');
  if (!appContainer) {
    return;
  }
  appContainer.innerHTML = `
    <div class="flex items-center justify-center min-h-screen text-flux-cyan font-display text-xl">
      Flux Professional Initialized
    </div>
  `;
}

if (typeof window !== 'undefined') {
  window.addEventListener('DOMContentLoaded', () => {
    initializeApp();
  });
}
