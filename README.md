# JFake
## Introduction
JFake is a simple system to generate fake data to databases by writing set specifications liberally licenced under the MIT licence.

It has 2 usage modes, either it runs as a command line tool or as a library for a web application.
When run as an web application library it will automatically generate the data only if there exists
a config file (WEB-INF/auto.jfake)  and ALL tables pointed to by the config file are empty (this is so that production data should not be overwritten!).

## Downoad

A binary package is available at [JFake JAR download](http://www.jlim.se/download/jfake.jar)

## JFake specifications
### Basic idea
The main idea is to write a specification on HOW the data should be generated rather than specific data.
For example one can write ```[5000,5001,5002,5003]``` to generate a list of numbers but if one wants a range it
could also be described as the range ```5000:5003``` .

A full sample is at the end of this README.md (also samples can be found under the samples directory)
### Syntax
The Jfake syntax is built up as the following.

Comments are single line and everything after a ```#``` is a comment.

__Sample__: ```# This is a comment```

A value is defined with the equals sign, a value defined outside a class is usually a constant or configuration value while all values inside a class definition defines a column to be generated.

__Sample__: ```@datasource="jdbc/mysource"``` sets a special configuration values so that JFake knows what datasource to connect to

A class is defined by an identfier followed by the definition inside a couple of curly braces, each value set inside the curly braces contributes to creating a column.

__Sample__: To create a product table with automatic ID values and 3 columns of names one defines it like this,
```
product {
  id=@autoid
  name="Product nr "+1:3
}
```

Numbers and strings are the basic data types, numbers are alphanumeric integers and strings are enclosed with paired ' or " characters.

__Sample__: ```123```   ``` "Hello world" ``` 

A sequence is a linear numeric sequence of integer values, with this one can specify any range of (64bit) integers.

__Sample__: ```1:1000``` generates the numbers 1:1000 or ```20:30``` generates the numbers 20 to 30

A list is a sequence of user specified values, these are used to produce a defined set of values. A list specification will also include any sub-sets of values to produce a unified list.

__Sample__: ```[1,2,3]``` is a list with 3 members containing the values 1,2,3, ```["Hello","World"]``` is a list containing the values "Hello" and "World" and finally a list that takes the contents of subsequences will be defined like ```[1:3,10:13]``` to generate a list with  1,2,3,10,11,12,13

## Sample

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


