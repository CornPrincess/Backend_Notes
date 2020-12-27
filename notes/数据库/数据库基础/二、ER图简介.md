# ER图简介

## Entity-Relationship(E-R) Model

The most important elements of the E-R model are entities, attributes, identifiers and relationships

## Entity-Relationship(E-R) Diagram

an E-R model is normally expressed as an entity-releationship diagram(E-R diagram or ERD)

### Entity

An entity is something of interest ot users and users want to track(e.g., a place, person, or an event)

- **Entity Type**(or. **Entity Class**):A collection of entities that share common preperties
- **Entity Instance**: The single occurrence of an entity type

**Note**: **The entities in an ERD are the entity types**. an entity type has many instances of an entity.

### Attribute

**Attributes are properties or characteristics of an entity type(or a relationship type)**

### Identifier

- An identifier is an attribute or combination of attributes whose value **distinguishes** individual instances of each entity type(also known as key attribute/attributes)
- A composite(or combined)identifier is an identifier that consists of two or more attributes
- some entity types may have more than one (candidate) identifier

### Relationship

Entities can be associated with one another in relationships.**Entities correspond to nouns. Relationships correspond to verbs or propositional phases.**

![relationship](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/database/relationship.png)

- **Relationship Type**: Associations between (or among) entity types
- **Relationship Instance**: Associations between (or among) entity instances

![relationship instance](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/database/relationship2.png)

#### Degree

The degree of a relationship is the number of entity types that participate(involved)in the relationship

- Unary. Relationship
- Binary Relationship
- ternary Relationship

#### Cardinality

Cardinalities constrain the number of entity instances that participate. in a relationship instance.

- Maximum cardinality is the maximum number of **entity instances** that can participate in a **relationship instance.**
- Minimum cardinality is the minimum number of **entity instances** that can participate in a **relationship instance.**

![cardinality](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/database/cardinality.png)

**A course may have no or many offerings**

**A offering is given for exactly one course.**

**Maximum cardianlity classifies relationships into:**

- **1-1**: if maxiumum cardinality = 1 in both sides
- **1-M**: if maximum cardinality  > 1 in one side
- **M-N**: if maximum cardinality > 1 both sides

**Minimum cardinality** classifies **mandatory** or **optional** relationship:

- 0: optional
- 1: mandatory

#### Example

![example](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/database/exampleER.png)

- A department has **one** or **more** employee
- An employee belongs to **zero** or **one** department 
- The relationship has is **optional** to EMPLOYEE
- 1-M relationship: one side is parent, many sides is child

#### Strong Entity and Weak Entity

##### Identifing Relationship

The relationship between a **weak entity type** and the **strong entity type** it depends on it called an **identifying relationship**

![Strong and weak entity](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/database/strong_entity.png)

##### Associated Entity

- Assocaited Entity: An entity that associated two or more entities and may contains attributes that are **particular to the relationship between those entity types**
- It is a **weak entity** that depends on those entities that. It connects.

![Associated Entity](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/database/associated_entity1.png)

![associated entity2 M-M relationship](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/database/associated_entity2.png)

