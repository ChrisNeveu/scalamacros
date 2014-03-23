{{ }} delimits a scala code block.

```
{#if boolean}
    <div>test</div>
{#endif}
```

Template statements take the form {#statement <optionally parameters>}

```
{#opt optionName as name}
    <p>Hello, {{name}}.</p>
{#none}
    <p>I don't know your name.</p>
{#endopt}
```

```
{#for item in list}
    <li>This is an item</li>
{#endfor}
```

```
{#match variable}
    {#case v: Int}Your variable is an Int.{#endcase}
    {#case v: String}Your variable is a String.{#endcase}
{#endmatch}
```

```
{#let var = foo + bar / baz * qux}
    Why does {{var}} sound so much better than {{var}}
{#endlet}
```

```
{#include otherTemplate.schrine}
```

Text inside a raw block is not interpreted.

```
{#raw}
    This is what the syntax looks like:
    {#if foo}
        bar
    {#endif}
{#endraw}
```

Apply takes a function of the form Html => Html and applies it to the contents of the block.

```
{#apply tripleHtml}
    <p>This html will get output three times<p>
{#endapply}
```

Url interpolation is a special form, it only works if a url generator has been plugged in.
{@ImageController.getUrl(id)}

Comments look like this:
```
{-- This section is commented out --}
