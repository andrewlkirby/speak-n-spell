/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import { VitePWA } from 'vite-plugin-pwa';

// Served from GitHub Pages at https://andrewlkirby.github.io/speak-n-spell/
export default defineConfig({
  base: '/speak-n-spell/',
  plugins: [
    VitePWA({
      registerType: 'autoUpdate',
      includeManifestIcons: false, // already precached by globPatterns below
      workbox: {
        // Precache everything, sounds included, so the app works fully offline after first visit.
        globPatterns: ['**/*.{js,css,html,svg,png,wav,webmanifest}'],
      },
      manifest: {
        name: 'Speak & Spell',
        short_name: 'Speak & Spell',
        description: 'A spelling game for kids: hear a word, spell it with letter tiles.',
        lang: 'en-US',
        display: 'standalone',
        orientation: 'any',
        background_color: '#FFFBF2',
        theme_color: '#FF6F3C',
        icons: [
          { src: 'icons/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'icons/icon-512.png', sizes: '512x512', type: 'image/png' },
          { src: 'icons/maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
    }),
  ],
  test: {
    environment: 'node',
  },
});
