package it.neckar.open.observable

/**
 * Convenience class; it is the same as [ObservableObject] with type-parameter [Long]
 */
class ObservableLong(initValue: Long) : ObservableObject<Long>(initValue), ReadOnlyObservableLong

/**
 * Convenience class; it is the same as [ReadOnlyObservableObject] with type-parameter [Long]
 */
interface ReadOnlyObservableLong : ReadOnlyObservableObject<Long>
