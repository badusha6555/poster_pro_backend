# Bundled fonts

Two open-license fonts are bundled here and registered at startup by
`com.posterpro.api.poster.FontRegistry`:

| File | Logical key (used in `schema_json.fontFamily`) | Role |
|---|---|---|
| `PlayfairDisplay-Regular.ttf` | `playfair` | Serif — headings (e.g. shop name) |
| `Inter-Regular.ttf` | `inter` | Sans — body text (e.g. gold rates) |

Both are variable fonts pinned to their default ("Regular") instance and are
licensed under the **SIL Open Font License 1.1**, sourced from the official
Google Fonts repository:

- https://github.com/google/fonts/tree/main/ofl/playfairdisplay
- https://github.com/google/fonts/tree/main/ofl/inter

The OFL permits bundling/redistribution as part of a larger software project,
which is why these were chosen over Windows system fonts (Arial, Calibri,
etc.), which are not licensed for redistribution.

## Adding more fonts

1. Drop a `.ttf` file into this directory (`src/main/resources/fonts/`).
2. Register it in `FontRegistry.FONT_FILES` with a logical key — that key is
   what template designers reference in `schema_json.fontFamily`.
3. Only regular-weight static (or default-instance variable) TTFs have been
   tested; bold/italic variants would need their own logical key and file
   (`Font.createFont` loads exactly one face per file).
