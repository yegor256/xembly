<img src="http://img.xembly.org/logo-256x256.png" width="64px" height="64px" />

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/xembly)](http://www.rultor.com/p/yegor256/xembly)

[![Build Status](https://travis-ci.org/yegor256/xembly.svg?branch=master)](https://travis-ci.org/yegor256/xembly)
[![Build status](https://ci.appveyor.com/api/projects/status/e5sbjdlhbrpahr8b/branch/master?svg=true)](https://ci.appveyor.com/project/yegor256/xembly/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi.incubator/xembly/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi.incubator/xembly)
[![Coverage Status](https://coveralls.io/repos/yegor256/xembly/badge.svg?branch=master&service=github)](https://coveralls.io/github/yegor256/xembly?branch=master)
[![JavaDoc](https://img.shields.io/badge/javadoc-html-blue.svg)](http://www.javadoc.io/doc/com.jcabi.incubator/xembly)

**Xembly** is an [Assembly](http://en.wikipedia.org/wiki/Assembly_language)-like
[imperative](http://en.wikipedia.org/wiki/Imperative_programming) programming language
for data manipulation in XML documents. Read this blog post
for a more detailed explanation: [Xembly, an Assembly for XML](http://www.yegor256.com/2014/04/09/xembly-intro.html).

For example, you have an XML document:

```xml
<orders>
  <order id="553">
    <amount>$45.00</amount>
  </order>
</orders>
```

And you want to change the amount of the order #553
from `$45.00` to `$140.00`. Xembly script would look like:

```
XPATH "orders/order[@id=553]";
SET "$140.00";
```

It is much simpler and compact than
[XSLT](http://www.w3.org/TR/xslt) or [XQuery](http://www.w3.org/TR/xquery).

This Java package implements Xembly:

```java
Document document = DocumentBuilderFactory.newInstance()
  .newDocumentBuilder().newDocument();
Iterable<Directive> dirs = new Directives(
  "ADD 'orders'; ADD 'order'; ATTR 'id', '553'; SET '$140.00';"
);
new Xembler(dirs).apply(document);
```

Since version 0.9 you can directly transform directives to XML:

```java
String xml = new Xembler(
  new Directives()
    .add("root")
    .add("order")
    .attr("id", "553")
    .set("$140.00")
).xml();
```

## How To Contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/yegor256/xembly/issues/new).

