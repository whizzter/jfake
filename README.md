# jfake
JFake is a simple system to generate fake data to databases by writing set specifications.

It has 2 usage modes, either it runs as a command line tool or as a library for a web application.
When run as an web application library it will automatically generate the data only if there exists
a config file (WEB-INF/auto.jfake)  and ALL tables pointed to by the config file are empty (this is so that production data should not be overwritten!).

A binary package is available at [JFake JAR download](http://www.jlim.se/download/jfake.jar)

The main idea is to write a specification on HOW the data should be generated rather than specific data.
For example one can write ```[5000,5001,5002,5003]``` to generate a list of numbers but if one wants a range it
could also be described as the range ```5000:5003``` .

A full sample file follows:
```
# Simple product/receipt jfake file

# What datasource do we use to talk to the database
@datasource="java:app/jdbc/ejb1"

# We want to start generating ID's at this ID (to avoid initial conflicts with JPA)
@startid=5000

# Generate product table
product {
  # Generate automatic ID
  id = @autoid
  
  # Generate some sample data
  name = ["Ferrari","Folka","Dator"]
}
                
# Generate data for receipt table
receipt {
  # Generate ID table
  id = @autoid
                        
  # Generate some sequential addresses.
  address = "Database road "+1:20
                                
  # Generate a random product ID link
  product_id = product.id.random
}
```


