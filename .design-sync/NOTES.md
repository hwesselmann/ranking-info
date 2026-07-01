# design-sync notes — ranking-info

## Repo shape

This repo has **no React/JS component library** — it's a server-rendered Java/Spring Boot app (jte templates) styled with Tailwind CSS v4 plus a small set of hand-authored `.ri-*` utility classes in `src/main/resources/static/css/application.src.css`. There is no `.storybook/`, no `*.stories.*`, no `.tsx`/`.jsx`/`.mdx` files anywhere. This is the documented "tokens-only DS" path (see `non-storybook/SKILL.md` → Known limitations): `package-build.mjs` emits `styles.css` only, with an empty-bodied `_ds_bundle.js`.

## Config choices and why

- `shape: "package"` — set explicitly; there's nothing for auto-detect to find.
- `entry: "./index.js"` — this file does **not** exist. It only exists so `--entry`'s directory-walk lands on the repo root's `package.json` (name `ranking-info`) instead of failing when `--node-modules`-based resolution can't find `node_modules/ranking-info` (this repo isn't installed as a dependency anywhere). `resolveDistEntry` then soft-fails on the missing file and the converter falls through to its synth-entry-from-src path, which finds zero `.tsx`/`.jsx` files and correctly produces the empty tokens-only bundle.
- `cssEntry: "src/main/resources/static/css/application.css"` — the **compiled** Tailwind output (committed per this repo's own convention; rebuild it first with `npx @tailwindcss/cli -i application.src.css -o application.css` before syncing, so the bundle reflects current styles). Do NOT point this at `application.src.css` — that file still has unresolved `@import "tailwindcss"` and raw `@theme` blocks.
- `runtimeFontPrefixes: ["Inter"]` — Inter is loaded via a Google Fonts `<link>` tag in `layout.jte`, not shipped as `@font-face`/woff2 anywhere in this repo. This suppresses `[FONT_MISSING]` legitimately (see `[FONT_MISSING]` / `[FONT_REMOTE]` table rows) — do not chase it or try to source font files.
- `readmeHeader: ".design-sync/conventions.md"` — authored to correct the generic React-bundle README boilerplate (which is misleading for a components-less DS) and to teach the design agent the real `.ri-*` class vocabulary + brand tokens.

## Build commands (for future re-syncs)

```
npx @tailwindcss/cli -i src/main/resources/static/css/application.src.css -o src/main/resources/static/css/application.css
mkdir -p .ds-sync && cp -r <skill-base-dir>/{package-build.mjs,package-validate.mjs,package-capture.mjs,resync.mjs,lib,storybook} .ds-sync/
echo '{"name":"ds-sync-deps","private":true}' > .ds-sync/package.json
(cd .ds-sync && npm i esbuild ts-morph @types/react react react-dom)
node .ds-sync/resync.mjs --config .design-sync/config.json --node-modules ./.ds-sync/node_modules --out ./ds-bundle --no-render-check --remote .design-sync/.cache/remote-sync.json
```

Note the extra `react react-dom` install (beyond the skill's documented `esbuild ts-morph @types/react`) — the converter's `vendorReact` step runs unconditionally even for a zero-component DS, and this repo has no React anywhere else to source it from.

## Render check

Skipped with `--no-render-check` (there are 0 `<Name>.html` previews to render — playwright/chromium would verify nothing). User confirmed this via AskUserQuestion on the first sync. Re-confirm this is still the right call if components are ever added later (i.e. this stops being a tokens-only DS).

## Re-sync risks

- If this project ever gains real components (e.g. a React admin UI added alongside the Java app), the `entry`/`cssEntry`/`runtimeFontPrefixes` config above still applies, but `shape` detection and the tokens-only branch (`cfg.cssEntry || existsSync(styles.css)`) should be re-checked — the zero-component path was chosen deliberately, not a fallback to fix.
- `application.css` is committed but must be freshly rebuilt before every sync (see build commands above) — a stale compiled CSS silently uploads outdated tokens/classes.
- 5 tokens declared in `application.src.css`'s `@theme` block (`--color-brand-dark`, `--color-accent-dark`, `--color-surface-alt`, `--color-rank-up`, `--color-rank-down`) are **not** emitted into the compiled CSS because Tailwind v4 only emits `@theme` custom properties that are actually referenced by a generated utility class; these five are only ever used as hardcoded hex literals elsewhere in the same CSS file, never via `var(--...)` in a jte template. They were deliberately left out of `conventions.md`'s token table since they don't exist in the shipped bundle. If a future template starts using them via an arbitrary-value utility (e.g. `text-[color:var(--color-rank-up)]`), they'll start appearing in the compiled output and should be added to the conventions table then.
- `readmeHeader` (`conventions.md`) was authored this run — on future re-syncs it is validated, not rewritten; if it starts naming a class/token that no longer verifies against a fresh build, that's a drift finding to fix here, not something the converter will catch on its own.
