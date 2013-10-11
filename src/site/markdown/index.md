# Assembly for XML

Xembly is an imperative language for data manipulations in
an XML document. It is a much simplier alternative to
XSLT and XQuery. For example:

```
ADDIF 'orders';
ADD 'order';
ATTR 'id', '55';
ADD 'amount';
SET '$29.99';
UP;
ADD "summary";
SET "free &quot;subscription&quot;";
```

Will transform `<root/>` into:

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
current node(s). Program starts at the root node of the document.

Arguments may be wrapped into single or double quotes.

## Directives

Full list of supported directives in the current version:

### ADD

`ADD` directive adds a new node to every node in the current node set.
`ADD` expects exactly one mandatory argument, which is the name of
a new node to be added (case insensitive):

```
ADD 'orders';
ADD 'order';
```

Even if the node with the same name already exists, a new node
will be added. Use `ADDIF` if you need to add only if the same-name node
is absent.

After execution, `ADD` directive changes current nodes to the nodes just
added.

### ADDIF

`ADDIF` directive adds a new node to every node of the current set,
only if it's absent. `ADDIF` expects exactly one argument, which
is the name of the node to be added (case insensitive):

```
ADD 'orders';
ADDIF 'order';
```

After execution, `ADDIF` directive changes current nodes to the ones just
added.

### SET

`SET` changes text content of all current nodes, and expects
exactly one argument, which is the text content to set:

```
ADD "employee";
SET "John Smith";
```

### XSET

`XSET` changes text content of all current nodes to a value
calculated with XPath expression:

```
ADD "product-1";
ADD "price";
XSET "sum(/products/price) div count(/products)";
```

### UP

`UP` moves all current nodes to their parents.

### XPATH

`XPATH` changes current nodes to the all found by XPath expression:

```
XPATH "//employee[@id='234' and name='John Smith']/name";
SET "John R. Smith";
```

### REMOVE

`REMOVE` removes current nodes and jumps to their parents:

```
ADD "employee";
REMOVE;
```

### STRICT

`STRICT` checks that there is certain number of current nodes:

```
XPATH "/root/employee[name='John Doe']";
STRICT "1";
```

### PI

`PI` directives add a new processing directive to the XML:

```
PI "xsl-stylesheet" "href='http://example.com'";
```


