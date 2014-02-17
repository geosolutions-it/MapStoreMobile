# MapStoreMobile #
MapStore Mobile is an Android native application based on MapStore to display MapStore maps, with offline data support.

## Features

### v 0.1

#### Local Database

* Browse the local database and add layers to the map
* Spatial Query on the local database
    * features that intersects a Bounding box
    * features that intersects a Circle (width center coordinates and radius size)
    * features that intersects Point (with a configurable buffer)
    * features that intersects a Polygon

#### Styling 
* Max and Min zoom level
* Decimation factor
* Point
    * shape 
    * fill color and opacity
    * border color and opacity
* Lines
    * dashing style
    * line color and opacity
* Polygon
    * Border color, dashing style and Opacity
    * fill color and opacity
    
#### GPS
* Draw my position on the map
* Follow my position 

#### Offline
* Offline background and usage of spatialite database

#### Online Maps
* Allows to load maps and add WMS Layers from any MapStore Instance on the web.
* Smart network usage grouping requests to the same WMS servers.
