package toycpp.lex

private var counter = 0 // Used to ensure unique values unless two enum values share a name.

/**
 * Indicates the kind of a preprocessor token.
 *
 * This list is used at an early stage, before regular C++ tokens are produced. However, base lexing is done only once.
 * Therefore, as much work as possible is done in that process to make conversion easier later.
 * The names of these tokens are mainly based on how they sound when reading code aloud, but in the context of the preprocessor, not the language proper.
 * Tokens that are read differently in different contexts have more than one name, but are given the same value.
 */
enum class Pptok(value: Int) {
    // Constructs
    Identifier(counter++), // e.g., abc, a123, foo_bar, a\u3021b, auto
    Ppnum(counter++), // e.g., 2, 2.0, 2e5, 2.2.2.2
    CharLit(counter++), // e.g., 'a', '\n', u8'a', '123'
    CharUdl(counter++), // e.g., 'a'_foo
    StringLit(counter++), // e.g., "abc", L"\x20", R"(abc)"
    StringUdl(counter++), // e.g., "abc"_foo

    // Punctuation
    Pound(counter++), // #, %:
    Stringize(counter),
    Concat(counter++), // ##, %:%:
    LBrace(counter++), // {, <%
    RBrace(counter++), // }, %>
    LSquareBracket(counter++), // [, <:
    RSquareBracket(counter++), // ], :>
    OpenParen(counter++), // (
    CloseParen(counter++), // )
    Semicolon(counter++), // ;
    Colon(counter++), // :
    Ellipsis(counter++), // ...
    Cond(counter++), // ?
    ColonColon(counter++), // ::
    Dot(counter++), // .
    DotStar(counter++), // .*
    Arrow(counter++), // ->
    ArrowStar(counter++), // ->*
    Compl(counter++), // ~, compl
    Not(counter++), // !, not
    Plus(counter++), // +
    Minus(counter++), // -
    Times(counter++), // *
    Over(counter++), // /
    Mod(counter++), // % - not a mod operation, but...
    Xor(counter++), // ^, xor
    BitAnd(counter++), // &, bitand
    BitOr(counter++), // |, bitor
    Assign(counter++), // = - "equals" is too ambiguous
    PlusEquals(counter++), // +=
    MinusEquals(counter++), // -=
    TimesEquals(counter++), // *=
    OverEquals(counter++), // /=
    ModEquals(counter++), // %=
    XorEquals(counter++), // ^=, xor_eq
    AndEquals(counter++), // &=, and_eq
    OrEquals(counter++), // |=, or_eq
    EqualTo(counter++), // ==
    NotEqualTo(counter++), // !=, not_eq
    LessThan(counter++), // <
    GreaterThan(counter++), // >
    LessThanOrEqualTo(counter++), // <=
    GreaterThanOrEqualTo(counter++), // >=
    Spaceship(counter++), // <=>
    And(counter++), // &&, and
    Or(counter++), // ||, or
    LeftShift(counter++), // <<
    RightShift(counter++), // >>
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
    Comment(counter++), // //foo, /*foo*/

    // Pseudotokens that aren't part of the language
    StartOfLine(counter++), // Makes it easier to parse preprocessing directives, which must begin the line
}