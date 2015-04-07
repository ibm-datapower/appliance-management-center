# appliance-management-center# IBM Appliance Management Center Open Source Edition

Appliance Management Center provides fast time to value, with firmware deployment, domain and service configuration, and deployment policy management capabilities.

Centrally manage appliance firmware and configurations across groups of DataPower appliances, and monitor key metrics and events from groups of DataPower appliances

* Web-based user interface
* Support for multiple generations of DataPower appliances
* Firmware management
* Domain and configuration management including support for deployment policies across multiple environments
* Appliance monitoring

## Prerequisites

* [Appliance Management Toolkit](http://github.com/ibm-datapower/appliance-management-toolkit) installed on the local Maven respository
* [Apache Tomee JAXRS 1.7.1](http://tomee.apache.org)

## Quick Start

1. download the zip and unzip, or git clone
2. cd amc
3. mvn clean package
4. copy the files under src/main/tomee/conf to the Apache Tomee conf directory
5. edit the Apache Tomee conf/server.xml and enable SSL per instructions found [here](https://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html)
6. copy the Maven output target/amc.war into the Apache Tomee webapps directory
7. start Apache Tomee from inside the Tomee root directory with bin\startup.bat or bin/startup.sh
8. login to the console by using the browser URL: [https://localhost:8443/amc](https://localhost:8443/amc) - replace the hostname and port if using different values or accessing it from a different computer. Use **wamcadmin** and **wamcpass** as credentials if using the supplied *tomcat-users.xml* file.

## Supported Browsers

All major browsers should work correctly, including Internet Explorer, Google Chrome, Safari, Opera and Firefox.

## Comparison with IBM WebSphere Appliance Management Center

The following features are not present in the open source version:

* custom role management and UI
* static roles defined in the web.xml file of the application
* upload progress bar for firmware upload

## Documentation

TODO

## Contributing

If you want to contribute to the project, you will need to fill in the appropriate Contributor License agreement which can be found under the CLA directory. Follow the directions inside the document so that any submissions can be properly accepted into the repository.

## Authors

* IBM

For more open-source projects from IBM, head over [here](http://ibm.github.io).

## Copyright and license

Copyright 2015 IBM Corp. under [the Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
