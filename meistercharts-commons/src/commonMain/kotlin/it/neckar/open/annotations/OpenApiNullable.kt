package it.neckar.open.annotations

/**
 * Marks a type where `null` has special semantic meaning in JSON serialization.
 *
 * When applied to a type, the OpenAPI schema generator will always include `null`
 * as a valid type in the schema, even if the field has a default value.
 *
 * Example use case: `PatchValue` where `null` means "delete" (different from "keep"/omitted).
 *
 * Attention: Use this annotation with care! In nearly all cases, it is unnecessary to include `null`
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class OpenApiNullable
