# PHINet

PHINet is a Health-IoT testbed framework for the future Named Data Networking Internet architecture. 

This project is described in a paper accepted by the IEEE Mobile Services conference (link will be provided soon).

## Abstract of Work

Named Data Networking, a future networking paradigm, allows for features such as the naming of content for identification and routing as well as router-based caching for ubiquitous storage. These features are well suited for an Internet of Things (IoT) which can network identifiable and addressable electronic devices such as sensors that, in an IoT, are often configured in a Wireless Body Area Network. To bridge the gap between research approaches in the area of both Health-IoT and content centric networking, this paper presents the first content centric networking testbed framework called PHINet for experimentation with Health-IoT. PHINet aims to fulfill the following goals: ease of use, seamless scaling, support for testing, development and integration of health services and sensors, support for experimentation with live traffic underneath, and integration of the cloud into Health-IoT. Architecture, design, and implementation of PHINet is presented. Furthermore, through several use cases, we demonstrate the applicability of the proposed work.

# Install

Our client application will soon be available for download on android devices.

[Visit our website for additional information/functionality.](http://ndn-healthnet.elasticbeanstalk.com/)

# Contributing

All documented and tested contributions that further the aim of this project are welcome. Consult the following list for contribution ideas.


GENERAL
- REQUIRED: improve docs
- REQUIRED: improve design / handle all TODOs 
- REQUIRED: most robust testing / handle corner cases 

CLIENT
- REQUIRED: modify/user all NDN packet components
- DESIRED: multi-sensor graph
- DESIRED: improve sensor axes
- DESIRED: improve layout

WEB
- REQUIRED: view patient data (if applicable)
- REQUIRED: edit profile data
- REQUIRED: basic testing page (to test with device)
- REQUIRED: modify/use all NDN packet components
- DESIRED: perform more complex analytics
- DESIRED: improve layout
- DESIRED: make mobile friendly

# Tests

In order to run the PHINetCloud tests, you must install mocha (preferably globally): "npm install -g mocha". Then, within the tests directory, run "mocha test_x.js" where "x" is the test you'd like to run.

In order to run PHINetDroid tests, right click on the ApplicationTest file and click the android icon (if your configrations are different, you must delete them and then click).

# Support 

Send all questions to cseales6@gmail.com.

# License

GNU GENERAL PUBLIC LICENSE, view LICENSE.