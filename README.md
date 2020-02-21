# Virtualized Collections

Virtualized collections decompose the representation 
of a collection from the space it is stored. The main goal of 
this project is to provide a general library for collection interfaces
built in Java to store the corresponding data independently.

## Quick Start

Let's say you want to work with a list type of collection in your program. 

```java
IStorage<Long, String> storage = new MemoryStorage<>(new SequentialLongGenerator(), null, IStorage.NO_MAX_SIZE);
List<String> list = new VArrayList(storage, Function.identity());

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
IStorage<Long, String> storage = new FileStorage();
```


You realize its slow, so you want to introduce some cache.
You need to change the storage as follows:

```java
IStorage<Long, String> superset;
IStorage<Long, String> subset;
IStorage<Long, String> storage;
```


Note, you have not changed anything in the list construction.
With virtualized collections you are able to configure a persistent 
storage used for a collection used by your program without to change 
the rest of the application.
You are able to virtualize the storage itself, and define a 
clustered-, replicated-, chained-, or cached storage for your collection. 


For further possibility please read gthe [Developers Manual](). 

## License




