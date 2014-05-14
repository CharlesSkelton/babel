Babel for kdb+
=========
Babel for kdb+ allows the **ultra-fast** database kdb+ from [Kx Systems] to query other databases via jdbc. It runs as a daemon, accepts connections from kdb+ processes, and queries other databases via the jdbc interface, transforming the result into a kdb+ compatible object.

![alt tag](https://raw.githubusercontent.com/CharlesSkelton/babel/master/meta/Babel.png)

Current Version
----
1.34 2014.03.24

Installation
--------------
Download the latest release from

https://github.com/CharlesSkelton/babel/tree/master/dist

Start it with (e.g. if connecting to mysql)

    java -jar babel.jar 6868 com.mysql.jdbc.Driver

which is of the pattern

    java -jar babel.jar listeningport jdbcdriver1 jdbcdriver2 ... jdbcdriverN

Actual output is similar to

    $java -jar babel.jar 6868 com.mysql.jdbc.Driver
    Babel for kdb+ Version 1.0 beta
    2008.01.08 11:42:13.839 Listening on port 6868 for connections...
    2008.01.08 11:42:22.818 New connection from /127.0.0.1:62929
    2008.01.08 11:42:22.838 Request from /127.0.0.1:62929
    2008.01.08 11:42:22.934 Response sent
    2008.01.08 11:42:22.935 Request from /127.0.0.1:62929
    2008.01.08 11:42:22.940 Response sent
    2008.01.08 11:42:22.958 Request from /127.0.0.1:62929
    2008.01.08 11:42:22.962 Response sent
    2008.01.08 11:42:22.981 Request from /127.0.0.1:62929
    2008.01.08 11:42:23.057 Response sent

Invocation
----------
It can be started with

    java -Xmx512m -jar babel.jar 6868 com.mysql.jdbc.Driver

which is of the pattern

    java -jar babel.jar listeningport jdbcdriver1 jdbcdriver2 ... jdbcdriverN

If you don't specify the port 6868 it will default to listening on port 9999. It will accept connections from localhost only, and logs to stdout.
Alternately, you can ignore that babel.jar is an executable jar file and run with full command line options. For Oracle, this could be:

    java -Xmx1024m -Doracle.jdbc.defaultRowPrefetch=10000 -cp "babel.jar:lib/ojdbc6.jar" de.skelton.babel.Babel 6868 oracle.jdbc.OracleDriver

Building a connection and issuing a query
-----------------------------------------
From kdb+ you connect as

    q)h:hopen 6868

and send queries as a sync request using the following format

    q)handle("query|update|string";"jdbc url";"query text")

e.g.

    q)r:h("query";"jdbc:mysql://ensembldb.ensembl.org:3306?user=anonymous&zeroDateTimeBehavior=convertToNull";"show databases")

Replacing "query" with "string" will force all columns to be interpreted as varchar type; this is intended for debug of type mappings only, and not recommended as being used for regular queries.

Async Requests
--------------
In addition to sync requests, Babel can process async requests in the form

    q)neg[handle]([`callbackFn`callbackTag!`myfn`mytag;]"query|update";"jdbc url";"query text")

e.g. Babel will callback myfn[`mytag;(0|1;errorText|resultSet)]

    q)myfn:{0N!(x;y);}
    q)neg[h](`callbackFn`callbackTag!`myfn`mytag;"query";"jdbc:mysql://ensembldb.ensembl.org:3306?user=anonymous&zeroDateTimeBehavior=convertToNull";"show databases")

or collect the result via h[]

    q)neg[h]("query";"jdbc:mysql://ensembldb.ensembl.org:3306?user=anonymous&zeroDateTimeBehavior=convertToNull";"show databases")
    q)r:h[] / (0|1;errorText|resultSet)


This demo uses http://www.ensembl.org/info/data/mysql.html

Specifying your jdbc driver and repacking the jar
-------------------------------------------------
If you want to continue to launch the executable JAR file directly, and you need to add your driver to the path, do the following: download and unzip the latest babel.zip, then extract the jar to its raw components

    jar -xvf babel.jar

then edit META-INF/MANIFEST.MF adding your database jar file path to the classpath in that manifest file, then update the jar file with the modified file

    jar umf META-INF/MANIFEST.MF babel.jar

If you know of a better way to achieve this please let us know.

Default Row Prefetch
--------------------
When extracting data from some oracle databases, users have discovered that the default fetch size for the Oracle jdbc driver is too small and impacts performance very badly. This can be influenced by setting the default fetch size as a command line option to java, using

    -Doracle.jdbc.defaultRowPrefetch=10000

e.g.

    $java -Doracle.jdbc.defaultRowPrefetch=10000 -Xmx512m -jar babel.jar 6868 ....

Data Type Conversion for NUMERIC and DECIMAL types
--------------------------------------------------
The default type for NUMERIC and DECIMAL is a String. However, the data will be converted into an actual number if the following criteria are met:

|Precision | Scale         | Data Type|
|----------|:-------------:|----------|
|<= 9	   | 0 or -127	   | Integer  |
|<= 18	   | 0 or -127	   | Long     |
|<= 7	   | not 0 or -127 | Float    |
|<= 15	   | not 0 or -127 | Double   |



[Kx Systems]:http://www.kx.com

