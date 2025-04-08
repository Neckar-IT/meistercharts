package it.neckar.open.observable

import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*


/**
 * Converts this [ObservableObject] to a [Flow].
 */
fun <T> ObservableObject<T>.asFlow(): Flow<T> {
  return callbackFlow {
    val disposable = consume { newValue ->
      trySend(newValue).isSuccess
    }

    awaitClose {
      disposable.dispose()
    }
  }
}

/**
 * Creates a new flow that consumes all properties of this [ObservableProperties].
 */
fun ObservableProperties.asFlow(): Flow<Any?> {
  return callbackFlow {
    val disposable = consumeAllProperties { newValue ->
      trySend(newValue).isSuccess
    }

    awaitClose {
      disposable.dispose()
    }
  }
}

