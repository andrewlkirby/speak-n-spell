/** iPhone/iPad, including iPadOS which reports itself as a Mac with a touchscreen. */
export const isIos =
  /iPad|iPhone|iPod/.test(navigator.userAgent) || (navigator.userAgent.includes('Mac') && navigator.maxTouchPoints > 1);

export const isAndroid = /Android/.test(navigator.userAgent);

/** True when launched from the home screen rather than in a browser tab. */
export function isStandalone(): boolean {
  return (
    matchMedia('(display-mode: standalone)').matches ||
    (navigator as Navigator & { standalone?: boolean }).standalone === true
  );
}
