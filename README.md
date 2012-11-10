SOAP driver for Fitnesse SLIM

Usage
=====
Using the `add namespace prefix` and `set x path` you build up a SOAP message body.

First you add a namespace,

    |add prefix|p|namespace|http://www.example.com/|

then you can set values in the newly defined `p` prefix

    |set x path|p:ElementInMessage|value|Some text value|

you can inspect the request using `show`

    |show|request|

Sending the request to an endpoint is done using `send to`

    |send to|http://www.example.com/|

The response will be captured in `response`, shown using

    |show|response|

This reponse may be `null`. To inspect values from the response,
use `get x path`

    |check|get x path|//:p|Some value|

Setting HTTP headers
====================
You can optionally add HTTP headers to your SOAP request, and
these headers will be used for every SOAP request till they are
reset using:

    |reset headers|

You can add headers

    |set header| headerName|value|some value|

and inspect them using

    |show headers|


Example
=======
This example can be run using a basic Fitnesse/maven setup, for example using Xebium and after doing a local install of the driver adding the driver dependency:

    <dependency>
      <groupId>com.xebia.fitnesse.slim.soap</groupId>
      <artifactId>fitnesse-slim-soap-driver</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>

Then the followin Fitnesse page should do all the setup and run the first test:

    !define TEST_SYSTEM {slim}

    !*****> '''Classpath'''
    !pomFile pom.xml@runtime
    *****!

    |import|
    |com.xebia.fitnesse.slim.soap|

    | library |
    | soap fixture |

    !|script|
    |add prefix|wsx|namespace|http://www.webserviceX.NET/|
    |set x path|wsx:GetSunSetRiseTime/wsx:L/wsx:Latitude|value|1.0|
    |set x path|wsx:GetSunSetRiseTime/wsx:L/wsx:Longtitude|value|1.0|
    |set x path|wsx:GetSunSetRiseTime/wsx:L/wsx:SunSetTime|value|0|
    |set x path|wsx:GetSunSetRiseTime/wsx:L/wsx:SunRiseTime|value|0|
    |set x path|wsx:GetSunSetRiseTime/wsx:L/wsx:TimeZone|value|1|
    |set x path|wsx:GetSunSetRiseTime/wsx:L/wsx:Day|value|1|
    |set x path|wsx:GetSunSetRiseTime/wsx:L/wsx:Month|value|1|
    |set x path|wsx:GetSunSetRiseTime/wsx:L/wsx:Year|value|2012|
    |show|request|
    |send to|http://www.webservicex.net/sunsetriseservice.asmx|
    |show|response|
    |check|get x path|wsx:GetSunSetRiseTimeResponse/wsx:GetSunSetRiseTimeResult/wsx:Year|2012|
    |check|get x path|count(//wsx:GetSunSetRiseTimeResult)|1|



