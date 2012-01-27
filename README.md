# Compiler Base Plugin for Gradle

This is a base plugin for anyone who cares to implement a compiler. If you want to do that, it's presumed that you know enough to 
figure out how this plugin works.  If you need help figuring it out or would like some other help, feel free to contact 
[Robert Fischer](http://github.com/RobertFischer/) for consulting support.

## Known Uses

* [Mirah](http://github.com/RobertFischer/Gradle-Mirah-Compiler)
* [JavaCC/JJTree](http://github.com/RobertFischer/Gradle-Javacc-Plugin) 

## Changelog

### 0.0.4

* Fixed an issue where the main sourceset would attempt to be modified. Now working with gradle-1.0-milestone-7

### 0.0.3

* Added support for post-configuration of an AbstractCompile task, and eradicated the documentation stuff because we weren't using it anyway.

## TODOs 

* Provide support for "compilers" which generate code, with a special case for compilers which generate Java code. (Think JJTree and Javacc.)

