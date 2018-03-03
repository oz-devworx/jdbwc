Author: Tim Gall, 2010-04-11
Web: http://jdbwc.sourceforge.net/
==============================================
CONTENTS:
1) Using DataHandler in your projects

==============================================
==============================================
1) Using DataHandler in your projects:
----------------------------------------------
The DataHandler interface is a labelled-array.
Each array index can be reffered to by numeric-index or a String label.

Its particularly useful when dealing with SQL name->value pairs.
The basic concept of the interface is:
	Each index location has a label.
	The type configuration of the pairs is: 
		(String label -> Object value)
	Because the value is an Object place-holder, multidimensional DataHandler's are allowed.
	
You refer to the DataHandler by its interface in your code
and create it using the implementation to suit your needs or likes.
Even write your own implementation if a suitable one doesn't exist.
		
PROGRAMATIC USE:
----------------
// there's a few variations on the constructor 
// to customise the types behaviour. This is the most basic:
DataHandler data = new KeyedList();

data.addData("One Hundred", 100);
data.addData("myBoolean", true);
data.addData("my results", Statement.getResultSet());
// etc.

// The type will grow and shrink dynamically.
//
// You can request a specific type when retrieving data 
// or cast it back to its known type for other Objects.

int val       = data.getInt("One Hundred");
String val    = data.getString("One Hundred");
boolean val   = (boolean)data.getObject("myBoolean");
ResultSet val = (ResultSet)data.getObject("my results");

// you can also perform a range of other task including clearing the DataHandler,
// updating or removing an existing value and probing to find if a value exists.

==============================================
END OF README