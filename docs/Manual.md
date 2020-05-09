# Developers Manual
 
This manual is written to help you to understand Virtualized Collections 
in general, and develop your applications with it.
A Collection in an application represents a group of elements designated to hold data 
for operations. Collections are part of the standard library of today's 
programming languages, however, they are limited by the memory.
Virtualized collections decompose the representation 
of a collection from the space it is stored. The main goal of 
the project is to provide a general library to create collections 
using the standard library interfaces, meanwhile the implementation 
of the interfaces makes it possible to store it independently. 
 
In **Architecture** section the high-level overview of the concept of 
virtualized collection is explained. The concept of storage in vcollections 
is detailed in **Storage** section, where guideline is given to develop your own. 
In **VCollections** the available collections can be used with storage are detailed. 
In **Builders** section the external configuration for the used storage is detailed.
Finally **Modules** describes already developed storage modules and their builders 
for vcollections, and here you find guideline to develop your own.


## Architecture
![alt text][logo]

[logo]: vcollections_in_java.png "The architecture of vcollections"

Figure 1. The high-level architecture of vcollections

An additional abstract layer is introduced in the implementation of a collection 
between the executable operations and the space representation. This layer 
enables to store the collection independently of its representation (Figure 1.).

Technically, all collection interfaces implemented in vcollections using an interface 
for storage to store the elements of the implemented collection. The 
implemetation of a storage (usually) wrapping a client library of the 
technology we want to use to store our data. In this sense vcollection 
unifies the CRUD operations for different technologies and provides 
standard collections to accessing them.   

## Storage

Understanding the concept of Storage is crucial in order to 
use the capability of the virtualized collections. 
A Storage executes CRUD (Create, Read, Update, Delete) 
operations by using a client library of a technology 
we want to use. CRUD operations via a Storage interface are used by vcollections 
to accessing the stored data. Additionally a Storage 
interface provides methods to query the state of the storage 
(numbers of elements inside). 

Every Storage has a capacity. If the number of element reached the capacity of the Storage, 
an exception is thrown. 

### Memory Storage

This type of Storage dedicates to store the element in the memory. The corresponding builder for the MemoryStorage is the MemoryStorageBuilder. 
Examples for yaml configuration of this type of storage can be found [here](../core/src/test/resources/memorybuilder_test_source.yaml).  

### File Storage

This type of Storage dedicates to store the element in files. 
The corresponding builder for the FileStorage is the FileStorageBuilder. 
Examples for yaml configuration of this type of storage can be found [here](../core/src/test/resources/filebuilder_test_source.yaml).

### Key generators

KeyGenerators are called if Create operation in the Storage has been called. KeyGenerators are responsible to generate a unique key 
for value items determined to be pasted in the dedicated storage. 
For primitive type of keys Keygenerators are implemented and automatically called if no specific keygenerator is given in the builder.
For custom type of keys you need to implement the IKeyGenerator interface and provide the path of the class in the corresponding builder.

### Virtualized Storage

Storages can be virtualized. In that sense an implementation of the IStorage interface serve as a bridge between several other
IStorage realize a certain type of storage. In the core library the following basic virtual storages are implemented and tested:
Replicates-, Clustered-, Chained Storage. These storage are detailed in the following. 

#### Replicated Storage

Replicated storages are a type of virtualized storage. 
The aim of the Replicated Storage is to contain the same key value entries. In that so 
for CRUD operations it behave as follows:
Create: The Replicated Storage Keygenerator is used to generate a key for a new entry, and 
all storage held by the Replicated Storage got an update with the new key, and given value entry.
Read: The first storage has the value returns with the value
Update: All underlying storage got the key-value pair.
 

#### Clustered Storage

#### Chained Storage

### Cached Storage

## VCollections

Virtualized collections currently implemented for the following 
interfaces: List, Map, Set, Stack. They all use 

### VList

#### VArrayList

#### VLinkedList

### VMap

### VSet

### VStack

## Builders

### Validation

### Reading yaml files

### Reading json files

## Modules

### Mongo

### Redis

### Development guide






