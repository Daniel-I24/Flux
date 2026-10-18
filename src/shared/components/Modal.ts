export interface ModalOptions {
  title: string;
  contentElement: HTMLElement;
  onClose?: () => void;
}

export class Modal {
  private overlay: HTMLElement | null = null;
  private readonly options: ModalOptions;
  private keyHandler: ((e: KeyboardEvent) => void) | null = null;

  constructor(options: ModalOptions) {
    this.options = options;
  }

  public open(): void {
    if (this.overlay) return;

    this.overlay = document.createElement('div');
    this.overlay.className = 'fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fadeIn';
    this.overlay.setAttribute('role', 'dialog');
    this.overlay.setAttribute('aria-modal', 'true');
    this.overlay.setAttribute('aria-labelledby', 'fluxModalTitle');

    const dialog = document.createElement('div');
    dialog.className = 'bg-flux-elevated border border-flux-border rounded-2xl p-6 max-w-md w-full shadow-2xl relative text-slate-100 flex flex-col gap-4';

    const header = document.createElement('div');
    header.className = 'flex items-center justify-between border-b border-flux-border pb-3';

    const titleEl = document.createElement('h3');
    titleEl.id = 'fluxModalTitle';
    titleEl.className = 'text-lg font-bold font-display text-slate-100';
    titleEl.textContent = this.options.title;

    const closeBtn = document.createElement('button');
    closeBtn.className = 'text-slate-400 hover:text-slate-100 p-1 rounded-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-flux-cyan';
    closeBtn.innerHTML = '✕';
    closeBtn.setAttribute('aria-label', 'Cerrar diálogo');
    closeBtn.onclick = () => this.close();

    header.appendChild(titleEl);
    header.appendChild(closeBtn);
    dialog.appendChild(header);
    dialog.appendChild(this.options.contentElement);

    this.overlay.appendChild(dialog);
    this.overlay.addEventListener('click', (e) => {
      if (e.target === this.overlay) this.close();
    });

    this.keyHandler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') this.close();
    };
    window.addEventListener('keydown', this.keyHandler);

    document.body.appendChild(this.overlay);
  }

  public close(): void {
    if (this.overlay) {
      if (this.keyHandler) {
        window.removeEventListener('keydown', this.keyHandler);
      }
      this.overlay.remove();
      this.overlay = null;
      if (this.options.onClose) {
        this.options.onClose();
      }
    }
  }
}
