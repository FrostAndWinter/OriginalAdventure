# Original Adventure 
[![Build Status](https://travis-ci.org/FrostAndWinter/OriginalAdventure.svg?branch=master)](https://travis-ci.org/FrostAndWinter/OriginalAdventure)
[![Coverage Status](https://coveralls.io/repos/FrostAndWinter/OriginalAdventure/badge.svg?branch=master&service=github)](https://coveralls.io/github/FrostAndWinter/OriginalAdventure?branch=master)


The most original adventure of 2015
Github Repository: https://github.com/FrostAndWinter/OriginalAdventure

## Requirements

* Java 8 or above

* Gradle

## Running Game
We’ve provided two Gradle scripts to run the project since some machines (notably ones with Intel Graphics) often crash when run with the ‘correct’ settings. If at all possible, run the program using the high quality script, since on capable computers that will likely have better performance (it uses deferred shading, making the lighting cheaper). On the lower quality script, you may notice some texture issues on the chest, key, and ceiling, along with mediocre performance.

The modes are 'runPretty' and 'runUgly'

* ECS: run `./gradlew-ecs {mode} run`

* Otherwise: run `./gradlew {mode} run`

## Gameplay

The game is designed for two players to play; however, there are workarounds that we have left in the game so that one person can complete the majority of it.
In particular, both the primary mouse button and the E key can be used to perform a primary action. I’ll outline how you can use that to bypass a puzzle below.

Press ` or ~ (tilde) to view the controls.

## WALKTHROUGH OF FIRST SECTION

When you first enter the room, look in the chest on the right-hand side. Pick up the note and read it by pressing the I key.
The solution to the riddle it poses is to turn all of the levers in the room on; the one on the left wall as you spawn requires you to hold the action button. A key will appear between the lights as you do this (red and blue should be on, and green should be off). Then, look over to the key, move slightly closer, and hit the action button (E/left click, whichever you’re not holding for the lever). You’ll then pickup the key and be able to leave the room.

The rest of the game requires two people, but the first room demonstrates the key concepts and mechanics.
