### User Example

This project demonstrates the techniques for creating immutable simple
java data objects that are constructed via static factory methods via
fluid builder method chaining but also are suitable for efficient JSON
parsing/storage supporting shallow hierarchical relationships.

Root interface is ```User```

```StdUser``` implements this interface, this is the base concrete class.

```UserWithRoles``` is composed of a ```StdUser``` and all its methods manipulate and populate it.

See unit tests and inline comments for details.

Hierarchies should be kept shallow, implement ```User``` only. All data packed into
the ```StdUser``` attributes collection.