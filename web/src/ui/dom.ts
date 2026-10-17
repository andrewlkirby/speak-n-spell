type Attrs = Record<string, string | boolean | ((event: Event) => void)>;

/** Tiny element builder: h('button', { class: 'x', onclick: fn }, 'label', childEl). */
export function h<K extends keyof HTMLElementTagNameMap>(
  tag: K,
  attrs: Attrs = {},
  ...children: (Node | string | null | false)[]
): HTMLElementTagNameMap[K] {
  const element = document.createElement(tag);
  for (const [name, value] of Object.entries(attrs)) {
    if (typeof value === 'function') {
      element.addEventListener(name.replace(/^on/, ''), value);
    } else if (value === true) {
      element.setAttribute(name, '');
    } else if (value !== false) {
      element.setAttribute(name, value);
    }
  }
  for (const child of children) {
    if (child !== null && child !== false) element.append(child);
  }
  return element;
}

/** Opens a modal <dialog>; it removes itself from the page when closed. */
export function showDialog(dialog: HTMLDialogElement): void {
  dialog.addEventListener('close', () => dialog.remove());
  document.body.append(dialog);
  dialog.showModal();
}
