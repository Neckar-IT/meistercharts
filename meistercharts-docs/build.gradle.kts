import it.neckar.gradle.asciidoctor.AsciidoctorModel
import it.neckar.gradle.asciidoctor.convertToHtmlBook

description = """Meistercharts Docs"""

plugins {
  asciidoctor
}

asciidoc {
  publications.named(AsciidoctorModel.MainPublicationName) {
    sourceSet {
      setSourceDir(layout.projectDirectory.dir("src/docs/asciidoc"))
    }
    convertToHtmlBook()
  }
}
