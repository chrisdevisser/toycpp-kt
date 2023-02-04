package toycpp.lex

private var counter = 0 // Used to ensure unique values unless two enum values share a name.

/**
 * Indicates the kind of a C++ token.
 *
 * This list is used after preprocessing, for parsing the language proper.
 * The names of these tokens are mainly based on how they sound when reading code aloud.
 * Tokens read differently in different contexts have more than one name, but are given the same value.
 */
enum class Tok(value: Int) {
    // TODO
    // Constructs
    Identifier(counter++), // e.g., abc, a123, foo_bar, a\u3021b, xor, auto
    Ppnum(counter++), // e.g., 2, 2.0, 2e5, 2.2.2.2
    CharLit(counter++), // e.g., 'a', '\n', u8'a', '123'
    CharUdl(counter++), // e.g., 'a'_foo
    StringLit(counter++), // e.g., "abc", L"\x20", R"(abc)"
    StringUdl(counter++), // e.g., "abc"_foo

    // Punctuation
    Pound(counter++), // #, %:
    Stringize(counter),
    Concat(counter++), // ##, %:%:
    OpenBrace(counter++), // {, <%
    CloseBrace(counter++), // }, %>
    OpenSquareBracket(counter++), // [, <:
    CloseSquareBracket(counter++), // ], :>
    OpenParen(counter++), // (
    CloseParen(counter++), // )
    Semicolon(counter++), // ;
    Colon(counter++), // :
    Ellipsis(counter++), // ...
    DotDotDot(counter),
    Cond(counter++), // ?
    ColonColon(counter++), // ::
    Dot(counter++), // .
    DotStar(counter++), // .*
    Arrow(counter++), // ->
    ArrowStar(counter++), // ->*
    Tilde(counter++), // ~
    Compl(counter),
    Not(counter++), // !
    Plus(counter++), // +
    Minus(counter++), // -
    Star(counter++), // *
    Times(counter),
    Over(counter++), // /
    Mod(counter++), // %, not a mod operation, but...
    Xor(counter++), // ^
    Ref(counter++), // &
    Bitand(counter),
    Addressof(counter),
    Bitor(counter++), // |
    Assign(counter++), // =, "equals" is too ambiguous
    PlusEquals(counter++), // +=
    MinusEquals(counter++), // -=
    TimesEquals(counter++), // *=
    OverEquals(counter++), // /=
    ModEquals(counter++), // %=
    XorEquals(counter++), // ^=
    AndEquals(counter++), // &=
    OrEquals(counter++), // |=
    EqualTo(counter++), // ==
    NotEqualTo(counter++), // !=
    LessThan(counter++), // <
    OpenAngleBracket(counter),
    GreaterThan(counter++), // >
    CloseAngleBracket(counter),
    LessThanOrEqualTo(counter++), // <=
    GreaterThanOrEqualTo(counter++), // >=
    Spaceship(counter++), // <=>
    And(counter++), // &&
    RefRef(counter),
    Or(counter++), // ||
    LeftShift(counter++), // <<
    RightShift(counter++), // >>
    DoubleCloseAngleBracket(counter), // Template shenanigans require parsing X<X<int>> as > >
    LeftShiftEquals(counter++), // <<=
    RightShiftEquals(counter++), // >>=
    PlusPlus(counter++), // ++
    MinusMinus(counter++), // --
    Comma(counter++), // ,

    // Now the weird stuff starts
    HeaderName(counter++), // <abc.cpp>, "abc.cpp" - Requires PP context
    Import(counter++), // import soft keyword
    Module(counter++), // module soft keyword
    Export(counter++), // export soft keyword
    OtherCharacter(counter++), // Any non-whitespace character that cannot be one of the above, i.e., \
    StartOfLine(counter++), //
}