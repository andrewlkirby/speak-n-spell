// One-off: renders the home-screen icons from the Android launcher icon (white "A" on orange),
// plus the README's install QR code. Outputs are committed; rerun with `npm run icons` if the
// launcher icon or the site URL changes.

import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import QRCode from 'qrcode';
import sharp from 'sharp';

const webRoot = join(dirname(fileURLToPath(import.meta.url)), '..');
const res = join(webRoot, '..', 'app', 'src', 'main', 'res');
const iconsDir = join(webRoot, 'public', 'icons');
const SITE_URL = 'https://andrewlkirby.github.io/speak-n-spell/';

const foreground = readFileSync(join(res, 'drawable', 'ic_launcher_foreground.xml'), 'utf8');
const letterPath = foreground.match(/android:pathData="([^"]+)"/)[1];
const background = readFileSync(join(res, 'values', 'colors.xml'), 'utf8').match(
  /name="ic_launcher_background">([^<]+)</,
)[1];

// The launcher foreground is drawn on a 108×108 canvas whose central 72×72 is the safe zone,
// the same convention as maskable PWA icons — so one square, full-bleed artwork serves both.
const icon = (radius = 0) =>
  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">` +
  `<rect width="108" height="108" rx="${radius}" fill="${background}"/>` +
  `<path d="${letterPath}" fill="#FFFFFF"/></svg>`;

writeFileSync(join(iconsDir, 'favicon.svg'), icon(24) + '\n');

const pngs = [
  ['icon-192.png', 192],
  ['icon-512.png', 512],
  ['maskable-512.png', 512],
  ['apple-touch-icon-180.png', 180], // iOS applies its own rounded mask
];
for (const [name, size] of pngs) {
  await sharp(Buffer.from(icon())).resize(size, size).png().toFile(join(iconsDir, name));
}

await QRCode.toFile(join(webRoot, '..', 'docs', 'install-qr.png'), SITE_URL, { width: 320, margin: 2 });

console.log(`generate-icons: wrote ${pngs.length} PNGs, favicon.svg and docs/install-qr.png`);
