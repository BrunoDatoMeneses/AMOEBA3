#The Save System
The save system is composed of two main elements, and some GUI elements :

## The Backup System :
Found in `kernel.backup`.

A backup system is an object responsible for reading/writing the state of amoeba from/to a file. It implement the interface `IBackupSystem`. The implementation provided is `BackupSystem` and use xml to store data.

Usage :
```Java
AMOEBA amoeba = new AMOEBA()
IBackupSystem backupSystem = new BackupSystem(amoeba);
backupSystem.load(new File("path/to/a/save.xml"));
// do some learning with the amoeba ...
backupSystem.save(new File("path/to/a/new/file.xml"))
```

## The Save Helper :
Found in `kernel.backup`.

A save helper is an object that provide additional functionality over a BackupSystem for the user or other components. Most importantly it :
- Create a temporary directory for saves
- Allow automatic saving during amoeba execution
- Add the options to save/load on the GUI
- Add functionality used by the SaveExplorer.

See `ISaveHelper` for more.

Two implementation are provided : the fully functioning `SaveHelperImpl` and `SaveHelperDummy` that do nothing. The dummy is used to deactivate ALL automatic saving, giving a HUGE performance boost.

Each amoeba has a save helper, available with `amoeba.saver`
```Java
AMOEBA amoeba = new AMOEBA();
// This constructor initialize the amoeba's saver with a dummy, meaning :
amoeba.saver.save("this_does_not_save");
// will do nothing

// or ...
AMOEBA amoeba = new AMOEBA("path/to/save.xml", null);
// This constructor create and use a SaveHelperImpl to initilize the amoeba
amoeba.saver.save("a/valid/path/save.xml");
// will create a save, and
amoeba.saver.autosave();
// will create a save in the temporary directory creates by the save helper, located in 
System.out.println(SaveHelperImpl.dir)
```
If amoeba crash, the save files will not be automatically deleted, you may have to manually clean the save directory.  

## The Save Explorer
Found in `gui.saveExplorer`.

The save explorer read saves from an amoeba's SaveHelper and offer some graphical tool, most importantly the ability to preview a save and quickly cycle trough saves. Allowing to visually play back the execution of an amoeba.

See the [GUI description](gui.md) for more detail on the GUI.
