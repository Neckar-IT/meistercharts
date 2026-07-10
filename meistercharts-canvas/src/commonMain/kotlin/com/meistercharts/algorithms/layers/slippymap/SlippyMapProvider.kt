/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.algorithms.layers.slippymap

import com.meistercharts.tile.TileIndex
import it.neckar.open.http.Url
import kotlin.math.abs
import kotlin.reflect.KProperty0

/**
 * Provides urls to slippy map tile servers.
 *
 * For a list of tile servers see [TileServers](https://wiki.openstreetmap.org/wiki/Tile_servers)
 *
 * Or look here:
 * https://raw.githubusercontent.com/leaflet-extras/leaflet-providers/master/leaflet-providers.js
 *
 */
interface SlippyMapProvider {
  /**
   * Compute the url of a slippy map tile for the given tile index and zoom
   */
  fun url(tileIndex: TileIndex, zoom: Int): Url

  /**
   * Retrieve the legal notice for this provider
   */
  val legalNotice: String?

  companion object {
    /**
     * Contains all base map providers (opaque tiles that render as the map itself).
     */
    val all: List<SlippyMapProvider> = listOf(
      CartoDBPositron,
      CartoDBPositronNoLabels,
      CartoDBDarkMatter,
      CartoDBDarkMatterNoLabels,
      CartoDBVoyager,
      CartoDBVoyagerNoLabels,
      CartoDBVoyagerLabelsUnder,
      MtbMap,
      OpenTopoMap,
      MemoMaps,
      Cyclosm,
      CyclosmLite,
      OsmFrance,
      OpenStreetMap,
      OpenStreetMapDe,
      OpenStreetMapHumanitarian,
      WikimediaMaps
    )

    /**
     * Contains all overlay providers (transparent tiles meant to be painted on top of a base map from [all]).
     */
    val overlays: List<SlippyMapProvider> = listOf(
      OpenRailwayMap,
      OpenSeaMap,
      WaymarkedTrailsHiking,
      WaymarkedTrailsCycling,
      CartoDBLightOnlyLabels
    )
  }
}

/**
 * Returns the leftmost host label for the given [tileIndex] as a single lowercase letter, distributing tiles
 * across [subDomainCount] parallel tile hosts.
 *
 * Tile servers publish their tiles under several sibling subdomains that differ only in that first letter, so
 * a browser can open more parallel connections. The letters are always `a`, `b`, `c`, … (never `a1` or the
 * like); this label is prepended to the rest of the host in [SlippyMapProvider.url]:
 * - `subDomainCount = 3` → `a.tile.openstreetmap.org`, `b.tile.openstreetmap.org`, `c.tile.openstreetmap.org`
 * - `subDomainCount = 4` → `a.basemaps.cartocdn.com` … `d.basemaps.cartocdn.com`
 *
 * The letter is derived deterministically from the tile position, so the same tile always resolves to the
 * same host - which keeps the tile caches warm.
 */
private fun rotatingSubDomain(tileIndex: TileIndex, subDomainCount: Int): String {
  val modulo = (abs(tileIndex.subX.value) + abs(tileIndex.subY.value)) % subDomainCount
  return ('a' + modulo).toString()
}

/**
 * Creates a [SlippyMapProvider] that delegates all calls to the current value of this property.
 */
fun KProperty0<SlippyMapProvider>.delegate(): SlippyMapProvider {
  val property = this
  return object : SlippyMapProvider {
    override fun url(tileIndex: TileIndex, zoom: Int): Url {
      return property.get().url(tileIndex, zoom)
    }

    override val legalNotice: String?
      get() {
        return property.get().legalNotice
      }
  }
}

/**
 * Retrieve slippy map tiles from an openstreetmap server
 *
 * [Policies](https://operations.osmfoundation.org/policies/tiles/)
 */
data object OpenStreetMap : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 3)}.tile.openstreetmap.org/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  // see also https://www.openstreetmap.org/copyright/en
  override val legalNotice: String = "© OpenStreetMap contributors"

}

/**
 * Retrieve slippy map tiles from an openstreetmap server with german location names
 *
 * [Policies](https://operations.osmfoundation.org/policies/tiles/)
 */
data object OpenStreetMapDe : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 3)}.tile.openstreetmap.de/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  // see also https://www.openstreetmap.org/copyright/en
  override val legalNotice: String = "© OpenStreetMap contributors"

}

/**
 * Retrieve slippy map tiles from an openstreetmap server using the humanitarian map style
 *
 * [Policies](https://operations.osmfoundation.org/policies/tiles/)
 * [HumanitarianMap](https://wiki.openstreetmap.org/wiki/Humanitarian_map_style)
 */
data object OpenStreetMapHumanitarian : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 2)}.tile.openstreetmap.fr/hot/${zoom}/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  // see also https://www.openstreetmap.org/copyright/en
  override val legalNotice: String = "© OpenStreetMap contributors, Tiles style by Humanitarian OpenStreetMap Team"

}

/**
 * Retrieve slippy map tiles from the wikimedia server
 *
 * [TermsOfUse](https://foundation.wikimedia.org/wiki/Maps_Terms_of_Use)
 */
data object WikimediaMaps : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://maps.wikimedia.org/osm-intl/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  // https://foundation.wikimedia.org/wiki/Maps_Terms_of_Use
  override val legalNotice: String = "© OpenStreetMap contributors, Wikimedia"

}

// https://wiki.openstreetmap.org/wiki/Raster_tile_providers
data object Cyclosm : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 3)}.tile-cyclosm.openstreetmap.fr/cyclosm/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, Tiles style by CyclOSM, hosted by OpenStreetMap France"
}

data object CyclosmLite : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 3)}.tile-cyclosm.openstreetmap.fr/cyclosm-lite/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, Tiles style by CyclOSM, hosted by OpenStreetMap France"
}

data object OsmFrance : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 3)}.tile.openstreetmap.fr/osmfr/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, Tiles hosted by OpenStreetMap France"
}

data object MemoMaps : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://tile.memomaps.de/tilegen/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, Tiles: ÖPNVKarte / MeMoMaps"
}

data object OpenTopoMap : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 3)}.tile.opentopomap.org/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, SRTM, Map style: © OpenTopoMap (CC-BY-SA)"
}

// http://leaflet-extras.github.io/leaflet-providers/preview/index.html
// http://leaflet-extras.github.io/leaflet-providers/preview/index.html#filter=MtbMap
data object MtbMap : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://tile.mtbmap.cz/mtbmap_tiles/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors & USGS"
}

// http://leaflet-extras.github.io/leaflet-providers/preview/index.html
// http://leaflet-extras.github.io/leaflet-providers/preview/index.html#filter=CartoDB.Positron
data object CartoDBPositron : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 4)}.basemaps.cartocdn.com/light_all/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors © CARTO"
}

// http://leaflet-extras.github.io/leaflet-providers/preview/index.html
// http://leaflet-extras.github.io/leaflet-providers/preview/index.html#filter=CartoDB.PositronNoLabels
data object CartoDBPositronNoLabels : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 4)}.basemaps.cartocdn.com/light_nolabels/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors © CARTO"
}

// http://leaflet-extras.github.io/leaflet-providers/preview/index.html
// http://leaflet-extras.github.io/leaflet-providers/preview/index.html#filter=CartoDB.DarkMatter
data object CartoDBDarkMatter : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 4)}.basemaps.cartocdn.com/dark_all/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors © CARTO"
}

// http://leaflet-extras.github.io/leaflet-providers/preview/index.html
// http://leaflet-extras.github.io/leaflet-providers/preview/index.html#filter=CartoDB.DarkMatterNoLabels
data object CartoDBDarkMatterNoLabels : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 4)}.basemaps.cartocdn.com/dark_nolabels/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors © CARTO"
}

// http://leaflet-extras.github.io/leaflet-providers/preview/index.html
// http://leaflet-extras.github.io/leaflet-providers/preview/index.html#filter=CartoDB.Voyager
data object CartoDBVoyager : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 4)}.basemaps.cartocdn.com/rastertiles/voyager/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors © CARTO"
}

// http://leaflet-extras.github.io/leaflet-providers/preview/index.html
// http://leaflet-extras.github.io/leaflet-providers/preview/index.html#filter=CartoDB.VoyagerNoLabels
data object CartoDBVoyagerNoLabels : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 4)}.basemaps.cartocdn.com/rastertiles/voyager_nolabels/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors © CARTO"
}

// http://leaflet-extras.github.io/leaflet-providers/preview/index.html
// http://leaflet-extras.github.io/leaflet-providers/preview/index.html#filter=CartoDB.VoyagerLabelsUnder
data object CartoDBVoyagerLabelsUnder : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 4)}.basemaps.cartocdn.com/rastertiles/voyager_labels_under/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors © CARTO"
}

/**
 * Label-only overlay (transparent tiles) from CARTO.
 * Meant to be painted on top of a label-free base map.
 */
data object CartoDBLightOnlyLabels : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 4)}.basemaps.cartocdn.com/light_only_labels/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors © CARTO"
}

/**
 * Railway network overlay (transparent tiles).
 *
 * [OpenRailwayMap](https://www.openrailwaymap.org/)
 */
data object OpenRailwayMap : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://${rotatingSubDomain(tileIndex, 3)}.tiles.openrailwaymap.org/standard/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, Style: © OpenRailwayMap (CC-BY-SA 2.0)"
}

/**
 * Nautical seamark overlay (transparent tiles).
 *
 * [OpenSeaMap](https://www.openseamap.org/)
 */
data object OpenSeaMap : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://tiles.openseamap.org/seamark/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, © OpenSeaMap contributors"
}

/**
 * Hiking route overlay (transparent tiles).
 *
 * [Waymarked Trails](https://hiking.waymarkedtrails.org/)
 */
data object WaymarkedTrailsHiking : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://tile.waymarkedtrails.org/hiking/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, © waymarkedtrails.org (CC-BY-SA)"
}

/**
 * Cycling route overlay (transparent tiles).
 *
 * [Waymarked Trails](https://cycling.waymarkedtrails.org/)
 */
data object WaymarkedTrailsCycling : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return Url.absolute("https://tile.waymarkedtrails.org/cycling/$zoom/${tileIndex.xAsInt()}/${tileIndex.yAsInt()}.png")
  }

  override val legalNotice: String = "© OpenStreetMap contributors, © waymarkedtrails.org (CC-BY-SA)"
}
