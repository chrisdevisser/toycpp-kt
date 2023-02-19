Per [intro.compliance.general], this document identifies all conditionally-supported constructs and whether they are supported. For semantics, see the list of implementation-defined behaviour.

## [lex.header]
**Can ', \, /*, or // appear in a header-name, and can " appear in the <> form of a header-name?**

Not supported. \ can appear here in the source file as part of a line splice, but it is conceptually removed by this point.

## [lex/ccon]
**Are multicharacter literals supported?**

Not supported. These are an abomination that will be lexed properly purely for a better compiler error.

**Can a character literal be a single character not representable in the execution character set?**

Not supported. Better to confront the problem up front instead of picking a value.

**What additional escape sequences are supported?**

None.

## [lex.string]
**Which additional string literal concatenations are supported?**

None. Mixing encoding prefixes is more confusing than helpful.