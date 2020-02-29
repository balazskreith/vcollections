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
virtualized collection is explained. In **VCollections** the available collections 
can be used with external storage are detailed. The concept of storage in vcollections 
is detailed in **Storage** section, where guideline is given to develop your own.
In **Builders** section the external configuration for the used storage is detailed.
Finally **Modules** describes already developed storage modules and their builders 
for vcollections, and here you find guideline to develop your own.


## Architecture
![alt text][logo]

[logo]: vcollections_in_java.png "The architecture of vcollections"

Figure 1. The high-level architecture of vcollections



## VCollections

### VList

#### VArrayList

#### VLinkedList

### VMap

### VSet

### VStack



## Storage

### Memory Storage

### File Storage

### Key generators

### Virtualized Storage

#### Replicated Storage

#### Clustered Storage

#### Chained Storage

### Cached Storage

## Builders

### Validation

### Reading yaml files

### Reading json files

## Modules

### Mongo

### Redis

### Development guide






