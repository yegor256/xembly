# Assembly for XML

Xembly is an imperative language for data manipulations in
an XML document. It is a much simplier alternative to
XSLT and XQuery. For example:

```
ADD 'orders';
ADD 'order';
ATTR 'id', '55';
ADD 'amount';
SET '$29.99';
UP;
ADD "summary";
SET "free \"subscription\"";
```

Will transform `<root>` into:

```xml
<root>
  <orders>
    <order id="55">
      <amount>$29.99</amount>
      <summary>free "subscription"</summary>
    </order>
  </orders>
</root>
```

Xembly program (similar to Assembly program) consists of "directives". Each
directive may have a few or zero arguments. Every directive is executed at
current node. Program starts at the root node of the document.

Arguments may use single or double quotes.

## Directives

Full list of supported directives in the current version:

### ADD

`ADD` directive adds a node to the current node,
and expects exactly one argument:

```
ADD 'orders';
ADD 'order';
```

After exeucution `ADD` directive changes current node to the one just
added.

### ADDIF

`ADDIF` directive adds a node to the current node only if it's absent,
and expects exactly one argument:

```
ADD 'orders';
ADDIF 'order';
```

After exeucution `ADDIF` directive changes current node to the one just
added.

### SET

`SET` changes text content of the current node, and expects
exactly one argument:

```
ADD "employee";
SET "John Smith";
```

### UP

`UP` changes the current node to one level up (to it parent node).

### XPATH

`XPATH` changes the current node to the first one found by XPath expression:

```
XPATH "//employee[@id='234' and name='John Smith']/name";
SET "John R. Smith";
```

