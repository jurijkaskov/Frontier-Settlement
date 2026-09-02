# Frontier Settlement — Art Style Guide & Visual Direction

**Version:** 1.0 (Пункт 31 — Единый художественный стиль)  
**Target Genre:** Grounded Atmospheric Mobile 2D Survival & Economic Strategy  

---

## 1. Artistic Direction & Vision

Frontier Settlement is a grounded, atmospheric, tactile mobile strategy game set in a weathered post-collapse world.
The visual world feels **rugged, worn, calm, tactical, and grounded**.

### Key Aesthetic Pillars:
- **Tonal Grounding:** Muted earthy palettes (deep slate `#090D14`, weathered steel `#243247`, safe emerald `#10B981`, amber hazard `#F59E0B`).
- **Tactical Clarity:** High contrast for small mobile screens, generous touch targets (min 48dp), clear hierarchical typography.
- **Atmospheric Lighting:** Soft directional lighting, dusk/overcast horizons, subtle warm interior window glows contrasted against cold wilderness.
- **Physical Wear & Tear:** Patched canvas, welded scrap plating, rusted reinforcement, modular container architecture.
- **Zero Fantasy/Cyberpunk Slop:** No neon holograms, no glowing magic runes, no cartoonish anime expressions, no hyper-glossy fantasy tropes.

---

## 2. Art Consistency Prompt Prefix

When generating visual assets with generative AI models (Imagen, Midjourney, etc.), **ALWAYS** prepend the following master prefix to maintain absolute artistic cohesion across all categories:

```text
Frontier Settlement visual style: grounded semi-realistic 2D digital game illustration, modern post-collapse environment, muted earthy palette, subtle cinematic lighting, realistic proportions, slightly painterly texture, cohesive mobile strategy game art, no text, no logo, no UI.
```

---

## 3. Asset Categories & Generation Templates

### A. Character Portraits (768×768, 1:1, Neutral Dark Textured BG)
- **Framing:** Chest-up / waist-up portrait, 3/4 face turn.
- **Lighting:** Soft directional rim light, realistic weathered skin, grounded facial expressions.
- **Apparel:** Practical modular vests, survival collars, respirators around neck, canvas straps.
- **Prompt Template:**
  ```text
  [MASTER PREFIX] waist-up character portrait of [CHARACTER DESCRIPTION / ROLE], practical worn survival clothing, neutral dark textured background, soft directional cinematic lighting, readable facial expression, consistent camera angle, square composition.
  ```

### B. Location Hero Art (1280×720, 16:9, Landscape)
- **Framing:** Wide establishing panoramic shot, single dominant focal landmark.
- **Composition Safe Zone:** Bottom 35% reserved for UI gradient scrim and typography overlay.
- **Lighting:** Moody overcast daylight, foggy dawn, or dusk horizon.
- **Prompt Template:**
  ```text
  [MASTER PREFIX] wide atmospheric 2D digital concept illustration of [LOCATION DESCRIPTION / LANDMARK], cinematic overcast lighting, weathered industrial structures, overgrown vegetation, muted earthy tones, 16:9 landscape aspect ratio.
  ```

### C. Enemies & Hostile Creatures (768×768, 1:1)
- **Framing:** Menacing waist-up or three-quarter profile, grounded and realistic threat level.
- **Prompt Template:**
  ```text
  [MASTER PREFIX] waist-up creature or enemy portrait of [ENEMY DESCRIPTION], rugged survival gear, scarred leathery texture, dark neutral textured background, soft dramatic rim lighting, square composition.
  ```

### D. Transport & Vehicles (768×768 or 1024×576, Transparent BG)
- **Framing:** 3/4 isometric perspective, isolated on transparent background.
- **Style:** Rugged chassis, knobby off-road tires, welded bullbars, modular cargo racks.
- **Prompt Template:**
  ```text
  [MASTER PREFIX] isolated 2D game illustration of [VEHICLE DESCRIPTION], heavy reinforced steel frame, off-road equipment, realistic mechanical wear, transparent background, three-quarter view, centered.
  ```

### E. Items & Equipment (512×512, 1:1, Transparent BG)
- **Framing:** Centered isolated survival item, distinct silhouette.
- **Prompt Template:**
  ```text
  [MASTER PREFIX] isolated game item illustration of [ITEM DESCRIPTION], worn practical survival equipment, three-quarter view, clear silhouette, subtle realistic texture, transparent background, centered.
  ```

### F. Map POI Markers & Icons (256×256, Vector / Stylized Flat)
- **Framing:** High-contrast tactical silhouette icon with distinct geometric outline.
- **Prompt Template:**
  ```text
  [MASTER PREFIX] stylized tactical 2D map marker icon of [LANDMARK TYPE], bold geometric silhouette, high contrast, clean vector style, dark background, centered.
  ```

---

## 4. File Naming & Directory Structure Conventions

All raster and vector game assets must follow standardized lowercase snake_case naming:

| Asset Type | File Name Pattern | Recommended Resolution | Format |
|---|---|---|---|
| Locations | `loc_[category]_[name]_[idx].webp` | 1280×720 (16:9) | WEBP |
| Characters | `char_portrait_[role]_[idx].webp` | 768×768 (1:1) | WEBP |
| Enemies | `enemy_[type]_[name]_[idx].webp` | 768×768 (1:1) | WEBP |
| Vehicles | `veh_[type]_[idx].webp` | 768×768 (1:1) | WEBP (with alpha) |
| Items | `item_[category]_[name].webp` | 512×512 (1:1) | WEBP (with alpha) |
| Buildings | `bld_[name]_[lvl].webp` | 640×640 (1:1) | WEBP |
| Map Icons | `map_icon_[type].xml` | 24×24 / 48×48 | Android Vector XML / WEBP |

---

## 5. Visual "Do's" and "Don'ts"

###  DO:
- Use consistent warm amber (`#F59E0B`) and emerald (`#10B981`) highlights against dark slate surfaces (`#090D14`, `#111722`).
- Include practical survival details: zip-ties, duct-tape patches, reinforced welds, cargo netting.
- Maintain soft gradient overlays (`HeroImageOverlay`) behind text over hero artwork.
- Use distinct icons and colors simultaneously for semantic status (never color alone).

###  DON'T:
- NEVER bake text, letters, English words, or UI numbers directly into generated images.
- NEVER use cartoonish, exaggerated proportions or anime hair/eyes.
- NEVER introduce neon cyan/magenta cyberpunk lights or glowing magic sparkles.
- NEVER use generic Android Material defaults (e.g. purple/teal material theme, default floating elevation shadows).
