<img src="http://www.xembly.org/logo.png" width="64px" height="64px" />

[![EO principles respected here](http://www.elegantobjects.org/badge.svg)](http://www.elegantobjects.org)
[![Managed by Zerocracy](https://www.0crat.com/badge/C3RUBL5H9.svg)](https://www.0crat.com/p/C3RUBL5H9)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/xembly)](http://www.rultor.com/p/yegor256/xembly)
[![We recommend IntelliJ IDEA](http://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Build Status](https://travis-ci.org/yegor256/xembly.svg?branch=master)](https://travis-ci.org/yegor256/xembly)
[![Build status](https://ci.appveyor.com/api/projects/status/e5sbjdlhbrpahr8b/branch/master?svg=true)](https://ci.appveyor.com/project/yegor256/xembly/branch/master)
[![PDD status](http://www.0pdd.com/svg?name=yegor256/xembly)](http://www.0pdd.com/p?name=yegor256/xembly)
[![Coverage Status](https://coveralls.io/repos/yegor256/xembly/badge.svg?branch=master&service=github)](https://coveralls.io/github/yegor256/xembly?branch=master)
[![codebeat badge](https://codebeat.co/badges/c07bdf31-182b-4e4d-a25e-df405c1d877d)](https://codebeat.co/projects/github-com-yegor256-xembly)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2609eb5f77c24bf7a858f8633b4fbf7d)](https://www.codacy.com/app/github_90/xembly?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yegor256/xembly&amp;utm_campaign=Badge_Grade)

[![JavaDoc](https://img.shields.io/badge/javadoc-html-blue.svg)](http://www.javadoc.io/doc/com.jcabi.incubator/xembly)
[![jpeek report](http://i.jpeek.org/com.jcabi.incubator/xembly/badge.svg)](http://i.jpeek.org/com.jcabi.incubator/xembly/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi.incubator/xembly/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi.incubator/xembly)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/xembly)](https://hitsofcode.com/view/github/yegor256/xembly)

**Xembly** is an [Assembly](http://en.wikipedia.org/wiki/Assembly_language)-like
[imperative](http://en.wikipedia.org/wiki/Imperative_programming) programming language
for data manipulation in XML documents.
It is a much simplier alternative to
[XSLT](http://www.w3.org/TR/xslt) and [XQuery](http://www.w3.org/TR/xquery).
Read this blog post
for a more detailed explanation: [Xembly, an Assembly for XML](http://www.yegor256.com/2014/04/09/xembly-intro.html).

You need this dependency:

```xml
<dependency>
  <groupId>com.jcabi.incubator</groupId>
  <artifactId>xembly</artifactId>
  <version>0.22</version>
</dependency>
```

Here is a command line implementation (as Ruby gem): [xembly-gem](https://github.com/yegor256/xembly-gem)

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

This code will produce this XML document:

```xml
<root>
  <order id="553">$140</order>
</root>
```

## Directives

Full list of supported directives in the current version:

  * `ADD`: adds new node to all current nodes
  * `ADDIF`: adds new node, if it's absent
  * `SET`: sets text value of current node
  * `XSET`: sets text value, calculating it with XPath
  * `CDATA`: same as `SET`, but makes `CDATA`
  * `UP`: moves cursor one node up
  * `XPATH`: moves cursor to the nodes found by XPath
  * `REMOVE`: removes all current nodes
  * `STRICT`: throws an exception if cursor is missing nodes
  * `PI`: adds processing instruction
  * `PUSH`: saves cursor in stack
  * `POP`: retrieves cursor from stack
  * `NS`: sets namespace of all current nodes

"Cursor" or "current nodes" is where we're currently located
in the XML document. When Xembly script starts, the cursor is
empty and simply points to the highest level in the XML hierarchy.
Pay attention, it doesn't point to the root node. It points to one
level above the root. Remember, when document is empty, there is no root.

Then, we start executing directives one by one. After each directive
cursor is moving somewhere. There may be many nodes under the cursor,
or just one, or none. For example, let's assume we're starting
with this simple document `<car/>`:

```assembly
ADD 'hello';        // nothing happens, since cursor is empty
XPATH '/car';       // there is one node <car> under the cursor
ADD 'make';         // the result is "<car><make/></car>",
                    // cursor has one node "<make/>"
ATTR 'name', 'BMW'; // the result is "<car><make name='BMW'/></car>"
                    // cursor still has one node "<make/>"
UP;                 // cursor has one node "<car>"
ADD 'mileage';      // the result is "<car><make name='BMW'/><mileage/></car>"
                    // cursor still has one node "<car>"
XPATH '*';          // cursor has two nodes "<make name='BMW'/>"
                    // and "<mileage/>"
REMOVE;             // the result is "<car/>", since all nodes under
                    // the cursor are removed
```

You can create a collection of directives either from text or
via supplementary methods, one per each directive. In both cases,
you need to use class `Directives`:

```java
import org.xembly.Directives;
new Directives("XPATH '//car'; REMOVE;");
new Directives().xpath("//car").remove();
```

The second option is preferable, because it is faster - there is
no parsing involved.

### ADD

`ADD` directive adds a new node to every node in the current node set.
`ADD` expects exactly one mandatory argument, which is the name of
a new node to be added (case sensitive):

```assembly
ADD 'orders';
ADD 'order';
```

Even if the node with the same name already exists, a new node
will be added. Use `ADDIF` if you need to add only if the same-name node
is absent.

After execution, `ADD` directive moves the cursor to the nodes just added.

### ADDIF

`ADDIF` directive adds a new node to every node of the current set,
only if it's absent. `ADDIF` expects exactly one argument, which
is the name of the node to be added (case sensitive):

```assembly
ADD 'orders';
ADDIF 'order';
```

After execution, `ADDIF` directive moves the cursor to the nodes just added.

### SET

`SET` changes text content of all current nodes, and expects
exactly one argument, which is the text content to set:

```assembly
ADD "employee";
SET "John Smith";
```

`SET` doesn't move the cursor anywhere.

### XSET

`XSET` changes text content of all current nodes to a value
calculated with XPath expression:

```assembly
ADD "product-1";
ADD "price";
XSET "sum(/products/price) div count(/products)";
```

`XSET` doesn't move the cursor anywhere.

### UP

`UP` moves all current nodes to their parents.

### XPATH

`XPATH` changes current nodes to the all found by XPath expression:

```assembly
XPATH "//employee[@id='234' and name='John Smith']/name";
SET "John R. Smith";
```

### REMOVE

`REMOVE` removes current nodes under the cursor and
moves the cursor to their parents:

```assembly
ADD "employee";
REMOVE;
```

### STRICT

`STRICT` checks that there is certain number of current nodes:

```assembly
XPATH "//employee[name='John Doe']";  // move cursor to the employee
STRICT "1";                           // throw an exception if there
                                      // is not exactly one node under
                                      // the cursor
```

This is a very effective mechanism of validation of your script,
in production mode. It is similar to `assert`  statement in Java.
It is recommended to use `STRICT` regularly, to make sure your
cursor has correct amount of nodes, to avoid unexpected modifications.

`STRICT` doesn't move the cursor anywhere.

### PI

`PI` directive add a new processing directive to the XML:

```assembly
PI "xsl-stylesheet" "href='http://example.com'";
```

`PI` doesn't move the cursor anywhere.

### PUSH and POP

`PUSH` and `POP` directives saves current DOM position to stack
and restores it from there.

Let's say you start your Xembly manipulations from a place in DOM,
which location is not determined for you. After your manipulations are
done, you want to get back to exactly the same place. You should
use `PUSH` to save your current location and `POP` to restore it
back, when manipulations are finished, for example:

```assemlby
PUSH;                        // doesn't matter where we are
                             // we just save the location to stack
XPATH '//user[@id="123"]';   // move the cursor to a completely
                             // different location in the XML
ADD 'name';                  // add "<name/>" to all nodes under the cursor
SET 'Jeff';                  // set text value to the nodes
POP;                         // get back to where we were before the PUSH
```

`PUSH` basically saves the cursor into stack and `POP` restores it from there.
This is a very similar technique to `PUSH`/`POP` directives in Assembly. The
stack has no limits, you can push multiple times and pop them back. It is
a stack, that's why it is First-In-Last-Out (FILO).

This operation is fast and it is highly recommended to use it everywhere,
to be sure you're not making unexpected changes to the XML document. Every time
you're not sure where your

### NS

`NS` adds a namespace attribute to a node:

```assembly
XPATH '/garage/car';                // move cursor to "<car/>" node(s)
NS "http://www.w3.org/TR/html4/";   // set namespace there
```

If original document was like this:

```xml
<garage>
  <car>BMW</car>
  <car>Toyota</car>
</garage>
```

After applying that two directives it will look like this:

```xml
<garage xmlns:a="http://www.w3.org/TR/html4/">
  <a:car>BMW</a:car>
  <a:car>Toyota</a:car>
</garage>
```

The namspace prefix may no necessarily be `a:`, but it doesn't
really matter.

`NS` doesn't move the cursor anywhere.

## XML Collections

Let's say you want to build an XML document with a collection
of names:

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

Standard output will contain this text:

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
method `append()` adds them all together to the main list.

Unfortunately, not every valid XML document can be parsed by `copyOf()`. For
example, this one will lead to a runtime exception:
`<car>2015<name>BMW</name></car>`. Read more about Xembly limitations,
a few paragraphs below.

## Escaping Invalid XML Text

XML, as standard, doesn't allow certain characters in its body. For example,
this code will throw an exception:

```java
String xml = new Xembler(
  new Directives().add("car").set("\u00")
).xml();
```

Character `\u00` is not allowed in XML. Actually, these ranges
are not allowed: `\u00..\u08`, `\u0B..\u0C`, `\u0E..\u1F`,
`\u7F..\u84`, and `\u86..u9F`.

This means that you should validate everything and make sure you're
setting only "valid" text values to XML nodes. Sometimes, it's not feasible
to always check them. Sometimes you may simply need to save whatever
is possible and call it a day. There a utility static method `Xembler.escape()`, to help
you do that:

```java
String xml = new Xembler(
  new Directives().add("car").set(Xembler.escape("\u00"))
).xml();
```

This code won't throw an exception. Method `Xembler.escape()` will
conver "\u00" to "\\u0000". It is recommended to use this method
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

  * You can't add, remove, or modify XML comments
    (but you can find them with XPath)

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

```
$ mvn clean install -Pqulice
```

You must fix all static analysis issues, otherwise we won't be able
to merge your pull request. The build must be "clean".

## Delivery Pipeline

Git `master` branch is our cutting edge of development. It always contains
the latest version of the product, always in `-SNAPSHOT` suffixed version. Nobody
is allowed to commit directly to `master` &mdash; this branch is basically
[read-only](http://www.yegor256.com/2014/07/21/read-only-master-branch.html).
Everybody contributes changes via
[pull requrests](http://www.yegor256.com/2014/04/15/github-guidelines.html). We are
using [rultor](http://www.rultor.com), a hosted
[chatbot](http://www.yegor256.com/2015/11/03/chatbot-better-than-ui-for-microservice.html),
in order to merge pull requests into `master`. Only our architect is allowed to send pull
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
[Sonatype](http://central.sonatype.org/) and [Maven Central](http://search.maven.org/).
A new Git tag is created. A new GitHub release is created and briefly documented.
All this is done automatically by @rultor.

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/yegor256/xembly/issues/new).
But keep these
[Five Principles of Bug Tracking](http://www.yegor256.com/2014/11/24/principles-of-bug-tracking.html)
in mind.
