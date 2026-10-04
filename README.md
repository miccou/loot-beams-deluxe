# Loot Beams Deluxe

[![GitHub last commit](https://img.shields.io/github/last-commit/miccou/loot-beams-deluxe)](https://github.com/miccou/loot-beams-deluxe/commits/main/)
[![Active Installs](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/loot-beams-deluxe)](https://runelite.net/plugin-hub/show/loot-beams-deluxe)
[![Plugin Rank](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/loot-beams-deluxe)](https://runelite.net/plugin-hub/show/loot-beams-deluxe)

Ever wish your lootbeams had a bit more style and customization?

Loot Beams Deluxe swaps the Ground Items beams for effects pulled from all over the game, like Yama's meteor, the Doom of Mokhaiotl unique burrow glow, the quest cape emote, and other carefully selected animations. They are a lot more spectacular than a plain beam, can be recoloured, and add an extra bit of satisfaction when you get a good drop.

![Loot Beams Deluxe in action](demo.gif)

## Features

- **Eight value tiers, plus highlighted items** - each with its own gp threshold, colours, beam style, and fanfare.
- **Drop fanfare** - an optional effect that plays once the moment loot lands, then the beam takes over and stays while the item is on the ground.
- **Two-tone colours** - the beam is one colour, the detail wrapping around it is another, like Modern's lattice or Fire's bands. (some beams are single-colour assets, so only the first picker applies to those.)
- **Beam preview** - put a tier's fanfare and beam on a tile next to you, so you can see how it looks without waiting for a drop.
- **Ground Items sync** - reads your existing highlighted/hidden lists so you're not maintaining two sets. Same `name`, `name*`, and `name>quantity` syntax everywhere, plus an extra beam-only highlight list if you want one.

## Getting started

Setting it up is a lot like Ground Items:

1. Install Loot Beams Deluxe from the Plugin Hub.
2. Turn off the built-in beams in Ground Items (_Show lootbeam tier_ → Off, and untick _Show lootbeam for highlighted_) or you'll get doubled-up beams.
3. Your Ground Items highlighted and hidden lists are picked up straight away. Anything that should only get a beam goes in _Also highlight_.
4. Each tier comes with a gp value already set. Adjust them to suit, then pick each tier's colours, style, and fanfare (fanfares are off until you pick one).
5. Tick a tier under _Preview beams_ at the bottom of the config to see it next to you while you tweak it.

## How it decides what gets a beam

Same rules as Ground Items, so it should feel familiar: highlighted items always beam, hidden items never do, and otherwise the most valuable stack on the tile picks the tier. Value can be GE price, High Alch, or whichever is higher. Setting a tier to 0 gp disables it.

Which items count also follows Ground Items' ownership setting, unless you set _Ownership filter_ yourself.

## Styles

### Beams

| Style              | Where it's from                                                             |
| ------------------ | --------------------------------------------------------------------------- |
| Light              | the classic Ground Items beam                                               |
| Modern             | the newer Ground Items beam                                                 |
| Fire               | the flames you get for stealing Zamorak's wine                              |
| Electric           | the Grotesque Guardians' lightning                                          |
| Cloud              | a low rolling mist                                                          |
| Smoke              | a smoke devil's plume                                                       |
| Miasma             | the Abyssal Sire's swirl                                                    |
| Mokhaiotl glow     | the burrow-hole plume from a Doom of Mokhaiotl unique, with the dirt hidden |
| Level up fireworks | the level up fireworks, on repeat                                           |
| Dancing shade      | a ghost doing the goblin salute                                             |
| Quest cape         | the quest cape emote's flying saucer                                        |
| Kree'arra tornado  | Kree'arra's whirlwind attack                                                |
| Sun keris spec     | the sun keris special attack                                                |
| Yama shadow        | Yama's swirling shadow attack                                               |

### Fanfares

| Style             | Where it's from                                                    |
| ----------------- | ------------------------------------------------------------------ |
| Lightning strike  | the ToA Wardens: warning flash, then the strike                    |
| Rising column     | a ToA column that rises out of the ground and sinks back           |
| Trophy teleport   | the Combat Achievements trophy teleport, with its spinning laurels |
| Drakan incinerate | Drakan's fireball from Sins of the Father                          |
| Revenant heal     | the forked bolt a revenant calls down on itself when it heals      |
| Yama meteor       | Yama's meteor, shrunk down to fit a tile or two                    |
| Yama flame        | Yama's flame attack                                                |
| Yama shadow stomp | the cracks from Yama's shadow stomp                                |
| Blood torva       | the storm cloud from making sanguine torva                         |

## Issues and suggestions

Found a bug, or know an animation that would make a great beam? [Open an issue](https://github.com/miccou/loot-beams-deluxe/issues).

## Development

```
JAVA_HOME=/path/to/jdk11 ./gradlew build
./gradlew run   # boots a dev client with the plugin loaded (requires an OSRS login)
```
