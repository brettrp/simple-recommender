simple-recommender
==================
Example of implementing an item-based and user-based recommender and service using Apache Mahout, Groovy, and Jetty/Jersey.

Item data is stored in an in-memory database; to adapt this to very large datasets, this could easily be extended to persist to a search engine (e.g. ElasticSearch) or database.

Recommendations are not precomputed; for a more scalable and performant solution, recommendations would be precomputed and stored in the search engine.

It also provides a REST interface for retrieving recommendations, implemented using embedded Jetty and Jersey.

simple-recommender is bundled with a sample dataset of anime from  
https://www.kaggle.com/CooperUnion/anime-recommendations-database  
but there is nothing specific to this dataset in the code, and other similar recommendations databases could easily be used instead.

Getting the Source
=========
Before you can download the source files, you will need to install git-lfs so that GitHub can handle the large data files bundled with the project: https://git-lfs.github.com

Then, use  `git clone https://github.com/brettrp/simple-recommender.git`  to download the source - don't use `Download Zip`


Prequisites
===========
- Java 1.8
- Maven 3.2 or later

Building
========
```bash
mvn clean install
```
Note that building runs tests on the full bundled dataset, so it does take a few minutes.

Running
=======
```bash
cd target
java -Xmx1024m -jar recommender-1.0-SNAPSHOT.jar
```

This will build the item database and recommenders, and start a web service listening on port 8080.
Note that the default implementation looks for the datasets in the `datasets` directory, so you do need to run from `target`.

This allocates 1GB of memory to the recommender; if you're testing with a larger dataset and are still using the in-memory implementation, you may need to increase this.

Testing
=======

Using a REST client:


Get the top 3 user-based recommendations for user 883:
```bash
$ curl http://localhost:8080/recommender/user/883?count=3
[{"anime_id":"30757","name":"Monster Strike","genre":"Action, Fantasy, Game","type":"ONA","episodes":"51","rating":"6.66","members":"7089"},
 {"anime_id":"4991","name":"Wakakusa Monogatari","genre":"Historical, Slice of Life","type":"Special","episodes":"1","rating":"6.56","members":"373"},
 {"anime_id":"4073","name":"Kyouryuu Daisensou Aizenborg","genre":"Action, Mecha","type":"TV","episodes":"39","rating":"6.90","members":"411"}]
```

Get the default number (currently 4) of item-based recommendations for item 214:
```bash
$ curl http://localhost:8080/recommender/item/214
[{"anime_id":"30158","name":"Okore!! Nonkuro","genre":"Kids","type":"Special","episodes":"1","rating":"3.93","members":"52"},
 {"anime_id":"33266","name":"Nanocore","genre":"Sci-Fi","type":"ONA","episodes":"10","rating":"6.17","members":"163"},
 {"anime_id":"31211","name":"Choegang Top Plate","genre":"Action, Comedy, Kids, Sports","type":"TV","episodes":"26","rating":"7.20","members":"86"},
 {"anime_id":"32247","name":"Tekkon Kinkreet Pilot","genre":"Action, Adventure, Psychological, Supernatural","type":"Special","episodes":"1","rating":"5.20","members":"345"}]
```

Scaling Up
==========
This is a demo application. But it could be scaled up fairly easily:
- Persist item data to a search engine such as ElasticSearch
- Precompute and persist recommendations to the search engine, and use the search engine to serve them
- Persist the recommenders so that they don't need to be rebuilt on each restart
- Allow incremental updates of user interactions
- Apache Mahout is integrated with Hadoop, so you can scale up using Map-Reduce by choosing an algorithm such as ALS-WR and running in parallel.


Further Reading
==============
Apache Mahout: 
http://mahout.apache.org/users/algorithms/recommender-overview.html

Introduction to Recommender Systems:
http://www.summa.com/blog/2014/06/24/recommender-system-basics-part-1
http://www.summa.com/blog/2014/06/30/recommender-system-basics-part-2







