Babel for kdb+
allows jdbc compatible databases to be queried directly from kdb+.

Start it with

java -jar babel.jar 6868

skelton8:dist cskelton$ java -jar babel.jar 6868
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

skelton8:~ cskelton$ q
KDB+ 2.4 2007.12.04 Copyright (C) 1993-2007 Kx Systems
m64/ 2()core 3072MB cskelton skelton8. 255.255.255.255 EXPIRE 2008.11.01
charlie skelton #42003

h:hopen 6868      / connect to babel server
h ("update";"jdbc:hsqldb:hsql://localhost";"drop TABLE tradeXX");
h ("update";"jdbc:hsqldb:hsql://localhost";"CREATE TABLE tradeXX(time
timestamp default 'now',sym VARCHAR, price double, size integer)");
h ("update";"jdbc:hsqldb:hsql://localhost";"INSERT INTO
tradeXX(sym,price,size) VALUES('VOD.L', 130.25, 100)");
q)h ("query";"jdbc:hsqldb:hsql://localhost"; "SELECT * FROM trade")
TIME                    SYM   PRICE  SIZE
-----------------------------------------
2008.08.01T11:42:22.961 VOD.L 130.25 100
q)

This was using hsql. Your ms sql will be different :-)

As you can see there are 2 functions available to you - update and query.
The params are

h ("update"|"query";"url";"query")

Disclaimer - this version is not production software, should not be used
for anything other than evaluation against test/dev servers, and even then i accept no liability for any losses!

