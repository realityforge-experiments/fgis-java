FGIS - Fire Ground Information System
=====================================

[![Build Status](https://secure.travis-ci.org/rhok-melbourne/fgis-java.png?branch=master)](http://travis-ci.org/rhok-melbourne/fgis-java)

What is FGIS
--------------

FGIS was initiated at a Random Hacks Of Kindness (RHok) day and is designed to help fire fighters in the bush gain access to information that helps them ensure the safety of their teams and give them better tools to manage the fire.

The initial project was done with a combination of RoR 3.2, CoffeeScript, Bootstrap etc. This is a spike to re-implement the functionality in a technology more familiar to the author.

How-to Build
------------

FGIS uses [Apache Buildr](http://buildr.apache.org) to build the project which is a ruby based build tool. The easiest way to build the project is to use [rbenv](https://github.com/sstephenson/rbenv) to manage the ruby version and [bundler](http://gembundler.com/) to manage the gem dependencies for buildr.

Under OSX with [Homebrew](http://mxcl.github.com/homebrew/) installed you can install the tool via;

    $ brew update
    $ brew install rbenv
    $ brew install ruby-build
    $ ruby-build install 1.9.3-p327
    $ cd ../path/to/fgis
    $ bundle install
    $ rbenv rehash

To build you run the following commands

    $ bundle exec buildr clean package

How-to Run
----------

FGIS uses PostGIS as the back end data store and runs in the GlassFish application store. Under OSX with [Homebrew](http://mxcl.github.com/homebrew/) installed you can install the tools via;

    $ brew update
    $ brew install postgis
    $ brew install glassfish

Then you need to start the datbase server. On an OSX, brew install it is

    $ pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start

You also need to setup the database structure. This is easiest done using the buildr command.

    $ bundle exec buildr dbt:create

After creating the database structure then you need to start up the GlassFish server and configure it via;

    $ alias asadmin=/usr/local/Cellar/glassfish/3.1.2.2/libexec/glassfish/bin/asadmin
    $ asadmin start-domain
    $ source config/setup.sh
    $ asadmin deploy --name fgis --contextroot fgis --force=true target/fgis-*.war

Then you can visit the local website at [http://127.0.0.1:8080/fgis](http://127.0.0.1:8080/fgis);

Credits
-------

The project was mostly a rewrite with front end code inspired by the initial RHoK hack.
