# CompAnalyzer
Extract components informaion (mainly dependency) for modularization analysis for Java software.

Modularity is essential for modern large software but is often overlooked. In order to see how a software system drifts from it's 
initial modular design, I designed this useful tool, CompAnalyzer. 

With CompAnalyzer, you can decompose your software into any granularity components (the samllest unit of a component is a package), 
and measure them according the modularity constraints defined on them. Thus you can get a overall shape on your software on modularity,
 and even you can design a formula to compute the "technical debts" with the components dependency data.

The most central concept behind CompAnalyzer is component, a mudular unit. A component can be hierarchical, i.e., contain subcomponents,
which could also contail sub-subcomponents. A component is a "black-box", i.e., its internal is invisible to the outside except 
the declared interfaces. Interfaces declared on a component can be grouped into different views, and you can specify a component's 
black list and white list in term of views, thus allowing a flexible access control. Each component at least one view (default) if 
you declare interfaces on them and each interface may be included into more than one views. Note, interfaces of subcomponents can 
not be seen outside the parent component unless you explicitly declare them on the parent component level (i.e., interface promotion).
I also provide a special component, called layer (also a component, could be nested), for layered architecture, which inherently 
has additional constrains, i.e., access cross layers.

When these concepts mapped to Java, I define a component a collection of Java packages (at least one), an interface is a Java class.
I use jDepend (slightly modified and included in CompAnalyzer as source) to gather dependency infomation among classes and packages, 
and propagate them to component level. Therefore you can get all dependency information on components including all problems found 
(i.e., violating access constraints). However, CompAnalyzer does not support the analysis for circles among classes, packages, and 
components in the current viersion. This may make you disappointed, but I did really tried and coundn't find an efficiency algorithm 
to compute them without memery overflow or too long time. So, if you have one, please inform me at zjg_robin@hotmail.com. 
Thank you very much!

You can design a component architecture for your software with Java API provided by CompAnalyzer or alternatively with a component 
defintion file (.xml) feeded to CompAnalyzer. In test/ directory, I provide such a file (maybe the simplest), and the result is
output in another file for your reference. Since CompAnalyzer leverage jDepend, you can configure a system (to be analyzed) with
jDepend APIs.

Hope you like it! And I appreciate any feedback from you, where bugs and other addvices. 
