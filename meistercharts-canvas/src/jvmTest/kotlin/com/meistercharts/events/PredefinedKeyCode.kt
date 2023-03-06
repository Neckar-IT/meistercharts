/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.events

/**
 * The type of the key code
 */
enum class KeyCodeType {
  Function,
  Navigation,
  Arrow,
  Modifier,
  Letter,
  Digit,
  Keypad,
  Whitespace,
  Media,
}

/**
 * Contains the unused key code
 */
enum class PredefinedKeyCode constructor(
  internal val code: Int,
  /**
   * Gets name of this key code.
   * @return Name of this key code
   */
  val description: String,
  vararg keyCodeType: KeyCodeType = arrayOf()
) {
  /**
   * Constant for the `Enter` key.
   */
  ENTER(0x0A, "Enter", KeyCodeType.Whitespace),

  /**
   * Constant for the `Backspace` key.
   */
  BACK_SPACE(0x08, "Backspace"),

  /**
   * Constant for the `Tab` key.
   */
  TAB(0x09, "Tab", KeyCodeType.Whitespace),

  /**
   * Constant for the `Cancel` key.
   */
  CANCEL(0x03, "Cancel"),

  /**
   * Constant for the `Clear` key.
   */
  CLEAR(0x0C, "Clear"),

  /**
   * Constant for the `Shift` key.
   */
  SHIFT(0x10, "Shift", KeyCodeType.Modifier),

  /**
   * Constant for the `Ctrl` key.
   */
  CONTROL(0x11, "Ctrl", KeyCodeType.Modifier),

  /**
   * Constant for the `Alt` key.
   */
  ALT(0x12, "Alt", KeyCodeType.Modifier),

  /**
   * Constant for the `Pause` key.
   */
  PAUSE(0x13, "Pause"),

  /**
   * Constant for the `Caps Lock` key.
   */
  CAPS(0x14, "Caps Lock"),

  /**
   * Constant for the `Esc` key.
   */
  ESCAPE(0x1B, "Esc"),

  /**
   * Constant for the `Space` key.
   */
  SPACE(0x20, "Space", KeyCodeType.Whitespace),

  /**
   * Constant for the `Page Up` key.
   */
  PAGE_UP(0x21, "Page Up", KeyCodeType.Navigation),

  /**
   * Constant for the `Page Down` key.
   */
  PAGE_DOWN(0x22, "Page Down", KeyCodeType.Navigation),

  /**
   * Constant for the `End` key.
   */
  END(0x23, "End", KeyCodeType.Navigation),

  /**
   * Constant for the `Home` key.
   */
  HOME(0x24, "Home", KeyCodeType.Navigation),

  /**
   * Constant for the non-numpad **left** arrow key.
   */
  LEFT(0x25, "Left", KeyCodeType.Arrow, KeyCodeType.Navigation),

  /**
   * Constant for the non-numpad **up** arrow key.
   */
  UP(0x26, "Up", KeyCodeType.Arrow, KeyCodeType.Navigation),

  /**
   * Constant for the non-numpad **right** arrow key.
   */
  RIGHT(0x27, "Right", KeyCodeType.Arrow, KeyCodeType.Navigation),

  /**
   * Constant for the non-numpad **down** arrow key.
   */
  DOWN(0x28, "Down", KeyCodeType.Arrow, KeyCodeType.Navigation),

  /**
   * Constant for the comma key, ","
   */
  COMMA(0x2C, "Comma"),

  /**
   * Constant for the minus key, "-"
   */
  MINUS(0x2D, "Minus"),

  /**
   * Constant for the period key, "."
   */
  PERIOD(0x2E, "Period"),

  /**
   * Constant for the forward slash key, "/"
   */
  SLASH(0x2F, "Slash"),

  /**
   * Constant for the `0` key.
   */
  DIGIT0(0x30, "0", KeyCodeType.Digit),

  /**
   * Constant for the `1` key.
   */
  DIGIT1(0x31, "1", KeyCodeType.Digit),

  /**
   * Constant for the `2` key.
   */
  DIGIT2(0x32, "2", KeyCodeType.Digit),

  /**
   * Constant for the `3` key.
   */
  DIGIT3(0x33, "3", KeyCodeType.Digit),

  /**
   * Constant for the `4` key.
   */
  DIGIT4(0x34, "4", KeyCodeType.Digit),

  /**
   * Constant for the `5` key.
   */
  DIGIT5(0x35, "5", KeyCodeType.Digit),

  /**
   * Constant for the `6` key.
   */
  DIGIT6(0x36, "6", KeyCodeType.Digit),

  /**
   * Constant for the `7` key.
   */
  DIGIT7(0x37, "7", KeyCodeType.Digit),

  /**
   * Constant for the `8` key.
   */
  DIGIT8(0x38, "8", KeyCodeType.Digit),

  /**
   * Constant for the `9` key.
   */
  DIGIT9(0x39, "9", KeyCodeType.Digit),

  /**
   * Constant for the semicolon key, ";"
   */
  SEMICOLON(0x3B, "Semicolon"),

  /**
   * Constant for the equals key, "="
   */
  EQUALS(0x3D, "Equals"),

  /**
   * Constant for the `A` key.
   */
  A(0x41, "A", KeyCodeType.Letter),

  /**
   * Constant for the `B` key.
   */
  B(0x42, "B", KeyCodeType.Letter),

  /**
   * Constant for the `C` key.
   */
  C(0x43, "C", KeyCodeType.Letter),

  /**
   * Constant for the `D` key.
   */
  D(0x44, "D", KeyCodeType.Letter),

  /**
   * Constant for the `E` key.
   */
  E(0x45, "E", KeyCodeType.Letter),

  /**
   * Constant for the `F` key.
   */
  F(0x46, "F", KeyCodeType.Letter),

  /**
   * Constant for the `G` key.
   */
  G(0x47, "G", KeyCodeType.Letter),

  /**
   * Constant for the `H` key.
   */
  H(0x48, "H", KeyCodeType.Letter),

  /**
   * Constant for the `I` key.
   */
  I(0x49, "I", KeyCodeType.Letter),

  /**
   * Constant for the `J` key.
   */
  J(0x4A, "J", KeyCodeType.Letter),

  /**
   * Constant for the `K` key.
   */
  K(0x4B, "K", KeyCodeType.Letter),

  /**
   * Constant for the `L` key.
   */
  L(0x4C, "L", KeyCodeType.Letter),

  /**
   * Constant for the `M` key.
   */
  M(0x4D, "M", KeyCodeType.Letter),

  /**
   * Constant for the `N` key.
   */
  N(0x4E, "N", KeyCodeType.Letter),

  /**
   * Constant for the `O` key.
   */
  O(0x4F, "O", KeyCodeType.Letter),

  /**
   * Constant for the `P` key.
   */
  P(0x50, "P", KeyCodeType.Letter),

  /**
   * Constant for the `Q` key.
   */
  Q(0x51, "Q", KeyCodeType.Letter),

  /**
   * Constant for the `R` key.
   */
  R(0x52, "R", KeyCodeType.Letter),

  /**
   * Constant for the `S` key.
   */
  S(0x53, "S", KeyCodeType.Letter),

  /**
   * Constant for the `T` key.
   */
  T(0x54, "T", KeyCodeType.Letter),

  /**
   * Constant for the `U` key.
   */
  U(0x55, "U", KeyCodeType.Letter),

  /**
   * Constant for the `V` key.
   */
  V(0x56, "V", KeyCodeType.Letter),

  /**
   * Constant for the `W` key.
   */
  W(0x57, "W", KeyCodeType.Letter),

  /**
   * Constant for the `X` key.
   */
  X(0x58, "X", KeyCodeType.Letter),

  /**
   * Constant for the `Y` key.
   */
  Y(0x59, "Y", KeyCodeType.Letter),

  /**
   * Constant for the `Z` key.
   */
  Z(0x5A, "Z", KeyCodeType.Letter),

  /**
   * Constant for the open bracket key, "["
   */
  OPEN_BRACKET(0x5B, "Open Bracket"),

  /**
   * Constant for the back slash key, "\"
   */
  BACK_SLASH(0x5C, "Back Slash"),

  /**
   * Constant for the close bracket key, "]"
   */
  CLOSE_BRACKET(0x5D, "Close Bracket"),

  /**
   * Constant for the `Numpad 0` key.
   */
  NUMPAD0(0x60, "Numpad 0", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 1` key.
   */
  NUMPAD1(0x61, "Numpad 1", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 2` key.
   */
  NUMPAD2(0x62, "Numpad 2", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 3` key.
   */
  NUMPAD3(0x63, "Numpad 3", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 4` key.
   */
  NUMPAD4(0x64, "Numpad 4", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 5` key.
   */
  NUMPAD5(0x65, "Numpad 5", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 6` key.
   */
  NUMPAD6(0x66, "Numpad 6", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 7` key.
   */
  NUMPAD7(0x67, "Numpad 7", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 8` key.
   */
  NUMPAD8(0x68, "Numpad 8", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Numpad 9` key.
   */
  NUMPAD9(0x69, "Numpad 9", KeyCodeType.Digit, KeyCodeType.Keypad),

  /**
   * Constant for the `Multiply` key.
   */
  MULTIPLY(0x6A, "Multiply"),

  /**
   * Constant for the `Add` key.
   */
  ADD(0x6B, "Add"),

  /**
   * Constant for the Numpad Separator key.
   */
  SEPARATOR(0x6C, "Separator"),

  /**
   * Constant for the `Subtract` key.
   */
  SUBTRACT(0x6D, "Subtract"),

  /**
   * Constant for the `Decimal` key.
   */
  DECIMAL(0x6E, "Decimal"),

  /**
   * Constant for the `Divide` key.
   */
  DIVIDE(0x6F, "Divide"),

  /**
   * Constant for the `Delete` key.
   */
  DELETE(0x7F, "Delete"), /* ASCII:Integer   DEL */

  /**
   * Constant for the `Num Lock` key.
   */
  NUM_LOCK(0x90, "Num Lock"),

  /**
   * Constant for the `Scroll Lock` key.
   */
  SCROLL_LOCK(0x91, "Scroll Lock"),

  /**
   * Constant for the F1 function key.
   */
  F1(0x70, "F1", KeyCodeType.Function),

  /**
   * Constant for the F2 function key.
   */
  F2(0x71, "F2", KeyCodeType.Function),

  /**
   * Constant for the F3 function key.
   */
  F3(0x72, "F3", KeyCodeType.Function),

  /**
   * Constant for the F4 function key.
   */
  F4(0x73, "F4", KeyCodeType.Function),

  /**
   * Constant for the F5 function key.
   */
  F5(0x74, "F5", KeyCodeType.Function),

  /**
   * Constant for the F6 function key.
   */
  F6(0x75, "F6", KeyCodeType.Function),

  /**
   * Constant for the F7 function key.
   */
  F7(0x76, "F7", KeyCodeType.Function),

  /**
   * Constant for the F8 function key.
   */
  F8(0x77, "F8", KeyCodeType.Function),

  /**
   * Constant for the F9 function key.
   */
  F9(0x78, "F9", KeyCodeType.Function),

  /**
   * Constant for the F10 function key.
   */
  F10(0x79, "F10", KeyCodeType.Function),

  /**
   * Constant for the F11 function key.
   */
  F11(0x7A, "F11", KeyCodeType.Function),

  /**
   * Constant for the F12 function key.
   */
  F12(0x7B, "F12", KeyCodeType.Function),

  /**
   * Constant for the F13 function key.
   */
  F13(0xF000, "F13", KeyCodeType.Function),

  /**
   * Constant for the F14 function key.
   */
  F14(0xF001, "F14", KeyCodeType.Function),

  /**
   * Constant for the F15 function key.
   */
  F15(0xF002, "F15", KeyCodeType.Function),

  /**
   * Constant for the F16 function key.
   */
  F16(0xF003, "F16", KeyCodeType.Function),

  /**
   * Constant for the F17 function key.
   */
  F17(0xF004, "F17", KeyCodeType.Function),

  /**
   * Constant for the F18 function key.
   */
  F18(0xF005, "F18", KeyCodeType.Function),

  /**
   * Constant for the F19 function key.
   */
  F19(0xF006, "F19", KeyCodeType.Function),

  /**
   * Constant for the F20 function key.
   */
  F20(0xF007, "F20", KeyCodeType.Function),

  /**
   * Constant for the F21 function key.
   */
  F21(0xF008, "F21", KeyCodeType.Function),

  /**
   * Constant for the F22 function key.
   */
  F22(0xF009, "F22", KeyCodeType.Function),

  /**
   * Constant for the F23 function key.
   */
  F23(0xF00A, "F23", KeyCodeType.Function),

  /**
   * Constant for the F24 function key.
   */
  F24(0xF00B, "F24", KeyCodeType.Function),

  /**
   * Constant for the `Print Screen` key.
   */
  PRINTSCREEN(0x9A, "Print Screen"),

  /**
   * Constant for the `Insert` key.
   */
  INSERT(0x9B, "Insert"),

  /**
   * Constant for the `Help` key.
   */
  HELP(0x9C, "Help"),

  /**
   * Constant for the `Meta` key.
   */
  META(0x9D, "Meta", KeyCodeType.Modifier),

  /**
   * Constant for the `Back Quote` key.
   */
  BACK_QUOTE(0xC0, "Back Quote"),

  /**
   * Constant for the `Quote` key.
   */
  QUOTE(0xDE, "Quote"),

  /**
   * Constant for the numeric keypad **up** arrow key.
   */
  KP_UP(0xE0, "Numpad Up", KeyCodeType.Arrow, KeyCodeType.Navigation, KeyCodeType.Keypad),

  /**
   * Constant for the numeric keypad **down** arrow key.
   */
  KP_DOWN(0xE1, "Numpad Down", KeyCodeType.Arrow, KeyCodeType.Navigation, KeyCodeType.Keypad),

  /**
   * Constant for the numeric keypad **left** arrow key.
   */
  KP_LEFT(0xE2, "Numpad Left", KeyCodeType.Arrow, KeyCodeType.Navigation, KeyCodeType.Keypad),

  /**
   * Constant for the numeric keypad **right** arrow key.
   */
  KP_RIGHT(0xE3, "Numpad Right", KeyCodeType.Arrow, KeyCodeType.Navigation, KeyCodeType.Keypad),

  /**
   * Constant for the `Dead Grave` key.
   */
  DEAD_GRAVE(0x80, "Dead Grave"),

  /**
   * Constant for the `Dead Acute` key.
   */
  DEAD_ACUTE(0x81, "Dead Acute"),

  /**
   * Constant for the `Dead Circumflex` key.
   */
  DEAD_CIRCUMFLEX(0x82, "Dead Circumflex"),

  /**
   * Constant for the `Dead Tilde` key.
   */
  DEAD_TILDE(0x83, "Dead Tilde"),

  /**
   * Constant for the `Dead Macron` key.
   */
  DEAD_MACRON(0x84, "Dead Macron"),

  /**
   * Constant for the `Dead Breve` key.
   */
  DEAD_BREVE(0x85, "Dead Breve"),

  /**
   * Constant for the `Dead Abovedot` key.
   */
  DEAD_ABOVEDOT(0x86, "Dead Abovedot"),

  /**
   * Constant for the `Dead Diaeresis` key.
   */
  DEAD_DIAERESIS(0x87, "Dead Diaeresis"),

  /**
   * Constant for the `Dead Abovering` key.
   */
  DEAD_ABOVERING(0x88, "Dead Abovering"),

  /**
   * Constant for the `Dead Doubleacute` key.
   */
  DEAD_DOUBLEACUTE(0x89, "Dead Doubleacute"),

  /**
   * Constant for the `Dead Caron` key.
   */
  DEAD_CARON(0x8a, "Dead Caron"),

  /**
   * Constant for the `Dead Cedilla` key.
   */
  DEAD_CEDILLA(0x8b, "Dead Cedilla"),

  /**
   * Constant for the `Dead Ogonek` key.
   */
  DEAD_OGONEK(0x8c, "Dead Ogonek"),

  /**
   * Constant for the `Dead Iota` key.
   */
  DEAD_IOTA(0x8d, "Dead Iota"),

  /**
   * Constant for the `Dead Voiced Sound` key.
   */
  DEAD_VOICED_SOUND(0x8e, "Dead Voiced Sound"),

  /**
   * Constant for the `Dead Semivoiced Sound` key.
   */
  DEAD_SEMIVOICED_SOUND(0x8f, "Dead Semivoiced Sound"),

  /**
   * Constant for the `Ampersand` key.
   */
  AMPERSAND(0x96, "Ampersand"),

  /**
   * Constant for the `Asterisk` key.
   */
  ASTERISK(0x97, "Asterisk"),

  /**
   * Constant for the `Double Quote` key.
   */
  QUOTEDBL(0x98, "Double Quote"),

  /**
   * Constant for the `Less` key.
   */
  LESS(0x99, "Less"),

  /**
   * Constant for the `Greater` key.
   */
  GREATER(0xa0, "Greater"),

  /**
   * Constant for the `Left Brace` key.
   */
  BRACELEFT(0xa1, "Left Brace"),

  /**
   * Constant for the `Right Brace` key.
   */
  BRACERIGHT(0xa2, "Right Brace"),

  /**
   * Constant for the "@" key.
   */
  AT(0x0200, "At"),

  /**
   * Constant for the ":" key.
   */
  COLON(0x0201, "Colon"),

  /**
   * Constant for the "^" key.
   */
  CIRCUMFLEX(0x0202, "Circumflex"),

  /**
   * Constant for the "$" key.
   */
  DOLLAR(0x0203, "Dollar"),

  /**
   * Constant for the Euro currency sign key.
   */
  EURO_SIGN(0x0204, "Euro Sign"),

  /**
   * Constant for the "!" key.
   */
  EXCLAMATION_MARK(0x0205, "Exclamation Mark"),

  /**
   * Constant for the inverted exclamation mark key.
   */
  INVERTED_EXCLAMATION_MARK(0x0206, "Inverted Exclamation Mark"),

  /**
   * Constant for the "(" key.
   */
  LEFT_PARENTHESIS(0x0207, "Left Parenthesis"),

  /**
   * Constant for the "#" key.
   */
  NUMBER_SIGN(0x0208, "Number Sign"),

  /**
   * Constant for the "+" key.
   */
  PLUS(0x0209, "Plus"),

  /**
   * Constant for the ")" key.
   */
  RIGHT_PARENTHESIS(0x020A, "Right Parenthesis"),

  /**
   * Constant for the "_" key.
   */
  UNDERSCORE(0x020B, "Underscore"),

  /**
   * Constant for the Microsoft Windows "Windows" key.
   * It is used for both the left and right version of the key.
   */
  WINDOWS(0x020C, "Windows", KeyCodeType.Modifier),

  /**
   * Constant for the Microsoft Windows Context Menu key.
   */
  CONTEXT_MENU(0x020D, "Context Menu"),

  /**
   * Constant for input method support on Asian Keyboards.
   */
  FINAL(0x0018, "Final"),

  /**
   * Constant for the Convert function key.
   */
  CONVERT(0x001C, "Convert"),

  /**
   * Constant for the Don't Convert function key.
   */
  NONCONVERT(0x001D, "Nonconvert"),

  /**
   * Constant for the Accept or Commit function key.
   */
  ACCEPT(0x001E, "Accept"),

  /**
   * Constant for the `Mode Change` key.
   */
  MODECHANGE(0x001F, "Mode Change"),

  /**
   * Constant for the `Kana` key.
   */
  KANA(0x0015, "Kana"),

  /**
   * Constant for the `Kanji` key.
   */
  KANJI(0x0019, "Kanji"),

  /**
   * Constant for the Alphanumeric function key.
   */
  ALPHANUMERIC(0x00F0, "Alphanumeric"),

  /**
   * Constant for the Katakana function key.
   */
  KATAKANA(0x00F1, "Katakana"),

  /**
   * Constant for the Hiragana function key.
   */
  HIRAGANA(0x00F2, "Hiragana"),

  /**
   * Constant for the Full-Width Characters function key.
   */
  FULL_WIDTH(0x00F3, "Full Width"),

  /**
   * Constant for the Half-Width Characters function key.
   */
  HALF_WIDTH(0x00F4, "Half Width"),

  /**
   * Constant for the Roman Characters function key.
   */
  ROMAN_CHARACTERS(0x00F5, "Roman Characters"),

  /**
   * Constant for the All Candidates function key.
   */
  ALL_CANDIDATES(0x0100, "All Candidates"),

  /**
   * Constant for the Previous Candidate function key.
   */
  PREVIOUS_CANDIDATE(0x0101, "Previous Candidate"),

  /**
   * Constant for the Code Input function key.
   */
  CODE_INPUT(0x0102, "Code Input"),

  /**
   * Constant for the Japanese-Katakana function key.
   * This key switches to a Japanese input method and selects its Katakana input mode.
   */
  JAPANESE_KATAKANA(0x0103, "Japanese Katakana"),

  /**
   * Constant for the Japanese-Hiragana function key.
   * This key switches to a Japanese input method and selects its Hiragana input mode.
   */
  JAPANESE_HIRAGANA(0x0104, "Japanese Hiragana"),

  /**
   * Constant for the Japanese-Roman function key.
   * This key switches to a Japanese input method and selects its Roman-Direct input mode.
   */
  JAPANESE_ROMAN(0x0105, "Japanese Roman"),

  /**
   * Constant for the locking Kana function key.
   * This key locks the keyboard into a Kana layout.
   */
  KANA_LOCK(0x0106, "Kana Lock"),

  /**
   * Constant for the input method on/off key.
   */
  INPUT_METHOD_ON_OFF(0x0107, "Input Method On/Off"),

  /**
   * Constant for the `Cut` key.
   */
  CUT(0xFFD1, "Cut"),

  /**
   * Constant for the `Copy` key.
   */
  COPY(0xFFCD, "Copy"),

  /**
   * Constant for the `Paste` key.
   */
  PASTE(0xFFCF, "Paste"),

  /**
   * Constant for the `Undo` key.
   */
  UNDO(0xFFCB, "Undo"),

  /**
   * Constant for the `Again` key.
   */
  AGAIN(0xFFC9, "Again"),

  /**
   * Constant for the `Find` key.
   */
  FIND(0xFFD0, "Find"),

  /**
   * Constant for the `Properties` key.
   */
  PROPS(0xFFCA, "Properties"),

  /**
   * Constant for the `Stop` key.
   */
  STOP(0xFFC8, "Stop"),

  /**
   * Constant for the input method on/off key.
   */
  COMPOSE(0xFF20, "Compose"),

  /**
   * Constant for the AltGraph function key.
   */
  ALT_GRAPH(0xFF7E, "Alt Graph", KeyCodeType.Modifier),

  /**
   * Constant for the Begin key.
   */
  BEGIN(0xFF58, "Begin"),

  /**
   * This value is used to indicate that the keyCode is unknown.
   * Key typed events do not have a keyCode value; this value
   * is used instead.
   */
  UNDEFINED(0x0, "Undefined"),


  //--------------------------------------------------------------
  //
  // Mobile and Embedded Specific Key Codes
  //
  //--------------------------------------------------------------

  /**
   * Constant for the `Softkey 0` key.
   */
  SOFTKEY_0(0x1000, "Softkey 0"),

  /**
   * Constant for the `Softkey 1` key.
   */
  SOFTKEY_1(0x1001, "Softkey 1"),

  /**
   * Constant for the `Softkey 2` key.
   */
  SOFTKEY_2(0x1002, "Softkey 2"),

  /**
   * Constant for the `Softkey 3` key.
   */
  SOFTKEY_3(0x1003, "Softkey 3"),

  /**
   * Constant for the `Softkey 4` key.
   */
  SOFTKEY_4(0x1004, "Softkey 4"),

  /**
   * Constant for the `Softkey 5` key.
   */
  SOFTKEY_5(0x1005, "Softkey 5"),

  /**
   * Constant for the `Softkey 6` key.
   */
  SOFTKEY_6(0x1006, "Softkey 6"),

  /**
   * Constant for the `Softkey 7` key.
   */
  SOFTKEY_7(0x1007, "Softkey 7"),

  /**
   * Constant for the `Softkey 8` key.
   */
  SOFTKEY_8(0x1008, "Softkey 8"),

  /**
   * Constant for the `Softkey 9` key.
   */
  SOFTKEY_9(0x1009, "Softkey 9"),

  /**
   * Constant for the `Game A` key.
   */
  GAME_A(0x100A, "Game A"),

  /**
   * Constant for the `Game B` key.
   */
  GAME_B(0x100B, "Game B"),

  /**
   * Constant for the `Game C` key.
   */
  GAME_C(0x100C, "Game C"),

  /**
   * Constant for the `Game D` key.
   */
  GAME_D(0x100D, "Game D"),

  /**
   * Constant for the `Star` key.
   */
  STAR(0x100E, "Star"),

  /**
   * Constant for the `Pound` key.
   */
  POUND(0x100F, "Pound"),

  /**
   * Constant for the `Power` key.
   */
  POWER(0x199, "Power"),

  /**
   * Constant for the `Info` key.
   */
  INFO(0x1C9, "Info"),

  /**
   * Constant for the `Colored Key 0` key.
   */
  COLORED_KEY_0(0x193, "Colored Key 0"),

  /**
   * Constant for the `Colored Key 1` key.
   */
  COLORED_KEY_1(0x194, "Colored Key 1"),

  /**
   * Constant for the `Colored Key 2` key.
   */
  COLORED_KEY_2(0x195, "Colored Key 2"),

  /**
   * Constant for the `Colored Key 3` key.
   */
  COLORED_KEY_3(0x196, "Colored Key 3"),

  /**
   * Constant for the `Eject` key.
   */
  EJECT_TOGGLE(0x19E, "Eject", KeyCodeType.Media),

  /**
   * Constant for the `Play` key.
   */
  PLAY(0x19F, "Play", KeyCodeType.Media),

  /**
   * Constant for the `Record` key.
   */
  RECORD(0x1A0, "Record", KeyCodeType.Media),

  /**
   * Constant for the `Fast Forward` key.
   */
  FAST_FWD(0x1A1, "Fast Forward", KeyCodeType.Media),

  /**
   * Constant for the `Rewind` key.
   */
  REWIND(0x19C, "Rewind", KeyCodeType.Media),

  /**
   * Constant for the `Previous Track` key.
   */
  TRACK_PREV(0x1A8, "Previous Track", KeyCodeType.Media),

  /**
   * Constant for the `Next Track` key.
   */
  TRACK_NEXT(0x1A9, "Next Track", KeyCodeType.Media),

  /**
   * Constant for the `Channel Up` key.
   */
  CHANNEL_UP(0x1AB, "Channel Up", KeyCodeType.Media),

  /**
   * Constant for the `Channel Down` key.
   */
  CHANNEL_DOWN(0x1AC, "Channel Down", KeyCodeType.Media),

  /**
   * Constant for the `Volume Up` key.
   */
  VOLUME_UP(0x1bf, "Volume Up", KeyCodeType.Media),

  /**
   * Constant for the `Volume Down` key.
   */
  VOLUME_DOWN(0x1C0, "Volume Down", KeyCodeType.Media),

  /**
   * Constant for the `Mute` key.
   */
  MUTE(0x1C1, "Mute", KeyCodeType.Media),

  /**
   * Constant for the Apple `Command` key.
   * @since JavaFX 2.1
   */
  COMMAND(0x300, "Command", KeyCodeType.Modifier),

  /**
   * Constant for the `Shortcut` key.
   */
  SHORTCUT(-1, "Shortcut");

  /**
   * The key code types
   */
  private val keyCodeTypes: Set<KeyCodeType> = setOf(*keyCodeType)

  /**
   * Function keys like F1, F2, etc...
   * @return true if this key code corresponds to a functional key
   * @since JavaFX 2.2
   */
  val isFunctionKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Function)

  /**
   * Navigation keys are arrow keys and Page Down, Page Up, Home, End
   * (including keypad keys)
   * @return true if this key code corresponds to a navigation key
   * @since JavaFX 2.2
   */
  val isNavigationKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Navigation)

  /**
   * Left, right, up, down keys (including the keypad arrows)
   * @return true if this key code corresponds to an arrow key
   * @since JavaFX 2.2
   */
  val isArrowKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Arrow)

  /**
   * Keys that could act as a modifier
   * @return true if this key code corresponds to a modifier key
   * @since JavaFX 2.2
   */
  val isModifierKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Modifier)

  /**
   * All keys with letters
   * @return true if this key code corresponds to a letter key
   * @since JavaFX 2.2
   */
  val isLetterKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Letter)

  /**
   * All Digit keys (including the keypad digits)
   * @return true if this key code corresponds to a digit key
   * @since JavaFX 2.2
   */
  val isDigitKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Digit)

  /**
   * All keys on the keypad
   * @return true if this key code corresponds to a keypad key
   * @since JavaFX 2.2
   */
  val isKeypadKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Keypad)

  /**
   * Space, tab and enter
   * @return true if this key code corresponds to a whitespace key
   * @since JavaFX 2.2
   */
  val isWhitespaceKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Whitespace)

  /**
   * All multimedia keys (channel up/down, volume control, etc...)
   * @return true if this key code corresponds to a media key
   * @since JavaFX 2.2
   */
  val isMediaKey: Boolean
    get() = keyCodeTypes.contains(KeyCodeType.Media)

  companion object {
    /**
     * Returns the KeyCode from an int
     */
    fun fromCode(value: Int): PredefinedKeyCode {
      for (current in values()) {
        if (current.code == value) {
          return current
        }
      }
      throw IllegalArgumentException("No key code found for $value")
    }
  }

}

