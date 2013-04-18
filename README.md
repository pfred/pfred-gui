pfred-gui
=========

Repository for PFRED GUI codes

All third party libraries needed to compile the PFRED GUI are provided in the lib folder. MarvinBeans
from ChemAxon is required to run PFRED GUI, but is not provided.

Compiling
---------
* Generate PFRED-version.jar - `ant jar`

Running
--------
In order to run, you need to procure MarvinBeans-5.x.jar from [ChemAxon](http://www.chemaxon.com), 
and modify marvin.lib.dir and marvin.jar.name in `build.xml` to link to it.

* Run PFRED - `ant run`

