## What this design system actually is

This is **not** a React component library — it's a server-rendered Java app (jte templates) styled with **Tailwind CSS v4 + a small set of custom utility classes**. No components are bound here (`_ds_bundle.js` is an intentionally empty placeholder — ignore the generic "React must be on the page" loading instructions above; only `styles.css` matters). Build designs as plain HTML/CSS using the vocabulary below, not as React components imported from a bundle.

## Setup

Load one file: `<link rel="stylesheet" href="styles.css">`. No provider, no script tag, no JS runtime needed.

## Brand tokens (`var(--*)`)

Real CSS custom properties shipped in `_ds_bundle.css`, declared once and reused everywhere — always reference them via `var(--name)`, never hardcode the hex:

| Token | Value | Use |
|---|---|---|
| `--color-brand` | `#0B3A75` (navy) | primary actions, headers, hero background |
| `--color-brand-light` | `#1A5BA8` | links, lighter accents |
| `--color-ink` | `#0B2240` | body text, headings |
| `--color-accent` | `#2ED0B7` (teal) | highlights, active states, eyebrows |
| `--color-surface` | `#F0F5FB` | card headers, hover backgrounds, subtle fills |
| `--color-muted` | `#5A7A9E` | secondary text |
| `--color-faint` | `#9BB5D0` | tertiary/label text |
| `--font-sans` | `"Inter", -apple-system, …` | the only font family — loaded at runtime via Google Fonts (`Inter:wght@400;500;600;700`), not shipped as a file. Always set `font-family: var(--font-sans)` rather than naming "Inter" directly. |

Beyond these named brand tokens, the full Tailwind utility palette (spacing, generic colors, breakpoints, etc.) is also available in `_ds_bundle.css` — read it directly for anything not covered above.

## The styling idiom: Tailwind utilities + `.ri-*` component classes

Two layers, used together: standard Tailwind utility classes for layout/spacing (`flex`, `gap-4`, `p-4`, `grid-cols-2`, …), plus a small set of hand-authored `.ri-*` classes for the recurring UI patterns this app actually has. Prefer an existing `.ri-*` class over rebuilding the same look from utilities.

| Class | Pattern |
|---|---|
| `.ri-card`, `.ri-card-pad`, `.ri-card-header`, `.ri-card-footer` | White rounded card (`border-radius: 0.75rem`), optional padded body, a tinted header bar, and a footer of evenly-split links |
| `.ri-hero`, `.ri-hero-tag` | Navy hero block with a teal uppercase eyebrow tag, white heading, translucent-white body text |
| `.ri-btn`, `.ri-btn-primary`, `.ri-btn-accent`, `.ri-btn-ghost` | Inline-flex pill/rounded buttons: solid navy, solid teal, or ghost-on-dark |
| `.ri-table-wrap`, `.ri-table` | Bordered, rounded table container; dark navy header row, zebra-striped body, hover highlight |
| `.ri-select`, `.ri-input` | Form controls with a shared border/focus style (teal focus ring) |
| `.ri-toc`, `.ri-toc-title` | Sidebar table-of-contents nav with an active-state left border |
| `.ri-section-label` | Small uppercase teal "eyebrow" label above a section |
| `.ri-faq`, `.ri-faq-q`, `.ri-faq-a` | Card-style FAQ entry with icon + question + answer |
| `.ri-metric` | Stat block: small muted label + large navy value |
| `.ri-status-badge` (with `.k`/`.v` children) | Two-tone key/value pill (dark key, teal value) |
| `.ri-imprint` (with `.row`/`.lbl`/`.val`) | Label/value row list (used for contact/imprint info) |
| `.ri-note` | Left-accented callout/note box |
| `.ri-icon`, `.ri-icon-green/-red/-yellow/-teal` | Inline icon sizing + semantic color variants |

## Where the truth lives

Read `_ds_bundle.css` (via `styles.css`'s import) for the exact compiled rules and the full Tailwind utility set. The original hand-authored source (for reference, not shipped) is `src/main/resources/static/css/application.src.css` in the app repo — every `.ri-*` class and brand token above is defined there under `@theme` and `@layer components`.

## Example

```html
<div class="ri-card">
  <div class="ri-card-header">Latest Ranking</div>
  <div class="ri-card-pad">
    <p class="ri-section-label">This week</p>
    <div class="ri-metric">
      <p class="label">Position</p>
      <p class="value">#12</p>
    </div>
  </div>
  <div class="ri-card-footer">
    <a href="#">Details</a>
    <a href="#">History</a>
  </div>
</div>
```
