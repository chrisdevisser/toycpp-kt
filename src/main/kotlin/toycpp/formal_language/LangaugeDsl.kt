package toycpp.formal_language
//
//open class LanguageDsl<Letter> {
//    val nothing = SingleWordLanguage(emptyList<Letter>())
//
//    fun lit(letter: Letter) = SingleWordLanguage(listOf(letter))
//    operator fun Letter.unaryPlus() = lit(this)
//
//    fun seq(vararg languages: Language<Letter>) = ConcatenatedLanguages(languages.asIterable())
//    fun seq(vararg letters: Letter) = SingleWordLanguage(letters.asIterable())
//    infix fun Language<Letter>.then(next: Language<Letter>) = seq(this, next)
//
//    fun anyOf(vararg languages: Language<Letter>) = LanguageChoices(languages.asIterable())
//    infix fun Language<Letter>.or(alternative: Language<Letter>) = anyOf(this, alternative)
//
//    fun oneOrMore(language: Language<Letter>) = RepeatedLanguage(language)
//    fun zeroOrMore(language: Language<Letter>) = anyOf(nothing, oneOrMore(language))
//}
//
//// This provides some utility for String that would be in a specialization if Kotlin had them.
//class CharLanguageDsl : LanguageDsl<Char>() {
//    private fun String.toTypedArray() = toCharArray().toTypedArray() // TODO: Char version of seq result
//
//    fun lit(str: String) = seq(*str.toTypedArray())
//    operator fun String.unaryPlus() = lit(this)
//}
//
//fun<Letter> language(block: LanguageDsl<Letter>.() -> Language<Letter>): Language<Letter> =
//    LanguageDsl<Letter>().block()
//
//fun charLanguage(name: String, block: CharLanguageDsl.() -> Language<Char>): Language<Char>  =
//    CharLanguageDsl().block()
//
//val x = LanguageDsl<Char>().lit("")