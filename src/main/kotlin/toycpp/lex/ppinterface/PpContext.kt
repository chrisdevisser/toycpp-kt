package toycpp.lex.ppinterface

/**
 * Provides necessary preprocessing context that can be fed back to earlier translation steps.
 * This is necessary for lexing because header names are contextual to a high degree.
 * `<foo>` is lexed completely differently in different contexts. In a replacement list, it can
 * even be lexed both ways depending on the context of the code using the macro.
 */
class PpContextHolder(var value: PpContext = PpContext.NothingSpecial)

enum class PpContext {
    NothingSpecial,
    InHeaderName
}