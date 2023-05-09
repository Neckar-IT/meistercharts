package it.neckar.gradle.kastree

import kastree.ast.Node

/**
 * Returns true if this is a class
 */
fun Node.Decl.Structured.isClass(): Boolean {
  return this.form == Node.Decl.Structured.Form.CLASS
}


/**
 * Returns true if the property is private
 */
fun Node.Decl.Property.isPrivate(): Boolean {
  return mods.isPrivate()
}

/**
 * Returns true if the modifiers contain "private"
 */
fun List<Node.Modifier>.isPrivate(): Boolean {
  return any {
    it is Node.Modifier.Lit &&
      it.keyword == Node.Modifier.Keyword.PRIVATE
  }
}

/**
 * Returns true if the modifiers contain "protected"
 */
fun List<Node.Modifier>.isProtected(): Boolean {
  return any {
    it is Node.Modifier.Lit &&
      it.keyword == Node.Modifier.Keyword.PROTECTED
  }
}

/**
 * Returns true if the modifiers contain "abstract"
 */
fun List<Node.Modifier>.isAbstract(): Boolean {
  return any {
    it is Node.Modifier.Lit &&
      it.keyword == Node.Modifier.Keyword.ABSTRACT
  }
}

