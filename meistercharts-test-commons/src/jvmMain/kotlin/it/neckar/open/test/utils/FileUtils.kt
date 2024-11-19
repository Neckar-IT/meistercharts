package it.neckar.open.test.utils

import java.io.File

  /** Returns names of all children of the given File in a List<String>*/
  fun File.getAllChildrenNames(): List<String>{
    val returnList = emptyList<String>().toMutableList()
    this.walk().forEach { returnList += it.name  }
    return returnList
  }
  /** Returns files of all children of the given File in a List<File>*/
  fun File.getAllChildrenFilesRecursively(): List<File>{
    val returnList = emptyList<File>().toMutableList()

    this.walk().forEach { returnList += it  }
    return returnList
  }
  /** Searches the children of the given file for a child with the given name and returns it if found. Returns null if no child with the given name could be found.*/
  fun File.findChildRecursively(fileName: String): File? {
    this.getAllChildrenFilesRecursively().forEach { if (it.name == fileName){
      return it
    }}
    return null
  }
  /** Prints all children of the given file*/
  fun File.printAllChildrenRecursively(){
    this.walk().forEach {
      println(it)
    }
  }
