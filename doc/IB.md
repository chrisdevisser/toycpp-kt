Per [intro.compliance.general], this is a list of implementation-defined behaviours that are required to be documented.

TODO: document "all locale-specific characteristics": https://docs.oracle.com/cd/E19205-01/819-5265/bjayv/index.html

## [defns.diagnostic]
**Which output messages are diagnostics?**

TODO

## [defns.order.ptr]
**What is the ordering of pointer values?**

Pointers are compared via the integral value of their address.
A pointer p1 is ordered before a distinct pointer p2 iff the integral value of p1's address is less than the integral value of p2's address.

This ordering is simple and intuitive. It is straightforward to implement and imposes no overhead.

## [intro.abstract]
**What constitutes an interactive device?**

TODO

## [lex.phases]
**How are physical source file characters mapped to the basic source character set?**

The mapping from accepted ASCII source file characters to the basic source character set is an identity mapping.
A source file character is mapped to the identical character in the basic source character set.

This restriction is intended to be lifted in the future.

TODO: Unicode

## [lex.phases]
**What is the set of physical source file characters accepted?**

The set of accepted physical source file characters is exactly the basic source character set in ASCII representation.
Any character present that is not in the basic source character set will trigger an error.

This restriction is intended to be lifted in the future.

TODO: Unicode

## [lex.phases]
**Are the sources for interface dependency module units and header units required to be available?**

TODO

## [lex.phases]
**Is the source code for the definitions of required templates required to be available?**

TODO

## [lex.charset]
**What are the execution character set and execution wide-character set?**

The only supported execution character set is the basic execution character set.
The only supported execution wide-character set is the basic execution wide-character set.

This restriction is intended to be lifted in the future.

TODO: Unicode

## [lex.header] [cpp.include]
**How are characters of header-names mapped to headers or to external source file names?**

The characters are treated as a file path with separator converted from / to the system path separator. The <> form treats this path as relative to the system include path and then each user-provided include path. The "" form treats this path as relative to the file containing the #include directive, __has_include, or import directive containing the header-name in question.

Whitespace in the <> form is condensed to a single space.

A line splice inside of a header-name is removed.

TODO: UCNs
TODO: Specify system include path for each supported system
TODO: Specify how the user provides include paths

## [lex.header]
**[Conditional] What are the semantics of ', \, /*, and // in a header-name, and " in the <> form of a header-name?**

None of these are officially supported. \ can appear as part of a line splice, but that line splice is removed by this point.

## [lex.ccon]
**[Conditional] What is the value of a multicharacter literal?**

Multicharacter literals are not supported.

**[Conditional] What is the value of a character literal with a single character not representable in the execution character set?**

This is not supported.

**What is the value of a wide character literal with a single character not representable in the execution wide character set?**

The value is 0xFBADC0DE. The leading F keeps it near the maximum value, out of codepoint range, while BADC0DE indicates a bug. TODO: This will also trigger a compiler warning.

**What is the value of a wide multicharacter literal?**

The value is 0xFBADC0DE for the same reasons as before.

**[Conditional] What are the semantics of the additionally supported escape sequences?**

No additional escape sequences are supported. These will trigger a compiler error.

**What is the value of a character literal if it doesn't fit into char or wchar_t?**

The value is 0 for char. This minimizes the damage done when trying to use it. For wchar_t, the value is 0xFBADC0DE for the same reasons as before. This will also trigger a compiler warning.

**What is the encoding used to translate a UCN in a character literal if the character named has no encoding in the appropriate execution character set?**

The UCN will be encoded as the value 0 in the execution character set and the value 0xFBADC0DE in the wide execution character set. TODO: Unicode

## [lex.fcon]
**If the value of a floating-point literal is not representable, how is the value chosen?**

The value will be whatever Kotlin's kotlin.text.toDouble function chooses when given the lexeme. String to floating-point conversion is just a giant can of worms to open for a project like this.

## [lex.string]
**What is the behaviour of concatenating adjacent string literals with encoding prefixes other than the pairs specified?**

No additional pairs are supported. A compiler error is issued.

------------------

Index of the rest of the implementation-defined behavior

#pragma, [cpp.pragma]
additional execution policies supported by parallel algorithms, [execpol.type], [algorithms.parallel.exec]
additional file_type enumerators for file systems supporting additional types of file, [fs.enum.file.type]
additional formats for time_get::do_get_date, [locale.time.get.virtuals]
additional supported forms of preprocessing directive, [cpp.pre]
algorithms for producing the standard random number distributions, [rand.dist.general]
alignment, [basic.align]
alignment additional values, [basic.align]
alignment of bit-fields within a class object, [class.bit]
allocation of bit-fields within a class object, [class.bit]
any use of an invalid pointer other than to perform indirection or deallocate, [basic.stc.general]
argument values to construct ios_base::failure, [iostate.flags]
assignability of placeholder objects, [func.bind.place]
behavior of iostream classes when traits::pos_type is not streampos or when traits::off_type is not streamoff, [iostreams.limits.pos]
behavior of non-standard attributes, [dcl.attr.grammar]
behavior of strstreambuf::setbuf, [depr.strstreambuf.virtuals]
bits in a byte, [intro.memory]
code unit sequence for conditional-escape-sequence, [lex.string]
code unit sequence for non-representable string-literal, [lex.string]
conversions between pointers and integers, [expr.reinterpret.cast]
converting function pointer to object pointer and vice versa, [expr.reinterpret.cast]
default configuration of a pool, [mem.res.pool.mem]
default next_buffer_size for a monotonic_buffer_resource, [mem.res.monotonic.buffer.ctor]
default number of buckets in unordered_map, [unord.map.cnstr]
default number of buckets in unordered_multimap, [unord.multimap.cnstr]
default number of buckets in unordered_multiset, [unord.multiset.cnstr]
default number of buckets in unordered_set, [unord.set.cnstr]
default value for least_max_value template parameter of counting_semaphore, [thread.sema.cnt]
defining main in freestanding environment, [basic.start.main]
definition and meaning of __STDC__, [cpp.predefined], [diff.cpp]
definition and meaning of __STDC_VERSION__, [cpp.predefined]
definition of NULL, [support.types.nullptr], [diff.null]
derived type for typeid, [expr.typeid]
dynamic initialization of static inline variables before main, [basic.start.dynamic]
dynamic initialization of static variables before main, [basic.start.dynamic]
dynamic initialization of thread-local variables before entry, [basic.start.dynamic]
effect of calling associated Laguerre polynomials with n >= 128 or m >= 128, [sf.cmath.assoc.laguerre]
effect of calling associated Legendre polynomials with l >= 128, [sf.cmath.assoc.legendre]
effect of calling basic_filebuf::setbuf with nonzero arguments, [filebuf.virtuals]
effect of calling basic_filebuf::sync when a get area exists, [filebuf.virtuals]
effect of calling basic_streambuf::setbuf with nonzero arguments, [stringbuf.virtuals]
effect of calling cylindrical Bessel functions of the first kind with nu >= 128, [sf.cmath.cyl.bessel.j]
effect of calling cylindrical Neumann functions with nu >= 128, [sf.cmath.cyl.neumann]
effect of calling Hermite polynomials with n >= 128, [sf.cmath.hermite]
effect of calling ios_base::sync_with_stdio after any input or output operation on standard streams, [ios.members.static]
effect of calling irregular modified cylindrical Bessel functions with nu >= 128, [sf.cmath.cyl.bessel.k]
effect of calling Laguerre polynomials with n >= 128, [sf.cmath.laguerre]
effect of calling Legendre polynomials with l >= 128, [sf.cmath.legendre]
effect of calling regular modified cylindrical Bessel functions with nu >= 128, [sf.cmath.cyl.bessel.i]
effect of calling spherical associated Legendre functions with l >= 128, [sf.cmath.sph.legendre]
effect of calling spherical Bessel functions with n >= 128, [sf.cmath.sph.bessel]
effect of calling spherical Neumann functions with n >= 128, [sf.cmath.sph.neumann]
effect of conditional-escape-sequence on encoding state, [lex.string]
effect of filesystem::copy, [fs.op.copy]
effect on C locale of calling locale::global, [locale.statics]
error_category for errors originating outside the operating system, [value.error.codes]
exception type when random_device constructor fails, [rand.device]
exception type when random_device::operator() fails, [rand.device]
exception type when shared_ptr constructor fails, [util.smartptr.shared.const]
exceptions thrown by standard library functions that have a potentially-throwing exception specification, [res.on.exception.handling]
exit status, [support.start.term]
extended signed integer types, [basic.fundamental]
file type of the file argument of filesystem::status, [fs.op.status]
formatted character sequence generated by time_put::do_put in C locale, [locale.time.put.virtuals]
forward progress guarantees for implicit threads of parallel algorithms (if not defined for thread), [algorithms.parallel.exec]
growth factor for monotonic_buffer_resource, [mem.res.monotonic.buffer.ctor], [mem.res.monotonic.buffer.mem]
headers for freestanding implementation, [compliance]
how random_device::operator() generates values, [rand.device]
how the set of importable headers is determined, [module.import]
integer-class type, [iterator.concept.winc]
interpretation of the path character sequence with format path::auto_format, [fs.enum.path.format]
largest supported value to configure the largest allocation satisfied directly by a pool, [mem.res.pool.options]
largest supported value to configure the maximum number of blocks to replenish a pool, [mem.res.pool.options]
last enumerator of launch, [future.syn]
linkage of main, [basic.start.main]
linkage of names from C standard library, [using.linkage]
linkage of objects between C++ and other languages, [dcl.link]
locale names, [locale.cons]
lvalue-to-rvalue conversion of an invalid pointer value, [conv.lval]
manner of search for included source file, [cpp.include]
mapping from name to catalog when calling messages::do_open, [locale.messages.virtuals]
mapping of pointer to integer, [expr.reinterpret.cast]
mapping to message when calling messages::do_get, [locale.messages.virtuals]
maximum depth of recursive template instantiations, [temp.inst]
maximum size of an allocated object, [expr.new], [new.badlength]
meaning of asm declaration, [dcl.asm]
meaning of attribute declaration, [dcl.pre]
meaning of dot-dot in root-directory, [fs.path.generic]
negative value of character-literal in preprocessor, [cpp.cond]
nesting limit for #include directives, [cpp.include]
NTCTS in basic_ostream<charT, traits>& operator<<(nullptr_t), [ostream.inserters]
number of placeholders for bind expressions, [functional.syn], [func.bind.place]
number of threads in a program under a freestanding implementation, [intro.multithread.general]
numeric values of character-literals in #if directives, [cpp.cond]
operating system on which implementation depends, [fs.conform.os]
parameters to main, [basic.start.main]
passing argument of class type through ellipsis, [expr.call]
presence and meaning of native_handle_type and native_handle, [thread.req.native]
rank of extended signed integer type, [conv.rank]
required alignment for atomic_ref type's operations, [atomics.ref.generic.general], [atomics.ref.int], [atomics.ref.float], [atomics.ref.pointer]
required libraries for freestanding implementation, [intro.compliance.general]
resource limits on a message catalog, [locale.messages.virtuals]
result of filesystem::file_size, [fs.op.file.size]
result of inexact floating-point conversion, [conv.double]
return value of bad_alloc::what, [bad.alloc]
return value of bad_any_cast::what, [any.bad.any.cast]
return value of bad_array_new_length::what, [new.badlength]
return value of bad_cast::what, [bad.cast]
return value of bad_exception::what, [bad.exception]
return value of bad_function_call::what, [func.wrap.badcall]
return value of bad_optional_access::what, [optional.bad.access]
return value of bad_typeid::what, [bad.typeid]
return value of bad_variant_access::what, [variant.bad.access]
return value of bad_weak_ptr::what, [util.smartptr.weak.bad]
return value of char_traits<char16_t>::eof, [char.traits.specializations.char16.t]
return value of char_traits<char32_t>::eof, [char.traits.specializations.char32.t]
return value of exception::what, [exception]
return value of type_info::name(), [type.info]
search locations for "" header, [cpp.include]
search locations for <> header, [cpp.include]
semantics of an access through a volatile glvalue, [dcl.type.cv]
semantics of linkage specifiers, [dcl.link]
semantics of parallel algorithms invoked with implementation-defined execution policies, [algorithms.parallel.exec]
semantics of stacktrace_entry::native_handle, [stacktrace.entry.obs]
semantics of token parameter and default token value used by random_device constructors, [rand.device]
sequence of places searched for a header, [cpp.include]
set of character types that iostreams templates can be instantiated for, [locale.category], [iostreams.limits.pos]
signedness of char, [dcl.type.simple]
sizeof applied to fundamental types other than char, signed char, and unsigned char, [expr.sizeof]
stack unwinding before invocation of std::terminate, [except.handle], [except.terminate]
stacktrace_entry::native_handle_type, [stacktrace.entry.overview]
startup and termination in freestanding environment, [basic.start.main]
string resulting from __func__, [dcl.fct.def.general]
support for always lock-free integral atomic types in freestanding environments, [compliance]
support for extended alignments, [basic.align]
support for module-import-declarations with non-C++ language linkage, [dcl.link]
supported multibyte character encoding rules, [char.traits.specializations.char]
supported root-names in addition to any operating system dependent root-names, [fs.path.generic]
text of __DATE__ when date of translation is not available, [cpp.predefined]
text of __TIME__ when time of translation is not available, [cpp.predefined]
threads and program points at which deferred dynamic initialization is performed, [basic.start.dynamic]
type aliases atomic_signed_lock_free and atomic_unsigned_lock_free in freestanding environments, [compliance]
type of a directory-like file, [fs.class.directory.iterator.general], [fs.class.rec.dir.itr.general]
type of array::const_iterator, [array.overview]
type of array::iterator, [array.overview]
type of basic_stacktrace::const_iterator, [stacktrace.basic.overview]
type of basic_stacktrace::difference_type, [stacktrace.basic.overview]
type of basic_stacktrace::size_type, [stacktrace.basic.overview]
type of basic_string::const_iterator, [basic.string.general]
type of basic_string::iterator, [basic.string.general]
type of basic_string_view::const_iterator, [string.view.template.general], [string.view.iterators]
type of default_random_engine, [rand.predef]
type of deque::const_iterator, [deque.overview]
type of deque::difference_type, [deque.overview]
type of deque::iterator, [deque.overview]
type of deque::size_type, [deque.overview]
type of forward_list::const_iterator, [forwardlist.overview]
type of forward_list::difference_type, [forwardlist.overview]
type of forward_list::iterator, [forwardlist.overview]
type of forward_list::size_type, [forwardlist.overview]
type of list::const_iterator, [list.overview]
type of list::difference_type, [list.overview]
type of list::iterator, [list.overview]
type of list::size_type, [list.overview]
type of map::const_iterator, [map.overview]
type of map::difference_type, [map.overview]
type of map::iterator, [map.overview]
type of map::size_type, [map.overview]
type of multimap::const_iterator, [multimap.overview]
type of multimap::difference_type, [multimap.overview]
type of multimap::iterator, [multimap.overview]
type of multimap::size_type, [multimap.overview]
type of multiset::const_iterator, [multiset.overview]
type of multiset::difference_type, [multiset.overview]
type of multiset::iterator, [multiset.overview]
type of multiset::size_type, [multiset.overview]
type of ptrdiff_t, [expr.add], [support.types.layout]
type of regex_constants::error_type, [re.err]
type of regex_constants::match_flag_type, [re.matchflag]
type of set::const_iterator, [set.overview]
type of set::difference_type, [set.overview]
type of set::iterator, [set.overview]
type of set::size_type, [set.overview]
type of size_t, [support.types.layout]
type of span::iterator, [span.overview], [span.iterators]
type of streamoff, [stream.types]
type of streamsize, [stream.types]
type of syntax_option_type, [re.synopt]
type of unordered_map::const_iterator, [unord.map.overview]
type of unordered_map::const_local_iterator, [unord.map.overview]
type of unordered_map::difference_type, [unord.map.overview]
type of unordered_map::iterator, [unord.map.overview]
type of unordered_map::local_iterator, [unord.map.overview]
type of unordered_map::size_type, [unord.map.overview]
type of unordered_multimap::const_iterator, [unord.multimap.overview]
type of unordered_multimap::const_local_iterator, [unord.multimap.overview]
type of unordered_multimap::difference_type, [unord.multimap.overview]
type of unordered_multimap::iterator, [unord.multimap.overview]
type of unordered_multimap::local_iterator, [unord.multimap.overview]
type of unordered_multimap::size_type, [unord.multimap.overview]
type of unordered_multiset::const_iterator, [unord.multiset.overview]
type of unordered_multiset::const_local_iterator, [unord.multiset.overview]
type of unordered_multiset::difference_type, [unord.multiset.overview]
type of unordered_multiset::iterator, [unord.multiset.overview]
type of unordered_multiset::local_iterator, [unord.multiset.overview]
type of unordered_multiset::size_type, [unord.multiset.overview]
type of unordered_set::const_iterator, [unord.set.overview]
type of unordered_set::const_local_iterator, [unord.set.overview]
type of unordered_set::difference_type, [unord.set.overview]
type of unordered_set::iterator, [unord.set.overview]
type of unordered_set::local_iterator, [unord.set.overview]
type of unordered_set::size_type, [unord.set.overview]
type of vector::const_iterator, [vector.overview]
type of vector::difference_type, [vector.overview]
type of vector::iterator, [vector.overview]
type of vector::size_type, [vector.overview]
type of vector<bool>::const_iterator, [vector.bool]
type of vector<bool>::const_pointer, [vector.bool]
type of vector<bool>::difference_type, [vector.bool]
type of vector<bool>::iterator, [vector.bool]
type of vector<bool>::pointer, [vector.bool]
type of vector<bool>::size_type, [vector.bool]
underlying type for enumeration, [dcl.enum]
underlying type of bool, [basic.fundamental]
underlying type of char, [basic.fundamental]
underlying type of wchar_t, [basic.fundamental]
unit suffix when Period::type is micro, [time.duration.io]
value of bit-field that cannot represent
assigned value, [expr.ass]
incremented value, [expr.post.incr]
initializer, [dcl.init.general]
value of ctype<char>::table_size, [facet.ctype.special.general]
value of future_errc::broken_promise, [future.syn]
value of future_errc::future_already_retrieved, [future.syn]
value of future_errc::no_state, [future.syn]
value of future_errc::promise_already_satisfied, [future.syn]
value of has-attribute-expression for non-standard attributes, [cpp.cond]
value of pow(0,0), [complex.transcendentals]
value of result of inexact integer to floating-point conversion, [conv.fpint]
value representation of floating-point types, [basic.fundamental]
value representation of pointer types, [basic.compound]
values of a trivially copyable type, [basic.types.general]
values of various ATOMIC_..._LOCK_FREE macros, [atomics.lockfree]
whether <cfenv> functions can be used to manage floating-point status, [cfenv.syn]
whether a given atomic type's operations are always lock free, [atomics.types.generic.general], [atomics.types.operations], [atomics.types.int], [atomics.types.float], [atomics.types.pointer], [util.smartptr.atomic.shared], [util.smartptr.atomic.weak]
whether a given atomic_ref type's operations are always lock free, [atomics.ref.generic.general], [atomics.ref.int], [atomics.ref.float], [atomics.ref.pointer]
whether an implementation has relaxed or strict pointer safety, [basic.stc.dynamic.safety]
whether functions from Annex K of the C standard library are declared when C++ headers are included, [headers]
whether get_pointer_safety returns pointer_safety::relaxed or pointer_safety::preferred if the implementation has relaxed pointer safety, [util.dynamic.safety]
whether locale object is global or per-thread, [locale.general]
whether pragma FENV_ACCESS is supported, [cfenv.syn]
whether rand may introduce a data race, [c.math.rand]
whether sequence pointers are copied by basic_filebuf move constructor, [filebuf.cons]
whether sequence pointers are copied by basic_stringbuf move constructor, [stringbuf.cons]
whether sequence pointers are initialized to null pointers, [stringbuf.cons]
whether source file inclusion of importable header is replaced with import directive, [cpp.include]
whether stack is unwound before invoking the function std::terminate when a noexcept specification is violated, [except.terminate]
whether the implementation is hosted or freestanding, [compliance]
whether the lifetime of a parameter ends when the callee returns or at the end of the enclosing full-expression, [expr.call]
whether the thread that executes main and the threads created by std::thread or std::jthread provide concurrent forward progress guarantees, [intro.progress]
whether time_get::do_get_year accepts two-digit year numbers, [locale.time.get.virtuals]
whether values are rounded or truncated to the required precision when converting between time_t values and time_point objects, [time.clock.system.members]
which functions in the C++ standard library may be recursively reentered, [reentrancy]
which non-standard-layout objects containing no data are considered empty, [intro.object]
which scalar types have unique object representations, [meta.unary.prop]
width of integral type, [basic.fundamental]