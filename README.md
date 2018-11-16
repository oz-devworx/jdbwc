<a href="https://github.com/oz-devworx/jdbwc/wiki"><img src="https://jdbwc.oz-devworx.com.au/images/7/70/MediaWikiSidebarLogo.png" align="right" hspace="10" vspace="10" /></a>
# jdbwc
Type-3 JDBC Driver - with middleware server written in PHP.<br>
[More info is available in the wiki](https://github.com/oz-devworx/jdbwc/wiki)

What's what?
============
*Don't rename any folders or files or it might break the Ant script.*


1) In your IDE, set the Java source folders. They begin with "*src-*".
   The PHP sources are in *resources/server-side-bundle/upload*,

2) The dependencies are in the *dependencies* folder. Add them to your editors class path (as a user library).
   They also get used by the Ant build so don't move or rename the folder or files.

3) Use the Ant build script (*build.xml*) to automate the build process, then look for the folder *DISTRIBUTION* (created during the build process).


**src-jdbwc**        Contains the Driver source files

**src-dataHandler**  Contains the DataHandler source files (required by the Driver)

**src-jdbwctest**    Contains test sources (for testing the Driver)

**dependencies**     Contains the JDBWC dependencies for this release.

**resources**        Contains the server-side PHP bundle

**build.xml**        Ant build file. Builds distribution set with javadocs to a folder named *DISTRIBUTION* in the same directory as this file.

**README_DEV.txt**   This file.

---
For details on driver installation see *Install_xxxx.html* in the resources folder (where *xxxx* represents the version number).
  (After running the build script the Install doc will also be in the distribution dir.)
  
For basic details on using the DataHandler (other than in the driver) see its *META-INF/README*.
