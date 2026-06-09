# LTweaks — заметки по проекту

Мод на Minecraft **1.21.1 / NeoForge** (`com.ltweaks`, modId `ltweaks`).
Только клиент. Все хендлеры — через `@EventBusSubscriber(value = Dist.CLIENT)`.

Этот файл — «якорь» состояния проекта. Если переписка сожмётся, читать сначала его, потом сами файлы в воркспейсе (они всегда актуальны).

---

## Структура

```
LTweaks/
├── build.gradle              (moddev 1.0.21, Java 21)
├── gradle.properties         (neo_version=21.1.233)
├── settings.gradle
├── NOTES.md                  (этот файл)
└── src/main/
    ├── java/com/ltweaks/
    │   ├── LTweaks.java                 главный @Mod, пустой
    │   └── client/
    │       ├── Easing.java              easeInOutQuad/easeOutQuad/easeExpoOut/clamp01
    │       ├── HotbarAnimator.java      анимация хотбара (меню + чат)
    │       ├── HealthState.java         битовая маска эффектов (8 состояний)
    │       ├── HealthBar.java           рендер полоски HP + брони + сердца
    │       ├── ItemNameAnim.java        scale-анимация HIN при смене слота
    │       └── HudHandler.java          все @SubscribeEvent, точка сборки
    └── resources/
        ├── META-INF/accesstransformer.cfg   (открывает Gui.renderHotbar)
        ├── META-INF/neoforge.mods.toml
        ├── pack.mcmeta
        └── assets/ltweaks/textures/gui/
            ├── health.png   320x40
            ├── armor.png    160x5
            └── hearts.png   18x72
```

---

## Реализованные фичи

1. **Удалены полоски над хотбаром** — отменяются слои: PLAYER_HEALTH, FOOD_LEVEL, AIR_LEVEL, ARMOR_LEVEL, EXPERIENCE_BAR, EXPERIENCE_LEVEL, JUMP_METER, VEHICLE_HEALTH. Хотбар не трогается.
2. **HUD за меню** — хотбар при меню рисуется в `ScreenEvent.Render.Pre` (до фона/блюра экрана), поэтому уходит ПОД блюр.
3. **Хотбар уезжает вниз при меню** — easeInOutQuad 0.33s, вверх при закрытии — easeOutQuad 0.33s. Смещение MENU_DROP = 60px.
4. **Чат поднимает хотбар** на CHAT_RISE = 16px, easeExpoOut 0.33s (открытие и закрытие).
5. **Кастомная полоска HP** над хотбаром (см. ниже).
6. **HIN (selected item name)** — scale 1.25→1.0 easeExpoOut при смене слота; НЕ заменяет ваниль, только оборачивает слой в transform. Двигается вместе с хотбаром (yOffset).
7. **Чат «шторкой»** — scissor-маска раскрывается снизу-вверх по chatProgress (easeExpoOut). Применяется и к слою CHAT (сообщения, +CHAT_LIFT), и ко всему ChatScreen (включая поле ввода) в onScreenPre/Post.
8. **Поле ввода чата** проявляется той же маской-шторкой (не slide). Хотбар при этом поднимается на CHAT_RISE через yOffset.
9. **Чат приподнят** на CHAT_LIFT = 10px чтобы не наслаиваться на HP-бар.
10. **Прицел и индикатор атаки отцентрованы** — слой CROSSHAIR сдвинут на (+0.5, +0.5).
11. **Анимация открытия контейнеров** (AbstractContainerScreen: сундуки, инвентарь) — выезд снизу-вверх, 0.2s, easeExpoOut, SLIDE=24px. Меню паузы/моды/главное меню НЕ анимируются (они не AbstractContainerScreen).
12. Везде: НЕ заменять ванильные элементы (совместимость с модами). Исключение — ручная перерисовка хотбара при меню (нужно для анимации ухода).

---

## Полоска здоровья (HealthBar.java)

Геометрия:
- BAR_W=80, BAR_H=5, HEART=9
- X_LEFT=91 → `x = guiWidth/2 - 91` (как ванильный HP)
- `y = guiHeight - 29` (2px над хотбаром)
- сердце: центр = левый край полоски (`hx = x - HEART/2`)

Слои отрисовки (снизу вверх по экрану): back → ghost(белая) → fill(красная) → armor_back → armor_fill → outline(blink) → сердце.
Порядок «броня сверху как обводка» — намеренный (запрос пользователя).

Анимации:
- красная полоска (fill) — МГНОВЕННАЯ (показывает актуальное HP)
- белая ghost — плавно опускается при потере HP, easeExpoOut, GHOST_DURATION=0.6s
- броня — плавно, easeExpoOut, ARMOR_DURATION=0.4s
- мерцание при HP<35% (LOW_THRESHOLD) — outline + 2-й проход сердца, период BLINK_PERIOD=1.0s (0.5 видно / 0.5 нет)
- тряска при HP<50% (SHAKE_THRESHOLD) — sin/cos дрожание, амплитуда SHAKE_MAX=1.5px * t² (экспон. рост к 0 HP), SHAKE_SPEED=28

---

## Состояния эффектов (HealthState.java)

Битовая маска, COUNT = 8:
- WITHER=1 (MobEffects.WITHER)
- POISON=2 (MobEffects.POISON)
- FREEZE=4 (player.isFullyFrozen())

Индексы состояний (= ряд в атласах):
```
0 none
1 wither
2 poison
3 wither+poison
4 freeze
5 wither+freeze
6 poison+freeze
7 wither+poison+freeze (all)
```

---

## Раскладка атласов

### health.png (320×40)
4 слоя по горизонтали × 8 состояний по вертикали.
- `uOffset = layer*80`  (layer: 0=back, 1=ghost, 2=fill, 3=outline)
- `vOffset = state*5`
- заполнение по % = обрезка ширины: `w = 80*percent` (uOffset слоя фикс → берётся левая часть)

### armor.png (160×5)
2 слоя по горизонтали, без состояний.
- `uOffset = layer*80` (0=back, 1=fill)
- `vOffset = 0`

### hearts.png (18×72)
2 колонки × 8 строк.
- `uOffset = low ? 9 : 0` (col0 = >50% HP, col1 = ≤50% HP)
- `vOffset = state*9`

Чтобы перерисовать спрайты — соблюдать раскладку. Поменять число слоёв/порядок — через константы вверху HealthBar.java.

---

## Анимации хотбара/чата (HotbarAnimator.java)

Все длительности DURATION = 0.33s. Время от System.nanoTime() (1 вызов на update).
- menuCurrent: 0..1, открытие easeInOutQuad / закрытие easeOutQuad
- chatCurrent: 0..1, easeExpoOut в обе стороны
- `yOffset() = menuCurrent*60 - chatCurrent*16`
- `chatRise() = chatCurrent*16`
- `chatProgress() = chatCurrent`
- `update()` зовётся из HOTBAR-слоя (нет меню) и из onScreenPre

Константы:
- MENU_DROP=60, CHAT_RISE=16

## HudHandler константы
- CHAT_LIFT=10 (подъём слоя CHAT над HP-баром)
- CHAT_BOTTOM=40, CHAT_AREA=90 (область scissor-маски слоя CHAT)
- ChatScreen маскируется целиком (от низа экрана на guiHeight*progress вверх)

## ContainerAnim
- DURATION=0.2s, OPEN_SLIDE=24px, easeExpoOut
- ОТКРЫТИЕ: ScreenEvent.Opening -> open(); slide-up живого экрана (openSlideOffset в onScreenPre/Post)
- ЗАКРЫТИЕ (подход "ghost"): Mixin onClose НЕ отменяет закрытие -> экран закрывается сразу
  (игрок свободен, хотбар едет вверх). Mixin сохраняет ссылку на Screen как ghost.
  HudHandler.onRenderGuiPost (RenderGuiEvent.Post, когда mc.screen==null) рисует ghost.render()
  со сдвигом вниз за нижний край экрана (closeSlideOffset(guiHeight)). При closeDone() -> clearGhost().
- БЕЗ fade/затемнения, просто уезжает вниз за экран.
- containerPushed: флаг баланса push/pop для open-slide

## Mixin
- ContainerScreenMixin: onClose @At HEAD (без cancel) -> captureGhost. Надёжно.
- ScreenMixin: отменяет renderBlurredBackground(float) и renderTransparentBackground(GuiGraphics)
  у ВСЕХ экранов -> нет затемнения/блюра позади меню.
- НЕ инжектить в renderBg (abstract -> NPE) и не в render (NeoForge переделал -> 0 targets).
- mixin-конфиг: src/main/resources/ltweaks.mixins.json, в neoforge.mods.toml [[mixins]]

## Движение при открытом меню
- HudHandler.onClientTick (ClientTickEvent.Post): когда mc.screen != null, синхронизирует
  KeyMapping движения (keyUp/Down/Left/Right/Jump/Shift/Sprint) с реальным состоянием GLFW
  через InputConstants.isKeyDown -> key.setDown(). WASD+прыжок+шифт+спринт работают.
  Мышь остаётся на слотах (обзор не крутится). Sneak синхронен с физ. шифтом
  (держишь шифт - крадёшься, отпустил - встал; сохраняется при откр/закр меню).
- При паузе одиночной игры ClientTickEvent не идёт -> движение в pause screen недоступно (ожидаемо).

## HUD над закрывающимся меню
- ghost рисуется в RenderGuiEvent.Pre (ДО HUD-слоёв) -> HUD/хотбар рисуются ПОВЕРХ ghost.

## Чат (актуальная логика)
- слой CHAT (входящие в HUD): только lift на CHAT_LIFT=10px, БЕЗ маски
- ChatScreen (при открытии): scissor-шторка снизу-вверх по chatProgress (нижние h*progress пикселей)
- хотбар при чате поднимается на CHAT_RISE=16 через yOffset

---

## Известные ограничения / TODO

- П.6: маска чата раскрывается снизу-вверх, но НЕ привязана к Y-позиции последнего сообщения (требует копаться в ChatComponent, хрупко → отложено). Сообщения при этом не исчезают.
- Абсорбция (жёлтые сердца) в проценте HP не учитывается — только MAX_HEALTH.
- Текстуры сейчас — плейсхолдеры (сгенерированы скриптом), нужно перерисовать.

---

## Ключевые API (1.21.1, проверено)
- `Gui.renderHotbar(GuiGraphics, DeltaTracker)` — private, открыт через AT
- `GuiGraphics.blit(ResourceLocation, x, y, float u, float v, w, h, texW, texH)`
- `GuiGraphics.enableScissor(minX, minY, maxX, maxY)` / `disableScissor()`
- `ScreenEvent.Render.Pre/Post` — getGuiGraphics(), getScreen()
- `RenderGuiLayerEvent.Pre/Post` — getName(), getGuiGraphics()
- `VanillaGuiLayers.*` — HOTBAR, CHAT, SELECTED_ITEM_NAME, и т.д.
- `mc.getTimer()` → DeltaTracker
- `player.getInventory().selected` — текущий слот хотбара
