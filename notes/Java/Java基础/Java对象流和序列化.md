# Java 对象流与序列化

Java 支持一种称为对象徐硫化（object serialization）的非常通用的机制，可以将任何对写出到流，并在之后将其读回，我们可以使用 `ObjectOutputStream` 和 `ObjectInputStream` 来进行序列化和反序列化操作。

所有支持序列化的类都必须实现 `Serializable` 接口，这也是一个标记接口。

> Classes that require special handling during the serialization and deserialization process must implement special methods with these exact signatures:

```java
 private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
 private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException;
 private void readObjectNoData()
     throws ObjectStreamException;
```

> The writeObject method is responsible for writing the state of the object for its particular class so that the corresponding readObject method can restore it. The default mechanism for saving the Object's fields can be invoked by calling out.defaultWriteObject. The method does not need to concern itself with the state belonging to its superclasses or subclasses. State is saved by writing the individual fields to the ObjectOutputStream using the writeObject method or by using the methods for primitive data types supported by DataOutput.

> The readObject method is responsible for reading from the stream and restoring the classes fields. It may call in.defaultReadObject to invoke the default mechanism for restoring the object's non-static and non-transient fields. The defaultReadObject method uses information in the stream to assign the fields of the object saved in the stream with the correspondingly named fields in the current object. This handles the case when the class has evolved to add new fields. The method does not need to concern itself with the state belonging to its superclasses or subclasses. State is restored by reading data from the ObjectInputStream for the individual fields and making assignments to the appropriate fields of the object. Reading primitive data types is supported by DataInput.

> The readObjectNoData method is responsible for initializing the state of the object for its particular class in the event that the serialization stream does not list the given class as a superclass of the object being deserialized. This may occur in cases where the receiving party uses a different version of the deserialized instance's class than the sending party, and the receiver's version extends classes that are not extended by the sender's version. This may also occur if the serialization stream has been tampered; hence, readObjectNoData is useful for initializing deserialized objects properly despite a "hostile" or incomplete source stream.

当对一个对象序列化时，这个对象内部可能存在复杂的对象网络，我们不能保存和恢复内部变量的内存地址，因为当对象被重新加载时，它可能占据的是与原来完全不同的内存地址。**因此，每个对象都是用一个序列号（serial number）保存的，这就是这种机制被称为对象序列化的原因。**

我们可以使用序列化将对象集合保存在磁盘文件中，并按照他们被存储的样子获取他们。**序列化的另一个非常重要的应用是通过网络将对象集合传送到另一台计算机上，远程调用就是用到这一原理。**

```java

```

