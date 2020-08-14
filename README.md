# Virtualized Collections

Virtualized collections decompose the representation 
of a collection from the allocated storage used to the values. The main goal of 
the project is to provide a general library for collection interfaces
built in Java to store data independent from technological stack.

## Quick Start

### Install and Run

The packages for vcollections are stored in [jcenter](https://bintray.com/wobserver/vcollections/)
To include it in your project:

```
<dependency>
  <groupId>com.wobserver.vcollections</groupId>
  <artifactId>vcollections-core</artifactId>
  <version>0.2.3</version>
  <type>pom</type>
</dependency>
```

Or in Gradle

```
implementation 'com.wobserver.vcollections:vcollections-core:0.2.3'
```

After that you can create your own collections. In Java:

```java
IStorage<Long, String> storage = new MemoryStorage<>(new SequentialLongGenerator(), null, IStorage.NO_MAX_SIZE);
List<String> list = new VArrayList(storage, Long.class);

```
Here you defined the storage with a sequential key 
generator (gives the next index every time we add a new item) 
and the collection separately, and the defined storage 
(memory in this case) 
passed as a parameters to VArrayList, the virtualized 
version of the ArrayList.

### Using Custom Storage

Let's assume you decided to save contents in a file, instead of keeping it in the memory. 
Then you need to change the storage as follows:

```java
IStorage<Long, String> storage = new FileStorage(
  PrimitiveTypesMapperFactory.make(Long.class, String.class), // mapper for keys
  String.class, // type of values
  new ObjectMapper(), // mapper for values
  "temp/",  // path of directory key, values are stored
  new SequentialLongGenerator(),  // a generator of keys for create operation
  IStorage.NO_MAX_SIZE // indicate the capacity of this storage in a number of elemenets
);
```
Here you defined a file storage for any VCollection. 
For the file storage you need to define a mapper 
between a the filename and your key type (Long in this case), 
define the type of value (String in this case), 
the ObjectMapper capable of mapping your type of value, 
the path of the directory you want to save the file, 
a generator of unique keys for create operations, 
and the capacity of your storage.


A Storage can be virtualized too! Let's say you realize your storage is slow, 
so you want to introduce some cache. You need to change the storage as follows:

```java
IStorage<Long, String> superset = new FileStorage(...)
IStorage<Long, String> subset = new MemoryStorage<>(new SequentialLongGenerator(), null, IStorage.NO_MAX_SIZE);;
IStorage<Long, String> storage = new CachedStorage(Long.class, subset, superset);
```


### Configure externally

You can make a yaml, or json file with this parameter and provide a builder 
for this storage. 

```yaml
storageProfiles:
  myCachedStorageProfile:
    builder: CachedStorageBuilder
    configuration:
      superset:
        builder: FileStorageBuilder
        configuration:
          valueType: java.lang.String
          keyType: java.lang.Long    
          path: "temp/"
      subset:
        builder: MemoryStorageBuilder
        configuration:
          capacity: 10
```

To use your configuration type:

```java
StorageProvider storageProvider = new StorageProvider().withYamlFile("myYamlFile");
IStorage<Long, String> customers = storageProvider.getStorageFor("myFileStorargeProfileName");
``` 


For further possibility please read gthe [Developers Manual](docs/Manual.md). 

## License

Before you include this library into your production, 
please read the [license](LICENSE.md).



