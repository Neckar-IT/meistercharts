plantUml {
  render input: 'docs/uml/*.puml', output: 'docs/uml', format: 'svg', withMetadata: true
  //render input: 'diagrams/some.ditaa', output: 'some.png', withMetadata: true
  //render input: 'diagrams/release/*.puml', output: "${project.buildDir.absolutePath}/release", format: 'png', withMetadata: false
}
