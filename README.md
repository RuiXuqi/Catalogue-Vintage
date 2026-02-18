# Catalogue Vintage

Backport of MrCrayfish's wonderful mod [Catalogue, aka MrCrayfish's Mod List](https://github.com/MrCrayfish/Catalogue) to mc 1.12.2.

![processed_image](https://github.com/user-attachments/assets/db62091c-797d-48aa-bf1b-f02ade1c8b98)

## Notes

- Catalogue Vintage mod does not have the ability to read custom props in mcmod.info since legacy Forge doesn't provide any APIs.
- There are two branches: 1.12.2-Cleanroom and 1.12.2-Forge. Make sure you have chosen the right platform.
- The repo is just a test field of [Cleanroom#422](https://github.com/CleanroomMC/Cleanroom/pull/422) and the cleanroom version will stop updating if the PR is merged.
- For developers, you can try to add new props in mcmod.info. Catalogue in the PR above has the ability to read them and apply. The new props follow. 
  - iconFile: a string, should point to a square PNG file.
  - iconItem: a string, should be in the format "minecraft:stone". Metadata is allowed. This will not be applied if iconFile is valid.
  - backgroundFile: a string, should point to a 512x256 PNG file. It will be the mod's background.
  - license: a string, will be displayed at the bottom of the mod info page.
  - issueTrackerUrl: a string, should be a link. The "Submit Bug" button will open it when clicked.

  Or if you want to apply them to Catalogue mod, you can use `@Mod.CustomProperty` in `@Mod` annotation.
  For reference, see [Catalogue.java](src/main/java/com/cleanroommc/catalogue/Catalogue.java).

  Only the new props are available through annotation, and will be overridden by those in mcmod.info in the PR.

  It is completely unnecessary to add Catalogue to your build script.

![Catalogue Banner](https://github.com/user-attachments/assets/0692aba1-b38f-42b6-b0f5-5f5de278c3e0)
**Available on:**

![fabric](https://github.com/user-attachments/assets/cce9715b-7a3c-454d-9717-7acafbf27a5c) ![neoforge](https://github.com/user-attachments/assets/5c01362c-e3b7-4e58-9299-2f7023d107e1)  ![forge](https://github.com/user-attachments/assets/ea78b0c2-3fbb-480c-bb20-2f47ccabc58f) 

**Download:**

[![Download](https://img.shields.io/static/v1?label=&message=Official%20Website&color=2d2d2d&labelColor=dddddd&style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAALGPC/xhBQAAAAlwSFlzAAALEQAACxEBf2RfkQAAAAd0SU1FB98BHA41LJJkRpIAAAAYdEVYdFNvZnR3YXJlAHBhaW50Lm5ldCA0LjAuMvvhp8YAAAGGSURBVDhPjZK9SgNBFIVHBEUE8QeMhLAzdzdYBFL7CmnstEjjO4iFjWClFiGJm42VFpYSUptKRfABFBQE7UUI2AmGmPXM5k6cjFE8cJjZ3fPduTOzIiJarvl+bPmuLuWS+K/KmcwUoK4pEGoTnetvYTY7GUpZOCTa0sZi+QRyhQ83pkBShKjDUBvzGKNxr6rU8W4uN8FoX4CKEcB6EMR61OZOEtApEqNIndFvAbiNuAB30QeU+sDYBXSCdw3MrzHvlnx/kdFkC3kU6NkwF3iM0ukFnNM8R0UplZquKHWE85nhV0Kg4o4GrDMwre5x5G9h9aaBHL/VPI849rtqRFfwqALaIcd+F87gYnDy1ha0sY12VcoiR0cLq5/ae3e3gyLbHB2tMAgKQ1fXh+z5a1mpDXQyy8hPIdgyAEMDW8+fWOQJHbfgzXUhxhkXYh/3jcC9C7s2V43c+9DPpHXgeXMIXbqQbV7gAV5hbFixEGMVolUEmvjjXuBeRcoOnp/R/hm81hi0LsQX8OcRBvBjZ8YAAAAASUVORK5CYII=)](https://mrcrayfish.com/mods?id=catalogue) [![Curseforge](http://cf.way2muchnoise.eu/full_catalogue_downloads.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/catalogue)
# Catalogue

Catalogue is a universal mod list menu that is designed to work on all modloaders. Catalogue creates a rich experience for players by: 
- Having a **modern and intuitive** UI design
- Giving you the option to **search the mod list**.
- **Filtering** mods based on if they have an update, a config, and more.
- Allowing you to **Favourite a mod**, which allows for quicker access when sorted by _Favourite First_.
- **Showing the dependencies** of a mod by _Right Click > Show Dependencies_ on a mod in the list.
- **Hiding libaries** added by modloaders and mods.
- Providing **shortcuts** to access a mod's configs, wesbite homepage, and submit issues to their tracker.

Catalogue automatically supports logo images of Forge/NeoForge mods, and Icons from Fabric mods. It will also utilise items from a mod if the icon is not present. If you're using Fabric, you can allow Mod Menu's config entrypoint to work with Catalogue using [Menulogue](https://www.curseforge.com/minecraft/mc-mods/menulogue).

## Developers

By default, Catalogue will attempt to use an existing assets like Fabric's `logo` and NeoForge/Forge's `logoFile`. However Catalogue has additional branding options that give you full control over the look and feel of your mod. To learn more about this feature, see the [Branding Guide](https://github.com/MrCrayfish/Catalogue/wiki/Branding-Guide).

Catalogue can be added to your workspace simply through CurseForge Maven.

```gradle
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "CurseForge"
                url = "https://cursemaven.com"
            }
        }
        filter {
            includeGroup "curse.maven"
        }
    }
}
```
Then depending on your platform, implement the mod as follows and replace `<file_id>` with the id of a file coresponding to the version you want to include. You can find the list of files for Catalogue [here](https://www.curseforge.com/minecraft/mc-mods/catalogue/files/all). Simply click on a file and find the `Curse Maven Snippet` section and it will show the file id. Learn more about CurseMaven and its features at the [offical website](https://cursemaven.com/).

```geadle
dependencies {
    // Fabric
    modRuntimeOnly "curse.maven:catalogue-459701:<file_id>"

    // NeoForge
    runtimeOnly "curse.maven:catalogue-459701:<file_id>"

    // Forge
    runtimeOnly fg.deobf("curse.maven:catalogue-459701:<file_id>")
}
```

## Screenshots
![processed_image](https://github.com/user-attachments/assets/5843a6c7-49f4-4bbf-a52f-362abaaf5de1)
