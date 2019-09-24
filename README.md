# IntervalStoreJ

A Java read-write implementation of the Nested Containment List data structure, as described in
  
_Alexander V. Alekseyenko, Christopher J. Lee; Nested Containment List (NCList): a new algorithm for accelerating interval query of genome alignment and interval databases, Bioinformatics, Volume 23, Issue 11, 1 June 2007, Pages 1386–1393, https://doi.org/10.1093/bioinformatics/btl647_

NCList provides efficient lookup of intervals overlapping a given range in time _O(M log N)_ where N is the number of intervals stored, and M the number of overlaps found.

Key features of `IntervalStore`
* is parameterised by `<T extends IntervalI>`
* so can store any Java type that implements `IntervalI`, that is, has methods `getBegin()` and `getEnd()` (where begin <= end)
* extends `java.util.AbstractCollection<T>`
    - may be referred to as `Collection<T>` in code
    - exposes methods for `add, contains, remove, iterator, size` etc
* has a 'bulk load' constructor, and methods to add or remove entries, while retaining lookup efficiency
* optimises storage and search of sparsely nested intervals by storing non-nested intervals separately
* incorporates NCList to store any properly nested intervals

To use IntervalStore in your application:
* add intervalstore.jar to the classpath
* let your type `T` to be stored implement `intervalstore.api.IntervalI`
* construct, add to and query `intervalstore.impl.IntervalStore<T>` as required

Unit tests give 99% coverage of the code. 
These require the TestNG library, available from the [Eclipse Marketplace](https://marketplace.eclipse.org/content/testng-eclipse), 
or <https://testng.org/doc/download.html>.

If you use IntervalStoreJ, please cite:
Carstairs et al, (2019), "IntervalStoreJ: A Reusable Read-Write Java Implementation of Nested Containment List" (in preparation).


------------------------------------------------------------


## Version History
v1.1 24-Sep-2019 

Performance and related enhancements suggested by BobHanson
* avoid use of 'capturing lambda' (with object creation) in `BinarySearcher`
* provide an overloaded `IntervalStoreI.findOverlaps` that accepts a list to add to
* provide an overloaded `IntervalStoreI.add(T o, boolean allowDuplicates)` to allow 'deferred check for contains' in implementations
* shortcut in `BinarySearcher.findFirst` when adding features in increasing start order

All comparators are now defined in `IntervalI`.

v1.0 29-Mar-2019

First public release
