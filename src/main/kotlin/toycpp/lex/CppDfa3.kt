package toycpp.lex

import toycpp.lex.ppinterface.PpContextHolder
import toycpp.location.SourceChar
import toycpp.parser.*
import toycpp.parser.combinators.*

typealias CppLexer = Parser<List<PpToken>, SourceChar>

private val anyImpl = buildAny<SourceChar, Char>("") { it.c }
private val anyExceptImpl = buildAnyExcept<SourceChar, Char>("") { it.c }
private val seqImpl = buildSeq<SourceChar, Char>("") { it.c }

private fun single(c: Char) = anyImpl(listOf(c)) withValue { listOf(it) }
private operator fun Char.unaryPlus() = single(this)
private fun any(options: Iterable<Char>) = any(options.joinToString(""))
private fun any(options: String) = anyImpl(options.asIterable()) withValue { listOf(it) }
private fun anyExcept(exclusion: Char) = anyExceptImpl(listOf(exclusion)) withValue { listOf(it) }
private fun anyExcept(exclusions: String) = anyExceptImpl(exclusions.asIterable()) withValue { listOf(it) }

private fun seq(s: String) = seqImpl(s.toList())
private operator fun String.unaryPlus() = seq(this)

private fun genToken(kind: Pptok): (Iterable<SourceChar>) -> PpToken {
    return { chars ->
        PpToken(kind, chars.map { it.c }.joinToString(""), chars.first().loc, chars.last().loc, false) // TODO
    }
}

fun createCppLexer(ppContext: PpContextHolder, lexContext: LexContextHolder): CppLexer {
    val verticalTab = Char(11)
    val formFeed = Char(12)

    // First, primitives. These can be reused, but are direct representations of the standard token descriptions.
    // [lex.name]
    val nondigit = any('a'..'z') or any('A'..'Z') or +'_'
    val digit = any('0'..'9')

    // [lex.icon]
    val hexDigit = digit or any('a'..'f') or any('A'..'F')
    val octalDigit = any('0'..'7')

    // [lex.charset]
    val hexQuad = repeatL(hexDigit, 4)
    val ucn = +"\\u" thenL hexQuad or
            (+"\\U" thenL repeatL(hexQuad, 2))

    // Next, comments and whitespace
    // [lex.comment]
    val lineComment = +"//" thenL zeroOrMoreL(anyExcept('\n')) // TODO: detect \v and \f
    val blockComment = +"/*" thenL zeroOrMoreL(anyExcept('*') or (+'*' thenL anyExcept('/'))) thenL +"*/"
    val comment = lineComment or blockComment

    // [lex.pptoken]
    val rawWhitespace = oneOrMoreL(any(" \t\n${verticalTab}${formFeed}"))
    val whitespace = comment or rawWhitespace

    // Finally, actual tokens
    // Identifiers: [lex.name]
    val identifierNondigit = (nondigit or ucn)
    val identifier = identifierNondigit thenL zeroOrMoreL(digit or identifierNondigit)

    // PP Numbers: [lex.ppnumber]
    val sign = any("+-")
    val ppnum = (digit or +'.') thenL zeroOrMoreL(
        digit or
        identifierNondigit or +'.' or
        (+"'" thenL (digit or nondigit) or
        (any("eEpP") thenL sign))
    )

    // Character Literals: [lex.ccon]
    val encodingPrefix = +"u8" or +'u' or +'U' or +'L'

    val simpleEscapeSequence = +'\\' thenL any("'\"?\\abfnrtv")
    val octalEscapeSequence = +'\\' thenL repeatL(octalDigit, 1, 3)
    val hexEscapeSequence = +"\\x" thenL oneOrMoreL(hexDigit)
    val escapeSequence = simpleEscapeSequence or octalEscapeSequence or hexEscapeSequence

    val charLitElem = anyExcept("'\n\\") or escapeSequence or ucn
    val charLit = optionalList(encodingPrefix) thenL +"'" thenL oneOrMoreL(charLitElem) thenL +"'"

    // String Literals: [lex.string]
    val stringLitElem = anyExcept("\"\n\\") or escapeSequence or ucn

    val rawStringElem = anyExcept(')') // TODO: Save and check delimiter, max length 16
    val rawStringDelimElem = anyExcept(" ()\\\t\n${verticalTab}${formFeed}")
    val rawString = +"R\"" thenL zeroOrMoreL(rawStringDelimElem) thenL +'(' thenL zeroOrMoreL(rawStringElem) thenL +')' thenL zeroOrMoreL(rawStringDelimElem) thenL +'"'

    val stringLit = optionalList(encodingPrefix) thenL (+'"' thenL zeroOrMoreL(stringLitElem) thenL +'"' or rawString)

    // UDLs: [lex.ext]
    val charUdl = charLit thenL identifier
    val stringUdl = stringLit thenL identifier

    // Operators and Punctuation: [lex.operators]
    // Any common prefix in tokens needs the longer one to come first because of matching top-down.
    val ppOpOrPunc =
        // Digraphs: [lex.digraph]
        (+"<%" withValue genToken(Pptok.LBrace)) or
        (+"%>" withValue genToken(Pptok.RBrace)) or
        (+"<::" withValue genToken(Pptok.SpecialCaseTemplateLex)) or
        (+"<:" withValue genToken(Pptok.LSquareBracket)) or
        (+":>" withValue genToken(Pptok.RSquareBracket)) or
        (+"%:%:" withValue genToken(Pptok.Concat)) or
        (+"%:" withValue genToken(Pptok.Pound)) or
        (+"bitor" withValue genToken(Pptok.BitOr)) or
        (+"compl" withValue genToken(Pptok.Compl)) or
        (+"bitand" withValue genToken(Pptok.BitAnd)) or
        (+"and_eq" withValue genToken(Pptok.AndEquals)) or
        (+"or_eq" withValue genToken(Pptok.OrEquals)) or
        (+"xor_eq" withValue genToken(Pptok.XorEquals)) or
        (+"not_eq" withValue genToken(Pptok.NotEqualTo))
        (+"not" withValue genToken(Pptok.Not)) or
        (+"and" withValue genToken(Pptok.And)) or
        (+"or" withValue genToken(Pptok.Or)) or
        (+"xor" withValue genToken(Pptok.Xor))

        // The normal stuff
        (+"##" withValue genToken(Pptok.Concat)) or
        (+"" withValue genToken(Pptok.Pound)) or
        (+"{" withValue genToken(Pptok.LBrace)) or
        (+"}" withValue genToken(Pptok.RBrace)) or
        (+"[" withValue genToken(Pptok.LSquareBracket)) or
        (+"]" withValue genToken(Pptok.RSquareBracket)) or
        (+"(" withValue genToken(Pptok.OpenParen)) or
        (+")" withValue genToken(Pptok.CloseParen)) or
        (+";" withValue genToken(Pptok.Semicolon)) or
        (+"::" withValue genToken(Pptok.ColonColon)) or
        (+":" withValue genToken(Pptok.Colon)) or
        (+"..." withValue genToken(Pptok.Ellipsis)) or
        (+"?" withValue genToken(Pptok.Cond)) or
        (+".*" withValue genToken(Pptok.DotStar)) or
        (+"." withValue genToken(Pptok.Dot)) or
        (+"->*" withValue genToken(Pptok.ArrowStar)) or
        (+"->" withValue genToken(Pptok.Arrow)) or
        (+"+=" withValue genToken(Pptok.PlusEquals)) or
        (+"-=" withValue genToken(Pptok.MinusEquals)) or
        (+"*=" withValue genToken(Pptok.TimesEquals)) or
        (+"/=" withValue genToken(Pptok.OverEquals)) or
        (+"%=" withValue genToken(Pptok.ModEquals)) or
        (+"^=" withValue genToken(Pptok.XorEquals)) or
        (+"&=" withValue genToken(Pptok.AndEquals)) or
        (+"|=" withValue genToken(Pptok.OrEquals)) or
        (+"==" withValue genToken(Pptok.EqualTo)) or
        (+"!=" withValue genToken(Pptok.NotEqualTo)) or
        (+"~" withValue genToken(Pptok.Compl)) or
        (+"!" withValue genToken(Pptok.Not)) or
        (+"*" withValue genToken(Pptok.Times)) or
        (+"/" withValue genToken(Pptok.Over)) or
        (+"%" withValue genToken(Pptok.Mod)) or
        (+"^" withValue genToken(Pptok.Xor)) or
        (+"=" withValue genToken(Pptok.Assign)) or
        (+"<=>" withValue genToken(Pptok.Spaceship)) or
        (+"<=" withValue genToken(Pptok.LessThanOrEqualTo)) or
        (+">=" withValue genToken(Pptok.GreaterThanOrEqualTo)) or
        (+"&&" withValue genToken(Pptok.And)) or
        (+"||" withValue genToken(Pptok.Or)) or
        (+"&" withValue genToken(Pptok.BitAnd)) or
        (+"|" withValue genToken(Pptok.BitOr)) or
        (+"<<" withValue genToken(Pptok.LeftShift)) or
        (+">>" withValue genToken(Pptok.RightShift)) or
        (+"<<=" withValue genToken(Pptok.LeftShiftEquals)) or
        (+">==" withValue genToken(Pptok.RightShiftEquals)) or
        (+"++" withValue genToken(Pptok.PlusPlus)) or
        (+"--" withValue genToken(Pptok.MinusMinus)) or
        (+"+" withValue genToken(Pptok.Plus)) or
        (+"-" withValue genToken(Pptok.Minus)) or
        (+"<" withValue genToken(Pptok.LessThan)) or
        (+">" withValue genToken(Pptok.GreaterThan)) or
        (+"," withValue genToken(Pptok.Comma))


    // Anything not covered except ` @ $, which aren't in the BSCS.
    // ' and " are UB as tokens on their own per [lex.pptoken]/2
    val leftoverPunctuation = +"\\" withValue genToken(Pptok.OtherCharacter)

    return zeroOrMore(
        ppOpOrPunc or // Before identifier because of alternative tokens
        (identifier withValue genToken(Pptok.Identifier)) or
        (ppnum withValue genToken(Pptok.Ppnum)) or
        (charUdl withValue genToken(Pptok.CharUdl)) or // Before charLit because it adds to the end of one
        (charLit withValue genToken(Pptok.CharLit)) or
        (stringUdl withValue genToken(Pptok.StringUdl)) or // Before stringLit because it adds to the end of one
        (stringLit withValue genToken(Pptok.StringLit)) or
        leftoverPunctuation // After identifier because of UCNs
    )

//    return +'a' withValue {listOf(PpToken(Pptok.And, "", SourceLocation("", 0, 0), false))}
}