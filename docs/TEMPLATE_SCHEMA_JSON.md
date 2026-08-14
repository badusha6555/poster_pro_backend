# `templates.schema_json` — Poster Layout Schema

Defines where each dynamic field is drawn on top of a template's
`background_image_key` image when a poster is generated
(`POST /api/templates/{id}/generate`).

## Shape

```json
{
  "canvasWidth": 1080,
  "canvasHeight": 1350,
  "placeholders": [
    { "type": "text", "field": "shopName", "x": 60, "y": 1180, "fontSize": 42, "fontFamily": "playfair", "color": "#F5D061", "align": "left" },
    { "type": "text", "field": "rate22k916", "x": 60, "y": 1240, "fontSize": 32, "fontFamily": "inter", "color": "#FFFFFF", "align": "left" },
    { "type": "image", "field": "shopLogo", "x": 900, "y": 1150, "width": 120, "height": 120 }
  ]
}
```

| Key | Type | Applies to | Meaning |
|---|---|---|---|
| `canvasWidth` / `canvasHeight` | int | root | Expected pixel size of the background image. The background is drawn stretched to this size if it doesn't already match. |
| `placeholders` | array | root | Ordered list of things to paint on top of the background, in order (later entries paint over earlier ones). |
| `type` | `"text"` \| `"image"` | placeholder | Whether this placeholder renders text or an image. |
| `field` | string | placeholder | Which data source to pull the value from — see mapping below. Unknown/unrecognized `field` values are skipped (logged, not fatal), so older schemas keep working if new field types are added later. |
| `x` / `y` | int | placeholder | Top-left drawing position in canvas pixels. For text, `y` is the text baseline's row per `align`, not a strict top-left (standard `Graphics2D.drawString` behavior). |
| `fontSize` | int | text | Font size in points. |
| `fontFamily` | string | text | Logical font key resolved via `FontRegistry` — currently `"playfair"` (headings/serif) or `"inter"` (body/sans). See [Adding fonts](#adding-fonts). |
| `color` | string (`#RRGGBB`) | text | Text fill color. |
| `align` | `"left"` \| `"center"` \| `"right"` | text | Horizontal alignment relative to `x`. |
| `width` / `height` | int | image | Box the image (currently only the shop logo) is scaled into. |

## Field → data source mapping

`field` names are kept **consistent with existing DTO/entity property names**
(`GoldRateDto`, `GoldRateProfile`, `User`) rather than inventing new ones:

| `field` | Source | Notes |
|---|---|---|
| `shopName` | `User.shopName` | The generating shop owner's profile. |
| `shopLogo` | `User.logoUrl` | Treated as a **MinIO object key** (not a public URL — there is no logo upload endpoint yet, so this is forward-compatible only). If null/blank, or the object can't be loaded, the logo placeholder is skipped silently — it does not fail poster generation. |
| `rate22k916` | Request override, falling back to `GoldRateProfile.rate22k916` | |
| `rate18k` | Request override, falling back to `GoldRateProfile.rate18k` | |
| `rate14k` | Request override, falling back to `GoldRateProfile.rate14k` | |
| `rate9k` | Request override, falling back to `GoldRateProfile.rate9k` | |

Rate placeholders render as `₹<value>` with thousands separators (e.g. `₹6,850`).
If neither a request override nor a saved profile value exists for a given
purity, that placeholder is skipped.

## Adding fonts

See `src/main/resources/fonts/README.md`.

## Background image vs. thumbnail

`templates.thumbnail_url` is a small **public** catalogue preview image (shown
in template browse/search cards). `templates.background_image_key` is a
separate, **private**, print-resolution image stored in MinIO and only used
as the base layer for poster generation — the two are intentionally decoupled
so the catalogue thumbnail can be a lightweight/compressed asset while the
generation source stays full quality.
