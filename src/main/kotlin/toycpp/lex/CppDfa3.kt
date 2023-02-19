package toycpp.lex

import toycpp.encoding.formFeed
import toycpp.encoding.verticalTab
import toycpp.lex.ppinterface.PpContextHolder
import toycpp.location.SourceChar
import toycpp.parser.*
import toycpp.parser.combinators.*

typealias CppLexer = Parser<List<PpToken>, SourceChar>

private val anyImpl = buildAny<SourceChar, Char>("") { it.c }
private val anyExceptImpl = buildAnyExcept<SourceChar, Char>("") { it.c }
private val seqImpl = buildSeq<SourceChar, Char>("") { it.c }

private fun single(c: Char) = anyImpl(listOf(c)) mapValue { listOf(it) }
private operator fun Char.unaryPlus() = single(this)
private fun any(options: Iterable<Char>) = any(options.joinToString(""))
private fun any(options: String) = anyImpl(options.asIterable()) mapValue { listOf(it) }
private fun anyExcept(exclusion: Char) = anyExceptImpl(listOf(exclusion)) mapValue { listOf(it) }
private fun anyExcept(exclusions: String) = anyExceptImpl(exclusions.asIterable()) mapValue { listOf(it) }

private fun seq(s: String) = seqImpl(s.toList())
private operator fun String.unaryPlus() = seq(this)

private fun genToken(kind: Pptok): (Iterable<SourceChar>) -> PpToken {
    return { chars ->
        PpToken(kind, chars.map { it.c }.joinToString(""), chars.first().loc, chars.last().loc, false) // TODO
    }
}

fun createCppLexer(ppContext: PpContextHolder, lexContext: LexContextHolder): CppLexer {
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
    val rawWhitespace = oneOrMoreL(any(" \t\n$verticalTab$formFeed"))
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
        (+"<%" mapValue genToken(Pptok.LBrace)) or
        (+"%>" mapValue genToken(Pptok.RBrace)) or
        (+"<::" mapValue genToken(Pptok.SpecialCaseTemplateLex)) or
        (+"<:" mapValue genToken(Pptok.LSquareBracket)) or
        (+":>" mapValue genToken(Pptok.RSquareBracket)) or
        (+"%:%:" mapValue genToken(Pptok.Concat)) or
        (+"%:" mapValue genToken(Pptok.Pound)) or
        (+"bitor" mapValue genToken(Pptok.BitOr)) or
        (+"compl" mapValue genToken(Pptok.Compl)) or
        (+"bitand" mapValue genToken(Pptok.BitAnd)) or
        (+"and_eq" mapValue genToken(Pptok.AndEquals)) or
        (+"or_eq" mapValue genToken(Pptok.OrEquals)) or
        (+"xor_eq" mapValue genToken(Pptok.XorEquals)) or
        (+"not_eq" mapValue genToken(Pptok.NotEqualTo))
        (+"not" mapValue genToken(Pptok.Not)) or
        (+"and" mapValue genToken(Pptok.And)) or
        (+"or" mapValue genToken(Pptok.Or)) or
        (+"xor" mapValue genToken(Pptok.Xor))

        // The normal stuff
        (+"##" mapValue genToken(Pptok.Concat)) or
        (+"" mapValue genToken(Pptok.Pound)) or
        (+"{" mapValue genToken(Pptok.LBrace)) or
        (+"}" mapValue genToken(Pptok.RBrace)) or
        (+"[" mapValue genToken(Pptok.LSquareBracket)) or
        (+"]" mapValue genToken(Pptok.RSquareBracket)) or
        (+"(" mapValue genToken(Pptok.OpenParen)) or
        (+")" mapValue genToken(Pptok.CloseParen)) or
        (+";" mapValue genToken(Pptok.Semicolon)) or
        (+"::" mapValue genToken(Pptok.ColonColon)) or
        (+":" mapValue genToken(Pptok.Colon)) or
        (+"..." mapValue genToken(Pptok.Ellipsis)) or
        (+"?" mapValue genToken(Pptok.Cond)) or
        (+".*" mapValue genToken(Pptok.DotStar)) or
        (+"." mapValue genToken(Pptok.Dot)) or
        (+"->*" mapValue genToken(Pptok.ArrowStar)) or
        (+"->" mapValue genToken(Pptok.Arrow)) or
        (+"+=" mapValue genToken(Pptok.PlusEquals)) or
        (+"-=" mapValue genToken(Pptok.MinusEquals)) or
        (+"*=" mapValue genToken(Pptok.TimesEquals)) or
        (+"/=" mapValue genToken(Pptok.OverEquals)) or
        (+"%=" mapValue genToken(Pptok.ModEquals)) or
        (+"^=" mapValue genToken(Pptok.XorEquals)) or
        (+"&=" mapValue genToken(Pptok.AndEquals)) or
        (+"|=" mapValue genToken(Pptok.OrEquals)) or
        (+"==" mapValue genToken(Pptok.EqualTo)) or
        (+"!=" mapValue genToken(Pptok.NotEqualTo)) or
        (+"~" mapValue genToken(Pptok.Compl)) or
        (+"!" mapValue genToken(Pptok.Not)) or
        (+"*" mapValue genToken(Pptok.Times)) or
        (+"/" mapValue genToken(Pptok.Over)) or
        (+"%" mapValue genToken(Pptok.Mod)) or
        (+"^" mapValue genToken(Pptok.Xor)) or
        (+"=" mapValue genToken(Pptok.Assign)) or
        (+"<=>" mapValue genToken(Pptok.Spaceship)) or
        (+"<=" mapValue genToken(Pptok.LessThanOrEqualTo)) or
        (+">=" mapValue genToken(Pptok.GreaterThanOrEqualTo)) or
        (+"&&" mapValue genToken(Pptok.And)) or
        (+"||" mapValue genToken(Pptok.Or)) or
        (+"&" mapValue genToken(Pptok.BitAnd)) or
        (+"|" mapValue genToken(Pptok.BitOr)) or
        (+"<<" mapValue genToken(Pptok.LeftShift)) or
        (+">>" mapValue genToken(Pptok.RightShift)) or
        (+"<<=" mapValue genToken(Pptok.LeftShiftEquals)) or
        (+">==" mapValue genToken(Pptok.RightShiftEquals)) or
        (+"++" mapValue genToken(Pptok.PlusPlus)) or
        (+"--" mapValue genToken(Pptok.MinusMinus)) or
        (+"+" mapValue genToken(Pptok.Plus)) or
        (+"-" mapValue genToken(Pptok.Minus)) or
        (+"<" mapValue genToken(Pptok.LessThan)) or
        (+">" mapValue genToken(Pptok.GreaterThan)) or
        (+"," mapValue genToken(Pptok.Comma))


    // Anything not covered except ` @ $, which aren't in the BSCS.
    // ' and " are UB as tokens on their own per [lex.pptoken]/2
    val leftoverPunctuation = +"\\" mapValue genToken(Pptok.OtherCharacter)

    return zeroOrMore(
        ppOpOrPunc or // Before identifier because of alternative tokens
        (identifier mapValue genToken(Pptok.Identifier)) or
        (ppnum mapValue genToken(Pptok.Ppnum)) or
        (charUdl mapValue genToken(Pptok.CharUdl)) or // Before charLit because it adds to the end of one
        (charLit mapValue genToken(Pptok.CharLit)) or
        (stringUdl mapValue genToken(Pptok.StringUdl)) or // Before stringLit because it adds to the end of one
        (stringLit mapValue genToken(Pptok.StringLit)) or
        leftoverPunctuation // After identifier because of UCNs
    )

//    return +'a' withValue {listOf(PpToken(Pptok.And, "", SourceLocation("", 0, 0), false))}
}