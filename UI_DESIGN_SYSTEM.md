# Frontier Settlement — UI Design System & Component Reference

**Version:** 1.0 (Пункт 31 — Единая дизайн-система)  
**Framework:** Jetpack Compose + Material Design 3 Customized Design Tokens  

---

## 1. Overview & Core Philosophy

The Frontier Settlement UI Design System provides a coherent, tactile, and highly readable tactical interface tailored for mobile landscape and portrait play.

Key Principles:
1. **Grounded Tactical Canvas:** Dark slate surfaces with calibrated contrast (`GameTheme.colors.background` = `#090D14`, `surface` = `#111722`, `surfaceElevated` = `#182232`).
2. **Predictable Semantic Palette:** Emerald for safe/positive, Amber for warning/craft, Cyan for tech/AP, Crimson for danger/combat, Violet for anomalies/storage.
3. **Hierarchy of Action:** Every screen has exactly ONE dominant primary action (`PrimaryActionButton`), supported by secondary buttons (`SecondaryActionButton`) or compact row triggers (`CompactActionButton`).
4. **No Raw Hardcoded Hex:** All screens reference `GameTheme.colors`, `GameTheme.typography`, `GameTheme.spacing`, and `GameTheme.shapes`.
5. **Universal Fallback Rendering:** Any missing AI art automatically falls back to atmospheric procedural canvas artwork via `VisualAssetResolver`.

---

## 2. Token System Reference (`GameTokens.kt`)

### Colors (`GameTheme.colors`)
- **Backgrounds:** `background` (`#090D14`), `backgroundAlt` (`#06090F`), `backgroundModal` (`#0D131D`)
- **Surfaces:** `surface` (`#111722`), `surfaceElevated` (`#182232`), `surfaceHighlight` (`#222F44`), `surfaceSelected` (`#162B28`)
- **Borders:** `border` (`#243247`), `borderLight` (`#3B4F6E`), `borderFocus` (`#10B981`)
- **Accents:** `primary` (`#10B981`), `secondary` (`#38BDF8`), `accentWarm` (`#F59E0B`), `accentOrange` (`#FB923C`), `accentMilitary` (`#E11D48`)
- **Semantic Feedback:** `success` (`#10B981`), `warning` (`#F59E0B`), `danger` (`#EF4444`), `info` (`#38BDF8`), `disabled` (`#334155`)
- **Resource Colors:**
  - `resFood` (`#22C55E`), `resWater` (`#06B6D4`), `resFuel` (`#F97316`), `resMaterials` (`#FB923C`)
  - `resMedicine` (`#14B8A6`), `resAmmo` (`#E11D48`), `resComponents` (`#818CF8`), `resCredits` (`#FACC15`)

### Spacing (`GameTheme.spacing`)
- `xxs`: 2.dp, `xs`: 4.dp, `sm`: 8.dp, `md`: 12.dp, `lg`: 16.dp, `xl`: 24.dp, `xxl`: 32.dp
- `screenHorizontal`: 16.dp, `screenVertical`: 12.dp, `cardPadding`: 14.dp, `touchTargetMin`: 48.dp

### Shapes (`GameTheme.shapes`)
- `badge`: 4.dp, `small`: 8.dp, `medium`: 12.dp, `card`: 14.dp, `large`: 18.dp, `modal`: 20.dp, `full`: 999.dp

### Typography (`GameTheme.typography`)
- `gameTitle`: 26.sp Black
- `screenTitle`: 20.sp Bold
- `sectionTitle`: 15.sp Bold
- `cardTitle`: 14.sp SemiBold
- `body`: 13.sp Normal (`#CBD5E1`)
- `bodySecondary`: 12.sp Normal (`#94A3B8`)
- `caption`: 10.sp Medium (`#64748B`)
- `numericHero`: 24.sp Black
- `numericValue`: 15.sp Bold
- `buttonText`: 13.sp Bold
- `badgeText`: 10.sp Bold

---

## 3. Standard Component Library

| Component | Purpose | Key Parameters |
|---|---|---|
| `PrimaryActionButton` | Dominant primary CTA on a screen (48dp height) | `text`, `onClick`, `icon`, `isEnabled`, `isLoading` |
| `SecondaryActionButton` | Outlined secondary action (44dp height) | `text`, `onClick`, `icon`, `isEnabled` |
| `DangerActionButton` | Critical destructive actions (red theme) | `text`, `onClick`, `icon` |
| `CompactActionButton` | Dense list actions and trade buttons (34dp height) | `text`, `onClick`, `icon`, `containerColor` |
| `IconGameButton` | Tactical 40dp square icon button with optional badge | `icon`, `contentDescription`, `onClick`, `badgeColor` |
| `GameCard` | Base tactile container with 1dp border | `backgroundColor`, `borderColor`, `content` |
| `InteractiveCard` | Selectable card with active feedback & outline | `isSelected`, `onClick`, `content` |
| `CompactCard` | Dense horizontal row card | `content` |
| `WarningCard` | Highlighted warning/danger box for alerts | `title`, `message`, `isCritical`, `actionText` |
| `SectionHeader` | Standard section divider with colored accent pill | `title`, `accentColor`, `actionText`, `counterText` |
| `DangerBadge` | Standardized DangerLevel pill | `dangerLevel: DangerLevel` |
| `ResourceAmount` | Standard resource value + icon + delta tag | `type`, `amount`, `delta`, `isDeficit` |
| `CharacterPortrait` | Role avatar with border, badge & leader star | `role`, `isLeader`, `isSelected`, `isInExpedition` |
| `StatProgressBar` | Progress bar for HP, Cargo, Storage, Morale | `label`, `current`, `max`, `barColor` |
| `ActionPointsBar` | Tactical segmented AP pips | `currentAP`, `maxAP`, `pipSize` |
| `GameTopBar` | Unified top bar for all sub-screens | `title`, `subtitle`, `onBack`, `actions` |
| `GameConfirmationDialog` | Tactical modal dialog for critical decisions | `title`, `message`, `confirmText`, `isDanger` |
| `GameEmptyState` | Empty state placeholder with tactical icon | `title`, `description`, `icon`, `actionText` |
| `LocationHeroArt` | Atmospheric hero artwork with fallback shader | `visualAssetId`, `locationType`, `height` |

---

## 4. UI Gallery Inspector

Access the interactive design system inspector anytime in-game via:  
**Меню → Отладка и генераторы → UI Design System Gallery (Пункт 31)**.

This live gallery provides visual test cases for every button variant, card, badge, progress indicator, dialog, and empty state.
