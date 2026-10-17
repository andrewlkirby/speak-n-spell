// Generates the web app's data from the Android app's sources, so there's one source of truth:
//   - src/generated/words.json  ← Levels.kt (levels + word lists) and res/drawable/img_*.xml (as SVG)
//   - public/sounds/*.wav       ← res/raw/*.wav
// Runs automatically before dev, build and test. Fails loudly if Levels.kt's shape changes in a
// way this parser doesn't understand, rather than silently shipping a broken word list.

import { copyFileSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const webRoot = join(dirname(fileURLToPath(import.meta.url)), '..');
const androidMain = join(webRoot, '..', 'app', 'src', 'main');
const levelsKt = join(androidMain, 'java', 'com', 'andrew', 'speaknspell', 'data', 'Levels.kt');
const drawableDir = join(androidMain, 'res', 'drawable');
const rawDir = join(androidMain, 'res', 'raw');

function fail(message) {
  console.error(`extract-words: ${message}`);
  process.exit(1);
}

const source = readFileSync(levelsKt, 'utf8');

// --- Level enum entries -------------------------------------------------------------------------
const levelPattern =
  /^\s*(\w+)\("([^"]+)",\s*"([^"]+)",\s*showWordText = (true|false),\s*distractorCount = (\d+|null)\)/gm;
const levels = [...source.matchAll(levelPattern)].map(([, id, displayName, icon, showWordText, distractorCount]) => ({
  id,
  displayName,
  icon,
  showWordText: showWordText === 'true',
  distractorCount: distractorCount === 'null' ? null : Number(distractorCount),
  words: [],
}));
if (levels.length === 0) fail('found no Level enum entries in Levels.kt');

// --- Word lists ---------------------------------------------------------------------------------
const wordPattern = /Word\("([^"]+)",\s*"([^"]+)"(?:,\s*R\.drawable\.(\w+))?\)/g;
const sections = source.split(/Level\.(\w+) to listOf\(/);
// split() with a capture group yields [preamble, id1, body1, id2, body2, ...]
for (let i = 1; i < sections.length; i += 2) {
  const level = levels.find((l) => l.id === sections[i]);
  if (!level) fail(`word list for unknown level ${sections[i]}`);
  for (const [, text, emoji, image] of sections[i + 1].matchAll(wordPattern)) {
    level.words.push(image ? { text, emoji, image } : { text, emoji });
  }
}

const parsedCount = levels.reduce((sum, l) => sum + l.words.length, 0);
const wordCallCount = (source.match(/\bWord\(/g) ?? []).length;
if (parsedCount !== wordCallCount) {
  fail(`parsed ${parsedCount} words but Levels.kt has ${wordCallCount} Word(...) calls — parser out of date?`);
}
for (const level of levels) {
  if (level.words.length === 0) fail(`level ${level.id} has no words`);
}

// --- Vector drawables → SVG ---------------------------------------------------------------------
/** Android colors are #RGB, #RRGGBB or #AARRGGBB; SVG wants alpha last. */
function color(value) {
  if (/^#[0-9a-f]{8}$/i.test(value)) {
    const alpha = parseInt(value.slice(1, 3), 16) / 255;
    return { color: `#${value.slice(3)}`, alpha };
  }
  return { color: value, alpha: 1 };
}

function vectorToSvg(xml) {
  const tags = xml.replace(/<!--[\s\S]*?-->/g, '').matchAll(/<(\/?)(\w+)([^>]*?)(\/?)>/g);
  let svg = '';
  for (const [, closing, name, attrText, selfClosing] of tags) {
    const attrs = Object.fromEntries([...attrText.matchAll(/android:(\w+)="([^"]*)"/g)].map((m) => [m[1], m[2]]));
    if (closing) {
      svg += name === 'vector' ? '</svg>' : name === 'group' ? '</g>' : '';
      continue;
    }
    if (name === 'vector') {
      svg += `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${attrs.viewportWidth} ${attrs.viewportHeight}">`;
    } else if (name === 'group') {
      const rotate = attrs.rotation ? ` transform="rotate(${attrs.rotation} ${attrs.pivotX ?? 0} ${attrs.pivotY ?? 0})"` : '';
      svg += `<g${rotate}>`;
      if (selfClosing) svg += '</g>';
    } else if (name === 'path') {
      let out = `<path d="${attrs.pathData}"`;
      if (attrs.fillColor) {
        const fill = color(attrs.fillColor);
        const alpha = fill.alpha * Number(attrs.fillAlpha ?? 1);
        out += ` fill="${fill.color}"${alpha < 1 ? ` fill-opacity="${+alpha.toFixed(3)}"` : ''}`;
      } else {
        out += ' fill="none"';
      }
      if (attrs.strokeColor) {
        const stroke = color(attrs.strokeColor);
        out += ` stroke="${stroke.color}" stroke-width="${attrs.strokeWidth ?? 1}"`;
        if (stroke.alpha < 1) out += ` stroke-opacity="${+stroke.alpha.toFixed(3)}"`;
        if (attrs.strokeLineCap) out += ` stroke-linecap="${attrs.strokeLineCap}"`;
        if (attrs.strokeLineJoin) out += ` stroke-linejoin="${attrs.strokeLineJoin}"`;
      }
      svg += `${out}/>`;
    } else {
      fail(`unsupported vector drawable element <${name}>`);
    }
  }
  return svg;
}

const images = {};
for (const image of new Set(levels.flatMap((l) => l.words.map((w) => w.image).filter(Boolean)))) {
  images[image] = vectorToSvg(readFileSync(join(drawableDir, `${image}.xml`), 'utf8'));
}

// --- Write outputs ------------------------------------------------------------------------------
const generatedDir = join(webRoot, 'src', 'generated');
mkdirSync(generatedDir, { recursive: true });
writeFileSync(join(generatedDir, 'words.json'), JSON.stringify({ levels, images }, null, 2) + '\n');

const soundsDir = join(webRoot, 'public', 'sounds');
mkdirSync(soundsDir, { recursive: true });
const sounds = readdirSync(rawDir).filter((f) => f.endsWith('.wav'));
for (const sound of sounds) copyFileSync(join(rawDir, sound), join(soundsDir, sound));

console.log(
  `extract-words: ${levels.map((l) => `${l.id}=${l.words.length}`).join(', ')}; ` +
    `${Object.keys(images).length} images; ${sounds.length} sounds`,
);
