Babel for kdb+
allows jdbc compatible databases to be queried directly from kdb+.

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

From kdb+

$ q
KDB+ 2.4 2007.12.04 Copyright (C) 1993-2007 Kx Systems
m64/ 2()core

q)h:hopen 6868      / connect to babel server
q)h ("query";"jdbc:mysql://ensembldb.ensembl.org:3306/ensembl_go_48?user=anonymous";"show tables")
TABLE_NAME             
-----------------------
"assoc_rel"            
"association"          
"association_qualifier"
"db"                   
"dbxref"               
"evidence"             
"evidence_dbxref"      
"gene_product"         
"gene_product_count"   
"gene_product_property"
...

This demo uses http://www.ensembl.org/info/data/mysql.html

As you can see there are 2 functions available to you - update and query.
The params are

h ("update"|"query";"url";"query")

Disclaimer - this version is not production software, should not be used
for anything other than evaluation against test/dev servers, and even then i accept no liability for any losses!

