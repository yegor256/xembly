# Building Collection in XML

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

Standard output will contains this text:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<actors>
  <actor>Jeffrey Lebowski</actor>
  <actor>Walter Sobchak</actor>
  <actor>Theodore Donald &apos;Donny&apos; Kerabatsos</actor>
</actors>
```
