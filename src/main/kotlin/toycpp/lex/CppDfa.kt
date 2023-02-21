package toycpp.lex

import toycpp.dfa.DfaNode.Id
import toycpp.dfa.DfaNodeDsl
import toycpp.dfa.dfa
import toycpp.encoding.formFeed
import toycpp.encoding.verticalTab
import toycpp.lex.Pptok.*
import toycpp.lex.ppinterface.PpContext
import toycpp.lex.ppinterface.PpContextHolder

fun chooseDfasFromPpContext(ppContext: PpContextHolder): () -> List<CppDfa> = {
    if (ppContext.state == PpContext.HeaderNameIsValid) {
        listOf(headerNameDfa, cppDfa)
    } else {
        listOf(cppDfa)
    }
}

// TODO: Consider how to allow an unfinished escape code or UCN.
//     It's still desirable to pretend it's valid and lex the rest of the token, but then produce an invalid token.
//     The normal result would be the lexer pointing to the start of a string literal and saying
//     Maybe a DSL element that specifies a fallback ID for where to go if nothing matches.
//     This needs to be used with care because it prevents the ordinary backtracking behaviour.

// First, primitives. These can be reused, but are direct representations of the standard token descriptions.

// [lex.name]
private val nondigit =
    ('a'..'z').joinToString("") +
    ('A'..'Z').joinToString("") +
    '_'

// [lex.name]
private val digit = ('0'..'9').joinToString("")

// [lex.icon]
private val hexDigit =
    digit +
    ('a'..'f').joinToString("") +
    ('A'..'F').joinToString("")

// [lex.icon]
private val octalDigit = ('0'..'7').joinToString("")

/**
 * A C++ DFA that models lex.pptoken and the following sections.
 */
/**
 * A C++ DFA that models lex.pptoken and the following sections.
 */
val cppDfa = dfa {
    // First comes stuff involving connecting nodes.
    // This allows said nodes to later be marked as accepting as needed.
    // The other way around doesn't work because separate nodes might already be created in the place of one.

    // Line comments: [lex.comment]
    seq("//") connects acceptingNode("line comment", Comment) {
        anyExcept("\n") connects selfId
    }

    // Block comments [lex.comment]
    seq("/*") connects acceptingNode("block comment", InvalidUnterminatedBlockComment) {
        val blockCommentId = selfId

        '*' connects node("block comment + '*'") {
            '/' accepts Comment
            '*' connects selfId
            anythingElse() connects blockCommentId
        }

        anythingElse() connects selfId
    }

    // PP numbers: [lex.ppnumber]
    val ppnum = acceptingNode("ppnum", Ppnum) {
        val ppnumId = selfId

        any(digit) connects selfId
        any(nondigit) except "eEpP" connects selfId
        connectUcnTo(selfId)

        '\'' connects node("ppnum separator") {
            any(digit) connects ppnumId
            any(nondigit) connects ppnumId
        }

        any("eEpP") connects node("ppnum exponent") {
            '+' connects ppnumId
            '-' connects ppnumId
        }

        '.' connects selfId
    }

    any(digit) connects ppnum
    '.' connects node(".") {
        any(digit) connects ppnum
    }

    // Character literals: [lex.ccon]
    // String literals: [lex.string]
    // UDLs: [lex.ext]
    val charLit = acceptingNode("character literal end", CharLit) {
        acceptIdentifier("character UDL", CharUdl)
    }

    val stringLit = acceptingNode("string literal end", StringLit) {
        acceptIdentifier("string UDL", StringUdl)
    }

    // This allows an empty character literal, which is later checked.
    // It's okay to delay the check because the alternative is a lone ' token, which is UB.
    val partialCharLit = buildPartialCharOrStringLiteral("character literal", '\'', charLit)
    val partialStringLit = buildPartialCharOrStringLiteral("string literal", '"', stringLit)

    '\'' connects partialCharLit
    '"' connects partialStringLit

    // Identifiers: [lex.name]
    val identifier = acceptingNode("identifier", Identifier) {
        continueIdentifierTo(selfId)
    }
    any(nondigit) except "RuUL" connects identifier
    connectUcnTo(identifier)

    // Raw strings are only recognized, not lexed here.
    val possibleRawString = acceptingNode("possible raw string", Identifier) {
        continueIdentifierTo(identifier)
        '"' accepts RawStringStart
    }

    'R' connects possibleRawString

    any("UL") connects acceptingNode("possible encoding prefix", Identifier) {
        fillEncodingPrefix(identifier, partialCharLit, partialStringLit, possibleRawString)
    }

    'u' connects acceptingNode("possible u encoding prefix", Identifier) {
        fillEncodingPrefix(identifier, partialCharLit, partialStringLit, possibleRawString, exclusions = "8")

        '8' connects acceptingNode("possible u8 encoding prefix", Identifier) {
            fillEncodingPrefix(identifier, partialCharLit, partialStringLit, possibleRawString)
        }
    }

    // The easy stuff: [lex.operators]
    '#' accepts Pound
    seq("##") accepts Concat
    '{' accepts LBrace
    '}' accepts RBrace
    '[' accepts LSquareBracket
    ']' accepts RSquareBracket
    '(' accepts OpenParen
    ')' accepts CloseParen
    ';' accepts Semicolon
    ':' accepts Colon
    seq("...") accepts Ellipsis
    '?' accepts Cond
    seq("::") accepts ColonColon
    '.' accepts Dot
    seq(".*") accepts DotStar
    seq("->") accepts Arrow
    seq("->*") accepts ArrowStar
    '~' accepts Compl
    '!' accepts Not
    '+' accepts Plus
    '-' accepts Minus
    '*' accepts Times
    '/' accepts Over
    '%' accepts Mod
    '^' accepts Xor
    '&' accepts BitAnd
    '|' accepts BitOr
    '=' accepts Assign
    seq("+=") accepts PlusEquals
    seq("-=") accepts MinusEquals
    seq("*=") accepts TimesEquals
    seq("/=") accepts OverEquals
    seq("%=") accepts ModEquals
    seq("^=") accepts XorEquals
    seq("&=") accepts AndEquals
    seq("|=") accepts OrEquals
    seq("==") accepts EqualTo
    seq("!=") accepts NotEqualTo
    '<' accepts LessThan
    '>' accepts GreaterThan
    seq("<=") accepts LessThanOrEqualTo
    seq(">=") accepts GreaterThanOrEqualTo
    seq("<=>") accepts Spaceship
    seq("&&") accepts And
    seq("||") accepts Or
    seq("<<") accepts LeftShift
    seq(">>") accepts RightShift
    seq("<<=") accepts LeftShiftEquals
    seq(">>=") accepts RightShiftEquals
    seq("++") accepts PlusPlus
    seq("--") accepts MinusMinus
    ',' accepts Comma

    // Whitespace: [lex.token]
    '\n' accepts Newline
    any(" \t$verticalTab$formFeed") connects acceptingNode("whitespace", Whitespace) {
        any(" \t$verticalTab$formFeed") connects selfId
    }

    // Anything not covered except ` @ $ (and non-whitespace control characters), which aren't in the BSCS.
    // ' and " are UB as tokens on their own per [lex.pptoken]/2
    '\\' accepts OtherCharacter

    // Digraphs: [lex.digraph]
    seq("<%") accepts LBrace
    seq("%>") accepts RBrace
    seq("<:") accepts LSquareBracket
    seq(":>") accepts RSquareBracket
    seq("<::") accepts SpecialCaseTemplateLex // This doesn't follow maximal munch. The lexer sometimes has to produce < ::
    seq("%:") accepts Pound
    seq("%:%:") accepts Concat
}

val headerNameDfa = dfa {
    '<' connects node("angled header") {
        anyExcept(">\n") connects selfId
        '>' connects acceptingNode("angled header end", AngledHeaderName)
    }

    '"' connects node("quoted header") {
        anyExcept("\"") connects selfId
        '"' connects acceptingNode("quoted header end", QuotedHeaderName)
    }
}

/**
 * Adds mid-identifier transitions to the given node.
 */
private fun DfaNodeDsl<Pptok>.continueIdentifierTo(identifierId: Id, exclusions: String = "") {
    any(nondigit + digit) except exclusions connects identifierId
    connectUcnTo(identifierId)
}

/**
 * Inserts an identifier DFA into the current node. Accepts the given value within the identifier.
 */
internal fun DfaNodeDsl<Pptok>.acceptIdentifier(id: String, acceptValue: Pptok) {
    val identifier = acceptingNode(id, acceptValue) {
        continueIdentifierTo(selfId)
    }
    any(nondigit) connects identifier
    connectUcnTo(identifier)
}

private fun DfaNodeDsl<Pptok>.fillEncodingPrefix(identifierId: Id, partialCharLitId: Id, partialStringLitId: Id, possibleRawStringId: Id, exclusions: String = "") {
    continueIdentifierTo(identifierId, exclusions = "R$exclusions")

    '\'' connects partialCharLitId
    '"' connects partialStringLitId
    'R' connects possibleRawStringId
}

// [lex.charset]: Inserts a UCN DFA into the current node
private fun DfaNodeDsl<Pptok>.connectUcnTo(target: Id) {
    seq("\\u") connects node(selfId.value + " + '\\u'") {
        nest(3) {
            any(hexDigit) connects target
        }
    }

    seq("\\U") connects node(selfId.value + " + '\\U'") {
        nest(7) {
            any(hexDigit) connects target
        }
    }
}

/**
 * Inserts a DFA into the current node for the part of a character or string literal after the opening quote.
 * Connects to the given end node upon seeing the given ending character.
 */
private fun DfaNodeDsl<Pptok>.buildPartialCharOrStringLiteral(id: String, endChar: Char, endId: Id) =
    // This state accepts a pseudotoken to mark it as an unterminated character or string literal.
    // It's okay to accept this instead of backtracking because the alternative is a single ' or " token, which is UB.
    acceptingNode(id, InvalidUnterminatedLiteral) {
        val partialLitId = selfId

        fillPartialLit(endChar, endId)
        any(octalDigit) connects selfId
        '\\' connects node("$partialLitId + '\\'") {
            val escSeqId = selfId

            any("'\"?\\abfnrtv") connects partialLitId

            'x' connects node("$partialLitId hex escape'") {
                any(hexDigit) connects node("$partialLitId + '\\x#'") {
                    anyExcept("$endChar\n\\$hexDigit") connects partialLitId
                    endChar connects endId
                    '\\' connects escSeqId

                    any(hexDigit) connects selfId
                }
            }

            any(octalDigit) connects node("$partialLitId + '\\#'") {
                fillPartialLit(endChar, endId)
                '\\' connects escSeqId
                any(octalDigit) connects node("$partialLitId + '\\##'") {
                    fillPartialLit(endChar, endId)
                    '\\' connects escSeqId
                    any(octalDigit) connects partialLitId
                }
            }
        }

        connectUcnTo(selfId)
    }

/**
 * Fills repetitive transitions for the partial character or string lit.
 */
private fun DfaNodeDsl<Pptok>.fillPartialLit(endChar: Char, endId: Id) {
    anyExcept("$endChar\n\\$octalDigit") connects selfId
    endChar connects endId
}