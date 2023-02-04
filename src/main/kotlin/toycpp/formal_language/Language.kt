package toycpp.formal_language

sealed class Language<Letter>

class SingleWordLanguage<Letter>(
    val word: Iterable<Letter>
) : Language<Letter>()

class ConcatenatedLanguages<Letter>(sequence: Iterable<Language<Letter>>) : Language<Letter>() {
    // seq(1, seq(2, 3)) -> seq(1, 2, 3)
    // Recursion is not needed because the nested Seq guarantees a simplified structure.
    val sequence: Iterable<Language<Letter>> =
        sequence.flatMap { if (it is ConcatenatedLanguages<Letter>) it.sequence else listOf(it) }
}

class LanguageChoices<Letter>(options: Iterable<Language<Letter>>) : Language<Letter>() {
    // anyOf(1, anyOf(2, 3)) -> anyOf(1, 2, 3)
    // Recursion is not needed because the nested AnyOf guarantees a simplified structure.
    val options: Iterable<Language<Letter>> =
        options.flatMap { if (it is LanguageChoices<Letter>) it.options else listOf(it) }
}

data class RepeatedLanguage<Letter>(
    val inner: Language<Letter>
) : Language<Letter>()