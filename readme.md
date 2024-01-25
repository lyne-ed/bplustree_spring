# B+ Tree implementation


## ⚠️ A lire

Suivre la section How to run, ensuite quand l'app est lancée, pour tester :

- Import (entries au format csv) : [http://localhost:8080/import?filePath=src/main/resources/10000_entries.csv](http://localhost:8080/import?filePath=src/main/resources/10000_entries.csv)
- Save (au format json un arbre) : [http://localhost:8080/save?filePath=src/main/resources/output.json](http://localhost:8080/save?filePath=src/main/resources/output.json)
- Import (arbre au format json) : [http://localhost:8080/import?filePath=src/main/resources/output.json](http://localhost:8080/import?filePath=src/main/resources/output.json)
- Benchmark : [http://localhost:8080/benchmark?filePath=src/main/resources/10000_entries.csv](http://localhost:8080/benchmark?filePath=src/main/resources/10000_entries.csv)

On peut modifier les fichiers et leurs noms dans les calls des endpoints dans les paramètres des requêtes

## Introduction
This is a basic implementation of a B+ Tree in Java. It supports the following operations:
* Insert
* Delete
* Search

Project is exposing a REST API to perform the operations. The API is documented using Swagger and can be accessed at http://localhost:8080/swagger-ui.html

Open api documentation is available at http://localhost:8080/v3/api-docs

## How to run
First, make sure you have Java 17 installed on your machine and JAVA_HOME environment variable is set on that path.

The project is using Gradle as build tool. To run the project, execute the following command:
```
./gradlew clean bootRun
``` 
Project will then be available at http://localhost:8080

## Frontend /!\ WORK IN PROGRESS /!\
To be able to see clearly the tree structure, a frontend application was developed using Angular. The frontend application is available at http://localhost:4200.

To run the frontend application, execute the following command:
```
cd frontend
npm install
npm start
```

## What's next
You first need to understand how this implementation works.
Next, you can try to implement a few features, the minimum being:
* Creating a way to import data from a file containing a least 100000 entries
* Creating a way to serialize the tree, so that it can be saved (to a file / in a mongodb instance / another solution) and loaded later
* Benchmarking the implementation to see how it performs
  * Pick a random number of entries existing in the tree then search for them, measure the time it takes to find them (average time, min time, max time, etc) and compare it with the same operation on a sequential seach directly on the file containing the entries.
  * Build a report with the results of the benchmarking and explain why the results are like that.

## How to submit your work
Your work should only be submitted through the Github classroom assignment link. You should not create a pull request to the original repository. (https://classroom.github.com/a/0oMLSUuz)

## References
Based on the following repository : https://github.com/linli2016/BPlusTree
Heavily changed to incorporate real b+ tree constraints and to use dynamic collections instead of fixed size arrays.

