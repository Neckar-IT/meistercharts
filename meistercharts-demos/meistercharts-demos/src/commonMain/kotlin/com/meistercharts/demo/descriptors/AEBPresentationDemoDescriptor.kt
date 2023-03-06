/**
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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.NeckarItFlowLayer
import com.meistercharts.algorithms.layers.SloganLayer
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontFamily
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.FontWeight
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyStroke

/**
 *
 */
class AEBPresentationDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "AEB Presentation"

  override val category: DemoCategory = DemoCategory.NeckarIT

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addLayer(FillBackgroundLayer {
            dark()
          })

          layers.addLayer(NeckarItFlowLayer {
            opacity = 0.1
          })


          val slideModel = SlideModel()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Notification

            override val keyEventHandler: CanvasKeyEventHandler = object : CanvasKeyEventHandler {
              override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
                if (event.keyStroke == KeyStroke(KeyCode.Left)) {
                  slideModel.previous()
                  markAsDirty()

                  return EventConsumption.Consumed
                }

                if (event.keyStroke == KeyStroke(KeyCode.Right)) {
                  slideModel.next()
                  markAsDirty()

                  return EventConsumption.Consumed
                }

                return EventConsumption.Ignored
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
            }
          })

          val topTextLayer = SloganLayer(SloganLayer.Data { _, _ -> slideModel.current().text }) {
            sloganFont = FontDescriptorFragment(family = FontFamily("Oswald"), weight = FontWeight.Regular, size = FontSize(170.0))
            anchorDirection = Direction.BottomCenter
            sloganOffsetY = -30.0
          }
          layers.addLayer(topTextLayer)

          val bottomTextLayer = SloganLayer(SloganLayer.Data { _, _ -> slideModel.current().subText.orEmpty() }) {
            sloganFont = FontDescriptorFragment(family = FontFamily("Oswald"), weight = FontWeight.ExtraLight, size = FontSize(120.0))
            anchorDirection = Direction.TopCenter
            sloganOffsetY = 0.0
          }
          layers.addLayer(bottomTextLayer)
        }
      }
    }
  }
}

class SlideModel(
  val slides: List<Slide> = listOf(
    Slide("NECKAR.IT", "Data brought to life"),
    Slide("Wobei helfen wir?"),
    Slide("Wir unterstützen bei der Transformation zum", "Data Centric Business"),
    Slide("Konkret", "Daten \"lebendig\" machen"),
    Slide("Konkret", "Daten schnell und schick anzeigen"),
    Slide("Dem Anwender leicht machen", "relevante Daten wahr zu nehmen"),

    Slide("Transformation?", "Wozu brauchen wir das?"),
    Slide("Beispiel: Backup", "Backup ist leicht, Restore ist schwer!"),
    Slide("Beispiel: Amazon Bestellungen", "Wie viele Klicks/Sekunden, um..."),
    Slide("Wie lange dauert es?", "Um die Auswertung vom 7.2. zu finden?"),
    Slide("Wie lange dauert es?", "Um Auffälligkeiten in der Response Time zu finden?"),
    Slide("Wie lange dauert es?", "Um den \"komischen\" Effekt am WE zu erkennen?"),
    Slide("Wie entwickelt sich die Performance?", "Wer hat diese Informationen?"),
    Slide("Wie verlässlich sind die Daten?", "Beweisbar korrekt? Kryptographie?"),
    Slide("→ Daten sind theoretisch verfügbar", "Aber praktisch schwer/langsam nutzbar"),
    Slide("Transformation", "Wir brauchen das!"),

    Slide("Wie Transformation?", "\"Low hanging fruits\" zuerst"),
    Slide("Verfügbare Daten", "performant und grafisch erlebbar machen"),
    Slide("Zusammenfassen: Downsampling", "Übersicht und Orientierung ermöglichen"),
    Slide("Schnelle Navigation (Zoom!)", "sonst macht es keinen Spaß"),
    Slide("Verfügbare Daten verknüpfen", "Wir wissen viel mehr als man sieht!"),
    Slide("Weiter Schritte:", "Mehr Daten verfügbar machen"),

    Slide("NECKAR.IT - ursprünglich", "Visualisierungs-Projekte"),
    Slide("Unsichtbar:", "Daten sammeln und aufbereiten"),
    Slide("Sichtbar:", "UI zur Visualisierung"),

    Slide("Heute", "MeisterCharts + Dienstleistung drum herum"),
    Slide("MeisterCharts", "HTML5-Canvas, performant, flexibel, pixelgenau"),
    Slide("MeisterCharts", "Essenz aus 8 Jahren Erfahrung"),
    Slide("Probleme elegant gelöst", "die andere noch gar nicht kennen"),
    Slide("Zooming + Panning", "grundsätzlich und richtig in der Architektur gelöst"),
    Slide("Downsampling", "als Grundprinzip verankert"),
    Slide("Flexibler Aufbau", "einfache Dinge sind fertig, komplizierte sind möglich"),
    Slide("Referenz-Kunde: SICK", "SICK nutzt MeisterCharts als strategische Basis für Cloud-Lösung"),

    Slide("Konkret", ""),

    Slide("Mit uns zusammen", "... geht Visualisierung viel leichter und schneller"),
    Slide("Mit uns zusammen", "... ist schnell ein Mehrwert sichtbar"),

    ),
) {
  private var index = 0

  /**
   * Returns the current slide
   */
  fun current(): Slide {
    return slides[index]
  }

  /**
   * Updates the index and returns the next slide
   */
  fun next(): Slide {
    index = (index + 1).coerceAtMost(slides.lastIndex)
    return current()
  }

  fun previous(): Slide {
    index = (index - 1).coerceAtLeast(0)
    return current()
  }
}

/**
 * Represents one slide
 */
data class Slide(
  val text: String,
  val subText: String? = null,
) {

}
