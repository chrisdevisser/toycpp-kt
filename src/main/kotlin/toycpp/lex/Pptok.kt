package toycpp.lex

/**
 * Indicates the kind of a preprocessor token.
 *
 * This list is used at an early stage, before regular C++ tokens are produced. However, base lexing is done only once.
 * Therefore, as much work as possible is done in that process to make conversion easier later.
 * The names of these tokens are mainly based on how they sound when reading code aloud, but in the context of the preprocessor, not the language proper.
 */
enum class Pptok {
    // Constructs
    Identifier, // e.g., abc, a123, foo_bar, a\u3021b, auto
    Ppnum, // e.g., 2, 2.0, 2e5, 2.2.2.2
    CharLit, // e.g., 'a', '\n', u8'a', '123'
    CharUdl, // e.g., 'a'_foo
    StringLit, // e.g., "abc", L"\x20", R"(abc)"
    StringUdl, // e.g., "abc"_foo

    // Punctuation
    Pound, // #, %:
    Concat, // ##, %:%:
    LBrace, // {, <%
    RBrace, // }, %>
    LSquareBracket, // [, <:
    RSquareBracket, // ], :>
    OpenParen, // (
    CloseParen, // )
    Semicolon, // ;
    Colon, // :
    Ellipsis, // ...
    Cond, // ?
    ColonColon, // ::
    Dot, // .
    DotStar, // .*
    Arrow, // ->
    ArrowStar, // ->*
    Compl, // ~, compl
    Not, // !, not
    Plus, // +
    Minus, // -
    Times, // *
    Over, // /
    Mod, // % - not a mod operation, but...
    Xor, // ^, xor
    BitAnd, // &, bitand
    BitOr, // |, bitor
    Assign, // = - "equals" is too ambiguous
    PlusEquals, // +=
    MinusEquals, // -=
    TimesEquals, // *=
    OverEquals, // /=
    ModEquals, // %=
    XorEquals, // ^=, xor_eq
    AndEquals, // &=, and_eq
    OrEquals, // |=, or_eq
    EqualTo, // ==
    NotEqualTo, // !=, not_eq
    LessThan, // <
    GreaterThan, // >
    LessThanOrEqualTo, // <=
    GreaterThanOrEqualTo, // >=
    Spaceship, // <=>
    And, // &&, and
    Or, // ||, or
    LeftShift, // <<
    RightShift, // >>
    LeftShiftEquals, // <<=
    RightShiftEquals, // >>=
    PlusPlus, // ++
    MinusMinus, // --
    Comma, // ,

    // Now the weird stuff starts
    AngledHeaderName, // <abc.cpp> - Created via a separate DFA used by the PP
    QuotedHeaderName, // "abc.cpp" - Created via a separate DFA used by the PP
    Import, // import soft keyword - Becomes a keyword when processing an import directive
    Module, // module soft keyword - Becomes a keyword when processing a module directive
    Export, // export soft keyword - Becomes a keyword when processing an import directive
    OtherCharacter, // Any non-whitespace character that cannot be one of the above, i.e., \ or anything outside the BSCS
    Comment, // //foo, /*foo*/

    // Pseudotokens that aren't part of the language
    Newline, // Used to process the source line by line (post-splice) for preprocessing directives
    Whitespace, // Used to save the lexer from tracking whitespace. Also helps while hand-lexing a raw string UDL
    SpecialCaseTemplateLex, // Represents <::, which is later fixed into < :: if needed
    RawStringStart, // Raw string literals are lexed manually by the lexer because of matching delimiters
    InvalidToken, // Helps to consolidate errors
    InvalidUnterminatedLiteral, // Helps to give a good error for an unterminated character or string literal
    InvalidUnterminatedBlockComment, // Helps to give a good error for an unterminated block comment
}