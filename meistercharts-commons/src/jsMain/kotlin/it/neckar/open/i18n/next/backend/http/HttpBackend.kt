package it.neckar.open.i18n.next.backend.http


//The suffix cjs seems to be very strange
//More information can maybe found here: https://github.com/i18next/next-i18next/issues/1319#issuecomment-882062088
@JsModule("i18next-http-backend/cjs")
@JsNonModule
external val I18nextHttpBackend: Backend

/**
 * See [https://github.com/i18next/i18next-http-backend/blob/master/lib/index.js]
 */
external interface Backend {
  fun reload()

  //TODO add more functions
}
