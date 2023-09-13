package it.neckar.open.provider

import it.neckar.open.annotations.NotBoxed

class BoxingDemo : BoxingDemoInterface<Double> {
  fun boxingReturnTypeOk(): @NotBoxed Int {
    TODO()
  }

  //Is detected
  //fun boxingReturnTypeNotOk(): @NotBoxed Int? {
  //  TODO()
  //}

  fun boxingParamOk(daParam: @NotBoxed Boolean) {
    TODO()
  }

  //fun boxingParamNotOk(daParam: @NotBoxed Boolean?) {
  //  TODO()
  //}

  //fun <T> boxingParamGenericsNotOk(daParam: @NotBoxed T) {
  //  TODO()
  //}

  ////NOT ok! Boxing!!!
  //override fun foobarNotOk(value: @NotBoxed Double) {
  //}
}


interface BoxingDemoInterface<T> {
//  fun foobarNotOk(value: T)
}
