# Droid Life
This is (another) clone of John Conway's game of life as an android live 
wallpaper. Although it was working well it has not yet been released, and at
the time of the initial commit some experiemental features have made it a
bit unstable. It is a work in progress.

## Life Engine
The game of life rules are implemented in me.dje.life.CellularGrid (possibly
a misleading name given that it is not generic) with graphics generated from
theme classes derived from me.dje.life.LifeBaseTheme which is an abstract base
class and handled by an instance of me.dje.life.ThemeManager which maps theme
names to classes.

## Graphs
Later in development i began to experiment with graphs which can be seen
in me.dje.graph, although that is an unfinished work and possibly the basis
of another project.

