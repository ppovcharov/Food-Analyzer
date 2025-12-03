Food Analyzer

This repository contains my implementation of the Food Analyzer coursework project for the Modern Java Technologies (MJT) module at the Faculty of Mathematics and Informatics, Sofia University. The project follows the official assignment specification published for the course and implements its required functionality, structure and behaviour.

Assignment Specification (Provided by FMI – MJT 2020/2021)
Food Analyzer
Project description

You are given a partially implemented client-side application (in the client package), which needs to connect to a server-side component that you must implement in the server package.

The Food Analyzer system processes food item information obtained from an open REST API. Clients send commands to the server, which must interpret them, fetch the requested data (either from cache or from the remote API), and return the results.

Your task is to implement the server-side component as specified below and ensure full communication between client and server.

Functional requirements
1. Commands

The client sends commands in plain text to the server. The supported commands are:

• get-food <food_name> – searches for a food item by name.
• get-food-report <fdcId> – retrieves detailed nutrition information for a food item by its FDC ID.
• get-food-by-barcode --code=<gtinUpc> – retrieves food data using a barcode number.
• get-food-by-barcode --img=<barcode_image_file> – reads and decodes a barcode from an image file using the ZXing library.

The server must parse each command, validate it, and execute the requested operation.

2. External REST API

The server communicates with the USDA FoodData Central API to retrieve food metadata, nutritional information and product records. Communication must be performed with HTTP requests and responses must be parsed from JSON into Java objects.

3. Caching

A local cache must be implemented.
If a food item has already been requested, the server should return the cached result instead of querying the external API again. The cache should persist data on the file system.

4. Barcode support

The ZXing library must be used to decode UPC/GTIN barcodes from image files supplied by the client.
Decoded barcodes must be used to retrieve matching products through the API.

5. Client–server communication

The server must:

• Listen on a TCP port
• Accept multiple clients
• Process each client’s commands independently
• Return results in plain text
• Handle errors gracefully and send meaningful messages back to the client

The communication protocol is text-based.

6. Concurrency

The server must support multiple clients simultaneously.
This may be implemented using:

• Java NIO (Selector, ServerSocketChannel, SocketChannel)
or
• A multi-threaded model using thread pools

The server must remain responsive while serving concurrent requests.

7. Error handling and logging

The server must:

• Validate incoming commands
• Report errors clearly to the client
• Log all exceptions, including full stack traces, to a file
• Continue operating after client or API errors

8. Code structure

The server implementation must be placed in the server package.
Client-side logic remains unchanged unless explicitly required.
Additional helper classes may be created as needed.

9. Assessment criteria

Projects will be evaluated based on:

• Correctness of the implementation
• Fulfilment of all functional requirements
• Error handling and robustness
• Code readability and design
• Proper use of Java features and libraries
• Compliance with the assignment specification
