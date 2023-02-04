package toycpp.lex

import toycpp.dfa.Dfa
import toycpp.dfa.DfaNode
import toycpp.dfa.dfa
import toycpp.lex.ppinterface.PpContextHolder

typealias CppDfa = Dfa<Pptok>
typealias Id = DfaNode.Id

/**
 * A C++ DFA that models lex.pptoken and the following sections.
 */
fun createCppDfa(context: PpContextHolder) = dfa<Pptok> {
    // First, primitives. These can be reused, but are direct representations of the standard token descriptions.

    // [lex.name]
    val nondigits =
        ('a'..'z').joinToString("") +
        ('A'..'Z').joinToString("") +
        '_'

    // [lex.name]
    val digits = ('0'..'9').joinToString("")

    // [lex.icon]
    val hexDigits =
        digits +
        ('a'..'f').joinToString("") +
        ('A'..'F').joinToString("")

    // [lex.charset]
    val hexQuad =

    start {
        // First comes stuff involving connecting nodes.
        // This allows said nodes to later be marked as accepting as needed.
        // The other way around doesn't work because separate nodes might already be created in the place of one.

        // [lex.comment]
        seq("//") connects acceptingNode("line comment", Pptok.Comment) {
            anyExcept("\n") connects selfId
        }

        // [lex.comment]
        seq("/*") connects node("block comment") {
            seq("*/") accepts Pptok.Comment
            anythingElse() connects selfId
        }

        // [lex.charset]/2
        seq("\\u") connects node("UCN ") {

        }
        seq("\\U")

        // The easy stuff: [lex.operators]
        '#' accepts Pptok.Pound
        seq("##") accepts Pptok.Concat
        '{' accepts Pptok.LBrace
        '}' accepts Pptok.RBrace
        '[' accepts Pptok.LSquareBracket
        ']' accepts Pptok.RSquareBracket
        '(' accepts Pptok.OpenParen
        ')' accepts Pptok.CloseParen
        ';' accepts Pptok.Semicolon
        ':' accepts Pptok.Colon
        seq("...") accepts Pptok.Ellipsis
        '?' accepts Pptok.Cond
        seq("::") accepts Pptok.ColonColon
        '.' accepts Pptok.Dot
        seq(".*") accepts Pptok.DotStar
        seq("->") accepts Pptok.Arrow
        seq("->*") accepts Pptok.ArrowStar
        '~' accepts Pptok.Compl
        '!' accepts Pptok.Not
        '+' accepts Pptok.Plus
        '-' accepts Pptok.Minus
        '*' accepts Pptok.Times
        '/' accepts Pptok.Over
        '%' accepts Pptok.Mod
        '^' accepts Pptok.Xor
        '&' accepts Pptok.BitAnd
        '|' accepts Pptok.BitOr
        '=' accepts Pptok.Assign
        seq("+=") accepts Pptok.PlusEquals
        seq("-=") accepts Pptok.MinusEquals
        seq("*=") accepts Pptok.TimesEquals
        seq("/=") accepts Pptok.OverEquals
        seq("%=") accepts Pptok.ModEquals
        seq("^=") accepts Pptok.XorEquals
        seq("&=") accepts Pptok.AndEquals
        seq("|=") accepts Pptok.OrEquals
        seq("==") accepts Pptok.EqualTo
        seq("!=") accepts Pptok.NotEqualTo
        '<' accepts Pptok.LessThan
        '>' accepts Pptok.GreaterThan
        seq("<=") accepts Pptok.LessThanOrEqualTo
        seq(">=") accepts Pptok.GreaterThanOrEqualTo
        seq("<=>") accepts Pptok.Spaceship
        seq("&&") accepts Pptok.And
        seq("||") accepts Pptok.Or
        seq("<<") accepts Pptok.LeftShift
        seq(">>") accepts Pptok.RightShift
        seq("<<=") accepts Pptok.LeftShiftEquals
        seq(">==") accepts Pptok.RightShiftEquals
        seq("++") accepts Pptok.PlusPlus
        seq("--") accepts Pptok.MinusMinus
        ',' accepts Pptok.Comma

        // Anything not covered except ` @ $, which aren't in the BSCS.
        // ' and " are UB as tokens on their own per [lex.pptoken]/2
        '\\' accepts Pptok.OtherCharacter

        // Digraphs: [lex.digraph]
        seq("<%") accepts Pptok.LBrace
        seq("%>") accepts Pptok.RBrace
        seq("<:") accepts Pptok.LSquareBracket // TODO: <:: is < ::, not <: :
        seq(":>") accepts Pptok.RSquareBracket
        seq("%:") accepts Pptok.Pound
        seq("%:%:") accepts Pptok.Concat // TODO: %:% is %: %, not an error
        seq("and") accepts Pptok.And
        seq("bitor") accepts Pptok.BitOr
        seq("or") accepts Pptok.Or
        seq("xor") accepts Pptok.Xor
        seq("compl") accepts Pptok.Compl
        seq("bitand") accepts Pptok.BitAnd
        seq("and_eq") accepts Pptok.AndEquals
        seq("or_eq") accepts Pptok.OrEquals
        seq("xor_eq") accepts Pptok.XorEquals
        seq("not") accepts Pptok.Not
        seq("not_eq") accepts Pptok.NotEqualTo

        // And some other fun stuff.
        '\n' accepts Pptok.StartOfLine
    }
}