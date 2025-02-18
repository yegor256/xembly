# XML Modifying Imperative Language (and Java Lib)

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/xembly)](http://www.rultor.com/p/yegor256/xembly)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/yegor256/xembly/actions/workflows/mvn.yml/badge.svg)](https://github.com/yegor256/xembly/actions/workflows/mvn.yml)
[![PDD status](http://www.0pdd.com/svg?name=yegor256/xembly)](http://www.0pdd.com/p?name=yegor256/xembly)
[![codecov](https://codecov.io/gh/yegor256/xembly/branch/master/graph/badge.svg)](https://codecov.io/gh/yegor256/xembly)
[![codebeat badge](https://codebeat.co/badges/c07bdf31-182b-4e4d-a25e-df405c1d877d)](https://codebeat.co/projects/github-com-yegor256-xembly)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/f244032e289f4fc2a8d9db8c84251490)](https://www.codacy.com/gh/yegor256/xembly/dashboard)
[![Javadoc](http://www.javadoc.io/badge/com.jcabi.incubator/xembly.svg)](http://www.javadoc.io/doc/com.jcabi.incubator/xembly)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi.incubator/xembly/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi.incubator/xembly)
![Hits-of-Code](https://raw.githubusercontent.com/yegor256/xembly/gh-pages/hoc-badge.svg)
![Lines-of-Code](https://raw.githubusercontent.com/yegor256/xembly/gh-pages/loc-badge.svg)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=yegor256_xembly&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=yegor256_xembly)

**Xembly** is an
[Assembly](http://en.wikipedia.org/wiki/Assembly_language)-like
[imperative](http://en.wikipedia.org/wiki/Imperative_programming)
programming language
for data manipulation in XML documents.
It is a much simplier alternative to
[DOM](https://en.wikipedia.org/wiki/Document_Object_Model),
[XSLT](http://www.w3.org/TR/xslt), and [XQuery](http://www.w3.org/TR/xquery).
Read this blog post
for a more detailed explanation: [Xembly, an Assembly for XML][blog].
You may also want to watch
[this webinar](https://www.youtube.com/watch?v=oNtTAF0UjjA).

You need this dependency:

```xml
<dependency>
  <groupId>com.jcabi.incubator</groupId>
  <artifactId>xembly</artifactId>
  <version>0.32.2</version>
</dependency>
```

Here is a command line implementation (as Ruby gem):
[xembly-gem](https://github.com/yegor256/xembly-gem)

For example, you have an XML document:

```xml
<orders>
  <order id="553">
    <amount>$45.00</amount>
  </order>
</orders>
```

Then, you want to change the amount of the order #553
from `$45.00` to `$140.00`. Xembly script would look like this:

```text
XPATH "orders/order[@id=553]";
XPATH "amount";
SET "$140.00";
```

As you see, it's much simpler and compact than
[DOM](https://en.wikipedia.org/wiki/Document_Object_Model),
[XSLT](http://www.w3.org/TR/xslt),
or [XQuery](http://www.w3.org/TR/xquery).

This Java package implements Xembly:

```java
Document document = DocumentBuilderFactory.newInstance()
  .newDocumentBuilder().newDocument();
new Xembler(
  new Directives(
    "ADD 'orders'; ADD 'order'; ATTR 'id', '553'; SET '$140.00';"
  )
).apply(document);
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

This code will produce the following XML document:

```xml
<root>
  <order id="553">$140</order>
</root>
```

## Directives

This is a full list of supported directives, in the current version:

* `ADD`: adds new node to all current nodes
* `ADDIF`: adds new node, if it's absent
* `ATTR`: sets new attribute to current nodes
* `SET`: sets text value of current node
* `XSET`: sets text value, calculating it with XPath
* `XATTR`: sets attribute value, calculating it with XPath
* `CDATA`: same as `SET`, but makes `CDATA`
* `UP`: moves cursor one node up
* `XPATH`: moves cursor to the nodes found by XPath
* `REMOVE`: removes all current nodes
* `STRICT`: throws an exception if cursor is missing nodes
* `PI`: adds processing instruction
* `PUSH`: saves cursor in stack
* `POP`: retrieves cursor from stack
* `NS`: sets namespace of all current nodes
* `COMMENT`: adds XML comment

The "cursor" or "current nodes" is where we're currently located
in the XML document. When Xembly script starts, the cursor is
empty: it simply points to the highest level in the XML hierarchy.
Pay attention, it doesn't point to the root node. It points to one
level above the root. Remember, when a document is empty, there is no root node.

Then, we start executing directives one by one. After each directive
the cursor is moving somewhere. There may be many nodes under the cursor,
or just one, or none. For example, let's assume we're starting
with this simple document `<car/>`:

```text
ADD 'hello';        // Nothing happens, since the cursor is empty
XPATH '/car';       // There is one node <car> under the cursor
ADD 'make';         // The result is "<car><make/></car>",
                    // the cursor has one node "<make/>"
ATTR 'name', 'BMW'; // The result is "<car><make name='BMW'/></car>",
                    // the cursor still points to one node "<make/>"
UP;                 // The cursor has one node "<car>"
ADD 'mileage';      // The result is "<car><make name='BMW'/><mileage/></car>",
                    // the cursor still has one node "<car>"
XPATH '*';          // The cursor has two nodes "<make name='BMW'/>"
                    // and "<mileage/>"
REMOVE;             // The result is "<car/>", since all nodes under
                    // the cursor are removed
```

You can create a collection of directives either from a text or
via supplementary methods, one per each directive. In both cases,
you need to use the `Directives` class:

```java
import org.xembly.Directives;
new Directives("XPATH '//car'; REMOVE;");
new Directives().xpath("//car").remove();
```

The second option is preferable, because it is faster — there is
no parsing involved.

### ADD

The `ADD` directive adds a new node to every node in the current node set.
`ADD` expects exactly one mandatory argument, which is the name of
a new node to be added (case sensitive):

```text
ADD 'orders';
ADD 'order';
```

Even if a node with the same name already exists, a new node
will be added. Use `ADDIF` if you need to add only if the same-name node
is absent.

After the execution, the `ADD` directive moves the cursor
to the nodes just added.

### ADDIF

The `ADDIF` directive adds a new node to every node of the current set,
only if it is absent. `ADDIF` expects exactly one argument, which
is the name of the node to be added (case sensitive):

```text
ADD 'orders';
ADDIF 'order';
```

After the execution, the `ADDIF` directive moves the cursor
to the nodes just added.

### ATTR

The `ATTR` directive sets an attribute to every node of the current set.
`ATTR` expects exactly two arguments, where the first is the name
of the attribute and the second is the value to set:

```text
ADD 'order';
ATTR 'price', '$49.99';
```

After the execution, `ATTR` doesn't move the cursor.

If it's necessary to make sure the attribute belongs to a certain
namespace, put the namespace and its prefix into the
attribute name separating them with spaces:

```text
ADD 'flower';
ATTR 'noNamespaceSchemaLocation xsi http://www.w3.org/2001/XMLSchema-instance', 'foo.xsd';
```

This will generate the following document:

```xml
<flower xsi:noNamespaceSchemaLocation="foo.xsd"/>
```

### SET

The `SET` directive changes text content of all current nodes, and expects
exactly one argument, which is the text content to set:

```text
ADD "employee";
SET "John Smith";
```

`SET` doesn't move the cursor anywhere.

### XSET

The `XSET` directive changes text content of all current nodes to a value
calculated with the provided XPath expression:

```text
ADD "product-1";
ADD "price";
XSET "sum(/products/price) div count(/products)";
```

`XSET` doesn't move the cursor anywhere.

### XATTR

The `XATTR` directive changes the value of an attribute of
all current nodes to a value
calculated with the provided XPath expression:

```text
ADD "product-1";
ADD "price";
XATTR "s", "sum(/products/price) div count(/products)";
```

`XATTR` doesn't move the cursor anywhere.

### UP

The `UP` directive moves all current nodes to their parents.

### XPATH

The `XPATH` directive re-points the cursor to the nodes found
by the provided XPath expression:

```text
XPATH "//employee[@id='234' and name='John Smith']/name";
SET "John R. Smith";
```

### REMOVE

The `REMOVE` directive removes current nodes under the cursor and
moves the cursor to their parents:

```text
ADD "employee";
REMOVE;
```

### STRICT

The `STRICT` directive checks that there is a certain number of current nodes:

```text
XPATH "//employee[name='John Doe']";  // Move the cursor to the employee
STRICT "1";                           // Throw an exception if there
                                      // is not exactly one node under
                                      // the cursor
```

This is a very effective mechanism of validation of your script,
in production mode. It is similar to `assert`  statement in Java.
It is recommended to use `STRICT` regularly, to make sure your
cursor has correct amount of nodes, to avoid unexpected modifications.

`STRICT` doesn't move the cursor anywhere.

### PI

The `PI` directive adds a new processing directive to the XML:

```text
PI "xsl-stylesheet" "href='http://example.com'";
```

`PI` doesn't move the cursor anywhere.

### PUSH and POP

The `PUSH` and `POP` directives save current DOM position to stack
and restore it from there.

Let's say, you start your Xembly manipulations from a place in DOM,
which location is not determined for you. After your manipulations are
done, you want to get back to exactly the same place. You should
use `PUSH` to save your current location and `POP` to restore it
back, when manipulations are finished, for example:

```assemlby
PUSH;                        // Doesn't matter where we are
                             // We just save the location to stack
XPATH '//user[@id="123"]';   // Move the cursor to a completely
                             // different location in the XML
ADD 'name';                  // Add "<name/>" to all nodes under the cursor
SET 'Jeff';                  // Set text value to the nodes
POP;                         // Get back to where we were before the PUSH
```

`PUSH` basically saves the cursor into stack and `POP` restores it from there.
This is a very similar technique to `PUSH`/`POP` directives in Assembly. The
stack has no limits, you can push multiple times and pop them back. It is
a stack, that's why it is First-In-Last-Out (FILO).

This operation is fast and it is highly recommended to use it everywhere,
to be sure you're not making unexpected changes to the XML document.

### NS

The `NS` directive adds a namespace attribute to a node:

```text
XPATH '/garage/car';                // Move the cursor to "<car/>" node(s)
NS "http://www.w3.org/TR/html4/";   // Set the namespace over there
```

If an original document was like this:

```xml
<garage>
  <car>BMW</car>
  <car>Toyota</car>
</garage>
```

After the applying of that two directives, it will look like this:

```xml
<garage xmlns:a="http://www.w3.org/TR/html4/">
  <a:car>BMW</a:car>
  <a:car>Toyota</a:car>
</garage>
```

The namspace prefix may not necessarily be `a:`.

`NS` doesn't move the cursor anywhere.

## XML Collections

Let's say you want to build an XML document with a collection of names:

```java
package org.xembly.example;
import org.xembly.Directives;
import org.xembly.Xembler;
public class XemblyExample {
  public static void main(String[] args) throws Exception {
    String[] names = new String[] {
      "Jeffrey Lebowski",
      "Walter Sobchak",
      "Theodore Donald 'Donny' Kerabatsos",
    };
    Directives directives = new Directives().add("actors");
    for (String name : names) {
      directives.add("actor").set(name).up();
    }
    System.out.println(new Xembler(directives).xml());
  }
}
```

The standard output will contain this text:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<actors>
  <actor>Jeffrey Lebowski</actor>
  <actor>Walter Sobchak</actor>
  <actor>Theodore Donald &apos;Donny&apos; Kerabatsos</actor>
</actors>
```

## Merging Documents

When you need to add an entire XML document, you can convert
it first into Xembly directives and then add them all together:

```java
Iterable<Iterable> dirs = new Directives()
  .add("garage")
  .append(Directives.copyOf(node))
  .add("something-else");
```

This static utility method `copyOf()` converts an instance of class
`org.w3c.dom.Node` into a collection of Xembly directives. Then,
the `append()` method adds them all together to the main list.

Unfortunately, not every valid XML document can be parsed by `copyOf()`. For
example, this one will lead to a runtime exception:
`<car>2015<name>BMW</name></car>`. Read more about Xembly limitations,
a few paragraphs below.

## Escaping Invalid XML Text

XML, as a standard, doesn't allow certain characters in its body. For example,
this code will throw an exception:

```java
String xml = new Xembler(
  new Directives().add("car").set("\u00")
).xml();
```

The character `\u00` is not allowed in XML. Actually, these ranges
are also not allowed: `\u00..\u08`, `\u0B..\u0C`, `\u0E..\u1F`,
`\u7F..\u84`, and `\u86..u9F`.

This means that you should validate everything and make sure you're
setting only the "valid" text values to your XML nodes. Sometimes,
it's not feasible
to always check them. Sometimes you may simply need to save whatever
is possible and call it a day. There a utility static method
`Xembler.escape()`, to help
you do that:

```java
String xml = new Xembler(
  new Directives().add("car").set(Xembler.escape("\u00"))
).xml();
```

This code won't throw an exception. The `Xembler.escape()` method will
convert "\u00" to "\\u0000". It is recommended to use this method
everywhere, if you are not sure about the quality of the content.

## Shaded Xembly JAR With Dependencies

Usually, you're supposed to use this dependency in your `pom.xml`:

```xml
<dependency>
  <groupId>com.jcabi.incubator</groupId>
  <artifactId>xembly</artifactId>
</dependency>
```

However, if you have conflicts between dependencies, you can
use our "shaded" JAR, that includes all dependencies:

```xml
<dependency>
  <groupId>com.jcabi.incubator</groupId>
  <artifactId>xembly</artifactId>
  <classifier>jar-with-dependencies</classifier>
</dependency>
```

## Known Limitations

Xembly is not intended to be a replacement of XSL or XQuery. It is
a lightweight (!) instrument for XML manipulations. There are a few things
that can't be done by means of Xembly:

* DTD section can't be modified

* Elements and text content can't be mixed, e.g.
this structure is not supported: `<test>hello <b>friend</a></test>`

Some of these limitations may be removed in the next versions. Please,
submit [an issue](https://github.com/yegor256/xembly/issues).

## How To Contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request, please run full Maven build:

```bash
mvn clean install -Pqulice
```

You must fix all static analysis issues, otherwise we won't be able
to merge your pull request. The build must be "clean".

## Delivery Pipeline

Git `master` branch is our cutting edge of development. It always contains
the latest version of the product, always in `-SNAPSHOT` suffixed version.
Nobody
is allowed to commit directly to `master` &mdash; this branch is basically
[read-only](http://www.yegor256.com/2014/07/21/read-only-master-branch.html).
Everybody contributes changes via
[pull requrests](http://www.yegor256.com/2014/04/15/github-guidelines.html).
We are
using [rultor](http://www.rultor.com), a hosted
[chatbot][blog-chatbots],
in order to merge pull requests into `master`.
Only our architect is allowed to send pull
requests to @rultor for merge, using `merge` command.
Before it happens, a mandatory code review must be performed for a pull request.

After each successful merge of a pull request, our project manager
gives `deploy` command to @rultor. The code from `master` branch is
tested, packaged, and deployed to [Sonatype](http://central.sonatype.org/),
in version `*-SNAPSHOT`.

Every once in a while, the architect may decide that it's time to release
a new [minor/major](http://www.semver.org) version of the product. When
it happens, he gives `release` command to @rultor. The code from `master`
branch is tested, versioned, packaged, and deployed to
[Sonatype](http://central.sonatype.org/) and
[Maven Central](http://search.maven.org/).
A new Git tag is created. A new GitHub release is created
and briefly documented.
All this is done automatically by @rultor.

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/yegor256/xembly/issues/new).
But keep these
[Five Principles of Bug Tracking][blog-bugs]
in mind.

[blog]: http://www.yegor256.com/2014/04/09/xembly-intro.html
[blog-bugs]: http://www.yegor256.com/2014/11/24/principles-of-bug-tracking.html
[blog-chatbots]: http://www.yegor256.com/2015/11/03/chatbot-better-than-ui-for-microservice.html
