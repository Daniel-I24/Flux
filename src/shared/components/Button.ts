export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost';

export interface ButtonOptions {
  label: string;
  variant?: ButtonVariant;
  iconSvg?: string;
  ariaLabel?: string;
  className?: string;
  onClick?: (event: MouseEvent) => void;
}

export function createButton(options: ButtonOptions): HTMLButtonElement {
  const btn = document.createElement('button');
  btn.type = 'button';
  btn.setAttribute('aria-label', options.ariaLabel || options.label);

  const baseClasses = 'inline-flex items-center justify-center gap-2 font-medium text-xs sm:text-sm rounded-xl px-3 py-1.5 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-flux-cyan disabled:opacity-50 disabled:cursor-not-allowed select-none';

  let variantClasses = '';
  switch (options.variant || 'primary') {
    case 'primary':
      variantClasses = 'bg-flux-cyan hover:bg-flux-cyan-deep text-slate-950 font-semibold shadow-glow-cyan';
      break;
    case 'secondary':
      variantClasses = 'bg-flux-surface hover:bg-flux-hover text-slate-200 border border-flux-border';
      break;
    case 'danger':
      variantClasses = 'bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30';
      break;
    case 'ghost':
      variantClasses = 'hover:bg-slate-500/10 text-slate-400 hover:text-slate-200';
      break;
  }

  btn.className = `${baseClasses} ${variantClasses} ${options.className || ''}`;
  
  if (options.iconSvg) {
    const iconSpan = document.createElement('span');
    iconSpan.innerHTML = options.iconSvg;
    btn.appendChild(iconSpan);
  }

  const textSpan = document.createElement('span');
  textSpan.textContent = options.label;
  btn.appendChild(textSpan);

  if (options.onClick) {
    btn.addEventListener('click', options.onClick);
  }

  return btn;
}
