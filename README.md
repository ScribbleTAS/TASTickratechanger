# TASTickrateChanger
Original by [Guichaguri](https://github.com/Guichaguri/TickrateChanger)  
Let's you change the server/client tickrate    
Fixed issues and hooked up things that didn't get affected by the tickrate  
Changed ASM method from coremod to [Mixin](https://github.com/SpongePowered/Mixin)  
Added tickrate 0, original code by [CubiTect](https://github.com/Cubitect/Cubitick)  
  
Original links to the mod:  
* [Downloads](http://guichaguri.github.io/TickrateChanger/)
* [CurseForge](http://minecraft.curseforge.com/mc-mods/230233-tickratechanger)
* [Minecraft Forums](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2421222-tickratechanger-change-the-speed-that-your-game)
* [Commands](https://github.com/Guichaguri/TickrateChanger/wiki/Commands)
* [API](https://github.com/Guichaguri/TickrateChanger/wiki/API)

## Development

Same old ForgeGradle setup: `gradlew setupDecompWorkspace`  
  
To build your mod, use `gradlew shadow`

To run it in your IDE, use `--mixin mixins.ticks.json --tweakClass org.spongepowered.asm.launch.MixinTweaker`in **Program Arguments**
