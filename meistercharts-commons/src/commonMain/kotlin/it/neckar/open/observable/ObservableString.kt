package it.neckar.open.observable

/**
 * Convenience class; it is the same as [ObservableObject] with type-parameter [String]
 */
class ObservableString(initValue: String) : ObservableObject<String>(initValue), ReadOnlyObservableString

/**
 * Convenience class; it is the same as [ReadOnlyObservableObject] with type-parameter [String]
 */
interface ReadOnlyObservableString : ReadOnlyObservableObject<String>
