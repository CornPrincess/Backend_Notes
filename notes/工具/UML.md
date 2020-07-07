# UML

UML 我们平时经常会用到，但有一些概念一直搞不清，在这里梳理一下。

## 基本概念

在这里使用微软文档中的图例进行说明

![uml](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/tools/uml.png)

| **Shape** | **Element**              | **Description**                                              |
| :-------- | :----------------------- | :----------------------------------------------------------- |
| 1         | **Class**                | A definition of objects that share given structural or behavioral characteristics. For more information, see [Properties of types on UML class diagrams](https://docs.microsoft.com/en-us/visualstudio/modeling/properties-of-types-on-uml-class-diagrams?view=vs-2015). |
| 1         | Classifier               | The general name for a class, interface, or enumeration. Components, use cases, and actors are also classifiers. |
| 2         | Collapse/ Expand control | If you cannot see the details of a classifier, click the expander at upper-left of the classifier. You might also have to click the [+] on each segment. |
| 3         | **Attribute**            | A typed value attached to each instance of a classifier.  To add an attribute, click the **Attributes** section and then press **ENTER**. Type the signature of the attribute. For more information, see [Properties of attributes on UML class diagrams](https://docs.microsoft.com/en-us/visualstudio/modeling/properties-of-attributes-on-uml-class-diagrams?view=vs-2015). |
| 4         | **Operation**            | A method or function that can be performed by instances of a classifier. To add an operation, click the **Operations** section and then press **ENTER**. Type the signature of the operation. For more information, see [Properties of operations on UML class diagrams](https://docs.microsoft.com/en-us/visualstudio/modeling/properties-of-operations-on-uml-class-diagrams?view=vs-2015). |
| 5         | **Association**          | A relationship between the members of two classifiers. For more information, see [Properties of associations on UML class diagrams](https://docs.microsoft.com/en-us/visualstudio/modeling/properties-of-associations-on-uml-class-diagrams?view=vs-2015). |
| 5a        | **Aggregation**          | An association representing a shared ownership relationship. The **Aggregation** property of the owner role is set to **Shared**. |
| 5b        | **Composition**          | An Association representing a whole-part relationship. The **Aggregation** property of the owner role is set to **Composite**. |
| 6         | **Association Name**     | The name of an association. The name can be left empty.      |
| 7         | **Role Name**            | The name of a role, that is, one end of an association. Can be used to refer to the associated object. In the previous illustration, for any Order `O`, `O.ChosenMenu` is its associated Menu.  Each role has its own properties, listed under the properties of the association. |
| 8         | **Multiplicity**         | Indicates how many of the objects at this end can be linked to each object at the other. In the example, each Order must be linked to exactly one Menu.  **\*** means that there is no upper limit to the number of links that can be made. |
| 9         | **Generalization**       | The *specific* classifier inherits part of its definition from the *general* classifier. The general classifier is at the arrow end of the connector. Attributes, associations, and operations are inherited by the specific classifier.  Use the **Inheritance** tool to create a generalization between two classifiers. |

其中重点记录一下Association，Aggregation和Composition

**Association**

> An *[association](https://en.wikipedia.org/wiki/Association_(object-oriented_programming))* represents a family of links. A binary association (with two ends) is normally represented as a line. An association can link any number of classes. An association with three links is called a ternary association. An association can be named, and the ends of an association can be adorned with role names, ownership indicators, multiplicity, visibility, and other properties.
> There are four different types of association: bi-directional, uni-directional, aggregation (includes composition aggregation) and reflexive. Bi-directional and uni-directional associations are the most common ones.
> For instance, a flight class is associated with a plane class bi-directionally. Association represents the static relationship shared among the objects of two classes.

Association简单理解就是代表两个类之间的关系。



**Aggregation**

> *[Aggregation](https://en.wikipedia.org/wiki/Aggregation_(object-oriented_programming))* is a variant of the "has a" association relationship; aggregation is more specific than association. It is an association that represents a part-whole or part-of relationship. As shown in the image, a Professor 'has a' class to teach. As a type of association, an aggregation can be named and have the same adornments that an association can. However, an aggregation may not involve more than two classes; it must be a binary association. Furthermore, there is hardly a difference between aggregations and associations during implementation, and the diagram may skip aggregation relations altogether.[[7\]](https://en.wikipedia.org/wiki/Class_diagram#cite_note-7)
>
> *Aggregation* can occur when a class is a collection or container of other classes, but the contained classes do not have a strong *lifecycle dependency* on the container. The contents of the container still exist when the container is destroyed.
>
> In [UML](https://en.wikipedia.org/wiki/Unified_Modeling_Language), it is graphically represented as a *hollow* [diamond shape](https://en.wikipedia.org/wiki/Rhombus) on the containing class with a single line that connects it to the contained class. The aggregate is semantically an extended object that is treated as a unit in many operations, although physically it is made of several lesser objects.
>
> Example: Library and Students. Here the student can exist without library, the relation between student and library is aggregation.

Aggregation.是 “has a”的关系，比association更加具体，它代表了part-whole或者part-of的关系，比如一个教授“has a”一个班级来上课。

当一个类是其他类的容器时，aggregation的关系就产生了，但是被包含的类与包含的类之间没有强的生命周期依赖，容器中的东西依然存在即使容器以及不在了。



**Composition**

composition关系中的两个类有强生命周期依赖，也即意味着一个类被删了，另一个类也随之被删了



## Differences between Composition and Aggregation[[edit](https://en.wikipedia.org/w/index.php?title=Class_diagram&action=edit&section=10)]

- Composition relationship

  1. When attempting to represent real-world whole-part relationships, e.g. an engine is a part of a car.

  2. When the container is destroyed, the contents are also destroyed, e.g. a university and its departments.

- Aggregation relationship

  1. When representing a software or database relationship, e.g. car model engine ENG01 is part of a car model CM01, as the engine, ENG01, may be also part of a different car model.[[8\]](https://en.wikipedia.org/wiki/Class_diagram#cite_note-8)

  2. When the container is destroyed, the contents are usually not destroyed, e.g. a professor has students; when the professor dies the students do not die along with them.

 

## Reference

1. [Types of UML Diagrams](https://www.lucidchart.com/blog/types-of-UML-diagrams)
2. [UML Class Diagrams: Reference](https://docs.microsoft.com/en-us/visualstudio/modeling/uml-class-diagrams-reference?view=vs-2015&redirectedfrom=MSDN)
3. [UML Association vs Aggregation vs Composition](https://www.visual-paradigm.com/guide/uml-unified-modeling-language/uml-aggregation-vs-composition/)
4. [Class diagram](https://en.wikipedia.org/wiki/Class_diagram#Association)

