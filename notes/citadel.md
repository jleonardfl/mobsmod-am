# Notes about Citadel
Citadel is an animation library made by the Alex Mobs guy.
Most of its classes look like boilerplate and the only ones the tasmanian
devil class use directly are in the `citadel.animation` subpackage: 
 * `citadel.animation.Animation`
 * `citadel.animation.AnimationHandler`
 * `citadel.animation.IAnimatedEntity`

There are some other interesting looking ones like a "leg solver", but they are
not used in the tasmanian devil code, and I didn't go digging.

## Animation
Animation only exposes one function, `Animation.create()`.  It accepts one 
argument, a duration integer measured in ticks. It gets used at the
top of the `EntityTasmanianDevil` class to declare a new animation object:

`public static final Animation ANIMATION_HOWL = Animation.create(40);`

`public static final Animation ANIMATION_ATTACK = Animation.create(8);`

The Animation class is probably where citadel stores the animation data.
You can check to see if `this.getAnimation() == ANIMATION_HOWL`, or you 
can use it to set animations.  This part actually seems pretty easy. 

`this.setAnimation(ANIMATION_HOWL)`

This function gets called in a lot of different places, for example, the 
`attackEntityAsMob()` class accepts an entity object and sets the animation to
`ANIMATION_ATTACK` if `this.getAnimation() == NO_ANIMATION`.  

## AnimationHandler
The TL;DR on this one is you only need it for one line at the end of the 
`tick()` function, which calls an `updateAnimations()` function to set the
changes made to the mob's animations on every tick.

The first use I've seen for AnimationHandler is at the end of the `tick()` 
function in `EntityTasmanianDevil.java`.  After a pretty gnarly series of 
`if()` statements to determine what the right animation is, we have: 

`AnimationHandler.INSTANCE.updateAnimations(this);`

Each Tasmanian Devil mob (or each running instance of 
`EntityTasmanianDevil.java`) sends a reference to itself to 
`updateAnimations()`, which is probably so the function can make changes to 
that Tasmanian Devil's data.
I don't know what the INSTANCE part is for, it's an enum constant declared in 
`AnimationHandler.class`, but the whole line it's declared on is just 
`INSTANCE;` so I'm going to assume it's a weird way to reference
an instance of something.

## IAnimatedEntity
This one is really important.  The class 
declaration line at line 44 of `EntityTasmanianDevil.java` contains
`implements IAnimatedEntity`.  Java files with a capital I at the beginning
of the filename are typically interfaces.  This is a special type of java file
that doesn't actually contain any code, but acts as a blueprint.  Basically,
you have to write the functions in the interface.  For example, the file
`IAnimatedEntity.class` contains:

`int getAnimationTick();`

which means you have to write a function called getAnimationTick() that returns
an int.  Similarly, it contains:

`com.github.alexthe666.citadel.animation.Animation[] getAnimations();`

which means you need to write a `getAnimations()` function that returns an 
array of `Animation` objects.  To see what these functions actually do in 
practice, you can CTRL+Click the `IAnimatedEntity` import at the top of the 
Tasmanian Devil file.  That'll open `IAnimatedEntity.class`.  If you get a
warning about the decompiler, I'd recommend disabling it.  Otherwise, it'll be
harder to look at what minecraft functions do without looking at the decompiled
code and possibly screwing yourself over as a mod dev.  From there, you can 
click the green circle to the left of the declarations in the interface
to browse through different implementations in different classes.  Intellij is
pretty nice.