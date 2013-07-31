# Assembly for XML

**Xembly** is an [Assembly](http://en.wikipedia.org/wiki/Assembly_language)-like
[imperative](http://en.wikipedia.org/wiki/Imperative_programming) programming language
for data manipulation in XML documents. For example, you have a document:

```xml
<orders>
  <order>
    <id>553</id>
    <amount>$45.00</amount>
  </order>
</orders>
```

And you want to change the amount of the order #553
from `$45.00` to `$140.00`. Xembly script would look like:

```
XPATH "orders/order[id=553]";
SET "$140.00";
```

It is simple and compact. For more complex scenarios you can use
[XSLT](http://www.w3.org/TR/xslt) or [XQuery](http://www.w3.org/TR/xquery/).

This Java package implements Xembly:

```java
Document document = // ready to use DOM document
Xembly xembly = new Xembly(
  "XPATH "orders/order[id=553]; SET "$140.00;"
);
xembly.exec(document);
```

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/rultor/rultor/issues/new),
or a question to our
[Google Group](https://groups.google.com/forum/#!forum/rultor).
