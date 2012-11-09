SOAP driver for Fitnesse SLIM

Usage
=====
Using the `add namespace prefix` and `set x path` you build up a SOAP message body.

First you add a namespace,

    |add namespace prefix|p|http://www.example.com/|

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


