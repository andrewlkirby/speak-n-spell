// Confetti palette — the theme accents from styles.css (ui/theme/Color.kt on Android).
const PALETTE = ['#FF6F3C', '#00B4A6', '#7C4DFF', '#FFD23F', '#4CD964', '#FF4D6D'];
const DURATION_MS = 1100;

interface Particle {
  angle: number;
  speed: number;
  color: string;
  size: number;
  spinsPerSecond: number;
}

function burst(count: number): Particle[] {
  return Array.from({ length: count }, () => ({
    angle: Math.random() * 2 * Math.PI,
    speed: 260 + Math.random() * 260,
    color: PALETTE[Math.floor(Math.random() * PALETTE.length)],
    size: 10 + Math.random() * 10,
    spinsPerSecond: 1 + Math.random() * 3,
  }));
}

/** A one-shot confetti burst over the whole of [canvas], from its center, 40% down. */
export function playConfetti(canvas: HTMLCanvasElement): void {
  if (matchMedia('(prefers-reduced-motion: reduce)').matches) return;
  const context = canvas.getContext('2d');
  if (!context) return;

  const ratio = window.devicePixelRatio || 1;
  const { width, height } = canvas.getBoundingClientRect();
  canvas.width = width * ratio;
  canvas.height = height * ratio;
  context.scale(ratio, ratio);

  const particles = burst(28);
  const originX = width / 2;
  const originY = height * 0.4;
  const start = performance.now();

  const frame = (now: number) => {
    const t = Math.min((now - start) / DURATION_MS, 1);
    context.clearRect(0, 0, width, height);
    if (t >= 1) return;
    context.globalAlpha = 1 - t;
    for (const p of particles) {
      const dx = Math.cos(p.angle) * p.speed * t;
      const dy = Math.sin(p.angle) * p.speed * t + 500 * t * t; // gravity
      context.save();
      context.translate(originX + dx, originY + dy);
      context.rotate(p.spinsPerSecond * t * 2 * Math.PI);
      context.fillStyle = p.color;
      context.fillRect(-p.size / 2, -p.size / 2, p.size, p.size);
      context.restore();
    }
    requestAnimationFrame(frame);
  };
  requestAnimationFrame(frame);
}
