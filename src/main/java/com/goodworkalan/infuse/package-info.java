/**
 *<h2>Dirt Simple Path Language</h2>
 *
 *<p>The path language is only to set and get bean properties. It is not
 *intended to grow or shrink lists, evaluate conditions, etc.</p>
 *
 *<p>Basic usage.</p>
 *
 *<code><pre>
 *    BeanPath path = new BeanPath("department.people['Alan Gutierrez'].aliases[1].firstName");
 *    path.set(department, "Rocko");
 *    String firstName = path.get(department);
 *    assert firstName.equals("Rocko");
 *</pre></code>
 */
package com.goodworkalan.infuse;
