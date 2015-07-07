# Virtual Slide Viewer
<p align="center">
<a href="https://raw.githubusercontent.com/wiki/Strachu/VirtualSlideViewer/Screenshots/MainWindow.png"><img src="https://raw.githubusercontent.com/wiki/Strachu/VirtualSlideViewer/Screenshots/MainWindow.png"/></a><br/>
<a href="https://github.com/Strachu/VirtualSlideViewer/wiki/Screenshots">More screenshots</a>
</p>
**Virtual Slide Viewer** is an application which allows you to view a virtual slide saved in one of more than 100 supported file formats.

# Features
- Cross platform (Windows, Linux)
- Viewing of **huge virtual slides** even on a computer with only 1GB RAM
- Conversion of a virtual slide saved in a closed format to a open format **OME-TIFF**
- More than **100** file formats supported. The application was tested with VSI, SVS, SCN and OME-TIFF formats.
- Support for virtual slides with multiple channels, Z planes and time points
- **Easy to use** and intuitive interface with customizable panels
- Arbitrary zoom level support
- **Fast** - the application uses multiple techniques for reducing the delay of loading the visible part of the image, such as the cache system, prefetching of invisible part and parallel, multi-core loading

# Requirements
To run the application you need:
- Microsoft Windows or any modern distribution of Linux (the application probably will also run on a Mac OS X but it was not tested)
- Any version (except OpenJDK 8u40) of <a href="http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html">Java JRE 8</a>
- At least 1GB RAM

Also a multicore CPU is recommended but not strictly required.

Additionally, if you want to compile the application you need:
- [Eclipse 4.4.1 or later](https://eclipse.org/)
- [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Apache Ant](http://ant.apache.org/) to create the executable JAR (optionally)

# Download
The binary releases and their corresponding source code snapshots can be downloaded at the [releases](https://github.com/Strachu/VirtualSlideViewer/releases) page.

If you would like to retrieve the most up to date source code and compile the application yourself, install git
and clone the repository by executing the command:
`git clone https://github.com/Strachu/VirtualSlideViewer.git` or alternatively, click the "Download ZIP" button at the side
panel of this page.

# Libraries
The application uses the following libraries:
- Swing for the creation of graphical user interface
- [Bioformats 5.1.1](http://www.openmicroscopy.org/site/support/bio-formats5.1/) to enable support for more than 100 file formats and allow for saving to a OME-TIFF.
- [Ehcache](http://ehcache.org/) as a cache memory system.
- [JUnit](http://junit.org/) as a unit test framework
- [Mockito 1.9.5](https://github.com/mockito/mockito) for the creation of stubs and mocks in unit tests

# Tools
During the creation of the application the following tools were used:
- [Eclipse Luna](https://eclipse.org/) with the following extensions:
  - [Window Builder](http://www.eclipse.org/windowbuilder/)
  - [Subclipse](http://subclipse.tigris.org/servlets/ProjectProcess?pageID=p4wYuA) (before the migration to GitHub)
- [Git](https://git-scm.com/)
- [Subversion](https://subversion.apache.org/) (before the migration to GitHub)
- [VisualVM](https://visualvm.java.net/) to detect the bottleneck in the application's performance
- [VirtualBox](https://www.virtualbox.org/) for testing the application on multiple operating system and testing the application in low memory conditions

# How to compile
1. Install a [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. Install Eclipse
3. Import the project to a eclipse:
  1. Right click in the area of "Package Explorer" panel
  2. Select "Import..."
  3. Select "General->Existing Projects into Workspace" and click the "Next" button
  4. Click the "Browse..." button at the side of "Select root directory:" and point to a directory where you have cloned the application
  5. Press the "Finish" button
4. The application will be built automatically, you can now run it by right-clicking the project's name and choosing "Run As" and then selecting "Main"

To build a JAR file:

1. Install an [Apache Ant](http://ant.apache.org/)
2. Open a command line
3. Execute (but first substitute the values in {} for correct path):
```
cd "{path_where_you_have_cloned_Virtual_Slide_Viewer}"
ant
```
4. The resulting files will be in {path_where_you_have_cloned_Virtual_Slide_Viewer}\build directory

# License
Virtual Slide Viewer is a free software distributed under the GNU GPL 3 or later license.

See LICENSE.txt and LICENSE_THIRD_PARTY.txt for more information.
