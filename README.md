Traitor - Associating Concepts using the World Wide Web
==================
naward13 / Lesley Wevers & Oliver Jundt & Wanno Drijfhout
--------

View a demo instance of Traitor at [http://evilgeniuses.ophanus.net](http://evilgeniuses.ophanus.net). Want to know more about Traitor? Visit the [About-page](http://evilgeniuses.ophanus.net/about), the [wiki](https://github.com/norvigaward/naward13/wiki) or read  [our report](https://github.com/norvigaward/naward13/blob/master/report/Traitor%20-%20Associating%20Concepts%20using%20the%20World%20Wide%20Web%20-%20naward13%20-%20Evil%20Geniuses.pdf).

### Overview repository contents
##### Back-end
* The folder `/doc/` contains JavaDoc for Traitor's Hadoop back-end.
* The folder `/launchers/` contains various Eclipse-launchers we used during development, mostly for launching Hadoop jobs (on the cluster).
> Note: create a folder `/traitor-data/` in your local check-out before running such a launcher.
* The folder `/lib/` contains various libraries for the Hadoop back-end.
* The folder `/src/` contains the Java source code for the Hadoop back-end.

##### Front-end
* The folder `/presentation/` contains a Python+CherryPy+Jinja2 web applications; run with `python ./presentation/server.py`. 
 > Note: the app requires a SQLite3-database file at `presentation/pairs.db`. You may symlink to `/traitor-data/cluster-fullset.db` or download [the database](http://evilgeniuses.ophanus.net/static/pairs.db) and save it as `presentation/pairs.db`.
* The folder `/report/` contains [our report](https://github.com/norvigaward/naward13/blob/master/report/Traitor%20-%20Associating%20Concepts%20using%20the%20World%20Wide%20Web%20-%20naward13%20-%20Evil%20Geniuses.pdf), [a presentation](https://github.com/norvigaward/naward13/blob/master/report/Presentation%2011%20January.pdf) and other files you will not care about.