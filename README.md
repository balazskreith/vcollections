# Virtualized Collections

Virtualized collections decompose the representation 
of a collection from the space it is stored. The main goal of 
this project is to provide a general library for collection interfaces
built in Java to store the corresponding data independently.

## Quick Start

Let's say you want to work with a list type of collection in your program. 

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


Now, let's assume you decided to save the content in file, instead of keeping it in the memory. 
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

You can make a yaml, or json file with this parameter and provide a builder 
for this storage. 
```yaml
storages:
  - key: myFileStorargeProfileName
    builder: FileStorageBuilder
      configuration:
        valueType: java.lang.String
        keyType: java.lang.Long    
        path: "temp/"
```

And to provide it:
```java
StorageProvider storageProvider = new StorageProvider();
storageProvider.addYamlFile(yourYamlFile);
IStorage<Long, String> customers = storageProvider.get("myFileStorargeProfileName");
``` 

The full capability of storage building is detailed [here]().

Storages can be virtualized too! Let's say you realize your storage is slow, 
so you want to introduce some cache. You need to change the storage as follows:

```java
IStorage<Long, String> superset = storageProvider.get("myFileStorargeProfileName");;
IStorage<Long, String> subset = new MemoryStorage<>(new SequentialLongGenerator(), null, IStorage.NO_MAX_SIZE);;
IStorage<Long, String> storage = new CachedStorage(String.class, subset, superset);
```


Note, you have not changed anything in the list construction.


With virtualized collections you are able to configure a persistent 
storage used for a collection used by your program without to change 
the rest of the application.


For further possibility please read gthe [Developers Manual](). 

## License




